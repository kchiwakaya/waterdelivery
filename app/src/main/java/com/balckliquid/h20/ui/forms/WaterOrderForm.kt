package com.balckliquid.h20.ui.forms

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.balckliquid.h20.models.Truck
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import com.balckliquid.h20.models.WaterOrder
import java.text.SimpleDateFormat

@Composable
fun WaterOrderForm(selectedTruckReg: String) {
    var waterAmount by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var placeOfOrder by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTruck by remember { mutableStateOf<Truck?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Date Picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
            showTimePicker = true
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time Picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    if (showDatePicker) {
        datePickerDialog.show()
        showDatePicker = false
    }

    if (showTimePicker) {
        timePickerDialog.show()
        showTimePicker = false
    }

    // Get the selected truck details when the form loads using registration number
    LaunchedEffect(selectedTruckReg) {
        val db = FirebaseFirestore.getInstance()
        db.collection("trucks")
            .whereEqualTo("registrationNumber", selectedTruckReg)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    selectedTruck = documents.documents[0].toObject(Truck::class.java)
                }
            }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Show selected truck info
        selectedTruck?.let { truck ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Selected Truck: ${truck.registrationNumber}")
                    Text("Capacity: ${truck.waterCapacity} Liters")
                    Text("Driver: ${truck.driver}")
                }
            }
        }
        
        OutlinedTextField(
            value = selectedDate?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = { },
            label = { Text("Select Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = waterAmount,
            onValueChange = { 
                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                    waterAmount = it
                }
            },
            label = { Text("Water Amount (Liters)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = totalCost,
            onValueChange = { 
                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                    totalCost = it
                }
            },
            label = { Text("Total Cost") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = placeOfOrder,
            onValueChange = { placeOfOrder = it },
            label = { Text("Place of Order") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (selectedDate == null || selectedTime.isEmpty()) {
                    Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                scope.launch {
                    val db = FirebaseFirestore.getInstance()
                    val waterOrder = WaterOrder(
                        id = UUID.randomUUID().toString(),
                        truckReg = selectedTruckReg,
                        waterAmount = waterAmount.toDoubleOrNull() ?: 0.0,
                        totalCost = totalCost.toDoubleOrNull() ?: 0.0,
                        date = selectedDate ?: System.currentTimeMillis(),
                        placeOfOrder = placeOfOrder
                    )
                    
                    db.collection("water_orders")
                        .document(waterOrder.id)
                        .set(waterOrder)
                        .addOnSuccessListener {
                            waterAmount = ""
                            totalCost = ""
                            placeOfOrder = ""
                            selectedDate = null
                            selectedTime = ""
                            Toast.makeText(context, "Water order submitted successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = waterAmount.isNotEmpty() && totalCost.isNotEmpty() && 
                     placeOfOrder.isNotEmpty() && selectedDate != null && selectedTime.isNotEmpty()
        ) {
            Text("Submit Order")
        }
    }
} 