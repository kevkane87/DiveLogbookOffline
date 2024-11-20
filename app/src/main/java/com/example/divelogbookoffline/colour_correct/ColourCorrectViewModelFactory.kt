package com.example.divelogbookoffline.colour_correct

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.divelogbookoffline.add_dive.AddDiveViewModel

class ColourCorrectViewModelFactory(val app: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColourCorrectViewModel::class.java)) {
            return ColourCorrectViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}