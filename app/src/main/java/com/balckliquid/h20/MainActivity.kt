package com.balckliquid.h20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balckliquid.h20.ui.theme.H20Theme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.balckliquid.h20.ui.forms.*
import com.balckliquid.h20.ui.screens.ReportsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.balckliquid.h20.ui.forms.ExpenseForm
import com.balckliquid.h20.ui.forms.DeliveryForm
import com.balckliquid.h20.ui.forms.DeliveryListScreen
import com.balckliquid.h20.ui.forms.WaterOrderForm

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get selected truck details from intent
        val selectedTruckId = intent.getStringExtra("SELECTED_TRUCK_ID") ?: ""
        val selectedTruckReg = intent.getStringExtra("SELECTED_TRUCK_REG") ?: ""
        
        setContent {
            H20Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        // Add a top bar showing selected truck
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("Truck $selectedTruckReg") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        MainScreen(selectedTruckReg)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(selectedTruckReg: String) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") { 
            MainContent(navController, selectedTruckReg) 
        }
        composable("expense_form") { 
            ExpenseForm(selectedTruckReg) 
        }
        composable(
            route = "water_order_form/{truckReg}",
            arguments = listOf(navArgument("truckReg") { type = NavType.StringType })
        ) { backStackEntry ->
            val truckReg = backStackEntry.arguments?.getString("truckReg") ?: ""
            WaterOrderForm(truckReg)
        }
        composable("delivery_form") { 
            DeliveryForm(selectedTruckReg) 
        }
        composable("delivery_list") { 
            DeliveryListScreen(selectedTruckReg) 
        }
        composable(
            route = "reports/{truckReg}",
            arguments = listOf(navArgument("truckReg") { type = NavType.StringType })
        ) { backStackEntry ->
            val truckReg = backStackEntry.arguments?.getString("truckReg") ?: ""
            ReportsScreen(truckReg)
        }
    }
}

@Composable
fun MainContent(navController: NavController, selectedTruckReg: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        FormCard("Water Order Form") { 
            navController.navigate("water_order_form/$selectedTruckReg") 
        }
        FormCard("Delivery Form") { 
            navController.navigate("delivery_form") 
        }
        FormCard("Expense Form") { 
            navController.navigate("expense_form") 
        }
        FormCard("Delivery List") { 
            navController.navigate("delivery_list") 
        }
        FormCard("Reports") { 
            navController.navigate("reports/$selectedTruckReg") 
        }
    }
}

@Composable
fun FormCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun TruckForm() {
    // Implementation of Truck Form with smooth transitions
    AnimatedVisibility(visible = true) {
        com.balckliquid.h20.ui.forms.TruckForm()
    }
}

@Composable
fun ExpenseForm(selectedTruckReg: String) {
    // Implementation of Expense Form with smooth transitions
    AnimatedVisibility(visible = true) {
        com.balckliquid.h20.ui.forms.ExpenseForm(selectedTruckReg)
    }
}

@Composable
fun WaterOrderForm(truckReg: String) {
    // Implementation of Water Order Form with smooth transitions
    AnimatedVisibility(visible = true) {
        com.balckliquid.h20.ui.forms.WaterOrderForm(truckReg)
    }
}

@Composable
fun DeliveryForm(selectedTruckReg: String) {
    // Implementation of Delivery Form with smooth transitions
    AnimatedVisibility(visible = true) {
        com.balckliquid.h20.ui.forms.DeliveryForm(selectedTruckReg)
    }
}

@Composable
fun DeliveryListScreen(selectedTruckReg: String) {
    // Implementation of Delivery List Screen
    com.balckliquid.h20.ui.forms.DeliveryListScreen(selectedTruckReg)
}

@Composable
fun SignInScreen() {
    // Implementation of Sign In Screen
}
