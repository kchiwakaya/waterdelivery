package com.balckliquid.h20.models

data class WaterOrder(
    val id: String = "",
    val truckId: String = "",
    val waterAmount: Double = 0.0,
    val totalCost: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val placeOfOrder: String = "" ,
    val truckReg:String =""
)
data class Delivery(
    val id: String = "",
    val orderId: String = "",
    val waterAmount: Double = 0.0,
    val customerName: String = "",
    val date: Long = 0,
    val  truckReg: String = "",
    val cost: Double = 0.0
)
data class Expense(
    val id: String = "",
    val amount: Double = 0.0, // Amount paid
    val nature: String = "", // Nature of expense (e.g., fuel, airtime, food)
    val date: Long = System.currentTimeMillis(),
    //val truckId: String = ""  // Add this line
    val truckReg: String = ""
)
data class Truck(
    val id: String = "",
    val registrationNumber: String = "",
    val waterCapacity: Double = 0.0,
    val makeAndModel: String = "",
    val driver: String = ""
)