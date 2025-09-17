package com.balckliquid.h20.ui.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import com.balckliquid.h20.models.Expense

@Composable
fun ExpenseForm(selectedTruckReg: String) {
    var amount by remember { mutableStateOf("") }
    var nature by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Date formatter for display
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
        // Show selected truck info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Selected Truck: $selectedTruckReg")
            }
        }

        // Date Selection
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
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = nature,
            onValueChange = { nature = it },
            label = { Text("Nature of Expense") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (selectedDate == null || amount.isEmpty() || nature.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                val db = FirebaseFirestore.getInstance()
                val expense = Expense(
                    id = UUID.randomUUID().toString(),
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    nature = nature,
                    date = selectedDate ?: System.currentTimeMillis(),
                    truckReg = selectedTruckReg
                )
                
                db.collection("expenses")
                    .document(expense.id)
                    .set(expense)
                    .addOnSuccessListener {
                        // Clear form
                        amount = ""
                        nature = ""
                        selectedDate = null
                        Toast.makeText(context, "Expense submitted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedDate != null && amount.isNotEmpty() && nature.isNotEmpty()
        ) {
            Text("Submit Expense")
        }
    }
} 