package com.example.remedialucp2_214.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi CRUD Buku dengan:
 * - Transaction support untuk operasi kompleks
 * - Soft delete operations
 * - Filter hanya data aktif (isDeleted = false)
 * - Query buku berdasarkan kategori dengan subkategori
 */
@Dao
interface BukuDao {

    /**
     * Insert buku baru
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(buku: Buku): Long

    /**
     * Update buku
     */
    @Update
    suspend fun update(buku: Buku)

    /**
     * Get semua buku aktif (tidak soft deleted)
     */
    @Query("SELECT * FROM buku WHERE isDeleted = 0 ORDER BY judul ASC")
    fun getAllActiveBuku(): Flow<List<Buku>>

    /**
     * Get buku by ID
     */
    @Query("SELECT * FROM buku WHERE id = :id AND isDeleted = 0")
    suspend fun getBukuById(id: Int): Buku?

    /**
     * Get buku by ID as Flow
     */
    @Query("SELECT * FROM buku WHERE id = :id AND isDeleted = 0")
    fun getBukuByIdFlow(id: Int): Flow<Buku?>

    /**
     * Get buku dengan kategori menggunakan @Relation
     */
    @Transaction
    @Query("SELECT * FROM buku WHERE isDeleted = 0 ORDER BY judul ASC")
    fun getAllBukuWithKategori(): Flow<List<BukuWithKategori>>

    /**
     * Get single buku dengan kategori
     */
    @Transaction
    @Query("SELECT * FROM buku WHERE id = :id AND isDeleted = 0")
    fun getBukuWithKategoriById(id: Int): Flow<BukuWithKategori?>

    /**
     * Get buku berdasarkan list kategori IDs (untuk recursive search)
     * Query ini akan dipanggil dengan hasil dari getAllSubkategoriIds
     */
    @Query("SELECT * FROM buku WHERE kategoriId IN (:kategoriIds) AND isDeleted = 0 ORDER BY judul ASC")
    fun getBukuByKategoriIds(kategoriIds: List<Int>): Flow<List<Buku>>

    /**
     * Get buku dengan kategori berdasarkan list kategori IDs
     */
    @Transaction
    @Query("SELECT * FROM buku WHERE kategoriId IN (:kategoriIds) AND isDeleted = 0 ORDER BY judul ASC")
    fun getBukuWithKategoriByKategoriIds(kategoriIds: List<Int>): Flow<List<BukuWithKategori>>

    /**
     * Soft delete buku
     */
    @Query("UPDATE buku SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    /**
     * Get buku by kategoriId
     */
    @Query("SELECT * FROM buku WHERE kategoriId = :kategoriId AND isDeleted = 0")
    suspend fun getBukuByKategoriId(kategoriId: Int): List<Buku>

    /**
     * Count buku yang dipinjam dalam kategori tertentu
     */
    @Query("SELECT COUNT(*) FROM buku WHERE kategoriId = :kategoriId AND status = 'dipinjam' AND isDeleted = 0")
    suspend fun countBukuDipinjamByKategori(kategoriId: Int): Int

    /**
     * Count semua buku aktif dalam kategori
     */
    @Query("SELECT COUNT(*) FROM buku WHERE kategoriId = :kategoriId AND isDeleted = 0")
    suspend fun countBukuByKategori(kategoriId: Int): Int

    /**
     * Pindahkan buku ke kategori lain
     */
    @Query("UPDATE buku SET kategoriId = :newKategoriId WHERE kategoriId = :oldKategoriId AND isDeleted = 0")
    suspend fun pindahkanBukuKeKategori(oldKategoriId: Int, newKategoriId: Int)

    /**
     * Soft delete semua buku dalam kategori
     */
    @Query("UPDATE buku SET isDeleted = 1 WHERE kategoriId = :kategoriId AND isDeleted = 0")
    suspend fun softDeleteBukuByKategori(kategoriId: Int)

    /**
     * Update audit log untuk buku
     */
    @Query("UPDATE buku SET auditLogBefore = :beforeState, auditLogAfter = :afterState WHERE id = :id")
    suspend fun updateAuditLog(id: Int, beforeState: String, afterState: String)

    /**
     * RECURSIVE: Count buku dipinjam di semua subkategori
     * Query ini menggunakan CTE untuk traverse hierarki kategori
     */
    @Query("""
        WITH RECURSIVE kategori_tree AS (
            SELECT id FROM kategori WHERE id = :parentKategoriId AND isDeleted = 0
            UNION ALL
            SELECT k.id FROM kategori k
            INNER JOIN kategori_tree kt ON k.parentId = kt.id
            WHERE k.isDeleted = 0
        )
        SELECT COUNT(*) FROM buku 
        WHERE kategoriId IN (SELECT id FROM kategori_tree) 
        AND status = 'dipinjam' 
        AND isDeleted = 0
    """)
    suspend fun countBukuDipinjamInSubtree(parentKategoriId: Int): Int

    @Query("SELECT COUNT(*) FROM buku WHERE kategoriId = :kategoriId AND isDeleted = 0")
    suspend fun countBooksInKategori(kategoriId: Int): Int

    @Query("UPDATE buku SET kategoriId = :newKategoriId WHERE kategoriId = :oldKategoriId AND isDeleted = 0")
    suspend fun moveBooksToKategori(oldKategoriId: Int, newKategoriId: Int)

    @Query("UPDATE buku SET isDeleted = 1 WHERE kategoriId = :kategoriId AND isDeleted = 0")
    suspend fun softDeleteByKategori(kategoriId: Int)
}
