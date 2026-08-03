package com.example.savebite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.savebite.navigation.InventoryNavigation
import com.example.savebite.ui.theme.SaveBiteTheme
import com.example.savebite.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaveBiteTheme {
                val inventoryViewModel: InventoryViewModel = viewModel()
                InventoryNavigation( viewModel = inventoryViewModel)
            }
        }
    }
}
