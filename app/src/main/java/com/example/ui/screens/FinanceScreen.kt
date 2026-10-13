package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.StatCard
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.viewmodel.BizPilotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val invoices by viewModel.invoices.collectAsState()

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showInvoiceDialog by remember { mutableStateOf(false) }

    var expCategory by remember { mutableStateOf("Rent") }
    var expAmount by remember { mutableStateOf("") }
    var expDesc by remember { mutableStateOf("") }
    var expVendor by remember { mutableStateOf("") }

    var invCustomer by remember { mutableStateOf("") }
    var invPhone by remember { mutableStateOf("") }
    var invAmount by remember { mutableStateOf("") }
    var invItems by remember { mutableStateOf("") }

    val totalRevenue = sales.sumOf { it.totalAmount }
    val totalExpense = expenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpense

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Finance & Bookkeeping", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("P&L, Wallet Float & SMS Auto-Reconciliation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showExpenseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Log Expense", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showInvoiceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("New Invoice", fontSize = 12.sp)
                    }
                }
            }
        }

        // Wallet Liquidity Glance Card
        item {
            val mpesaFloat by viewModel.mpesaFloatBalance.collectAsState()
            val equityBal by viewModel.equityBankBalance.collectAsState()
            val kcbBal by viewModel.kcbBankBalance.collectAsState()
            val cashBal by viewModel.cashInHandBalance.collectAsState()
            val totalWallet = mpesaFloat + equityBal + kcbBal + cashBal

            GlassCard(
                borderColor = MpesaGreen.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet", tint = MpesaGreen)
                            Text("SME Digital Wallet Float", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Text(viewModel.formatPrice(totalWallet), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MpesaGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("M-Pesa Float: ${viewModel.formatPrice(mpesaFloat)}", style = MaterialTheme.typography.labelSmall, color = MpesaGreen)
                        Text("Equity Bank: ${viewModel.formatPrice(equityBal)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("KCB Bank: ${viewModel.formatPrice(kcbBal)}", style = MaterialTheme.typography.labelSmall, color = SecondaryAmber)
                    }
                }
            }
        }

        // Financial Overview
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("P&L Ledger Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gross Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatPrice(totalRevenue), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MpesaGreen)
                        }
                        Column {
                            Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatPrice(totalExpense), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        }
                        Column {
                            Text("Net Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(viewModel.formatPrice(netProfit), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SecondaryAmber)
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Est. 16% VAT Liability:", style = MaterialTheme.typography.labelMedium)
                            Text(viewModel.formatPrice(totalRevenue * 0.16), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedButton(
                            onClick = {
                                val reportText = """
                                    📊 *BizPilot Financial P&L Statement*
                                    Business: Safari Retail & Tech Mart
                                    Gross Revenue: ${viewModel.formatPrice(totalRevenue)}
                                    Expenses: ${viewModel.formatPrice(totalExpense)}
                                    Net Profit: ${viewModel.formatPrice(netProfit)}
                                    Est. VAT (16%): ${viewModel.formatPrice(totalRevenue * 0.16)}
                                    Generated via BizPilot AI Business OS
                                """.trimIndent()

                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("P&L Statement", reportText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "P&L Report copied & ready to share!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Export Report", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export Report", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Invoices Section
        item {
            Text("Outstanding & Recent Invoices", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(invoices) { inv ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(inv.customerName, fontWeight = FontWeight.Bold)
                        Text("${inv.invoiceNumber} • Due: ${inv.dueDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Items: ${inv.itemsSummary}", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(viewModel.formatPrice(inv.totalAmount), fontWeight = FontWeight.Bold, color = MpesaGreen)
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = if (inv.status == "Paid") MpesaGreen.copy(alpha = 0.2f) else SecondaryAmber.copy(alpha = 0.2f)
                        ) {
                            Text(inv.status, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (inv.status == "Paid") MpesaGreen else SecondaryAmber, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Expense Logs Section
        item {
            Text("Recent Expenses Logged", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(expenses) { exp ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(exp.description, fontWeight = FontWeight.Bold)
                        Text("${exp.category} • Vendor: ${exp.vendor} (${exp.paymentMethod})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(viewModel.formatPrice(exp.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Add Expense Dialog
    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Log Business Expense", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = expDesc, onValueChange = { expDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = expAmount, onValueChange = { expAmount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = expVendor, onValueChange = { expVendor = it }, label = { Text("Vendor / Merchant") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = expCategory, onValueChange = { expCategory = it }, label = { Text("Category (Rent, Utilities, etc.)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expAmount.toDoubleOrNull() ?: 0.0
                        if (expDesc.isNotBlank() && amt > 0) {
                            viewModel.logExpense(expCategory, amt, expDesc, expVendor, "M-Pesa")
                            showExpenseDialog = false
                            Toast.makeText(context, "Expense Logged!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Invoice Dialog
    if (showInvoiceDialog) {
        AlertDialog(
            onDismissRequest = { showInvoiceDialog = false },
            title = { Text("Generate Customer Invoice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = invCustomer, onValueChange = { invCustomer = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = invPhone, onValueChange = { invPhone = it }, label = { Text("Customer Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = invAmount, onValueChange = { invAmount = it }, label = { Text("Total Amount") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = invItems, onValueChange = { invItems = it }, label = { Text("Items Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = invAmount.toDoubleOrNull() ?: 0.0
                        if (invCustomer.isNotBlank() && amt > 0) {
                            viewModel.createInvoice(invCustomer, invPhone, amt, "2026-08-15", invItems)
                            showInvoiceDialog = false
                            Toast.makeText(context, "Invoice Created!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Generate & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInvoiceDialog = false }) { Text("Cancel") }
            }
        )
    }
}
