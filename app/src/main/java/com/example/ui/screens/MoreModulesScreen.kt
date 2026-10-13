package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedModuleTab by remember { mutableStateOf(0) }

    val tabs = listOf("Wallet", "SMS Reader", "APIs & Banks", "Detailed Reports", "CRM", "HR", "Marketing", "Settings")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedModuleTab,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = (selectedModuleTab == index),
                    onClick = { selectedModuleTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedModuleTab) {
                0 -> WalletSection(viewModel)
                1 -> SmsReaderSection(viewModel)
                2 -> ApiIntegrationsSection(viewModel)
                3 -> DetailedReportsSection(viewModel)
                4 -> CrmSection(viewModel)
                5 -> HrSection(viewModel)
                6 -> MarketingSection(viewModel)
                7 -> SettingsSection(viewModel)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// WALLET SECTION (Float & Liquidity Management)
// ----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSection(viewModel: BizPilotViewModel) {
    val context = LocalContext.current
    val mpesaFloat by viewModel.mpesaFloatBalance.collectAsState()
    val equityBalance by viewModel.equityBankBalance.collectAsState()
    val kcbBalance by viewModel.kcbBankBalance.collectAsState()
    val cashBalance by viewModel.cashInHandBalance.collectAsState()
    val logs by viewModel.walletLogs.collectAsState()

    val totalLiquidity = mpesaFloat + equityBalance + kcbBalance + cashBalance

    var showTopUpDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showDisburseDialog by remember { mutableStateOf(false) }

    var topUpChannel by remember { mutableStateOf("M-Pesa") }
    var topUpAmountText by remember { mutableStateOf("") }

    var transferFrom by remember { mutableStateOf("M-Pesa") }
    var transferTo by remember { mutableStateOf("Equity Bank") }
    var transferAmountText by remember { mutableStateOf("") }

    var supplierName by remember { mutableStateOf("") }
    var supplierPaybill by remember { mutableStateOf("") }
    var disburseAmountText by remember { mutableStateOf("") }
    var disburseChannel by remember { mutableStateOf("M-Pesa") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            GlassCard(
                borderColor = MpesaGreen.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet", tint = MpesaGreen)
                            Text("SME Liquidity Wallet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Surface(shape = CircleShape, color = MpesaGreen.copy(alpha = 0.2f)) {
                            Text("Active Float", color = MpesaGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp)
                        }
                    }

                    Text("Total Business Balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(viewModel.formatPrice(totalLiquidity), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = MpesaGreen)

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showTopUpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = "Top Up", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Top Up", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showTransferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sweep/Transfer", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showDisburseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryAmber),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = "Pay Supplier", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Pay Supplier", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("Float & Bank Account Breakdown", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Smartphone, contentDescription = "M-Pesa", tint = MpesaGreen, modifier = Modifier.size(16.dp))
                                Text("M-Pesa Till/Paybill", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(viewModel.formatPrice(mpesaFloat), fontWeight = FontWeight.Bold, color = MpesaGreen)
                        }
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.AccountBalance, contentDescription = "Equity", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text("Equity Bank", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(viewModel.formatPrice(equityBalance), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.AccountBalance, contentDescription = "KCB", tint = TelegramBlue, modifier = Modifier.size(16.dp))
                                Text("KCB BAP Account", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(viewModel.formatPrice(kcbBalance), fontWeight = FontWeight.Bold, color = TelegramBlue)
                        }
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Payments, contentDescription = "Cash", tint = SecondaryAmber, modifier = Modifier.size(16.dp))
                                Text("Petty Cash Box", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(viewModel.formatPrice(cashBalance), fontWeight = FontWeight.Bold, color = SecondaryAmber)
                        }
                    }
                }
            }
        }

        item {
            Text("Wallet Activity History", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }

        items(logs) { log ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(log.title, fontWeight = FontWeight.Bold)
                        Text("${log.channel} • Ref: ${log.reference}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(
                        text = (if (log.type == "Disbursement") "- " else "+ ") + viewModel.formatPrice(log.amount),
                        fontWeight = FontWeight.Bold,
                        color = if (log.type == "Disbursement") MaterialTheme.colorScheme.error else MpesaGreen
                    )
                }
            }
        }
    }

    // Top Up Modal
    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("Fund Wallet / Load Float") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Channel:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("M-Pesa", "Equity Bank", "KCB Bank", "Cash").forEach { ch ->
                            FilterChip(
                                selected = (topUpChannel == ch),
                                onClick = { topUpChannel = ch },
                                label = { Text(ch, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = topUpAmountText,
                        onValueChange = { topUpAmountText = it },
                        label = { Text("Amount to Load") },
                        placeholder = { Text("e.g. 10000") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = topUpAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.topUpFloat(topUpChannel, amt) { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showTopUpDialog = false
                                topUpAmountText = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Text("Confirm Load")
                }
            },
            dismissButton = { TextButton(onClick = { showTopUpDialog = false }) { Text("Cancel") } }
        )
    }

    // Transfer Modal
    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Sweep / Transfer Between Float Accounts") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("From Account:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("M-Pesa", "Equity Bank", "KCB Bank", "Cash").forEach { ch ->
                            FilterChip(selected = (transferFrom == ch), onClick = { transferFrom = ch }, label = { Text(ch, fontSize = 10.sp) })
                        }
                    }
                    Text("To Account:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Equity Bank", "M-Pesa", "KCB Bank", "Cash").forEach { ch ->
                            FilterChip(selected = (transferTo == ch), onClick = { transferTo = ch }, label = { Text(ch, fontSize = 10.sp) })
                        }
                    }
                    OutlinedTextField(
                        value = transferAmountText,
                        onValueChange = { transferAmountText = it },
                        label = { Text("Transfer Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = transferAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.transferFloat(transferFrom, transferTo, amt) { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showTransferDialog = false
                                transferAmountText = ""
                            }
                        }
                    }
                ) {
                    Text("Execute Transfer")
                }
            },
            dismissButton = { TextButton(onClick = { showTransferDialog = false }) { Text("Cancel") } }
        )
    }

    // Disburse Supplier Modal
    if (showDisburseDialog) {
        AlertDialog(
            onDismissRequest = { showDisburseDialog = false },
            title = { Text("Pay Supplier / Vendor Float") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = supplierName, onValueChange = { supplierName = it }, label = { Text("Supplier Business Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = supplierPaybill, onValueChange = { supplierPaybill = it }, label = { Text("Paybill / Till Number / Acc") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = disburseAmountText, onValueChange = { disburseAmountText = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                    Text("Payment Channel:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("M-Pesa", "Equity Bank", "KCB Bank").forEach { ch ->
                            FilterChip(selected = (disburseChannel == ch), onClick = { disburseChannel = ch }, label = { Text(ch, fontSize = 10.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = disburseAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && supplierName.isNotBlank()) {
                            viewModel.disburseSupplierFloat(supplierName, supplierPaybill, amt, disburseChannel) { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                showDisburseDialog = false
                                supplierName = ""
                                supplierPaybill = ""
                                disburseAmountText = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryAmber)
                ) {
                    Text("Pay Vendor")
                }
            },
            dismissButton = { TextButton(onClick = { showDisburseDialog = false }) { Text("Cancel") } }
        )
    }
}

// ----------------------------------------------------------------------------
// SMS READER & PARSER SECTION
// ----------------------------------------------------------------------------
@Composable
fun SmsReaderSection(viewModel: BizPilotViewModel) {
    val context = LocalContext.current
    var smsInputText by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<BizPilotViewModel.ParsedTransactionMessage?>(null) }

    val sampleReceiptSms = "QX872134 Confirmed. You have received KES 3,500.00 from PETER KIMANI 0722112233 on 29/7/26 at 11:30 AM. New M-PESA balance is KES 65,900.00. Cost KES 0.00."
    val sampleExpenseSms = "QY998123 Confirmed. KES 12,000.00 paid to KPLC PREPAID on 29/7/26 at 9:00 AM. New M-PESA balance is KES 50,400.00. Cost KES 0.00."

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            GlassCard(borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Sms, contentDescription = "SMS Reader", tint = MaterialTheme.colorScheme.primary)
                        Text("Automated SMS Transaction Reader", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text("Paste any M-Pesa or Bank transaction SMS below to automatically extract data, update float balances, and reconcile sales/expenses.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Quick Sample SMS Loaders:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                smsInputText = sampleReceiptSms
                                parsedResult = viewModel.parseTransactionSms(sampleReceiptSms)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Received Sale SMS", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                smsInputText = sampleExpenseSms
                                parsedResult = viewModel.parseTransactionSms(sampleExpenseSms)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Paid Expense SMS", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = smsInputText,
                        onValueChange = {
                            smsInputText = it
                            parsedResult = viewModel.parseTransactionSms(it)
                        },
                        label = { Text("Paste M-Pesa / Bank SMS Here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = { Text("e.g. QX872134 Confirmed. You have received KES 3,500.00...") }
                    )
                }
            }
        }

        if (parsedResult != null) {
            val p = parsedResult!!
            item {
                GlassCard(
                    borderColor = if (p.type == "RECEIPT") MpesaGreen else MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = if (p.type == "RECEIPT") MpesaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)) {
                                Text(
                                    text = if (p.type == "RECEIPT") "INCOMING PAYMENT (SALE)" else "OUTGOING PAYMENT (EXPENSE)",
                                    color = if (p.type == "RECEIPT") MpesaGreen else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp
                                )
                            }
                            Text("Ref: ${p.code}", fontWeight = FontWeight.Bold)
                        }

                        Text("Amount: ${viewModel.formatPrice(p.amount)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (p.type == "RECEIPT") MpesaGreen else MaterialTheme.colorScheme.error)
                        Text("Party: ${p.partyName} (${p.phoneOrAccount})", style = MaterialTheme.typography.bodyMedium)
                        Text("New Extracted Balance: ${viewModel.formatPrice(p.updatedBalance)}", style = MaterialTheme.typography.bodySmall, color = MpesaGreen, fontWeight = FontWeight.Bold)

                        Button(
                            onClick = {
                                viewModel.reconcileTransactionSms(p) { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    smsInputText = ""
                                    parsedResult = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (p.type == "RECEIPT") MpesaGreen else MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = "Reconcile", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Auto-Record & Reconcile in Database")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// API GATEWAYS & BANKS INTEGRATION SECTION
// ----------------------------------------------------------------------------
@Composable
fun ApiIntegrationsSection(viewModel: BizPilotViewModel) {
    val context = LocalContext.current
    val key by viewModel.darajaConsumerKey.collectAsState()
    val secret by viewModel.darajaConsumerSecret.collectAsState()
    val shortcode by viewModel.darajaShortcode.collectAsState()
    val passkey by viewModel.darajaPasskey.collectAsState()
    val env by viewModel.darajaEnv.collectAsState()
    val darajaConn by viewModel.darajaConnected.collectAsState()

    val equityMerchant by viewModel.equityMerchantId.collectAsState()
    val equityConn by viewModel.equityConnected.collectAsState()
    val kcbClient by viewModel.kcbClientId.collectAsState()
    val kcbConn by viewModel.kcbConnected.collectAsState()

    var edKey by remember { mutableStateOf(key) }
    var edSecret by remember { mutableStateOf(secret) }
    var edShortcode by remember { mutableStateOf(shortcode) }
    var edPasskey by remember { mutableStateOf(passkey) }
    var edEnv by remember { mutableStateOf(env) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text("Banking & Payment Gateway Integrations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        item {
            GlassCard(borderColor = MpesaGreen.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Smartphone, contentDescription = "M-Pesa Daraja", tint = MpesaGreen)
                            Text("M-Pesa Daraja 2.0 API Gateway", fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = CircleShape, color = if (darajaConn) MpesaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)) {
                            Text(if (darajaConn) "ONLINE" else "OFFLINE", color = if (darajaConn) MpesaGreen else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(value = edShortcode, onValueChange = { edShortcode = it }, label = { Text("Till / Paybill Shortcode") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = edKey, onValueChange = { edKey = it }, label = { Text("Consumer Key") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = edSecret, onValueChange = { edSecret = it }, label = { Text("Consumer Secret") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = (edEnv == "Production"), onClick = { edEnv = "Production" }, label = { Text("Production") })
                        FilterChip(selected = (edEnv == "Sandbox"), onClick = { edEnv = "Sandbox" }, label = { Text("Sandbox (Dev)") })
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.updateDarajaCredentials(edKey, edSecret, edShortcode, edPasskey, edEnv)
                                Toast.makeText(context, "M-Pesa Credentials Saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Credentials")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.testApiHandshake("M-Pesa Daraja") { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Daraja Ping")
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "Equity", tint = MaterialTheme.colorScheme.primary)
                            Text("Equity Bank Eazzy API Gateway", fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = CircleShape, color = MpesaGreen.copy(alpha = 0.2f)) {
                            Text("ACTIVE", color = MpesaGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                        }
                    }
                    Text("Merchant ID: $equityMerchant", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(
                        onClick = {
                            viewModel.testApiHandshake("Equity Bank Eazzy") { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ping Equity Bank Gateway")
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "KCB", tint = TelegramBlue)
                            Text("KCB Bank BAP API", fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = CircleShape, color = MpesaGreen.copy(alpha = 0.2f)) {
                            Text("ACTIVE", color = MpesaGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                        }
                    }
                    Text("Client ID: $kcbClient", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(
                        onClick = {
                            viewModel.testApiHandshake("KCB BAP") { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ping KCB Gateway")
                    }
                }
            }
        }

        item {
            GlassCard(borderColor = MpesaGreen.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp Cloud API", tint = MpesaGreen)
                            Text("WhatsApp Business Cloud API", fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = CircleShape, color = MpesaGreen.copy(alpha = 0.2f)) {
                            Text("CONNECTED", color = MpesaGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp)
                        }
                    }

                    Text(
                        "Send automatic POS receipts, order updates, and CRM templates directly via Meta WhatsApp Cloud API.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    var waPhoneId by remember { mutableStateOf("109847291823901") }
                    var waTestPhone by remember { mutableStateOf("+254 712 345678") }

                    OutlinedTextField(
                        value = waPhoneId,
                        onValueChange = { waPhoneId = it },
                        label = { Text("WhatsApp Phone Number ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = waTestPhone,
                        onValueChange = { waTestPhone = it },
                        label = { Text("Test Recipient Phone (+254...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                WhatsAppHelper.openWhatsAppChat(
                                    context = context,
                                    phoneNumber = waTestPhone,
                                    message = "🎉 *BizPilot WhatsApp Integration Test*\n\nHello! Your WhatsApp Business integration is active and working perfectly with BizPilot AI POS!"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Test", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Test WhatsApp Chat")
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "WhatsApp Webhook Listener active on https://api.bizpilot.ai/whatsapp/webhook", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Check Webhook")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DETAILED REPORTS SECTION
// ----------------------------------------------------------------------------
@Composable
fun DetailedReportsSection(viewModel: BizPilotViewModel) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    val grossRevenue = sales.sumOf { it.totalAmount }
    val totalExpenses = expenses.sumOf { it.amount }
    val estimatedCogs = grossRevenue * 0.58 // 58% estimated Cost of Goods Sold
    val grossProfit = grossRevenue - estimatedCogs
    val operatingIncome = grossProfit - totalExpenses
    val vatLiability = grossRevenue * 0.16
    val netIncomeAfterTax = operatingIncome - vatLiability

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Detailed Executive Financial Statement", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Button(
                    onClick = {
                        val reportText = """
                            📊 *BizPilot Comprehensive P&L Report*
                            Gross Sales Revenue: ${viewModel.formatPrice(grossRevenue)}
                            Cost of Goods Sold (COGS): ${viewModel.formatPrice(estimatedCogs)}
                            Gross Profit: ${viewModel.formatPrice(grossProfit)} (Margin: 42.0%)
                            Operating Expenses: ${viewModel.formatPrice(totalExpenses)}
                            EBITDA / Operating Income: ${viewModel.formatPrice(operatingIncome)}
                            Est. 16% VAT Tax Liability: ${viewModel.formatPrice(vatLiability)}
                            Net Profit After Tax: ${viewModel.formatPrice(netIncomeAfterTax)}
                            Generated via BizPilot AI Operating System
                        """.trimIndent()

                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("P&L Report", reportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Full Financial Report Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export Statement", fontSize = 12.sp)
                }
            }
        }

        item {
            GlassCard(borderColor = MpesaGreen.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("P&L Income Statement Breakdown", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Revenue (Sales)")
                        Text(viewModel.formatPrice(grossRevenue), fontWeight = FontWeight.Bold, color = MpesaGreen)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Less: Direct COGS (Stock Cost)")
                        Text("- " + viewModel.formatPrice(estimatedCogs), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Profit (42.0% Margin)", fontWeight = FontWeight.Bold)
                        Text(viewModel.formatPrice(grossProfit), fontWeight = FontWeight.Bold, color = MpesaGreen)
                    }

                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Less: Total Operating Expenses")
                        Text("- " + viewModel.formatPrice(totalExpenses), color = MaterialTheme.colorScheme.error)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Operating EBITDA")
                        Text(viewModel.formatPrice(operatingIncome), fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Est. 16% VAT Tax Liability")
                        Text("- " + viewModel.formatPrice(vatLiability), color = SecondaryAmber)
                    }

                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Business Income (Profit)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(viewModel.formatPrice(netIncomeAfterTax), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MpesaGreen)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category Profit Margin Breakdown", fontWeight = FontWeight.Bold)
                    Text("• Electronics & Solar: Gross Margin 32.4% (Highest Contributor)", style = MaterialTheme.typography.bodySmall)
                    Text("• Organic Groceries: Gross Margin 28.2%", style = MaterialTheme.typography.bodySmall)
                    Text("• Tech & Accessories: Gross Margin 45.0%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Payment Channel Share", fontWeight = FontWeight.Bold)
                    Text("• M-Pesa Express & Till: 68% (KES 57,460)", style = MaterialTheme.typography.bodySmall, color = MpesaGreen, fontWeight = FontWeight.Bold)
                    Text("• Bank Transfer (Equity/KCB): 18% (KES 15,210)", style = MaterialTheme.typography.bodySmall)
                    Text("• Cash in Hand: 14% (KES 11,830)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// CRM MODULE: CUSTOMER RELATIONS & INTERACTION HISTORY
// ----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmSection(viewModel: BizPilotViewModel) {
    val customers by viewModel.customers.collectAsState()
    val interactions by viewModel.customerInteractions.collectAsState()
    val context = LocalContext.current

    var selectedCrmSubTab by remember { mutableStateOf(0) } // 0: Directory, 1: Interaction Timeline
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf("ALL") }

    // Dialog states
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<com.example.data.model.CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<com.example.data.model.CustomerEntity?>(null) }
    var showLogInteractionDialog by remember { mutableStateOf(false) }
    var preselectedCustomerForInteraction by remember { mutableStateOf<com.example.data.model.CustomerEntity?>(null) }
    var selectedCustomerForWhatsApp by remember { mutableStateOf<com.example.data.model.CustomerEntity?>(null) }
    var selectedCustomerHistoryView by remember { mutableStateOf<com.example.data.model.CustomerEntity?>(null) }

    // Customer Form Inputs
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var tierInput by remember { mutableStateOf("Bronze") }

    // Interaction Form Inputs
    var selectedCustomerForNewLog by remember { mutableStateOf<com.example.data.model.CustomerEntity?>(null) }
    var interactionTypeInput by remember { mutableStateOf("Phone Call") }
    var summaryInput by remember { mutableStateOf("") }
    var outcomeInput by remember { mutableStateOf("Resolved") }

    // WhatsApp Template Data
    var selectedTemplate by remember { mutableStateOf("Payment Reminder") }
    var customTemplateData by remember { mutableStateOf("KES 2,500") }

    val filteredCustomers = remember(customers, searchQuery, selectedTierFilter) {
        customers.filter { c ->
            val matchesQuery = searchQuery.isBlank() ||
                    c.name.contains(searchQuery, ignoreCase = true) ||
                    c.phone.contains(searchQuery, ignoreCase = true) ||
                    c.email.contains(searchQuery, ignoreCase = true) ||
                    c.address.contains(searchQuery, ignoreCase = true)
            val matchesTier = selectedTierFilter == "ALL" || c.tier.equals(selectedTierFilter, ignoreCase = true)
            matchesQuery && matchesTier
        }
    }

    val totalSpentSum = customers.sumOf { it.totalSpent }
    val totalLoyaltySum = customers.sumOf { it.loyaltyPoints }
    val vipCount = customers.count { it.tier == "VIP" || it.tier == "Gold" }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Module Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Customer Relationship Management",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Manage client contact records, tier loyalty, and contact history",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        nameInput = ""
                        phoneInput = ""
                        emailInput = ""
                        addressInput = ""
                        notesInput = ""
                        tierInput = "Bronze"
                        showAddCustomerDialog = true
                    },
                    modifier = Modifier.testTag("add_customer_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Customer", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        selectedCustomerForNewLog = customers.firstOrNull()
                        summaryInput = ""
                        interactionTypeInput = "Phone Call"
                        outcomeInput = "Resolved"
                        showLogInteractionDialog = true
                    },
                    modifier = Modifier.testTag("log_interaction_btn")
                ) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = "Log", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Log Action", fontSize = 12.sp)
                }
            }
        }

        // Summary KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Customers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${customers.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text("$vipCount VIP/Gold", style = MaterialTheme.typography.labelSmall, color = SecondaryAmber)
                }
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Interactions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${interactions.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MpesaGreen)
                    Text("Logged logs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Loyalty Pts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalLoyaltySum Pts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SecondaryAmber)
                    Text("Total Issued", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Search & Sub-Tabs Navigation
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, phone, email, or address...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sub-tab switcher
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = selectedCrmSubTab == 0,
                            onClick = { selectedCrmSubTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Directory (${filteredCustomers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        SegmentedButton(
                            selected = selectedCrmSubTab == 1,
                            onClick = { selectedCrmSubTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Interaction Log (${interactions.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Tier Filters
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL", "VIP", "Gold", "Silver", "Bronze").forEach { tier ->
                            FilterChip(
                                selected = (selectedTierFilter == tier),
                                onClick = { selectedTierFilter = tier },
                                label = { Text(tier, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Sub-Tab Content
        if (selectedCrmSubTab == 0) {
            // Customer Directory List
            if (filteredCustomers.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No customer records found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        val customerInteractionCount = interactions.count { it.customerId == customer.id }
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    customer.name.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Surface(
                                                    shape = MaterialTheme.shapes.extraSmall,
                                                    color = when (customer.tier) {
                                                        "VIP" -> Color(0xFFFFD700).copy(alpha = 0.2f)
                                                        "Gold" -> SecondaryAmber.copy(alpha = 0.2f)
                                                        "Silver" -> Color.LightGray.copy(alpha = 0.3f)
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                ) {
                                                    Text(
                                                        "${customer.tier} Tier",
                                                        color = when (customer.tier) {
                                                            "VIP", "Gold" -> SecondaryAmber
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Text("📱 ${customer.phone} ${if (customer.email.isNotBlank()) "• ✉️ ${customer.email}" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (customer.address.isNotBlank()) {
                                                Text("📍 Location: ${customer.address}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            if (customer.notes.isNotBlank()) {
                                                Text("📝 Notes: ${customer.notes}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                            }
                                            if (customer.purchaseHistory.isNotBlank()) {
                                                Text("🛒 Order History: ${customer.purchaseHistory}", style = MaterialTheme.typography.labelSmall, color = MpesaGreen)
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Spent: ${viewModel.formatPrice(customer.totalSpent)}", fontWeight = FontWeight.Bold, color = MpesaGreen, fontSize = 13.sp)
                                        Text("${customer.loyaltyPoints} Loyalty Pts", style = MaterialTheme.typography.labelSmall, color = SecondaryAmber)
                                        Text("$customerInteractionCount interaction(s)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Quick Actions Toolbar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = {
                                                selectedCustomerForWhatsApp = customer
                                                customTemplateData = "KES 2,500"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("WhatsApp CRM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                selectedCustomerForNewLog = customer
                                                summaryInput = ""
                                                interactionTypeInput = "Phone Call"
                                                outcomeInput = "Resolved"
                                                showLogInteractionDialog = true
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.AddComment, contentDescription = "Log", modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("+ Log Call/Visit", fontSize = 11.sp)
                                        }

                                        TextButton(
                                            onClick = { selectedCustomerHistoryView = customer },
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.History, contentDescription = "Logs", modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Timeline ($customerInteractionCount)", fontSize = 11.sp)
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                customerToEdit = customer
                                                nameInput = customer.name
                                                phoneInput = customer.phone
                                                emailInput = customer.email
                                                addressInput = customer.address
                                                notesInput = customer.notes
                                                tierInput = customer.tier
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { customerToDelete = customer },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Customer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Interaction History Timeline
            if (interactions.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No customer interaction history recorded yet. Click '+ Log Action' to add a interaction log.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(interactions, key = { it.id }) { log ->
                        val dateFormatted = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (log.interactionType) {
                                                "WhatsApp" -> MpesaGreen.copy(alpha = 0.2f)
                                                "Phone Call" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                "In-Person Visit" -> SecondaryAmber.copy(alpha = 0.2f)
                                                "Email" -> TelegramBlue.copy(alpha = 0.2f)
                                                "Complaint" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                val icon = when (log.interactionType) {
                                                    "Phone Call" -> Icons.Default.Call
                                                    "WhatsApp" -> Icons.Default.Send
                                                    "In-Person Visit" -> Icons.Default.Store
                                                    "Email" -> Icons.Default.Email
                                                    "Complaint" -> Icons.Default.Warning
                                                    "Quote Request" -> Icons.Default.Description
                                                    else -> Icons.Default.Chat
                                                }
                                                Icon(
                                                    icon,
                                                    contentDescription = log.interactionType,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = when (log.interactionType) {
                                                        "WhatsApp" -> MpesaGreen
                                                        "Phone Call" -> MaterialTheme.colorScheme.primary
                                                        "In-Person Visit" -> SecondaryAmber
                                                        "Email" -> TelegramBlue
                                                        "Complaint" -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                Text(
                                                    log.interactionType,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (log.interactionType) {
                                                        "WhatsApp" -> MpesaGreen
                                                        "Phone Call" -> MaterialTheme.colorScheme.primary
                                                        "In-Person Visit" -> SecondaryAmber
                                                        "Email" -> TelegramBlue
                                                        "Complaint" -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        }

                                        Text(log.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = when (log.outcome) {
                                            "Resolved" -> MpesaGreen.copy(alpha = 0.15f)
                                            "Follow-up Required", "Pending" -> SecondaryAmber.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Text(
                                            log.outcome,
                                            color = when (log.outcome) {
                                                "Resolved" -> MpesaGreen
                                                "Follow-up Required", "Pending" -> SecondaryAmber
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(log.summary, style = MaterialTheme.typography.bodyMedium, fontSize = 13.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Logged by ${log.loggedBy} on $dateFormatted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    IconButton(
                                        onClick = { viewModel.deleteCustomerInteraction(log.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Log", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // DIALOGS
    // ------------------------------------------------------------------------

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer", tint = MaterialTheme.colorScheme.primary)
                    Text("Register New Customer")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Customer Full Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number (+254...) *") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_phone_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_email_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Physical Location / City") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_address_input"),
                        singleLine = true
                    )
                    Text("Initial Customer Tier:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Bronze", "Silver", "Gold", "VIP").forEach { t ->
                            FilterChip(
                                selected = (tierInput == t),
                                onClick = { tierInput = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Customer Preferences / Internal Notes") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                            viewModel.registerCustomer(
                                name = nameInput.trim(),
                                phone = phoneInput.trim(),
                                email = emailInput.trim(),
                                address = addressInput.trim(),
                                notes = notesInput.trim(),
                                tier = tierInput
                            )
                            showAddCustomerDialog = false
                            Toast.makeText(context, "Customer Profile Created!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter customer Name and Phone number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("save_customer_btn")
                ) { Text("Save Profile") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Customer Dialog
    customerToEdit?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = MaterialTheme.colorScheme.primary)
                    Text("Edit Profile: ${customer.name}")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Customer Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Location / Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Customer Tier:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Bronze", "Silver", "Gold", "VIP").forEach { t ->
                            FilterChip(
                                selected = (tierInput == t),
                                onClick = { tierInput = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Internal Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                            val updated = customer.copy(
                                name = nameInput.trim(),
                                phone = phoneInput.trim(),
                                email = emailInput.trim(),
                                address = addressInput.trim(),
                                notes = notesInput.trim(),
                                tier = tierInput
                            )
                            viewModel.updateCustomer(updated)
                            customerToEdit = null
                            Toast.makeText(context, "Customer Profile Updated!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Save Changes") }
            },
            dismissButton = {
                TextButton(onClick = { customerToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // Delete Customer Confirmation Dialog
    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer Profile?") },
            text = { Text("Are you sure you want to delete '${customer.name}' (${customer.phone})? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(customer.id)
                        customerToDelete = null
                        Toast.makeText(context, "Customer Deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Log Interaction Dialog
    if (showLogInteractionDialog) {
        AlertDialog(
            onDismissRequest = { showLogInteractionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = "Log Interaction", tint = MaterialTheme.colorScheme.primary)
                    Text("Log Customer Interaction")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Customer:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var expandedDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomerForNewLog?.name ?: "Select Customer...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            customers.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.name} (${c.phone})") },
                                    onClick = {
                                        selectedCustomerForNewLog = c
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Interaction Channel / Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    val types = listOf("Phone Call", "WhatsApp", "In-Person Visit", "Email", "Quote Request", "Complaint", "Follow-up")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        types.chunked(3).forEach { rowTypes ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                rowTypes.forEach { type ->
                                    FilterChip(
                                        selected = (interactionTypeInput == type),
                                        onClick = { interactionTypeInput = type },
                                        label = { Text(type, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = summaryInput,
                        onValueChange = { summaryInput = it },
                        label = { Text("Interaction Summary / Notes *") },
                        modifier = Modifier.fillMaxWidth().testTag("interaction_summary_input"),
                        minLines = 3
                    )

                    Text("Outcome / Resolution Status:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Resolved", "Follow-up Required", "Pending", "Closed").forEach { status ->
                            FilterChip(
                                selected = (outcomeInput == status),
                                onClick = { outcomeInput = status },
                                label = { Text(status, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetCust = selectedCustomerForNewLog
                        if (targetCust != null && summaryInput.isNotBlank()) {
                            viewModel.addCustomerInteraction(
                                customerId = targetCust.id,
                                customerName = targetCust.name,
                                type = interactionTypeInput,
                                summary = summaryInput.trim(),
                                outcome = outcomeInput
                            )
                            showLogInteractionDialog = false
                            Toast.makeText(context, "Interaction Logged Successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please select a customer and enter summary notes", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("save_interaction_btn")
                ) { Text("Save Log") }
            },
            dismissButton = {
                TextButton(onClick = { showLogInteractionDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Customer Specific History Timeline BottomSheet / Dialog
    selectedCustomerHistoryView?.let { cust ->
        val custLogs = interactions.filter { it.customerId == cust.id }
        AlertDialog(
            onDismissRequest = { selectedCustomerHistoryView = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                    Text("Timeline: ${cust.name}")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Phone: ${cust.phone} • Tier: ${cust.tier} • Total Spent: ${viewModel.formatPrice(cust.totalSpent)}", style = MaterialTheme.typography.labelSmall)
                    Divider()
                    if (custLogs.isEmpty()) {
                        Text("No interaction records for this customer yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(custLogs) { l ->
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${l.interactionType} - ${l.outcome}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MpesaGreen)
                                            Text(java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(l.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(l.summary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedCustomerForWhatsApp = cust
                        selectedCustomerHistoryView = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp Customer")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerHistoryView = null }) { Text("Close") }
            }
        )
    }

    // WhatsApp Template Builder Dialog
    selectedCustomerForWhatsApp?.let { customer ->
        AlertDialog(
            onDismissRequest = { selectedCustomerForWhatsApp = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = MpesaGreen)
                    Text("WhatsApp CRM - ${customer.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Message Template:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    val templates = listOf("Payment Reminder", "Loyalty Reward", "Order Status Update", "Feedback & Followup")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        templates.forEach { template ->
                            FilterChip(
                                selected = (selectedTemplate == template),
                                onClick = { selectedTemplate = template },
                                label = { Text(template, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customTemplateData,
                        onValueChange = { customTemplateData = it },
                        label = { Text("Variable Value (Amount/Points/Order ID)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val generatedMessage = WhatsAppHelper.buildCrmTemplate(
                        customerName = customer.name,
                        templateType = selectedTemplate,
                        customData = customTemplateData
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Preview:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MpesaGreen)
                            Text(generatedMessage, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalMsg = WhatsAppHelper.buildCrmTemplate(
                            customerName = customer.name,
                            templateType = selectedTemplate,
                            customData = customTemplateData
                        )
                        WhatsAppHelper.openWhatsAppChat(context, customer.phone, finalMsg)
                        // Also auto-log interaction
                        viewModel.addCustomerInteraction(
                            customerId = customer.id,
                            customerName = customer.name,
                            type = "WhatsApp",
                            summary = "Sent template '$selectedTemplate' via WhatsApp.",
                            outcome = "Resolved"
                        )
                        selectedCustomerForWhatsApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Send via WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerForWhatsApp = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HrSection(viewModel: BizPilotViewModel) {
    val employees by viewModel.employees.collectAsState()
    val currentRole by viewModel.userRole.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Employee & Access Control", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Role-Based Access Switcher (Testing)", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Owner", "Manager", "Cashier", "Accountant").forEach { role ->
                        FilterChip(
                            selected = (currentRole == role),
                            onClick = { viewModel.setUserRole(role) },
                            label = { Text(role) }
                        )
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(employees) { emp ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(emp.name, fontWeight = FontWeight.Bold)
                            Text("Phone: ${emp.phone}", style = MaterialTheme.typography.labelSmall)
                        }
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                            Text(emp.role, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketingSection(viewModel: BizPilotViewModel) {
    val campaigns by viewModel.campaigns.collectAsState()
    val context = LocalContext.current

    var showCampaignDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("WhatsApp Business") }
    var msg by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("VIP & Gold Customers") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Marketing & Broadcasts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Button(onClick = { showCampaignDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)) {
                Icon(Icons.Default.Send, contentDescription = "Broadcast", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Broadcast")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(campaigns) { camp ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(camp.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (camp.channel.contains("WhatsApp", ignoreCase = true)) MpesaGreen.copy(alpha = 0.2f) else TelegramBlue.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    camp.channel,
                                    color = if (camp.channel.contains("WhatsApp", ignoreCase = true)) MpesaGreen else TelegramBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(camp.message, style = MaterialTheme.typography.bodySmall)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target: ${camp.targetAudience} • ${camp.conversionsCount} sales", style = MaterialTheme.typography.labelSmall, color = MpesaGreen, fontWeight = FontWeight.Bold)
                            
                            Button(
                                onClick = {
                                    val broadcastText = "📢 *${camp.title}*\n\n${camp.message}\n\n_Reply to order directly on WhatsApp!_"
                                    WhatsAppHelper.openWhatsAppChat(context, null, broadcastText)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Send WhatsApp", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCampaignDialog) {
        AlertDialog(
            onDismissRequest = { showCampaignDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = "Campaign", tint = MpesaGreen)
                    Text("Create Marketing Broadcast")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Campaign Title") }, modifier = Modifier.fillMaxWidth())
                    
                    Text("Broadcast Channel:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("WhatsApp Business", "Telegram", "SMS").forEach { ch ->
                            FilterChip(
                                selected = (channel == ch),
                                onClick = { channel = ch },
                                label = { Text(ch, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("Broadcast Message") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = audience, onValueChange = { audience = it }, label = { Text("Target Customer Segment") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createCampaign(title, channel, msg, audience)
                            if (channel.contains("WhatsApp", ignoreCase = true)) {
                                val broadcastText = "📢 *$title*\n\n$msg\n\n_Reply to order directly on WhatsApp!_"
                                WhatsAppHelper.openWhatsAppChat(context, null, broadcastText)
                            }
                            showCampaignDialog = false
                            Toast.makeText(context, "Broadcast Launched via $channel!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) { Text("Launch Broadcast") }
            },
            dismissButton = { TextButton(onClick = { showCampaignDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsSection(viewModel: BizPilotViewModel) {
    val context = LocalContext.current
    val currentCurr by viewModel.currency.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val userEmail by viewModel.authUserEmail.collectAsState()
    val userName by viewModel.authUserName.collectAsState()
    val isLoggedIn by viewModel.authIsLoggedIn.collectAsState()
    val syncStatus by viewModel.firestoreSyncStatus.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        item {
            Text("System & Platform Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        // Firebase Auth & Google Sign-In Card
        item {
            GlassCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "User Auth", tint = MaterialTheme.colorScheme.primary)
                            Text("Google Sign-In & Firebase Auth", fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = CircleShape, color = if (isLoggedIn) MpesaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)) {
                            Text(
                                if (isLoggedIn) "VERIFIED" else "SIGNED OUT",
                                color = if (isLoggedIn) MpesaGreen else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (isLoggedIn) {
                        Text("Merchant Account: $userName ($userEmail)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Firestore Status: $syncStatus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    viewModel.performFirestoreSync()
                                    Toast.makeText(context, "Business Data Synced to Firestore Cloud!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sync Firestore")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.handleSignOut()
                                    Toast.makeText(context, "Signed out of Firebase", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sign Out")
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.handleGoogleSignIn("pkoechkirui001@gmail.com", "Peter Kirui - Safari Retail")
                                Toast.makeText(context, "Google Sign-In Success! Authenticated via Firebase", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Login, contentDescription = "Google Sign In", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign In with Google Account")
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Base Business Currency", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("KES", "USD", "NGN", "GHS", "UGX").forEach { curr ->
                            FilterChip(
                                selected = (currentCurr == curr),
                                onClick = { viewModel.setCurrency(curr) },
                                label = { Text(curr) }
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("UI Theme & Platform Mode", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = (themeMode == AppThemeMode.DARK),
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            label = { Text("Dark Mode") }
                        )
                        FilterChip(
                            selected = (themeMode == AppThemeMode.TELEGRAM_MINI_APP),
                            onClick = { viewModel.setThemeMode(AppThemeMode.TELEGRAM_MINI_APP) },
                            label = { Text("Telegram Mini App") }
                        )
                        FilterChip(
                            selected = (themeMode == AppThemeMode.LIGHT),
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            label = { Text("Light Mode") }
                        )
                    }
                }
            }
        }
    }
}

