package com.example.divelogbookoffline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiveDAO {
    @Query("SELECT * FROM Dive")
    fun getAllDives(): Flow<List<Dive>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDive(dive: Dive)
}