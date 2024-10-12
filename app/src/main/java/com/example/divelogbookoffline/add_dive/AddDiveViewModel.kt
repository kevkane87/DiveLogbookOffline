package com.example.divelogbookoffline.add_dive

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.DiveDatabase.Companion.getDatabase
import com.example.divelogbookoffline.Repository
import kotlinx.coroutines.launch

class AddDiveViewModel(application: Application,): ViewModel() {

    private val repository = Repository(getDatabase(application))

    fun saveDive(dive: Dive){
        viewModelScope.launch {repository.insertDive(dive)}
    }

}