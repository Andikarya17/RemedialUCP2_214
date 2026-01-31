package com.example.remedialucp2_214.repositori

import android.content.Context
import com.example.remedialucp2_214.room.DatabaseBuku

interface ContainerApp {
    val repositoriBuku: RepositoriBuku
    val repositoriKategori: RepositoriKategori
    val repositoriPengarang: RepositoriPengarang
    val repositoriEksemplar: RepositoriEksemplar
}

class ContainerAppImpl(private val context: Context) : ContainerApp {

    private val database: DatabaseBuku by lazy {
        DatabaseBuku.getDatabase(context)
    }

    override val repositoriKategori: RepositoriKategori by lazy {
        RepositoriKategori(
            kategoriDao = database.kategoriDao(),
            eksemplarDao = database.eksemplarDao(),
            bukuDao = database.bukuDao(),
            database = database
        )
    }

    override val repositoriBuku: RepositoriBuku by lazy {
        RepositoriBuku(
            bukuDao = database.bukuDao(),
            kategoriDao = database.kategoriDao(),
            database = database
        )
    }

    override val repositoriPengarang: RepositoriPengarang by lazy {
        RepositoriPengarang(database.pengarangDao())
    }

    override val repositoriEksemplar: RepositoriEksemplar by lazy {
        RepositoriEksemplar(database.eksemplarDao())
    }
}
