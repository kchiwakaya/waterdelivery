package com.balckliquid.h20.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissValue
import androidx.compose.material.DismissDirection
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balckliquid.h20.models.Truck
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.background
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.DismissState
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.text.input.ImeAction
import android.content.Context
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TruckListScreen() {
    var trucks by remember { mutableStateOf<List<Truck>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTruck by remember { mutableStateOf<Truck?>(null) }
    var quickEditTruck by remember { mutableStateOf<Truck?>(null) }
    var isQuickEditVisible by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            isLoading = true
        }
    )

    LaunchedEffect(Unit) {
        db.collection("trucks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    trucks = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Truck::class.java)?.copy(id = doc.id)
                    }
                }
                isLoading = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trucks",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = trucks,
                        key = { it.id }
                    ) { truck ->
                        val dismissState = rememberDismissState()
                        if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                            scope.launch {
                                deleteTruck(truck.id, db, context)
                            }
                        } else if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
                            quickEditTruck = truck
                            isQuickEditVisible = true
                        }
                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                            background = {
                                DismissBackground(dismissState)
                            },
                            dismissContent = {
                                TruckCard(
                                    truck = truck,
                                    onEditClick = {
                                        selectedTruck = truck
                                        showEditDialog = true
                                    },
                                    onDriverClick = {
                                        quickEditTruck = truck
                                        isQuickEditVisible = true
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }

        if (isQuickEditVisible && quickEditTruck != null) {
            QuickEditDriverSheet(
                truck = quickEditTruck!!,
                onDismiss = {
                    isQuickEditVisible = false
                    quickEditTruck = null
                },
                onSave = { newDriver ->
                    scope.launch {
                        updateTruckDriver(quickEditTruck!!.copy(driver = newDriver), db, context)
                        isQuickEditVisible = false
                        quickEditTruck = null
                    }
                }
            )
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (showEditDialog && selectedTruck != null) {
        EditTruckDialog(
            truck = selectedTruck!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTruck ->
                scope.launch {
                    try {
                        db.collection("trucks")
                            .document(updatedTruck.id)
                            .set(updatedTruck)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Truck updated successfully", Toast.LENGTH_SHORT).show()
                                showEditDialog = false
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error updating truck: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun TruckCard(truck: Truck, onEditClick: () -> Unit, onDriverClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EditTruckDialog(truck: Truck, onDismiss: () -> Unit, onSave: (Truck) -> Unit) {
    var driver by remember { mutableStateOf(truck.driver) }
    var makeAndModel by remember { mutableStateOf(truck.makeAndModel) }
    var waterCapacity by remember { mutableStateOf(truck.waterCapacity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Truck") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = makeAndModel,
                    onValueChange = { makeAndModel = it },
                    label = { Text("Make and Model") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = waterCapacity,
                    onValueChange = { waterCapacity = it },
                    label = { Text("Water Capacity (Liters)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = driver,
                    onValueChange = { driver = it },
                    label = { Text("Driver") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        truck.copy(
                            makeAndModel = makeAndModel,
                            waterCapacity = waterCapacity.toDoubleOrNull() ?: truck.waterCapacity,
                            driver = driver
                        )
                    )
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DismissBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection ?: return
    val color = when (direction) {
        DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary
        DismissDirection.EndToStart -> MaterialTheme.colorScheme.error
    }
    val alignment = when (direction) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
    }
    val icon = when (direction) {
        DismissDirection.StartToEnd -> Icons.Default.Edit
        DismissDirection.EndToStart -> Icons.Default.Delete
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickEditDriverSheet(
    truck: Truck,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var driverName by remember { mutableStateOf(truck.driver) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Update Driver",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = driverName,
                onValueChange = { driverName = it },
                label = { Text("Driver Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSave(driverName) }
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onSave(driverName) }) {
                    Text("Update")
                }
            }
        }
    }
}

private fun deleteTruck(truckId: String, db: FirebaseFirestore, context: Context) {
    db.collection("trucks")
        .document(truckId)
        .delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Truck deleted successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error deleting truck: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun updateTruckDriver(truck: Truck, db: FirebaseFirestore, context: Context) {
    db.collection("trucks")
        .document(truck.id)
        .set(truck)
        .addOnSuccessListener {
            Toast.makeText(context, "Driver updated successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error updating driver: ${e.message}", Toast.LENGTH_LONG).show()
        }
} 