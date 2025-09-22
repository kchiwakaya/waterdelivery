package com.balckliquid.h20.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.util.*
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.graphics.vector.ImageVector
import com.balckliquid.h20.models.Delivery
import com.balckliquid.h20.models.Expense
import com.google.firebase.firestore.CollectionReference
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import java.time.ZoneId
import kotlin.math.abs

data class TruckReport(
    val totalWaterDelivered: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val profit: Double = 0.0,
    val deliveryCount: Int = 0,
    val averageDeliverySize: Double = 0.0,
    val monthlyStats: Map<String, MonthlyStats> = emptyMap()
)

data class MonthlyStats(
    val revenue: Double = 0.0,
    val expenses: Double = 0.0,
    val deliveries: Int = 0
)



@Composable
fun ReportsScreen(selectedTruckReg: String?) {
    var report by remember { mutableStateOf<TruckReport?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    val startDialogState = rememberMaterialDialogState()
    val endDialogState = rememberMaterialDialogState()
    var allTrucks by remember { mutableStateOf(selectedTruckReg == null) }

    val currencyFormatter = NumberFormat.getCurrencyInstance()

    // Compose-compatible date pickers
    MaterialDialog(
        dialogState = startDialogState,
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        }
    ) {
        datepicker { localDate ->
            startDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }
    }
    MaterialDialog(
        dialogState = endDialogState,
        buttons = {
            positiveButton("OK")
            negativeButton("Cancel")
        }
    ) {
        datepicker { localDate ->
            endDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    if (allTrucks) "Business Performance Report" else "Truck Performance Report",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.primary
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = allTrucks,
                        onCheckedChange = { allTrucks = it }
                    )
                    Text("All Trucks")
                }
                Row {
                    OutlinedTextField(
                        value = startDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: "",
                        onValueChange = {},
                        label = { Text("Start Date") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { startDialogState.show() },
                        enabled = false
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) } ?: "",
                        onValueChange = {},
                        label = { Text("End Date") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { endDialogState.show() },
                        enabled = false
                    )
                }
                Button(
                    onClick = { isLoading = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) { Text("Generate Report") }
            }
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                report?.let { report ->
                    item { SummaryCards(report) }
                    item { ProfitLossIndicator(report.profit) }
                    item { MonthlyStatistics(report.monthlyStats) }
                    item { PerformanceMetrics(report) }
                }
            }
        }
    }

    LaunchedEffect(isLoading, allTrucks, selectedTruckReg, startDate, endDate) {
        if (!isLoading) return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()
        try {
            var deliveriesQuery = db.collection("deliveries")
            var expensesQuery = db.collection("expenses")
            if (!allTrucks && selectedTruckReg != null) {
                deliveriesQuery = deliveriesQuery.whereEqualTo("truckReg", selectedTruckReg) as CollectionReference
                expensesQuery = expensesQuery.whereEqualTo("truckReg", selectedTruckReg) as CollectionReference
            }
            if (startDate != null) {
                deliveriesQuery = deliveriesQuery.whereGreaterThanOrEqualTo("date", startDate!!.time) as CollectionReference
                expensesQuery = expensesQuery.whereGreaterThanOrEqualTo("date", startDate!!.time) as CollectionReference
            }
            if (endDate != null) {
                deliveriesQuery = deliveriesQuery.whereLessThanOrEqualTo("date", endDate!!.time) as CollectionReference
                expensesQuery = expensesQuery.whereLessThanOrEqualTo("date", endDate!!.time) as CollectionReference
            }
            val deliveries = deliveriesQuery.get().await().toObjects(Delivery::class.java)
            val expenses = expensesQuery.get().await().toObjects(Expense::class.java)
            val monthlyStats = mutableMapOf<String, MonthlyStats>()
            val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            deliveries.forEach { delivery ->
                val monthKey = dateFormat.format(Date(delivery.date))
                val currentStats = monthlyStats.getOrDefault(monthKey, MonthlyStats())
                monthlyStats[monthKey] = currentStats.copy(
                    revenue = currentStats.revenue + delivery.cost,
                    deliveries = currentStats.deliveries + 1
                )
            }
            expenses.forEach { expense ->
                val monthKey = dateFormat.format(Date(expense.date))
                val currentStats = monthlyStats.getOrDefault(monthKey, MonthlyStats())
                monthlyStats[monthKey] = currentStats.copy(
                    expenses = currentStats.expenses + expense.amount
                )
            }
            val totalRevenue = deliveries.sumOf { it.cost }
            val totalExpenses = expenses.sumOf { it.amount }
            val totalWaterDelivered = deliveries.sumOf { it.waterAmount }
            report = TruckReport(
                totalWaterDelivered = totalWaterDelivered,
                totalRevenue = totalRevenue,
                totalExpenses = totalExpenses,
                profit = totalRevenue - totalExpenses,
                deliveryCount = deliveries.size,
                averageDeliverySize = if (deliveries.isNotEmpty()) totalWaterDelivered / deliveries.size else 0.0,
                monthlyStats = monthlyStats
            )
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
}

@Composable
fun SummaryCards(report: TruckReport) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Financial Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Revenue",
                        value = formatCurrency(report.totalRevenue),
                        icon = Icons.Default.AttachMoney
                    )
                    StatItem(
                        label = "Expenses",
                        value = formatCurrency(report.totalExpenses),
                        icon = Icons.Default.Receipt
                    )
                    StatItem(
                        label = "Profit",
                        value = formatCurrency(report.profit),
                        icon = Icons.Default.TrendingUp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfitLossIndicator(profit: Double) {
    val color = when {
        profit > 0 -> Color.Green
        profit < 0 -> Color.Red
        else -> Color.Gray
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (profit >= 0) "Profit" else "Loss",
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = formatCurrency(abs(profit)),
                style = MaterialTheme.typography.titleLarge,
                color = color
            )
        }
    }
}

@Composable
fun MonthlyStatistics(monthlyStats: Map<String, MonthlyStats>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Monthly Statistics",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            monthlyStats.entries.sortedByDescending { it.key }.forEach { (month, stats) ->
                MonthlyStatRow(month, stats)
                Divider()
            }
        }
    }
}

@Composable
fun PerformanceMetrics(report: TruckReport) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Performance Metrics",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Total Deliveries",
                    value = "${report.deliveryCount}",
                    icon = Icons.Default.LocalShipping
                )
                MetricItem(
                    label = "Avg. Delivery",
                    value = "${String.format("%.1f", report.averageDeliverySize)} L",
                    icon = Icons.Default.Water
                )
            }
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance().format(amount)
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun MonthlyStatRow(month: String, stats: MonthlyStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(month)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "Revenue: ${formatCurrency(stats.revenue)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Expenses: ${formatCurrency(stats.expenses)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Deliveries: ${stats.deliveries}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
