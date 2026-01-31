package com.example.remedialucp2_214.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pengarang")
data class Pengarang(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nama: String,
    val isDeleted: Boolean = false
)
