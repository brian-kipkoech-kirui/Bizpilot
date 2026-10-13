package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.SaleEntity
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue

@Composable
fun RevenueTrendsWidget(
    sales: List<SaleEntity>,
    expenses: List<ExpenseEntity>,
    formatPrice: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val totalRevenue = remember(sales) { sales.sumOf { it.totalAmount } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val netProfit = totalRevenue - totalExpenses
    val grossMarginRatio = if (totalRevenue > 0) ((netProfit / totalRevenue) * 100).toInt() else 0

    val avgOrderValue = remember(sales) {
        if (sales.isNotEmpty()) totalRevenue / sales.size else 0.0
    }

    GlassCard(
        borderColor = MpesaGreen.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Revenue Trends",
                        tint = MpesaGreen
                    )
                    Column {
                        Text(
                            text = "Revenue & Profitability Trends",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Financial health analysis & operational margins",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MpesaGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MpesaGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Margin $grossMarginRatio%",
                        color = MpesaGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Visual Ratio Comparison Bar (Revenue vs Expenses)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = CircleShape, color = MpesaGreen, modifier = Modifier.size(8.dp)) {}
                        Text("Gross Sales: ${formatPrice(totalRevenue)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.error, modifier = Modifier.size(8.dp)) {}
                        Text("Expenses: ${formatPrice(totalExpenses)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                val totalSum = (totalRevenue + totalExpenses).coerceAtLeast(1.0)
                val revRatio = (totalRevenue / totalSum).toFloat().coerceIn(0.05f, 0.95f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(revRatio)
                            .background(MpesaGreen)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - revRatio)
                            .background(MaterialTheme.colorScheme.error)
                    )
                }
            }

            // Financial Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Net Profit
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(12.dp))
                            Text("Net Profit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            formatPrice(netProfit),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) MpesaGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Metric 2: Avg Order Value
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(12.dp))
                            Text("Avg Order Value", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            formatPrice(avgOrderValue),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
