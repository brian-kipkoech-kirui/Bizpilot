package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.database.BizPilotDatabase
import com.example.data.firebase.FirebaseAuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PosSyncManager private constructor(private val context: Context) {

    private val TAG = "PosSyncManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val db = BizPilotDatabase.getDatabase(context)

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _syncStatus = MutableStateFlow("Background Sync Ready")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        registerNetworkCallback()
        observeUnsyncedSales()
    }

    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val builder = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                cm.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.d(TAG, "Network connection restored. Triggering background POS sync...")
                        _isNetworkAvailable.value = true
                        _syncStatus.value = "Online - Syncing POS transactions..."
                        triggerBackgroundSync()
                    }

                    override fun onLost(network: Network) {
                        Log.d(TAG, "Network connection lost. Operating in offline Room mode.")
                        _isNetworkAvailable.value = false
                        _syncStatus.value = "Offline Mode - Transactions cached locally"
                    }
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "ConnectivityManager callback setup skipped: ${e.message}")
        }
    }

    private fun observeUnsyncedSales() {
        scope.launch {
            try {
                db.saleDao().getUnsyncedSalesFlow().collect { unsyncedList ->
                    _pendingSyncCount.value = unsyncedList.size
                    if (unsyncedList.isNotEmpty() && _isNetworkAvailable.value) {
                        triggerBackgroundSync()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing unsynced sales: ${e.message}")
            }
        }
    }

    fun triggerBackgroundSync() {
        scope.launch {
            try {
                val unsyncedSales = db.saleDao().getUnsyncedSales()
                if (unsyncedSales.isEmpty()) {
                    _syncStatus.value = "All Room POS transactions synced to Firestore"
                    return@launch
                }

                _syncStatus.value = "Syncing ${unsyncedSales.size} local transactions to Firestore..."
                val firestore = FirebaseFirestore.getInstance()
                val uid = FirebaseAuthManager.currentUser.value?.uid ?: "local_merchant_001"

                var syncedCount = 0
                for (sale in unsyncedSales) {
                    val items = db.saleItemDao().getItemsForSaleSync(sale.id)

                    val saleData = hashMapOf(
                        "saleId" to sale.id,
                        "receiptNo" to sale.receiptNo,
                        "customerName" to sale.customerName,
                        "totalAmount" to sale.totalAmount,
                        "discount" to sale.discount,
                        "tax" to sale.tax,
                        "paymentMethod" to sale.paymentMethod,
                        "paymentReference" to sale.paymentReference,
                        "status" to sale.status,
                        "timestamp" to sale.timestamp,
                        "syncedAt" to System.currentTimeMillis(),
                        "items" to items.map { item ->
                            mapOf(
                                "productId" to item.productId,
                                "productName" to item.productName,
                                "quantity" to item.quantity,
                                "unitPrice" to item.unitPrice
                            )
                        }
                    )

                    firestore.collection("users")
                        .document(uid)
                        .collection("pos_transactions")
                        .document(sale.receiptNo)
                        .set(saleData)
                        .await()

                    db.saleDao().markSaleSynced(sale.id)
                    syncedCount++
                }

                _lastSyncTime.value = System.currentTimeMillis()
                _pendingSyncCount.value = db.saleDao().getUnsyncedSales().size
                _syncStatus.value = "Successfully synced $syncedCount transaction(s) to Firestore"
                Log.d(TAG, "Background sync completed: $syncedCount synced.")
            } catch (e: Exception) {
                Log.w(TAG, "Background sync failed (Will retry when online): ${e.message}")
                _syncStatus.value = "Sync Pending - Saved in Room DB"
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PosSyncManager? = null

        fun getInstance(context: Context): PosSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PosSyncManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
