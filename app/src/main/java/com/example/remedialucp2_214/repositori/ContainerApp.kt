package com.example.remedialucp2_214.repositori

import android.content.Context
import com.example.remedialucp2_214.room.DatabaseBuku

/**
 * Container untuk Dependency Injection manual.
 * Menyediakan instance database dan repository.
 */
interface ContainerApp {
    val repositoriBuku: RepositoriBuku
    val repositoriKategori: RepositoriKategori
}

/**
 * Implementasi ContainerApp dengan lazy initialization
 */
class ContainerAppImpl(private val context: Context) : ContainerApp {

    /**
     * Instance database (singleton)
     */
    private val database: DatabaseBuku by lazy {
        DatabaseBuku.getDatabase(context)
    }

    /**
     * Repository Kategori
     */
    override val repositoriKategori: RepositoriKategori by lazy {
        RepositoriKategori(database.kategoriDao())
    }

    /**
     * Repository Buku
     */
    override val repositoriBuku: RepositoriBuku by lazy {
        RepositoriBuku(
            bukuDao = database.bukuDao(),
            kategoriDao = database.kategoriDao(),
            database = database
        )
    }
}
