package com.example.divelogbookoffline.list_dives

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.divelogbookoffline.add_dive.AddDiveViewModel

class ListDivesViewModelFactory(val app: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListDivesViewModel::class.java)) {
            return ListDivesViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}