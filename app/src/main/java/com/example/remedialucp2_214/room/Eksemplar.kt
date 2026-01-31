package com.example.remedialucp2_214.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "eksemplar",
    foreignKeys = [
        ForeignKey(
            entity = Buku::class,
            parentColumns = ["id"],
            childColumns = ["bukuId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bukuId")]
)
data class Eksemplar(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bukuId: Int,
    val kodeEksemplar: String,
    val status: String = STATUS_TERSEDIA,
    val isDeleted: Boolean = false
) {
    companion object {
        const val STATUS_TERSEDIA = "tersedia"
        const val STATUS_DIPINJAM = "dipinjam"
    }
}
