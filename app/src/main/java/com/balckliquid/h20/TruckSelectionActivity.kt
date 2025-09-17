package com.balckliquid.h20

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balckliquid.h20.models.Truck
import com.balckliquid.h20.ui.theme.H20Theme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

class TruckSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            H20Theme {
                TruckSelectionScreen(
                    onTruckSelected = { truck ->
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("SELECTED_TRUCK_ID", truck.id)
                            putExtra("SELECTED_TRUCK_REG", truck.registrationNumber)
                        }
                        startActivity(intent)
                    },
                    onAddTruck = {
                        val intent = Intent(this, TruckFormActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun TruckSelectionScreen(
    onTruckSelected: (Truck) -> Unit,
    onAddTruck: () -> Unit
) {
    var trucks by remember { mutableStateOf<List<Truck>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("trucks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    trucks = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Truck::class.java)
                    }
                }
                isLoading = false
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTruck
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Truck")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Select a Truck",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (trucks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No trucks available. Please add trucks first.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trucks) { truck ->
                        TruckSelectionCard(
                            truck = truck,
                            onClick = { onTruckSelected(truck) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TruckSelectionCard(truck: Truck, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = truck.registrationNumber,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = truck.makeAndModel,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Capacity: ${truck.waterCapacity} Liters",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Driver: ${truck.driver}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 