package com.example.divelogbookoffline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface DiveDAO {
    @Query("SELECT * FROM Dive")
    fun getAllDives(): Flow<List<Dive>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDive(dive: Dive)
}