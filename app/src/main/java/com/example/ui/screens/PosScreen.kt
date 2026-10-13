package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showStkPushModal by remember { mutableStateOf(false) }
    var showBarcodeScannerModal by remember { mutableStateOf(false) }
    var showReceiptModal by remember { mutableStateOf(false) }
    var lastReceiptNo by remember { mutableStateOf("") }
    var lastMpesaRef by remember { mutableStateOf("") }
    var mpesaPin by remember { mutableStateOf("") }

    // Staff PIN Fallback Auth State
    var isPosLocked by remember { mutableStateOf(false) }
    var showStaffAuthModal by remember { mutableStateOf(false) }
    var showChangePinModal by remember { mutableStateOf(false) }
    var staffPinInput by remember { mutableStateOf("") }
    var storedStaffPin by remember { mutableStateOf("1234") }
    var newPinInput by remember { mutableStateOf("") }
    var authErrorText by remember { mutableStateOf("") }

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("254712345678") }
    var paymentMethod by remember { mutableStateOf("M-Pesa") }

    val categories = listOf("All", "Electronics", "Groceries", "Tech", "Accessories")

    val filteredProducts = products.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
    }

    val cartItems = cart.toList()
    val subtotal = cartItems.sumOf { (prod, qty) -> prod.sellingPrice * qty }
    val discountAmt = subtotal * (discountPercent / 100.0)
    val taxAmt = (subtotal - discountAmt) * 0.16
    val totalAmount = (subtotal - discountAmt) + taxAmt

    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val pendingPosSyncCount by viewModel.pendingPosSyncCount.collectAsState()
    val posSyncStatus by viewModel.posSyncStatus.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // POS Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Smart POS Counter", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isNetworkAvailable) MpesaGreen else SecondaryAmber,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Text(
                        if (isNetworkAvailable) "Online Sync Active" else "Offline Mode (Room Cache)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = CircleShape,
                    color = if (isPosLocked) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else TelegramBlue.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { showStaffAuthModal = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPosLocked) Icons.Default.Lock else Icons.Default.Security,
                            contentDescription = "Staff Auth",
                            tint = if (isPosLocked) MaterialTheme.colorScheme.error else TelegramBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isPosLocked) "Till Locked" else "Staff Auth",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isPosLocked) MaterialTheme.colorScheme.error else TelegramBlue
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.clickable { showBarcodeScannerModal = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Scan Barcode", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MpesaGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = "M-Pesa", tint = MpesaGreen, modifier = Modifier.size(16.dp))
                        Text("M-Pesa", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MpesaGreen)
                    }
                }
            }
        }

        if (pendingPosSyncCount > 0 || !isNetworkAvailable) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isNetworkAvailable) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else SecondaryAmber.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { viewModel.triggerPosBackgroundSync() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isNetworkAvailable) Icons.Default.CloudSync else Icons.Default.CloudOff,
                            contentDescription = "Sync",
                            tint = if (isNetworkAvailable) MaterialTheme.colorScheme.primary else SecondaryAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = posSyncStatus,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (pendingPosSyncCount > 0 && isNetworkAvailable) {
                        Text(
                            "Sync Now",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = (selectedCategory == cat),
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) }
                )
            }
        }

        // Product Catalog Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredProducts) { prod ->
                GlassCard(
                    onClick = { viewModel.addToCart(prod) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = prod.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Stock: ${prod.stockQuantity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (prod.stockQuantity <= prod.reorderLevel) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = prod.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = viewModel.formatPrice(prod.sellingPrice),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MpesaGreen
                            )

                            IconButton(
                                onClick = { viewModel.addToCart(prod) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Cart Summary Bar
        if (cart.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 80.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = MaterialTheme.colorScheme.primary)
                            Text("Current Cart (${cartItems.sumOf { it.second }} items)", fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear Cart", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total (incl. 16% VAT):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatPrice(totalAmount), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MpesaGreen)
                        }

                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = "Checkout")
                            Spacer(Modifier.width(8.dp))
                            Text("Checkout Now")
                        }
                    }
                }
            }
        }
    }

    // Barcode Scanner Modal Dialog
    if (showBarcodeScannerModal) {
        AlertDialog(
            onDismissRequest = { showBarcodeScannerModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", tint = MaterialTheme.colorScheme.primary)
                    Text("AI Barcode & QR Scanner", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera Viewfinder", tint = MpesaGreen, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Scanning barcode...", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Text("Quick Pick Scanned Sample:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        products.take(3).forEach { prod ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.addToCart(prod)
                                    showBarcodeScannerModal = false
                                    Toast.makeText(context, "Scanned & Added: ${prod.name}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${prod.barcode} - ${prod.name}", fontSize = 12.sp, maxLines = 1)
                                    Text(viewModel.formatPrice(prod.sellingPrice), color = MpesaGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBarcodeScannerModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Checkout Modal Dialog
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Checkout & Payment Settlement", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Total Payable: ${viewModel.formatPrice(totalAmount)}", fontWeight = FontWeight.Bold, color = MpesaGreen)

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("M-Pesa / Phone Number (+254...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Payment Method:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("M-Pesa", "Cash", "Card", "QR").forEach { method ->
                            FilterChip(
                                selected = (paymentMethod == method),
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        if (paymentMethod == "M-Pesa") {
                            showStkPushModal = true
                        } else {
                            val ref = "CS-" + (10000..99999).random()
                            lastMpesaRef = ref
                            viewModel.checkout(
                                customerName = customerName.ifBlank { "Walk-in Customer" },
                                paymentMethod = paymentMethod,
                                paymentRef = ref
                            ) {
                                lastReceiptNo = "REC-" + (1000..9999).random()
                                showReceiptModal = true
                                Toast.makeText(context, "Sale Completed via $paymentMethod!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Text(if (paymentMethod == "M-Pesa") "Send M-Pesa STK Push" else "Confirm & Issue Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // M-Pesa STK Push Prompt Modal Dialog
    if (showStkPushModal) {
        AlertDialog(
            onDismissRequest = { showStkPushModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Smartphone, contentDescription = "STK Push", tint = MpesaGreen)
                    Text("M-Pesa Express STK Push", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MpesaGreen.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Safari Retail & Tech Mart", fontWeight = FontWeight.Bold)
                            Text("Pay Bill / Till: 522522", style = MaterialTheme.typography.bodySmall)
                            Text("Amount: ${viewModel.formatPrice(totalAmount)}", fontWeight = FontWeight.Bold, color = MpesaGreen)
                            Text("Phone: +$customerPhone", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    OutlinedTextField(
                        value = mpesaPin,
                        onValueChange = { if (it.length <= 4) mpesaPin = it },
                        label = { Text("Enter M-Pesa PIN (Simulated)") },
                        placeholder = { Text("****") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ref = "QX" + (10000000..99999999).random()
                        lastMpesaRef = ref
                        showStkPushModal = false
                        viewModel.checkout(
                            customerName = customerName.ifBlank { "M-Pesa Customer" },
                            paymentMethod = "M-Pesa Express",
                            paymentRef = ref
                        ) {
                            lastReceiptNo = "REC-" + (1000..9999).random()
                            showReceiptModal = true
                            mpesaPin = ""
                            Toast.makeText(context, "M-Pesa Payment Confirmed! Ref: $ref", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                    enabled = mpesaPin.length == 4
                ) {
                    Text("Authorize Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStkPushModal = false }) {
                    Text("Cancel STK Push")
                }
            }
        )
    }

    // Receipt Modal Dialog
    if (showReceiptModal) {
        AlertDialog(
            onDismissRequest = { showReceiptModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = "Receipt", tint = MpesaGreen)
                    Text("Digital Sales Receipt", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Safari Retail & Tech Mart", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Receipt #: $lastReceiptNo", style = MaterialTheme.typography.labelSmall)
                        if (lastMpesaRef.isNotBlank()) {
                            Text("M-Pesa Ref: $lastMpesaRef", style = MaterialTheme.typography.labelSmall, color = MpesaGreen, fontWeight = FontWeight.Bold)
                        }
                        Text("Payment: $paymentMethod (Settled)", style = MaterialTheme.typography.labelSmall, color = MpesaGreen, fontWeight = FontWeight.Bold)
                        Divider()
                        Text("Thank you for your business!", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            val itemsSummary = cart.entries.joinToString("\n") { entry ->
                                "• ${entry.key.name} x${entry.value} - ${viewModel.formatPrice(entry.key.sellingPrice * entry.value)}"
                            }
                            val receiptText = WhatsAppHelper.buildReceiptText(
                                businessName = "Safari Retail & Tech Mart",
                                receiptNo = lastReceiptNo,
                                itemsSummary = itemsSummary,
                                totalAmount = viewModel.formatPrice(totalAmount),
                                paymentMethod = paymentMethod,
                                paymentRef = lastMpesaRef
                            )
                            WhatsAppHelper.openWhatsAppChat(context, customerPhone.ifBlank { null }, receiptText)
                            showReceiptModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Send via WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Receipt", "Safari Retail Receipt #$lastReceiptNo - Amount ${viewModel.formatPrice(totalAmount)} Settled via $paymentMethod Ref: $lastMpesaRef")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Receipt text copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy")
                        }

                        Button(
                            onClick = {
                                showReceiptModal = false
                                Toast.makeText(context, "Receipt saved to history!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = "Done", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Done")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showReceiptModal = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 4-Digit Staff PIN Fallback Authentication Modal Dialog
    if (showStaffAuthModal) {
        AlertDialog(
            onDismissRequest = { if (!isPosLocked) showStaffAuthModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column {
                        Text("Staff PIN Authentication", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("POS Counter Security Access", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Biometric Sensor Status Alert
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SecondaryAmber.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = SecondaryAmber, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Biometric Sensor Unavailable", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SecondaryAmber)
                                Text("Using 4-Digit Staff PIN fallback method for cashier terminal verification.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                        }
                    }

                    Text("Enter 4-Digit Staff PIN", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))

                    // 4-Digit Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < staffPinInput.length
                            Surface(
                                shape = CircleShape,
                                color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                            ) {}
                        }
                    }

                    if (authErrorText.isNotBlank()) {
                        Text(authErrorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    // Interactive Numeric Keypad
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val keypad = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "⌫")
                        )

                        keypad.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { key ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clickable {
                                                when (key) {
                                                    "C" -> {
                                                        staffPinInput = ""
                                                        authErrorText = ""
                                                    }
                                                    "⌫" -> {
                                                        if (staffPinInput.isNotEmpty()) {
                                                            staffPinInput = staffPinInput.dropLast(1)
                                                            authErrorText = ""
                                                        }
                                                    }
                                                    else -> {
                                                        if (staffPinInput.length < 4) {
                                                            staffPinInput += key
                                                            authErrorText = ""
                                                            if (staffPinInput.length == 4) {
                                                                if (staffPinInput == storedStaffPin) {
                                                                    isPosLocked = false
                                                                    showStaffAuthModal = false
                                                                    staffPinInput = ""
                                                                    Toast.makeText(context, "Staff Authenticated! (PIN Fallback Authorized)", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    authErrorText = "Invalid PIN. Try default '1234'"
                                                                    staffPinInput = ""
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showChangePinModal = true }) {
                            Text("Change PIN (Default: 1234)", fontSize = 11.sp)
                        }

                        if (!isPosLocked) {
                            TextButton(onClick = {
                                isPosLocked = true
                                showStaffAuthModal = false
                                Toast.makeText(context, "POS Till Terminal Locked!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(4.dp))
                                Text("Lock Terminal", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (staffPinInput == storedStaffPin) {
                            isPosLocked = false
                            showStaffAuthModal = false
                            staffPinInput = ""
                            Toast.makeText(context, "Staff Authenticated via PIN Fallback!", Toast.LENGTH_SHORT).show()
                        } else {
                            authErrorText = "Invalid PIN. Default is '1234'"
                            staffPinInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = staffPinInput.length == 4
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Authenticate Staff")
                }
            },
            dismissButton = {
                if (!isPosLocked) {
                    TextButton(onClick = { showStaffAuthModal = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Change Staff PIN Modal
    if (showChangePinModal) {
        AlertDialog(
            onDismissRequest = { showChangePinModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Update Staff 4-Digit PIN", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Set a new 4-digit PIN for quick cashier fallback access when biometric sensors are unavailable.", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) newPinInput = it },
                        label = { Text("New 4-Digit PIN") },
                        placeholder = { Text("e.g. 5678") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            storedStaffPin = newPinInput
                            showChangePinModal = false
                            newPinInput = ""
                            Toast.makeText(context, "Staff PIN updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newPinInput.length == 4
                ) {
                    Text("Save New PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
