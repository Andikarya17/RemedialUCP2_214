package com.example.remedialucp2_214.repositori

import androidx.room.withTransaction
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.BukuDao
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.DatabaseBuku
import com.example.remedialucp2_214.room.KategoriDao
import kotlinx.coroutines.flow.Flow

class RepositoriBuku(
    private val bukuDao: BukuDao,
    private val kategoriDao: KategoriDao,
    private val database: DatabaseBuku
) {

    fun getAllBuku(): Flow<List<Buku>> = bukuDao.getAllActiveBuku()

    fun getAllBukuWithKategori(): Flow<List<BukuWithKategori>> = bukuDao.getAllBukuWithKategori()

    suspend fun getBukuById(id: Int): Buku? = bukuDao.getBukuById(id)

    fun getBukuWithKategoriById(id: Int): Flow<BukuWithKategori?> = bukuDao.getBukuWithKategoriById(id)

    suspend fun getBukuByKategoriRecursive(kategoriId: Int): Flow<List<BukuWithKategori>> {
        val allKategoriIds = kategoriDao.getAllSubkategoriIds(kategoriId)
        return bukuDao.getBukuWithKategoriByKategoriIds(allKategoriIds)
    }

    suspend fun insertBuku(buku: Buku): Result<Long> {
        if (buku.judul.isBlank()) {
            return Result.failure(Exception("Judul buku tidak boleh kosong"))
        }
        if (buku.status !in listOf(Buku.STATUS_TERSEDIA, Buku.STATUS_DIPINJAM)) {
            return Result.failure(Exception("Status buku tidak valid"))
        }
        buku.kategoriId?.let { katId ->
            if (kategoriDao.getKategoriById(katId) == null) {
                return Result.failure(Exception("Kategori tidak ditemukan"))
            }
        }
        val id = bukuDao.insert(buku)
        return Result.success(id)
    }

    suspend fun updateBuku(buku: Buku): Result<Unit> {
        if (buku.judul.isBlank()) {
            return Result.failure(Exception("Judul buku tidak boleh kosong"))
        }
        if (buku.status !in listOf(Buku.STATUS_TERSEDIA, Buku.STATUS_DIPINJAM)) {
            return Result.failure(Exception("Status buku tidak valid"))
        }
        buku.kategoriId?.let { katId ->
            if (kategoriDao.getKategoriById(katId) == null) {
                return Result.failure(Exception("Kategori tidak ditemukan"))
            }
        }

        database.withTransaction {
            val currentBuku = bukuDao.getBukuById(buku.id)
            val beforeState = currentBuku?.toAuditJson() ?: ""
            val afterState = buku.toAuditJson()
            val bukuWithAudit = buku.copy(auditLogBefore = beforeState, auditLogAfter = afterState)
            bukuDao.update(bukuWithAudit)
        }
        return Result.success(Unit)
    }

    suspend fun softDeleteBuku(id: Int): Result<Unit> {
        bukuDao.softDelete(id)
        return Result.success(Unit)
    }

    sealed class DeleteKategoriResult {
        object Success : DeleteKategoriResult()
        data class HasBorrowedBooks(val count: Int) : DeleteKategoriResult()
        data class HasAvailableBooks(val count: Int) : DeleteKategoriResult()
        data class Error(val message: String) : DeleteKategoriResult()
    }

    /**
     * Check kondisi sebelum hapus kategori - now checks Eksemplar status
     */
    suspend fun checkDeleteKategori(kategoriId: Int): DeleteKategoriResult {
        val eksemplarDao = database.eksemplarDao()
        val dipinjamCount = eksemplarDao.countDipinjamInKategoriSubtree(kategoriId)
        if (dipinjamCount > 0) {
            return DeleteKategoriResult.HasBorrowedBooks(dipinjamCount)
        }

        val allKategoriIds = listOf(kategoriId) + kategoriDao.getAllSubkategoriIds(kategoriId)
        val totalBooks = allKategoriIds.sumOf { bukuDao.countBooksInKategori(it) }
        if (totalBooks > 0) {
            return DeleteKategoriResult.HasAvailableBooks(totalBooks)
        }

        return DeleteKategoriResult.Success
    }

    /**
     * Hapus kategori dengan opsi untuk buku - now soft deletes Eksemplar as well
     */
    suspend fun deleteKategoriWithBooks(kategoriId: Int, softDeleteBooks: Boolean): Result<Unit> {
        return try {
            database.withTransaction {
                val eksemplarDao = database.eksemplarDao()
                val dipinjamCount = eksemplarDao.countDipinjamInKategoriSubtree(kategoriId)
                if (dipinjamCount > 0) {
                    throw IllegalStateException("Tidak dapat menghapus kategori. Masih ada $dipinjamCount eksemplar yang dipinjam.")
                }

                val allKategoriIds = listOf(kategoriId) + kategoriDao.getAllSubkategoriIds(kategoriId)
                val tanpaKategori = kategoriDao.getTanpaKategori()
                    ?: throw IllegalStateException("Kategori 'Tanpa Kategori' tidak ditemukan")

                for (katId in allKategoriIds) {
                    if (softDeleteBooks) {
                        val bukuList = bukuDao.getBukuByKategoriId(katId)
                        bukuList.forEach { buku ->
                            eksemplarDao.softDeleteByBukuId(buku.id)
                        }
                        bukuDao.softDeleteByKategori(katId)
                    } else {
                        bukuDao.moveBooksToKategori(katId, tanpaKategori.id)
                    }
                }

                allKategoriIds.reversed().forEach { katId ->
                    kategoriDao.softDelete(katId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEmptyKategori(kategoriId: Int): Result<Unit> {
        return try {
            database.withTransaction {
                val allKategoriIds = listOf(kategoriId) + kategoriDao.getAllSubkategoriIds(kategoriId)
                allKategoriIds.reversed().forEach { katId ->
                    kategoriDao.softDelete(katId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
