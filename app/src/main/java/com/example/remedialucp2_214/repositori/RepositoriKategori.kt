package com.example.remedialucp2_214.repositori

import androidx.room.withTransaction
import com.example.remedialucp2_214.room.BukuDao
import com.example.remedialucp2_214.room.DatabaseBuku
import com.example.remedialucp2_214.room.EksemplarDao
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.room.KategoriDao
import kotlinx.coroutines.flow.Flow

class RepositoriKategori(
    private val kategoriDao: KategoriDao,
    private val eksemplarDao: EksemplarDao,
    private val bukuDao: BukuDao,
    private val database: DatabaseBuku
) {

    fun getAllKategori(): Flow<List<Kategori>> = kategoriDao.getAllActiveKategori()

    fun getRootKategori(): Flow<List<Kategori>> = kategoriDao.getRootKategori()

    suspend fun getKategoriById(id: Int): Kategori? = kategoriDao.getKategoriById(id)

    fun getKategoriByIdFlow(id: Int): Flow<Kategori?> = kategoriDao.getKategoriByIdFlow(id)

    suspend fun insertKategori(kategori: Kategori): Result<Long> {
        if (kategori.namaKategori.isBlank()) {
            return Result.failure(Exception("Nama kategori tidak boleh kosong"))
        }
        kategori.parentId?.let { parentId ->
            if (kategoriDao.getKategoriById(parentId) == null) {
                return Result.failure(Exception("Kategori parent tidak ditemukan"))
            }
        }
        val id = kategoriDao.insert(kategori)
        return Result.success(id)
    }

    suspend fun updateKategori(kategori: Kategori): Result<Unit> {
        if (kategori.namaKategori.isBlank()) {
            return Result.failure(Exception("Nama kategori tidak boleh kosong"))
        }
        kategori.parentId?.let { newParentId ->
            if (newParentId == kategori.id) {
                return Result.failure(Exception("Kategori tidak boleh menjadi parent dari dirinya sendiri"))
            }
            val descendants = kategoriDao.getAllSubkategoriIds(kategori.id)
            if (newParentId in descendants) {
                return Result.failure(Exception("Tidak dapat memilih kategori ini sebagai parent (akan membentuk loop)"))
            }
        }
        kategoriDao.update(kategori)
        return Result.success(Unit)
    }

    suspend fun softDeleteKategori(id: Int) {
        kategoriDao.softDelete(id)
    }

    suspend fun getAllSubkategoriIds(parentId: Int): List<Int> = kategoriDao.getAllSubkategoriIds(parentId)

    suspend fun wouldCreateCycle(kategoriId: Int, newParentId: Int): Boolean {
        if (kategoriId == newParentId) return true
        val descendants = kategoriDao.getAllSubkategoriIds(kategoriId)
        return newParentId in descendants
    }

    suspend fun getTanpaKategori(): Kategori? = kategoriDao.getTanpaKategori()

    suspend fun getDirectChildren(parentId: Int): List<Kategori> = kategoriDao.getDirectChildren(parentId)

    suspend fun countActiveChildren(parentId: Int): Int = kategoriDao.countActiveChildren(parentId)

    /**
     * Count active (non-deleted) books in a kategori and all its subcategories
     */
    suspend fun countBooksInKategoriSubtree(kategoriId: Int): Int {
        val allIds = listOf(kategoriId) + kategoriDao.getAllSubkategoriIds(kategoriId)
        return allIds.sumOf { bukuDao.countBooksInKategori(it) }
    }

    /**
     * Count borrowed eksemplar in kategori subtree
     */
    suspend fun countDipinjamInKategoriSubtree(kategoriId: Int): Int =
        eksemplarDao.countDipinjamInKategoriSubtree(kategoriId)

    /**
     * Delete kategori with transaction - rollback if any eksemplar is borrowed
     * @param softDeleteBooks true = soft delete buku & eksemplar, false = move to "Tanpa Kategori"
     */
    suspend fun deleteKategoriWithBooks(kategoriId: Int, softDeleteBooks: Boolean): Result<Unit> {
        return try {
            database.withTransaction {
                val dipinjamCount = eksemplarDao.countDipinjamInKategoriSubtree(kategoriId)
                if (dipinjamCount > 0) {
                    throw IllegalStateException("Tidak dapat menghapus kategori: ada $dipinjamCount eksemplar yang masih dipinjam")
                }

                val allKategoriIds = listOf(kategoriId) + kategoriDao.getAllSubkategoriIds(kategoriId)

                if (softDeleteBooks) {
                    allKategoriIds.forEach { kid ->
                        bukuDao.softDeleteByKategori(kid)
                    }
                } else {
                    val tanpaKategori = kategoriDao.getTanpaKategori()
                        ?: throw IllegalStateException("Kategori 'Tanpa Kategori' tidak ditemukan")
                    allKategoriIds.forEach { kid ->
                        bukuDao.moveBooksToKategori(kid, tanpaKategori.id)
                    }
                }

                allKategoriIds.forEach { kid ->
                    kategoriDao.softDelete(kid)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
