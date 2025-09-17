package com.balckliquid.h20.ui.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balckliquid.h20.models.Truck
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun TruckForm() {
    var registrationNumber by remember { mutableStateOf("") }
    var waterCapacity by remember { mutableStateOf("") }
    var makeAndModel by remember { mutableStateOf("") }
    var driver by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = registrationNumber,
            onValueChange = { registrationNumber = it },
            label = { Text("Registration Number") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = waterCapacity,
            onValueChange = { waterCapacity = it },
            label = { Text("Water Capacity (Liters)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = makeAndModel,
            onValueChange = { makeAndModel = it },
            label = { Text("Make and Model") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = driver,
            onValueChange = { driver = it },
            label = { Text("Driver") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (registrationNumber.isEmpty() || waterCapacity.isEmpty() || 
                    makeAndModel.isEmpty() || driver.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val db = FirebaseFirestore.getInstance()
                val truck = Truck(
                    id = UUID.randomUUID().toString(),
                    registrationNumber = registrationNumber,
                    waterCapacity = waterCapacity.toDoubleOrNull() ?: 0.0,
                    makeAndModel = makeAndModel,
                    driver = driver
                )
                
                // First check if registration number already exists
                db.collection("trucks")
                    .whereEqualTo("registrationNumber", registrationNumber)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // Registration number doesn't exist, proceed with adding new truck
                            db.collection("trucks")
                                .document(truck.id)
                                .set(truck)
                                .addOnSuccessListener {
                                    // Clear form
                                    registrationNumber = ""
                                    waterCapacity = ""
                                    makeAndModel = ""
                                    driver = ""
                                    // Show success message
                                    Toast.makeText(context, 
                                        "Truck registered successfully", 
                                        Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, 
                                        "Error: ${e.message}", 
                                        Toast.LENGTH_LONG).show()
                                }
                        } else {
                            // Registration number already exists
                            Toast.makeText(context, 
                                "Registration number already exists", 
                                Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, 
                            "Error checking registration: ${e.message}", 
                            Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Truck")
        }
    }
} 