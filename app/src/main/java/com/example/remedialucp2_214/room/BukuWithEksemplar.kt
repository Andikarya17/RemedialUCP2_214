package com.example.remedialucp2_214.room

import androidx.room.Embedded
import androidx.room.Relation

data class BukuWithEksemplar(
    @Embedded val buku: Buku,
    @Relation(
        parentColumn = "id",
        entityColumn = "bukuId"
    )
    val eksemplarList: List<Eksemplar>
) {
    val totalEksemplar: Int
        get() = eksemplarList.count { !it.isDeleted }

    val dipinjamCount: Int
        get() = eksemplarList.count { !it.isDeleted && it.status == Eksemplar.STATUS_DIPINJAM }

    val tersediaCount: Int
        get() = totalEksemplar - dipinjamCount
}
