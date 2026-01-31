package com.example.remedialucp2_214.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database untuk aplikasi perpustakaan.
 * - Entities: Kategori, Buku
 * - Prepopulate dengan kategori "Tanpa Kategori" sebagai default
 */
@Database(
    entities = [Kategori::class, Buku::class],
    version = 1,
    exportSchema = false
)
abstract class DatabaseBuku : RoomDatabase() {

    abstract fun kategoriDao(): KategoriDao
    abstract fun bukuDao(): BukuDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseBuku? = null

        fun getDatabase(context: Context): DatabaseBuku {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseBuku::class.java,
                    "database_buku"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback untuk prepopulate database dengan kategori default "Tanpa Kategori"
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.kategoriDao())
                }
            }
        }

        suspend fun populateDatabase(kategoriDao: KategoriDao) {
            // Insert kategori default "Tanpa Kategori"
            val tanpaKategori = Kategori(
                namaKategori = "Tanpa Kategori",
                parentId = null,
                isDeleted = false
            )
            kategoriDao.insert(tanpaKategori)
        }
    }
}
