package com.balckliquid.h20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.balckliquid.h20.ui.forms.TruckForm
import com.balckliquid.h20.ui.theme.H20Theme

class TruckFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            H20Theme {
                TruckForm()
            }
        }
    }
} 