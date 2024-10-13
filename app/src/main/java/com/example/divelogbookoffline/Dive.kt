package com.example.divelogbookoffline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dive(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "date") val date: String?,
        @ColumnInfo(name = "diveTitle") val diveTitle: String?,
        @ColumnInfo(name = "diveSite") val diveSite: String?,
        @ColumnInfo(name = "bottomTime") val bottomTime: Int?,
        @ColumnInfo(name = "maxDepth") val maxDepth: Double?,
    )

