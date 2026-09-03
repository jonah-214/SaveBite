package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.savebite.data.repo.ReportRepository

class ReportViewModelFactory(
    private val repository: ReportRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
