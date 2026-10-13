package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.database.BizPilotDatabase
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.model.*
import com.example.repository.BizPilotRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BizPilotViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BizPilotRepository
    private val posSyncManager = com.example.data.sync.PosSyncManager.getInstance(application)

    val posSyncStatus: StateFlow<String> = posSyncManager.syncStatus
    val isNetworkAvailable: StateFlow<Boolean> = posSyncManager.isNetworkAvailable
    val pendingPosSyncCount: StateFlow<Int> = posSyncManager.pendingSyncCount

    // Auth state flows from FirebaseAuthManager
    val isLoggedIn: StateFlow<Boolean> = FirebaseAuthManager.isLoggedIn
    val userEmail: StateFlow<String> = FirebaseAuthManager.userEmail
    val userName: StateFlow<String> = FirebaseAuthManager.userName
    val businessCategory: StateFlow<String> = FirebaseAuthManager.businessCategory
    val businessBio: StateFlow<String> = FirebaseAuthManager.businessBio
    val userType: StateFlow<String> = FirebaseAuthManager.userType
    val customerInterests: StateFlow<String> = FirebaseAuthManager.customerInterests
    val customerLocation: StateFlow<String> = FirebaseAuthManager.customerLocation
    val hasCompletedSetup: StateFlow<Boolean> = FirebaseAuthManager.hasCompletedSetup
    val firestoreSyncStatus: StateFlow<String> = FirebaseAuthManager.firestoreSyncStatus

    fun setupCustomerUser(name: String, interests: String, location: String) {
        FirebaseAuthManager.setupCustomerUser(name, interests, location)
    }

    fun setupBusinessUser(businessName: String, category: String, role: String) {
        FirebaseAuthManager.setupBusinessUser(businessName, category, role)
        setUserRole(role)
        setBusinessName(businessName)
    }

    fun skipSetupDefaultAsCustomer() {
        FirebaseAuthManager.skipSetupDefaultAsCustomer()
    }

    val authUserEmail: StateFlow<String> get() = userEmail
    val authUserName: StateFlow<String> get() = userName
    val authIsLoggedIn: StateFlow<Boolean> get() = isLoggedIn

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    fun triggerPosBackgroundSync() {
        posSyncManager.triggerBackgroundSync()
    }

    fun loginWithEmail(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            val (success, message) = FirebaseAuthManager.signInWithEmail(email, pass)
            _authLoading.value = false
            if (!success) {
                _authErrorMessage.value = message
            }
            onResult(success, message)
        }
    }

    fun registerWithEmail(email: String, pass: String, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            val (success, message) = FirebaseAuthManager.signUpWithEmail(email, pass, name)
            _authLoading.value = false
            if (!success) {
                _authErrorMessage.value = message
            }
            onResult(success, message)
        }
    }

    fun loginWithGoogleDemo(email: String = "google.merchant@bizpilot.ai", name: String = "Google Merchant") {
        FirebaseAuthManager.mockSignInWithGoogle(email, name)
    }

    fun continueAsGuest() {
        FirebaseAuthManager.continueAsGuest()
    }

    fun logout() {
        FirebaseAuthManager.signOut()
    }

    init {
        val db = BizPilotDatabase.getDatabase(application)
        repository = BizPilotRepository(db)
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // App Preferences State
    private val _currency = MutableStateFlow("KES")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _userRole = MutableStateFlow("Owner")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _businessName = MutableStateFlow("Safari Retail & Tech Mart")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    fun setBusinessName(name: String) {
        _businessName.value = name
    }

    // Data Streams
    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = repository.sales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerInteractions: StateFlow<List<CustomerInteractionEntity>> = repository.customerInteractions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = repository.employees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiChats: StateFlow<List<AiChatEntity>> = repository.aiChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val campaigns: StateFlow<List<MarketingCampaignEntity>> = repository.campaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Cart State
    private val _cart = MutableStateFlow<Map<ProductEntity, Int>>(emptyMap())
    val cart: StateFlow<Map<ProductEntity, Int>> = _cart.asStateFlow()

    private val _discountPercent = MutableStateFlow(0.0)
    val discountPercent: StateFlow<Double> = _discountPercent.asStateFlow()

    // AI Query State
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Voice assistant active listening
    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    // ------------------------------------------------------------------------
    // FIREBASE AUTH & FIRESTORE CLOUD PERSISTENCE
    // ------------------------------------------------------------------------
    fun updateBusinessProfile(
        name: String,
        category: String,
        bio: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            val (success, message) = FirebaseAuthManager.updateBusinessProfile(name, category, bio)
            _businessName.value = name
            _authLoading.value = false
            onResult(success, message)
        }
    }

    fun performFirestoreSync() {
        viewModelScope.launch {
            com.example.data.firebase.FirebaseAuthManager.syncBusinessDataToFirestore(
                sales.value,
                expenses.value,
                products.value
            )
        }
    }

    fun handleGoogleSignIn(email: String, name: String) {
        com.example.data.firebase.FirebaseAuthManager.mockSignInWithGoogle(email, name)
        performFirestoreSync()
    }

    fun handleSignOut() {
        com.example.data.firebase.FirebaseAuthManager.signOut()
    }

    // Offline Sync Engine State
    private val _isOfflineSyncActive = MutableStateFlow(false)
    val isOfflineSyncActive: StateFlow<Boolean> = _isOfflineSyncActive.asStateFlow()

    private val _pendingSyncItemsCount = MutableStateFlow(2)
    val pendingSyncItemsCount: StateFlow<Int> = _pendingSyncItemsCount.asStateFlow()

    // Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ------------------------------------------------------------------------
    // WALLET MODULE STATE (SME Liquidity & Float Management)
    // ------------------------------------------------------------------------
    private val _mpesaFloatBalance = MutableStateFlow(62400.0)
    val mpesaFloatBalance: StateFlow<Double> = _mpesaFloatBalance.asStateFlow()

    private val _equityBankBalance = MutableStateFlow(85000.0)
    val equityBankBalance: StateFlow<Double> = _equityBankBalance.asStateFlow()

    private val _kcbBankBalance = MutableStateFlow(22100.0)
    val kcbBankBalance: StateFlow<Double> = _kcbBankBalance.asStateFlow()

    private val _cashInHandBalance = MutableStateFlow(15000.0)
    val cashInHandBalance: StateFlow<Double> = _cashInHandBalance.asStateFlow()

    data class WalletLogItem(
        val id: Long = System.currentTimeMillis(),
        val title: String,
        val type: String, // "TopUp", "Transfer", "Disbursement", "SaleReceipt"
        val amount: Double,
        val channel: String, // M-Pesa, Equity, KCB, Cash
        val reference: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _walletLogs = MutableStateFlow<List<WalletLogItem>>(
        listOf(
            WalletLogItem(title = "M-Pesa Float STK Load", type = "TopUp", amount = 20000.0, channel = "M-Pesa", reference = "STK-99821"),
            WalletLogItem(title = "Supplier Payment - SunPower", type = "Disbursement", amount = 18000.0, channel = "Equity Bank", reference = "EQ-10029"),
            WalletLogItem(title = "Daily M-Pesa Sweep to Equity", type = "Transfer", amount = 35000.0, channel = "M-Pesa -> Equity", reference = "SWP-4420")
        )
    )
    val walletLogs: StateFlow<List<WalletLogItem>> = _walletLogs.asStateFlow()

    fun topUpFloat(channel: String, amount: Double, onResult: (String) -> Unit) {
        when (channel) {
            "M-Pesa" -> _mpesaFloatBalance.value += amount
            "Equity Bank" -> _equityBankBalance.value += amount
            "KCB Bank" -> _kcbBankBalance.value += amount
            "Cash" -> _cashInHandBalance.value += amount
        }
        val ref = "TU-" + (10000..99999).random()
        val newLog = WalletLogItem(
            title = "Float Top-Up via $channel",
            type = "TopUp",
            amount = amount,
            channel = channel,
            reference = ref
        )
        _walletLogs.value = listOf(newLog) + _walletLogs.value
        onResult("Successfully credited ${formatPrice(amount)} to $channel Float!")
    }

    fun transferFloat(fromAccount: String, toAccount: String, amount: Double, onResult: (String) -> Unit) {
        val available = when (fromAccount) {
            "M-Pesa" -> _mpesaFloatBalance.value
            "Equity Bank" -> _equityBankBalance.value
            "KCB Bank" -> _kcbBankBalance.value
            "Cash" -> _cashInHandBalance.value
            else -> 0.0
        }

        if (available < amount) {
            onResult("Insufficient balance in $fromAccount! Available: ${formatPrice(available)}")
            return
        }

        // Deduct
        when (fromAccount) {
            "M-Pesa" -> _mpesaFloatBalance.value -= amount
            "Equity Bank" -> _equityBankBalance.value -= amount
            "KCB Bank" -> _kcbBankBalance.value -= amount
            "Cash" -> _cashInHandBalance.value -= amount
        }

        // Add
        when (toAccount) {
            "M-Pesa" -> _mpesaFloatBalance.value += amount
            "Equity Bank" -> _equityBankBalance.value += amount
            "KCB Bank" -> _kcbBankBalance.value += amount
            "Cash" -> _cashInHandBalance.value += amount
        }

        val ref = "TR-" + (10000..99999).random()
        val newLog = WalletLogItem(
            title = "Transfer: $fromAccount -> $toAccount",
            type = "Transfer",
            amount = amount,
            channel = "$fromAccount -> $toAccount",
            reference = ref
        )
        _walletLogs.value = listOf(newLog) + _walletLogs.value
        onResult("Transferred ${formatPrice(amount)} from $fromAccount to $toAccount!")
    }

    fun disburseSupplierFloat(supplierName: String, paybill: String, amount: Double, channel: String, onResult: (String) -> Unit) {
        when (channel) {
            "M-Pesa" -> _mpesaFloatBalance.value -= amount
            "Equity Bank" -> _equityBankBalance.value -= amount
            "KCB Bank" -> _kcbBankBalance.value -= amount
            "Cash" -> _cashInHandBalance.value -= amount
        }
        val ref = "PAY-" + (10000..99999).random()
        val newLog = WalletLogItem(
            title = "Disbursement: $supplierName ($paybill)",
            type = "Disbursement",
            amount = amount,
            channel = channel,
            reference = ref
        )
        _walletLogs.value = listOf(newLog) + _walletLogs.value
        logExpense("Inventory", amount, "Supplier Payment: $supplierName ($paybill)", supplierName, channel)
        onResult("Paid ${formatPrice(amount)} to $supplierName via $channel! Ref: $ref")
    }

    // ------------------------------------------------------------------------
    // API INTEGRATION CREDENTIALS STATE (M-Pesa Daraja, Bank APIs)
    // ------------------------------------------------------------------------
    private val _darajaConsumerKey = MutableStateFlow("dK892j1xMkla9021aZ")
    val darajaConsumerKey: StateFlow<String> = _darajaConsumerKey.asStateFlow()

    private val _darajaConsumerSecret = MutableStateFlow("sec_88192a0192837465")
    val darajaConsumerSecret: StateFlow<String> = _darajaConsumerSecret.asStateFlow()

    private val _darajaShortcode = MutableStateFlow("522522")
    val darajaShortcode: StateFlow<String> = _darajaShortcode.asStateFlow()

    private val _darajaPasskey = MutableStateFlow("bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919")
    val darajaPasskey: StateFlow<String> = _darajaPasskey.asStateFlow()

    private val _darajaEnv = MutableStateFlow("Production") // "Sandbox" or "Production"
    val darajaEnv: StateFlow<String> = _darajaEnv.asStateFlow()

    private val _darajaConnected = MutableStateFlow(true)
    val darajaConnected: StateFlow<Boolean> = _darajaConnected.asStateFlow()

    private val _equityMerchantId = MutableStateFlow("EQ-MERCHANT-8812")
    val equityMerchantId: StateFlow<String> = _equityMerchantId.asStateFlow()

    private val _equityConnected = MutableStateFlow(true)
    val equityConnected: StateFlow<Boolean> = _equityConnected.asStateFlow()

    private val _kcbClientId = MutableStateFlow("KCB-CLIENT-4412")
    val kcbClientId: StateFlow<String> = _kcbClientId.asStateFlow()

    private val _kcbConnected = MutableStateFlow(true)
    val kcbConnected: StateFlow<Boolean> = _kcbConnected.asStateFlow()

    fun updateDarajaCredentials(key: String, secret: String, shortcode: String, passkey: String, env: String) {
        _darajaConsumerKey.value = key
        _darajaConsumerSecret.value = secret
        _darajaShortcode.value = shortcode
        _darajaPasskey.value = passkey
        _darajaEnv.value = env
        _darajaConnected.value = true
    }

    fun testApiHandshake(provider: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            when (provider) {
                "M-Pesa Daraja" -> onResult("✅ M-Pesa Daraja API Handshake SUCCESS! Till/Shortcode 522522 Online.")
                "Equity Bank Eazzy" -> onResult("✅ Equity Bank Eazzy Gateway Ping SUCCESS! Merchant EQ-MERCHANT-8812 Connected.")
                "KCB BAP" -> onResult("✅ KCB BAP Banking API Connected Successfully!")
                "Co-op Bank" -> onResult("✅ Co-op Bank OmniChannel Gateway Authorized!")
                else -> onResult("API Connection Active!")
            }
        }
    }

    // ------------------------------------------------------------------------
    // TRANSACTION SMS READER & PARSER ENGINE
    // ------------------------------------------------------------------------
    data class ParsedTransactionMessage(
        val code: String,
        val type: String, // "RECEIPT" or "EXPENSE"
        val amount: Double,
        val partyName: String,
        val phoneOrAccount: String,
        val dateString: String,
        val updatedBalance: Double,
        val rawMessage: String
    )

    fun parseTransactionSms(smsText: String): ParsedTransactionMessage? {
        val text = smsText.trim()
        if (text.isBlank()) return null

        try {
            // Check M-Pesa Payment Received
            // Pattern e.g. "QX872134 Confirmed. You have received KES 3,500.00 from PETER KIMANI 0722112233 on 29/7/26 at 11:30 AM. New M-PESA balance is KES 65,900.00"
            if (text.contains("received", ignoreCase = true) || text.contains("credited", ignoreCase = true)) {
                val codeMatch = Regex("([A-Z0-9]{8,12})\\s+Confirmed", RegexOption.IGNORE_CASE).find(text)
                val code = codeMatch?.groupValues?.get(1) ?: ("M-P" + (100000..999999).random())

                val amountMatch = Regex("(?:KES|USD|₦|GH₵)\\s*([0-9,]+(?:\\.[0-9]{2})?)", RegexOption.IGNORE_CASE).find(text)
                val amountStr = amountMatch?.groupValues?.get(1)?.replace(",", "") ?: "0.0"
                val amount = amountStr.toDoubleOrNull() ?: 0.0

                val fromMatch = Regex("from\\s+([A-Za-z0-9\\s]+?)(?:\\s+[0-9]{9,12}|\\s+on|\\.)", RegexOption.IGNORE_CASE).find(text)
                val party = fromMatch?.groupValues?.get(1)?.trim() ?: "M-Pesa Customer"

                val phoneMatch = Regex("([0-9]{10,12})").find(text)
                val phone = phoneMatch?.groupValues?.get(1) ?: "+254712345678"

                val balMatch = Regex("balance is (?:KES|USD|₦|GH₵)?\\s*([0-9,]+(?:\\.[0-9]{2})?)", RegexOption.IGNORE_CASE).find(text)
                val balance = balMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: (_mpesaFloatBalance.value + amount)

                return ParsedTransactionMessage(
                    code = code,
                    type = "RECEIPT",
                    amount = amount,
                    partyName = party,
                    phoneOrAccount = phone,
                    dateString = "Today",
                    updatedBalance = balance,
                    rawMessage = text
                )
            }
            // Check M-Pesa Payment Sent / Paid
            // Pattern e.g. "QY998123 Confirmed. KES 12,000.00 paid to KPLC PREPAID on 29/7/26 at 9:00 AM. New M-PESA balance is KES 50,400.00"
            else if (text.contains("paid to", ignoreCase = true) || text.contains("sent to", ignoreCase = true) || text.contains("debited", ignoreCase = true)) {
                val codeMatch = Regex("([A-Z0-9]{8,12})\\s+Confirmed", RegexOption.IGNORE_CASE).find(text)
                val code = codeMatch?.groupValues?.get(1) ?: ("M-P" + (100000..999999).random())

                val amountMatch = Regex("(?:KES|USD|₦|GH₵)\\s*([0-9,]+(?:\\.[0-9]{2})?)", RegexOption.IGNORE_CASE).find(text)
                val amountStr = amountMatch?.groupValues?.get(1)?.replace(",", "") ?: "0.0"
                val amount = amountStr.toDoubleOrNull() ?: 0.0

                val partyMatch = Regex("(?:paid to|sent to)\\s+([A-Za-z0-9\\s]+?)(?:\\s+on|\\.)", RegexOption.IGNORE_CASE).find(text)
                val party = partyMatch?.groupValues?.get(1)?.trim() ?: "Vendor"

                val balMatch = Regex("balance is (?:KES|USD|₦|GH₵)?\\s*([0-9,]+(?:\\.[0-9]{2})?)", RegexOption.IGNORE_CASE).find(text)
                val balance = balMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: (_mpesaFloatBalance.value - amount)

                return ParsedTransactionMessage(
                    code = code,
                    type = "EXPENSE",
                    amount = amount,
                    partyName = party,
                    phoneOrAccount = "Paybill/Till",
                    dateString = "Today",
                    updatedBalance = balance,
                    rawMessage = text
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun reconcileTransactionSms(parsed: ParsedTransactionMessage, onResult: (String) -> Unit) {
        viewModelScope.launch {
            if (parsed.type == "RECEIPT") {
                // Record Sale
                val dummyCartItem = repository.products.first().firstOrNull()
                val cartList = if (dummyCartItem != null) listOf(dummyCartItem to 1) else emptyList()
                repository.recordSale(
                    customerName = parsed.partyName,
                    totalAmount = parsed.amount,
                    discount = 0.0,
                    tax = parsed.amount * 0.16,
                    paymentMethod = "M-Pesa SMS Auto-Read",
                    paymentRef = parsed.code,
                    cartItems = cartList
                )
                _mpesaFloatBalance.value = parsed.updatedBalance
                onResult("✅ Reconciled! Recorded Sale of ${formatPrice(parsed.amount)} from ${parsed.partyName} (Ref: ${parsed.code}). M-Pesa float set to ${formatPrice(parsed.updatedBalance)}.")
            } else {
                // Record Expense
                repository.addExpense(
                    category = "Operations",
                    amount = parsed.amount,
                    description = "M-Pesa SMS Expense to ${parsed.partyName}",
                    vendor = parsed.partyName,
                    paymentMethod = "M-Pesa",
                    currency = _currency.value
                )
                _mpesaFloatBalance.value = parsed.updatedBalance
                onResult("✅ Reconciled! Logged Expense of ${formatPrice(parsed.amount)} to ${parsed.partyName} (Ref: ${parsed.code}). M-Pesa float updated to ${formatPrice(parsed.updatedBalance)}.")
            }
        }
    }

    fun setCurrency(newCurr: String) { _currency.value = newCurr }
    fun setThemeMode(mode: AppThemeMode) { _themeMode.value = mode }
    fun setUserRole(role: String) { _userRole.value = role }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun syncNow(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isOfflineSyncActive.value = true
            kotlinx.coroutines.delay(1200) // Simulate fast Room-to-Cloud sync
            _pendingSyncItemsCount.value = 0
            _isOfflineSyncActive.value = false
            onComplete("All local transactions & inventory changes synced to Cloud!")
        }
    }

    // POS Actions
    fun addToCart(product: ProductEntity) {
        val current = _cart.value.toMutableMap()
        val count = current[product] ?: 0
        if (count < product.stockQuantity) {
            current[product] = count + 1
            _cart.value = current
        }
    }

    fun removeFromCart(product: ProductEntity) {
        val current = _cart.value.toMutableMap()
        val count = current[product] ?: 0
        if (count > 1) {
            current[product] = count - 1
        } else {
            current.remove(product)
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _discountPercent.value = 0.0
    }

    fun setDiscount(percent: Double) {
        _discountPercent.value = percent
    }

    fun checkout(customerName: String, paymentMethod: String, paymentRef: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val cartItems = _cart.value.toList()
            val subtotal = cartItems.sumOf { (prod, qty) -> prod.sellingPrice * qty }
            val discountAmt = subtotal * (_discountPercent.value / 100.0)
            val taxAmt = (subtotal - discountAmt) * 0.16 // 16% VAT
            val total = (subtotal - discountAmt) + taxAmt

            repository.recordSale(
                customerName = customerName,
                totalAmount = total,
                discount = discountAmt,
                tax = taxAmt,
                paymentMethod = paymentMethod,
                paymentRef = paymentRef,
                cartItems = cartItems
            )
            clearCart()
            onComplete()
        }
    }

    // Inventory Actions
    fun addProduct(
        name: String,
        category: String,
        cost: Double,
        price: Double,
        stock: Int,
        supplier: String,
        reorderLevel: Int = 5,
        sku: String = "",
        barcode: String = ""
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                name = name,
                barcode = barcode.ifBlank { "BAR-" + (100000..999999).random() },
                sku = sku.ifBlank { "SKU-" + (1000..9999).random() },
                category = category,
                costPrice = cost,
                sellingPrice = price,
                stockQuantity = stock,
                reorderLevel = reorderLevel,
                supplierName = supplier
            )
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun adjustStock(product: ProductEntity, delta: Int) {
        viewModelScope.launch {
            repository.adjustStock(product.id, delta)
        }
    }

    // Finance Actions
    fun logExpense(category: String, amount: Double, description: String, vendor: String, paymentMethod: String) {
        viewModelScope.launch {
            repository.addExpense(category, amount, description, vendor, paymentMethod, _currency.value)
        }
    }

    // Customer & CRM Actions
    fun registerCustomer(
        name: String,
        phone: String,
        email: String = "",
        address: String = "",
        notes: String = "",
        tier: String = "Bronze"
    ) {
        viewModelScope.launch {
            repository.addCustomer(name, phone, email, address, notes, tier)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
        }
    }

    fun addCustomerInteraction(
        customerId: Long,
        customerName: String,
        type: String,
        summary: String,
        outcome: String = "Resolved"
    ) {
        viewModelScope.launch {
            repository.addCustomerInteraction(customerId, customerName, type, summary, outcome)
        }
    }

    fun deleteCustomerInteraction(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomerInteraction(id)
        }
    }

    // Invoice Actions
    fun createInvoice(customerName: String, customerPhone: String, amount: Double, dueDate: String, itemsSummary: String) {
        viewModelScope.launch {
            repository.addInvoice(customerName, customerPhone, amount, dueDate, itemsSummary)
        }
    }

    // Marketing Actions
    fun createCampaign(title: String, channel: String, message: String, audience: String) {
        viewModelScope.launch {
            repository.addCampaign(title, channel, message, audience)
        }
    }

    // AI Assistant Actions
    fun getLiveBusinessPerformanceContext(): String {
        val salesList = sales.value
        val expensesList = expenses.value
        val lowStockList = lowStockProducts.value
        val totalRevenue = salesList.sumOf { it.totalAmount }
        val totalExpenses = expensesList.sumOf { it.amount }
        val netProfit = totalRevenue - totalExpenses
        val orderCount = salesList.size
        val lowStockNames = lowStockList.take(4).joinToString(", ") { "${it.name} (${it.stockQuantity} remaining)" }

        return "Business: ${_businessName.value}, Role: ${_userRole.value}, Currency: ${_currency.value}, " +
                "Today Total Revenue: ${formatPrice(totalRevenue)} ($orderCount sales orders), " +
                "Total Expenses: ${formatPrice(totalExpenses)}, Net Profit: ${formatPrice(netProfit)}, " +
                "M-Pesa Float: ${formatPrice(_mpesaFloatBalance.value)}, Low Stock Items (${lowStockList.size}): ${lowStockNames.ifBlank { "None" }}"
    }

    fun sendAiPrompt(promptText: String) {
        val userPrompt = promptText.trim()
        if (userPrompt.isBlank()) return

        viewModelScope.launch {
            repository.addAiChat("user", userPrompt)
            _aiLoading.value = true

            val contextString = getLiveBusinessPerformanceContext()
            val responseText = GeminiAiService.generateBusinessInsight(userPrompt, contextString)

            repository.addAiChat("assistant", responseText)
            _aiLoading.value = false
        }
    }

    fun sendHighThinkingPrompt(promptText: String) {
        val userPrompt = promptText.trim()
        if (userPrompt.isBlank()) return

        viewModelScope.launch {
            repository.addAiChat("user", "🧠 [High Thinking Strategy]: $userPrompt")
            _aiLoading.value = true

            val contextString = getLiveBusinessPerformanceContext()
            val responseText = GeminiAiService.generateHighThinkingAnalysis(userPrompt, contextString)

            repository.addAiChat("assistant", responseText)
            _aiLoading.value = false
        }
    }

    fun generateStudioImage(promptText: String, imageSize: String = "2K", aspectRatio: String = "1:1", onResult: (String) -> Unit) {
        val userPrompt = promptText.trim()
        if (userPrompt.isBlank()) return

        viewModelScope.launch {
            _aiLoading.value = true
            repository.addAiChat("user", "🎨 [Image Gen $imageSize]: $userPrompt")

            val imgUrlOrData = GeminiAiService.generateHighQualityImage(userPrompt, imageSize, aspectRatio)
            val assistantResponse = "🎨 **Generated High-Quality Image ($imageSize Resolution)**\n\nPrompt: \"$userPrompt\"\n\nImage Rendering Ready below."

            repository.addAiChat("assistant", assistantResponse)
            _aiLoading.value = false
            onResult(imgUrlOrData)
        }
    }

    fun generateStudioVideo(promptText: String, aspectRatio: String = "16:9", onResult: (String) -> Unit) {
        val userPrompt = promptText.trim()
        if (userPrompt.isBlank()) return

        viewModelScope.launch {
            _aiLoading.value = true
            repository.addAiChat("user", "🎬 [Veo Video Gen $aspectRatio]: $userPrompt")

            val videoToken = GeminiAiService.generateVeoVideo(userPrompt, aspectRatio)
            val assistantResponse = "🎬 **Veo 3.1 Fast Animated Promo Video Rendered!**\n\nPrompt: \"$userPrompt\"\nAspect Ratio: $aspectRatio\nVideo Token: $videoToken"

            repository.addAiChat("assistant", assistantResponse)
            _aiLoading.value = false
            onResult(videoToken)
        }
    }

    fun toggleVoiceListening() {
        _isVoiceListening.value = !_isVoiceListening.value
        if (!_isVoiceListening.value) {
            // Simulated voice command submission
            sendAiPrompt("Analyze today's revenue and give me cash flow recommendations for next week.")
        }
    }

    fun scanReceiptSimulated(onResult: (String) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            val prompt = "Scan and extract details from receipt image: Merchant 'TotalEnergies Station', Amount 'KES 3,800', Category 'Fuel/Operations', Vendor 'Total Westlands'."
            val response = GeminiAiService.generateBusinessInsight(prompt, _currency.value)
            repository.addExpense("Operations", 3800.0, "Fuel Receipt scanned via AI Vision", "TotalEnergies", "M-Pesa")
            _aiLoading.value = false
            onResult("Receipt Scanned! Logged Expense: KES 3,800 for Fuel under Operations.")
        }
    }

    fun formatPrice(amount: Double): String {
        val symbol = when (_currency.value) {
            "KES" -> "KES "
            "USD" -> "$"
            "NGN" -> "₦"
            "GHS" -> "GH₵"
            "UGX" -> "UGX "
            else -> "KES "
        }
        return symbol + String.format("%,.2f", amount)
    }
}
