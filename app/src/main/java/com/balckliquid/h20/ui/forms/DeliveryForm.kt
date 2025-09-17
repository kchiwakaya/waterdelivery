package com.balckliquid.h20.ui.forms

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.balckliquid.h20.models.Delivery
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import java.text.SimpleDateFormat

@Composable
fun DeliveryForm(selectedTruckReg: String) {
    var orderId by remember { mutableStateOf("") }
    var waterAmount by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Date Picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showDatePicker) {
        datePickerDialog.show()
        showDatePicker = false
    }
    
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
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
            value = orderId,
            onValueChange = { orderId = it },
            label = { Text("Order ID") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = waterAmount,
            onValueChange = { waterAmount = it },
            label = { Text("Water Amount (Liters)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = cost,
            onValueChange = { cost = it },
            label = { Text("Cost") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (selectedDate == null) {
                    Toast.makeText(context, "Please select a date", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                val db = FirebaseFirestore.getInstance()
                val delivery = Delivery(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    waterAmount = waterAmount.toDoubleOrNull() ?: 0.0,
                    customerName = customerName,
                    date = selectedDate ?: System.currentTimeMillis(),
                    cost = cost.toDoubleOrNull() ?: 0.0,
                    truckReg = selectedTruckReg
                )
                
                db.collection("deliveries")
                    .document(delivery.id)
                    .set(delivery)
                    .addOnSuccessListener {
                        // Clear form
                        orderId = ""
                        waterAmount = ""
                        customerName = ""
                        cost = ""
                        selectedDate = null
                        Toast.makeText(context, "Delivery submitted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Delivery")
        }
    }
} 