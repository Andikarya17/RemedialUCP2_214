package com.example.remedialucp2_214.repositori

import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.room.KategoriDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository untuk operasi Kategori.
 * Menyediakan:
 * - CRUD operations
 * - Cyclic reference prevention
 * - Recursive subcategory retrieval
 * - Business validation
 */
class RepositoriKategori(private val kategoriDao: KategoriDao) {

    /**
     * Get semua kategori aktif
     */
    fun getAllKategori(): Flow<List<Kategori>> = kategoriDao.getAllActiveKategori()

    /**
     * Get kategori root (tanpa parent)
     */
    fun getRootKategori(): Flow<List<Kategori>> = kategoriDao.getRootKategori()

    /**
     * Get kategori by ID
     */
    suspend fun getKategoriById(id: Int): Kategori? = kategoriDao.getKategoriById(id)

    /**
     * Get kategori by ID sebagai Flow
     */
    fun getKategoriByIdFlow(id: Int): Flow<Kategori?> = kategoriDao.getKategoriByIdFlow(id)

    /**
     * Insert kategori baru dengan validasi
     * @return Result dengan ID kategori baru atau error message
     */
    suspend fun insertKategori(kategori: Kategori): Result<Long> {
        // Validasi: nama tidak boleh kosong
        if (kategori.namaKategori.isBlank()) {
            return Result.failure(Exception("Nama kategori tidak boleh kosong"))
        }

        // Validasi: jika ada parentId, pastikan parent exists
        kategori.parentId?.let { parentId ->
            val parent = kategoriDao.getKategoriById(parentId)
            if (parent == null) {
                return Result.failure(Exception("Kategori parent tidak ditemukan"))
            }
        }

        val id = kategoriDao.insert(kategori)
        return Result.success(id)
    }

    /**
     * Update kategori dengan validasi cyclic reference
     * @return Result success atau error message
     */
    suspend fun updateKategori(kategori: Kategori): Result<Unit> {
        // Validasi: nama tidak boleh kosong
        if (kategori.namaKategori.isBlank()) {
            return Result.failure(Exception("Nama kategori tidak boleh kosong"))
        }

        // Validasi cyclic reference jika ada perubahan parentId
        kategori.parentId?.let { newParentId ->
            // Cek self-parent
            if (newParentId == kategori.id) {
                return Result.failure(Exception("Kategori tidak boleh menjadi parent dari dirinya sendiri"))
            }

            // Cek indirect loop: apakah newParent adalah descendant dari kategori ini?
            val descendants = kategoriDao.getAllSubkategoriIds(kategori.id)
            if (newParentId in descendants) {
                return Result.failure(Exception("Tidak dapat memilih kategori ini sebagai parent (akan membentuk loop)"))
            }
        }

        kategoriDao.update(kategori)
        return Result.success(Unit)
    }

    /**
     * Soft delete kategori
     * Akan di-handle oleh repository buku untuk pengecekan buku dipinjam
     */
    suspend fun softDeleteKategori(id: Int) {
        kategoriDao.softDelete(id)
    }

    /**
     * Get semua ID subkategori dari kategori tertentu (recursive)
     * Menggunakan CTE query di DAO
     */
    suspend fun getAllSubkategoriIds(parentId: Int): List<Int> {
        return kategoriDao.getAllSubkategoriIds(parentId)
    }

    /**
     * Check apakah setting parentId akan membentuk cyclic reference
     * @return true jika akan membentuk loop, false jika aman
     */
    suspend fun wouldCreateCycle(kategoriId: Int, newParentId: Int): Boolean {
        // Self-parent check
        if (kategoriId == newParentId) return true

        // Check apakah newParent adalah descendant dari kategori ini
        val descendants = kategoriDao.getAllSubkategoriIds(kategoriId)
        return newParentId in descendants
    }

    /**
     * Get kategori "Tanpa Kategori" default
     */
    suspend fun getTanpaKategori(): Kategori? = kategoriDao.getTanpaKategori()

    /**
     * Get direct children dari kategori
     */
    suspend fun getDirectChildren(parentId: Int): List<Kategori> = 
        kategoriDao.getDirectChildren(parentId)

    /**
     * Count active children
     */
    suspend fun countActiveChildren(parentId: Int): Int = 
        kategoriDao.countActiveChildren(parentId)
}
