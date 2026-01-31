package com.example.remedialucp2_214.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "buku_pengarang",
    primaryKeys = ["bukuId", "pengarangId"],
    foreignKeys = [
        ForeignKey(
            entity = Buku::class,
            parentColumns = ["id"],
            childColumns = ["bukuId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Pengarang::class,
            parentColumns = ["id"],
            childColumns = ["pengarangId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bukuId"),
        Index("pengarangId")
    ]
)
data class BukuPengarang(
    val bukuId: Int,
    val pengarangId: Int
)
