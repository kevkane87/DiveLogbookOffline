package com.example.divelogbookoffline.list_dives

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.DiveDatabase
import com.example.divelogbookoffline.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListDivesViewModel(application: Application): ViewModel() {

    private val repository = Repository(DiveDatabase.getDatabase(application))

    private val _dives = MutableStateFlow<List<Dive>>(emptyList())
    val dives: StateFlow<List<Dive>> = _dives


    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            repository.getAllDives().collect { dives ->
                _dives.value = dives
            }
        }
    }

}