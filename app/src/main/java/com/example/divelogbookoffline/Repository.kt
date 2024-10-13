package com.example.divelogbookoffline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class Repository (private val database: DiveDatabase) {

    fun getAllDives(): Flow<List<Dive>> = database.diveDao.getAllDives()

    suspend fun insertDive(dive: Dive) =
        withContext(Dispatchers.IO){
        try{
            database.diveDao.insertDive(dive)
        }
        catch (_: Exception) {
        }
    }
}