package com.example.remedialucp2_214.room

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class untuk relasi Buku dengan Kategori menggunakan @Relation.
 * Digunakan untuk query yang membutuhkan data buku beserta kategorinya.
 */
data class BukuWithKategori(
    @Embedded
    val buku: Buku,
    
    @Relation(
        parentColumn = "kategoriId",
        entityColumn = "id"
    )
    val kategori: Kategori?
)
