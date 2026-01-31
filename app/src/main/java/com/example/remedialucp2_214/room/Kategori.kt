package com.example.remedialucp2_214.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity Kategori dengan dukungan hierarki tidak terbatas.
 * parentId nullable untuk kategori root.
 * isDeleted untuk soft delete.
 */
@Entity(
    tableName = "kategori",
    foreignKeys = [
        ForeignKey(
            entity = Kategori::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("parentId")]
)
data class Kategori(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val namaKategori: String,
    val parentId: Int? = null,
    val isDeleted: Boolean = false
)
