package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ReportRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.data.repo.UserRepository
import com.example.savebite.utils.SessionManager

class DashboardViewModelFactory(
    private val userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository,
    private val shoppingRepository: ShoppingRepository,
    private val reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DashboardViewModel(
            userRepository,
            inventoryRepository,
            shoppingRepository,
            reportRepository,
            sessionManager
        ) as T
    }
}