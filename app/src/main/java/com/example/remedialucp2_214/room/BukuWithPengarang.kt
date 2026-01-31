package com.example.remedialucp2_214.room

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BukuWithPengarang(
    @Embedded val buku: Buku,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BukuPengarang::class,
            parentColumn = "bukuId",
            entityColumn = "pengarangId"
        )
    )
    val pengarangList: List<Pengarang>
)
