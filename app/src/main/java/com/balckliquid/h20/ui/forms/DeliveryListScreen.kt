package com.balckliquid.h20.ui.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balckliquid.h20.models.Delivery
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun DeliveryListScreen(selectedTruckReg: String) {
    var deliveries by remember { mutableStateOf<List<Delivery>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    LaunchedEffect(selectedTruckReg) {
        try {
            val db = FirebaseFirestore.getInstance()
            deliveries = db.collection("deliveries")
                .whereEqualTo("truckReg", selectedTruckReg)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Delivery::class.java) }
                .sortedByDescending { it.date }
            isLoading = false
        } catch (e: Exception) {
            // Handle error
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            DeliveryListHeader(selectedTruckReg)

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(deliveries) { delivery ->
                        DeliveryCard(delivery, dateFormatter)
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryCard(delivery: Delivery, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order ID: ${delivery.orderId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconText(Icons.Default.Person, "Customer: ${delivery.customerName}")
                IconText(Icons.Default.WaterDrop, "Amount: ${delivery.waterAmount} L")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconText(Icons.Default.AttachMoney, "Cost: ${delivery.cost}")
                IconText(Icons.Default.LocalShipping, "Date: ${dateFormatter.format(Date(delivery.date))}")
            }
        }
    }
}

@Composable
fun IconText(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.onPrimaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun DeliveryListHeader(selectedTruckReg: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Deliveries for Truck: $selectedTruckReg",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.primary
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = colorScheme.onBackground
        )
    }
} 