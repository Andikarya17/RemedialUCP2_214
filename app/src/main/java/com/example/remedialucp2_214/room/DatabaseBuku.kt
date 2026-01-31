package com.example.remedialucp2_214.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Kategori::class,
        Buku::class,
        Pengarang::class,
        BukuPengarang::class,
        Eksemplar::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DatabaseBuku : RoomDatabase() {

    abstract fun kategoriDao(): KategoriDao
    abstract fun bukuDao(): BukuDao
    abstract fun pengarangDao(): PengarangDao
    abstract fun eksemplarDao(): EksemplarDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseBuku? = null

        fun getDatabase(context: Context): DatabaseBuku {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseBuku::class.java,
                    "database_buku"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateData(database)
                }
            }
        }

        private suspend fun prepopulateData(database: DatabaseBuku) {
            val kategoriDao = database.kategoriDao()
            val pengarangDao = database.pengarangDao()

            listOf(
                Kategori(namaKategori = "Tanpa Kategori"),
                Kategori(namaKategori = "Pemrograman"),
                Kategori(namaKategori = "Bisnis"),
                Kategori(namaKategori = "Manajemen"),
                Kategori(namaKategori = "Sejarah")
            ).forEach { kategoriDao.insert(it) }

            listOf(
                Pengarang(nama = "Robert C. Martin"),
                Pengarang(nama = "Martin Fowler"),
                Pengarang(nama = "Joshua Bloch")
            ).forEach { pengarangDao.insert(it) }
        }
    }
}
