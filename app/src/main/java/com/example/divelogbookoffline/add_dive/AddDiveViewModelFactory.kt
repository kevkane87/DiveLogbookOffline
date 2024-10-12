package com.example.divelogbookoffline.add_dive

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddDiveViewModelFactory(val app: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddDiveViewModel::class.java)) {
            return AddDiveViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}