package com.example.remedialucp2_214.repositori

import androidx.room.withTransaction
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.BukuDao
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.DatabaseBuku
import com.example.remedialucp2_214.room.KategoriDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository untuk operasi Buku.
 * Menyediakan:
 * - CRUD operations dengan validasi
 * - Transaction untuk delete kategori dengan buku
 * - Audit log management
 * - Recursive search berdasarkan kategori
 */
class RepositoriBuku(
    private val bukuDao: BukuDao,
    private val kategoriDao: KategoriDao,
    private val database: DatabaseBuku
) {

    /**
     * Get semua buku aktif
     */
    fun getAllBuku(): Flow<List<Buku>> = bukuDao.getAllActiveBuku()

    /**
     * Get semua buku dengan kategori
     */
    fun getAllBukuWithKategori(): Flow<List<BukuWithKategori>> = 
        bukuDao.getAllBukuWithKategori()

    /**
     * Get buku by ID
     */
    suspend fun getBukuById(id: Int): Buku? = bukuDao.getBukuById(id)

    /**
     * Get buku dengan kategori by ID
     */
    fun getBukuWithKategoriById(id: Int): Flow<BukuWithKategori?> = 
        bukuDao.getBukuWithKategoriById(id)

    /**
     * Get buku by kategori dengan recursive search ke semua subkategori
     * Menggunakan CTE query untuk mendapatkan semua ID subkategori
     */
    suspend fun getBukuByKategoriRecursive(kategoriId: Int): Flow<List<BukuWithKategori>> {
        val allKategoriIds = kategoriDao.getAllSubkategoriIds(kategoriId)
        return bukuDao.getBukuWithKategoriByKategoriIds(allKategoriIds)
    }

    /**
     * Insert buku baru dengan validasi
     * @return Result dengan ID buku baru atau error
     */
    suspend fun insertBuku(buku: Buku): Result<Long> {
        // Validasi: judul tidak boleh kosong
        if (buku.judul.isBlank()) {
            return Result.failure(Exception("Judul buku tidak boleh kosong"))
        }

        // Validasi: status harus valid
        if (buku.status !in listOf(Buku.STATUS_TERSEDIA, Buku.STATUS_DIPINJAM)) {
            return Result.failure(Exception("Status buku tidak valid"))
        }

        // Validasi: kategori harus ada jika diisi
        buku.kategoriId?.let { katId ->
            val kategori = kategoriDao.getKategoriById(katId)
            if (kategori == null) {
                return Result.failure(Exception("Kategori tidak ditemukan"))
            }
        }

        val id = bukuDao.insert(buku)
        return Result.success(id)
    }

    /**
     * Update buku dengan audit log
     * Menyimpan state before dan after dalam satu transaction
     */
    suspend fun updateBuku(buku: Buku): Result<Unit> {
        // Validasi: judul tidak boleh kosong
        if (buku.judul.isBlank()) {
            return Result.failure(Exception("Judul buku tidak boleh kosong"))
        }

        // Validasi: status harus valid
        if (buku.status !in listOf(Buku.STATUS_TERSEDIA, Buku.STATUS_DIPINJAM)) {
            return Result.failure(Exception("Status buku tidak valid"))
        }

        // Validasi: kategori harus ada jika diisi
        buku.kategoriId?.let { katId ->
            val kategori = kategoriDao.getKategoriById(katId)
            if (kategori == null) {
                return Result.failure(Exception("Kategori tidak ditemukan"))
            }
        }

        // Transaction untuk audit log + update
        database.withTransaction {
            // Get current state untuk audit log before
            val currentBuku = bukuDao.getBukuById(buku.id)
            val beforeState = currentBuku?.toAuditJson() ?: ""
            val afterState = buku.toAuditJson()

            // Update buku dengan audit log
            val bukuWithAudit = buku.copy(
                auditLogBefore = beforeState,
                auditLogAfter = afterState
            )
            bukuDao.update(bukuWithAudit)
        }

        return Result.success(Unit)
    }

    /**
     * Soft delete buku
     */
    suspend fun softDeleteBuku(id: Int): Result<Unit> {
        bukuDao.softDelete(id)
        return Result.success(Unit)
    }

    /**
     * Sealed class untuk hasil operasi hapus kategori
     */
    sealed class DeleteKategoriResult {
        object Success : DeleteKategoriResult()
        data class HasBorrowedBooks(val count: Int) : DeleteKategoriResult()
        data class HasAvailableBooks(val count: Int) : DeleteKategoriResult()
        data class Error(val message: String) : DeleteKategoriResult()
    }

    /**
     * Check kondisi sebelum hapus kategori
     * Mengembalikan status apakah bisa langsung hapus atau ada buku
     */
    suspend fun checkDeleteKategori(kategoriId: Int): DeleteKategoriResult {
        // Cek jumlah buku dipinjam di kategori dan semua subkategori
        val borrowedCount = bukuDao.countBukuDipinjamInSubtree(kategoriId)
        if (borrowedCount > 0) {
            return DeleteKategoriResult.HasBorrowedBooks(borrowedCount)
        }

        // Cek jumlah buku tersedia
        val allKategoriIds = kategoriDao.getAllSubkategoriIds(kategoriId)
        var totalBooks = 0
        for (katId in allKategoriIds) {
            totalBooks += bukuDao.countBukuByKategori(katId)
        }

        if (totalBooks > 0) {
            return DeleteKategoriResult.HasAvailableBooks(totalBooks)
        }

        return DeleteKategoriResult.Success
    }

    /**
     * Hapus kategori dengan opsi untuk buku
     * @param softDeleteBooks true = soft delete semua buku, false = pindahkan ke Tanpa Kategori
     * Menggunakan TRANSACTION untuk memastikan atomicity
     */
    suspend fun deleteKategoriWithBooks(
        kategoriId: Int,
        softDeleteBooks: Boolean
    ): Result<Unit> {
        return try {
            database.withTransaction {
                // Get semua ID kategori yang akan dihapus (termasuk subkategori)
                val allKategoriIds = kategoriDao.getAllSubkategoriIds(kategoriId)

                // Cek ulang apakah ada buku dipinjam (transaction safety)
                val borrowedCount = bukuDao.countBukuDipinjamInSubtree(kategoriId)
                if (borrowedCount > 0) {
                    // ROLLBACK via exception
                    throw Exception("Tidak dapat menghapus kategori. Masih ada $borrowedCount buku yang dipinjam.")
                }

                // Get kategori Tanpa Kategori untuk fallback
                val tanpaKategori = kategoriDao.getTanpaKategori()
                    ?: throw Exception("Kategori 'Tanpa Kategori' tidak ditemukan")

                // Proses buku sesuai opsi
                for (katId in allKategoriIds) {
                    if (softDeleteBooks) {
                        // Soft delete semua buku di kategori ini
                        bukuDao.softDeleteBukuByKategori(katId)
                    } else {
                        // Pindahkan ke Tanpa Kategori
                        bukuDao.pindahkanBukuKeKategori(katId, tanpaKategori.id)
                    }
                }

                // Soft delete semua kategori (dari leaf ke root)
                for (katId in allKategoriIds.reversed()) {
                    kategoriDao.softDelete(katId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus kategori tanpa buku (sudah dicek kosong)
     */
    suspend fun deleteEmptyKategori(kategoriId: Int): Result<Unit> {
        return try {
            database.withTransaction {
                val allKategoriIds = kategoriDao.getAllSubkategoriIds(kategoriId)
                for (katId in allKategoriIds.reversed()) {
                    kategoriDao.softDelete(katId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
