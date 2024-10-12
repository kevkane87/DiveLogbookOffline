package com.example.divelogbookoffline.list_dives

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.DiveDatabase
import com.example.divelogbookoffline.Repository
import kotlinx.coroutines.flow.Flow

class ListDivesViewModel(application: Application): ViewModel() {

    private val repository = Repository(DiveDatabase.getDatabase(application))

    fun allDives(): Flow<List<Dive>> = repository.getAllDives()

}