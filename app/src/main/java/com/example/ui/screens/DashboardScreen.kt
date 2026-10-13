package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DailySalesChart
import com.example.ui.components.GlassCard
import com.example.ui.components.MicrophoneInputDialog
import com.example.ui.components.RevenueTrendsWidget
import com.example.ui.components.StatCard
import com.example.ui.components.TopSellingProductsChart
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel
import com.example.util.ReportExporter
import com.example.util.VoiceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BizPilotViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val products by viewModel.products.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val todayRevenue = sales.sumOf { it.totalAmount }
    val todayExpenses = expenses.sumOf { it.amount }
    val netProfit = todayRevenue - todayExpenses
    val healthScore = if (todayRevenue > 0) 88 else 75

    // Export Dialog state
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedExportScope by remember { mutableStateOf("Executive") } // "Executive", "Sales", "Inventory"
    var selectedExportFormat by remember { mutableStateOf("PDF") } // "PDF", "CSV"
    var selectedTimeframe by remember { mutableStateOf("All Time") } // "All Time", "Today"

    fun executeExport(scope: String, format: String) {
        val targetSales = if (selectedTimeframe == "Today") {
            val startOfToday = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            sales.filter { it.timestamp >= startOfToday }
        } else sales

        val targetExpenses = if (selectedTimeframe == "Today") {
            val startOfToday = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            expenses.filter { it.timestamp >= startOfToday }
        } else expenses

        if (format == "CSV") {
            if (scope == "Inventory") {
                val file = ReportExporter.exportInventoryCsv(context, products) { viewModel.formatPrice(it) }
                if (file != null) {
                    Toast.makeText(context, "Inventory Audit CSV Exported!", Toast.LENGTH_SHORT).show()
                    ReportExporter.shareExportedFile(context, file, "text/csv", "BizPilot Inventory Audit CSV")
                } else {
                    Toast.makeText(context, "Failed to generate CSV", Toast.LENGTH_SHORT).show()
                }
            } else {
                val file = ReportExporter.exportSalesCsv(context, targetSales) { viewModel.formatPrice(it) }
                if (file != null) {
                    Toast.makeText(context, "Sales Report CSV Exported!", Toast.LENGTH_SHORT).show()
                    ReportExporter.shareExportedFile(context, file, "text/csv", "BizPilot Sales Report CSV")
                } else {
                    Toast.makeText(context, "Failed to generate CSV", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val file = ReportExporter.exportExecutivePdf(context, targetSales, products, targetExpenses) { viewModel.formatPrice(it) }
            if (file != null) {
                Toast.makeText(context, "Executive PDF Business Report Generated!", Toast.LENGTH_SHORT).show()
                ReportExporter.shareExportedFile(context, file, "application/pdf", "BizPilot Executive PDF Business Report")
            } else {
                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Voice Microphone AI CEO state
    var showDashboardMicDialog by remember { mutableStateOf(false) }
    val voiceManager = remember { VoiceManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    if (showDashboardMicDialog) {
        MicrophoneInputDialog(
            viewModel = viewModel,
            voiceManager = voiceManager,
            onDismiss = { showDashboardMicDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
    ) {

        // Voice AI CEO Quick Mic Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MpesaGreen.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ask_ai_ceo_voice_card")
                    .clickable { showDashboardMicDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MpesaGreen,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Microphone AI CEO", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text("Ask AI CEO via Microphone", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Speak questions on revenue, net profit & inventory", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = { showDashboardMicDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ask CEO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }


        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today Sales",
                    value = viewModel.formatPrice(todayRevenue),
                    subtitle = "${sales.size} Transactions",
                    icon = Icons.Default.Payments,
                    iconColor = MpesaGreen,
                    trendText = "+18.4%",
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Expenses",
                    value = viewModel.formatPrice(todayExpenses),
                    subtitle = "${expenses.size} Logged",
                    icon = Icons.Default.ReceiptLong,
                    iconColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Net Profit",
                    value = viewModel.formatPrice(netProfit),
                    subtitle = "Margin: 31.2%",
                    icon = Icons.Default.AccountBalance,
                    iconColor = SecondaryAmber,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Low Stock Alerts",
                    value = "${lowStock.size} Products",
                    subtitle = "Reorder suggested",
                    icon = Icons.Default.WarningAmber,
                    iconColor = if (lowStock.isNotEmpty()) MaterialTheme.colorScheme.error else MpesaGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Offline Business Data Export Card
        item {
            GlassCard(
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.FileDownload,
                                        contentDescription = "Export Data",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Offline Business Data Export",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Export sales & stock audit reports to CSV or PDF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("export_reports_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Export Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { executeExport("Executive", "PDF") },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("export_pdf_btn"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { executeExport("Sales", "CSV") },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("export_sales_csv_btn"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = "Sales CSV", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sales CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { executeExport("Inventory", "CSV") },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("export_stock_csv_btn"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = "Stock CSV", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Stock CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Daily Sales Revenue Visualizer Chart
        item {
            DailySalesChart(
                sales = sales,
                formatPrice = { viewModel.formatPrice(it) }
            )
        }

        // Top Selling Products Breakdown Chart
        item {
            TopSellingProductsChart(
                products = products,
                sales = sales,
                formatPrice = { viewModel.formatPrice(it) }
            )
        }

        // Revenue & Profitability Trends Breakdown Widget
        item {
            RevenueTrendsWidget(
                sales = sales,
                expenses = expenses,
                formatPrice = { viewModel.formatPrice(it) }
            )
        }

        // Quick Action Chips Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Business Actions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        ActionChip(
                            title = "Export Reports",
                            icon = Icons.Default.FileDownload,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { showExportDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "New Sale (POS)",
                            icon = Icons.Default.AddShoppingCart,
                            color = MpesaGreen,
                            onClick = { onNavigate("pos") }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Log Expense",
                            icon = Icons.Default.RemoveCircleOutline,
                            color = MaterialTheme.colorScheme.error,
                            onClick = { onNavigate("finance") }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Scan Receipt",
                            icon = Icons.Default.DocumentScanner,
                            color = SecondaryAmber,
                            onClick = { onNavigate("ai_chat") }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Telegram Broadcast",
                            icon = Icons.Default.Send,
                            color = TelegramBlue,
                            onClick = { onNavigate("marketing") }
                        )
                    }
                }
            }
        }

        // Low Stock Banner
        if (lowStock.isNotEmpty()) {
            item {
                GlassCard(
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = "Low Stock", tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = "Critical Inventory Alert (${lowStock.size} Items)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        lowStock.take(3).forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(p.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Only ${p.stockQuantity} left", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { onNavigate("inventory") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Manage Stock & Supplier Orders")
                        }
                    }
                }
            }
        }

        // Recent Sales Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = { onNavigate("reports") }) {
                    Text("View All")
                }
            }
        }

        items(sales.take(5)) { sale ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MpesaGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (sale.paymentMethod == "M-Pesa") Icons.Default.Smartphone else Icons.Default.PointOfSale,
                                    contentDescription = sale.paymentMethod,
                                    tint = MpesaGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = sale.customerName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${sale.receiptNo} • ${sale.paymentMethod} (${sale.paymentReference})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = viewModel.formatPrice(sale.totalAmount),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MpesaGreen
                    )
                }
            }
        }
    }

    // Modal Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = MaterialTheme.colorScheme.primary)
                    Text("Export Business Reports")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Report Type:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedExportScope == "Executive",
                            onClick = { selectedExportScope = "Executive" },
                            label = { Text("Executive Full", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                        FilterChip(
                            selected = selectedExportScope == "Sales",
                            onClick = { selectedExportScope = "Sales" },
                            label = { Text("Sales Only", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                        FilterChip(
                            selected = selectedExportScope == "Inventory",
                            onClick = { selectedExportScope = "Inventory" },
                            label = { Text("Stock Audit", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                    }

                    Text("Select Format:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedExportFormat == "PDF",
                            onClick = { selectedExportFormat = "PDF" },
                            label = { Text("PDF Document (.pdf)", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                        FilterChip(
                            selected = selectedExportFormat == "CSV",
                            onClick = { selectedExportFormat = "CSV" },
                            label = { Text("CSV Spreadsheet (.csv)", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                    }

                    Text("Timeframe Filter:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedTimeframe == "All Time",
                            onClick = { selectedTimeframe = "All Time" },
                            label = { Text("All Records", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedTimeframe == "Today",
                            onClick = { selectedTimeframe = "Today" },
                            label = { Text("Today's Operations", fontSize = 11.sp) }
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Summary preview
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📊 Export Summary Preview", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("• Sales Revenue: ${viewModel.formatPrice(todayRevenue)} (${sales.size} transactions)", fontSize = 11.sp)
                            Text("• Inventory Assets: ${products.size} products (${lowStock.size} low stock items)", fontSize = 11.sp)
                            Text("• File Type: $selectedExportFormat format ($selectedExportScope Scope)", fontSize = 11.sp, color = MpesaGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        executeExport(selectedExportScope, selectedExportFormat)
                        showExportDialog = false
                    },
                    modifier = Modifier.testTag("confirm_export_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    modifier = Modifier.testTag("close_export_dialog_btn")
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ActionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

