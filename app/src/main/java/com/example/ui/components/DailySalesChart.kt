package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleEntity
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import java.text.SimpleDateFormat
import java.util.*

data class DailySalesData(
    val dayLabel: String,
    val fullDate: String,
    val totalRevenue: Double,
    val orderCount: Int
)

@Composable
fun DailySalesChart(
    sales: List<SaleEntity>,
    formatPrice: (Double) -> String,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var chartType by remember { mutableStateOf("Bar") } // "Bar" or "Trend"
    var timeframeDays by remember { mutableStateOf(7) } // 7 or 14

    // Group sales into daily buckets
    val dailyDataList = remember(sales, timeframeDays) {
        val calendar = Calendar.getInstance()
        val list = mutableListOf<DailySalesData>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        for (i in (timeframeDays - 1) downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val startOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val daySales = sales.filter { it.timestamp in startOfDay..endOfDay }
            val revenue = daySales.sumOf { it.totalAmount }
            val dayLabel = dayFormat.format(calendar.time)
            val fullDate = dateFormat.format(calendar.time)

            list.add(
                DailySalesData(
                    dayLabel = dayLabel,
                    fullDate = fullDate,
                    totalRevenue = revenue,
                    orderCount = daySales.size
                )
            )
        }
        list
    }

    val maxRevenue = remember(dailyDataList) {
        val max = dailyDataList.maxOfOrNull { it.totalRevenue } ?: 1.0
        if (max == 0.0) 1000.0 else max
    }

    val totalPeriodRevenue = remember(dailyDataList) {
        dailyDataList.sumOf { it.totalRevenue }
    }

    GlassCard(
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
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
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Daily Revenue Chart",
                        tint = MpesaGreen
                    )
                    Column {
                        Text(
                            text = "Daily Sales Revenue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total (${timeframeDays}D): ${formatPrice(totalPeriodRevenue)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MpesaGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Controls Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = timeframeDays == 7,
                        onClick = { timeframeDays = 7 },
                        label = { Text("7D", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = timeframeDays == 14,
                        onClick = { timeframeDays = 14 },
                        label = { Text("14D", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Selected Bar/Day Info Badge
            selectedDayIndex?.let { index ->
                if (index in dailyDataList.indices) {
                    val selected = dailyDataList[index]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selected.dayLabel} (${selected.fullDate}):",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${formatPrice(selected.totalRevenue)} • ${selected.orderCount} sales",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MpesaGreen
                            )
                        }
                    }
                }
            }

            // Interactive Canvas Chart
            val barColor = MpesaGreen
            val accentColor = SecondaryAmber
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dailyDataList) {
                            detectTapGestures { offset ->
                                val widthPerItem = size.width / dailyDataList.size
                                val tappedIndex = (offset.x / widthPerItem).toInt().coerceIn(0, dailyDataList.size - 1)
                                selectedDayIndex = if (selectedDayIndex == tappedIndex) null else tappedIndex
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomPadding = 30.dp.toPx()
                    val topPadding = 10.dp.toPx()
                    val chartHeight = canvasHeight - bottomPadding - topPadding
                    val itemCount = dailyDataList.size
                    val itemWidth = canvasWidth / itemCount
                    val barWidth = (itemWidth * 0.55f).coerceAtMost(28.dp.toPx())

                    // Draw dashed target horizontal line
                    val targetY = topPadding + chartHeight * 0.25f
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, targetY),
                        end = Offset(canvasWidth, targetY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw Bars or Trend Line
                    if (chartType == "Bar") {
                        dailyDataList.forEachIndexed { index, data ->
                            val heightRatio = (data.totalRevenue / maxRevenue).toFloat().coerceIn(0.05f, 1.0f)
                            val barHeight = chartHeight * heightRatio
                            val x = (index * itemWidth) + (itemWidth - barWidth) / 2f
                            val y = canvasHeight - bottomPadding - barHeight

                            val isSelected = selectedDayIndex == index
                            val currentBarColor = if (isSelected) accentColor else barColor

                            // Bar gradient
                            val brush = Brush.verticalGradient(
                                colors = listOf(
                                    currentBarColor,
                                    currentBarColor.copy(alpha = 0.4f)
                                )
                            )

                            // Draw rounded rectangle bar
                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            // Highlight border if selected
                            if (isSelected) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    } else {
                        // Smooth line trend
                        val path = Path()
                        dailyDataList.forEachIndexed { index, data ->
                            val heightRatio = (data.totalRevenue / maxRevenue).toFloat().coerceIn(0.05f, 1.0f)
                            val x = (index * itemWidth) + (itemWidth / 2f)
                            val y = canvasHeight - bottomPadding - (chartHeight * heightRatio)

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }

                            // Point circle
                            drawCircle(
                                color = if (selectedDayIndex == index) accentColor else barColor,
                                radius = if (selectedDayIndex == index) 6.dp.toPx() else 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }

                        drawPath(
                            path = path,
                            color = barColor,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            // Days x-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dailyDataList.forEachIndexed { index, data ->
                    val isSelected = selectedDayIndex == index
                    Text(
                        text = data.dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SecondaryAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            selectedDayIndex = if (selectedDayIndex == index) null else index
                        }
                    )
                }
            }
        }
    }
}
