package com.example.remedialucp2_214.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity Buku dengan:
 * - Relasi MANY-TO-ONE ke Kategori
 * - Soft delete flag
 * - Audit log fields untuk menyimpan state before/after update
 */
@Entity(
    tableName = "buku",
    foreignKeys = [
        ForeignKey(
            entity = Kategori::class,
            parentColumns = ["id"],
            childColumns = ["kategoriId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("kategoriId")]
)
data class Buku(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val judul: String,
    val status: String, // "tersedia" atau "dipinjam"
    val kategoriId: Int? = null,
    val isDeleted: Boolean = false,
    val auditLogBefore: String = "", // JSON state sebelum update
    val auditLogAfter: String = ""   // JSON state setelah update
) {
    companion object {
        const val STATUS_TERSEDIA = "tersedia"
        const val STATUS_DIPINJAM = "dipinjam"
    }
    
    /**
     * Convert Buku to JSON string untuk audit log
     */
    fun toAuditJson(): String {
        return """{"id":$id,"judul":"$judul","status":"$status","kategoriId":${kategoriId ?: "null"},"isDeleted":$isDeleted}"""
    }
}
