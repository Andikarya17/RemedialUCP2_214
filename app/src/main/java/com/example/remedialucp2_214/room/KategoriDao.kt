package com.example.remedialucp2_214.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi CRUD Kategori dengan:
 * - Recursive CTE query untuk mendapatkan semua subkategori
 * - Soft delete operations
 * - Filter hanya data aktif (isDeleted = false)
 */
@Dao
interface KategoriDao {

    /**
     * Insert kategori baru
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kategori: Kategori): Long

    /**
     * Update kategori
     */
    @Update
    suspend fun update(kategori: Kategori)

    /**
     * Get semua kategori aktif (tidak soft deleted)
     */
    @Query("SELECT * FROM kategori WHERE isDeleted = 0 ORDER BY namaKategori ASC")
    fun getAllActiveKategori(): Flow<List<Kategori>>

    /**
     * Get kategori by ID
     */
    @Query("SELECT * FROM kategori WHERE id = :id AND isDeleted = 0")
    suspend fun getKategoriById(id: Int): Kategori?

    /**
     * Get kategori by ID as Flow
     */
    @Query("SELECT * FROM kategori WHERE id = :id AND isDeleted = 0")
    fun getKategoriByIdFlow(id: Int): Flow<Kategori?>

    /**
     * RECURSIVE CTE: Ambil semua ID subkategori dari kategori tertentu
     * Ini akan mengembalikan kategori parent beserta SEMUA turunannya
     * tanpa batas level kedalaman
     */
    @Query("""
        WITH RECURSIVE kategori_tree AS (
            SELECT id, namaKategori, parentId, isDeleted
            FROM kategori 
            WHERE id = :parentId AND isDeleted = 0
            
            UNION ALL
            
            SELECT k.id, k.namaKategori, k.parentId, k.isDeleted
            FROM kategori k
            INNER JOIN kategori_tree kt ON k.parentId = kt.id
            WHERE k.isDeleted = 0
        )
        SELECT id FROM kategori_tree
    """)
    suspend fun getAllSubkategoriIds(parentId: Int): List<Int>

    /**
     * RECURSIVE CTE: Ambil semua ancestor (parent chain) dari kategori tertentu
     * Digunakan untuk mendeteksi cyclic reference
     */
    @Query("""
        WITH RECURSIVE ancestor_tree AS (
            SELECT id, parentId
            FROM kategori
            WHERE id = :kategoriId AND isDeleted = 0
            
            UNION ALL
            
            SELECT k.id, k.parentId
            FROM kategori k
            INNER JOIN ancestor_tree at ON k.id = at.parentId
            WHERE k.isDeleted = 0
        )
        SELECT id FROM ancestor_tree
    """)
    suspend fun getAllAncestorIds(kategoriId: Int): List<Int>

    /**
     * Get direct children of a kategori
     */
    @Query("SELECT * FROM kategori WHERE parentId = :parentId AND isDeleted = 0")
    suspend fun getDirectChildren(parentId: Int): List<Kategori>

    /**
     * Soft delete kategori
     */
    @Query("UPDATE kategori SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    /**
     * Check if kategori has any active children
     */
    @Query("SELECT COUNT(*) FROM kategori WHERE parentId = :parentId AND isDeleted = 0")
    suspend fun countActiveChildren(parentId: Int): Int

    /**
     * Get root categories (no parent)
     */
    @Query("SELECT * FROM kategori WHERE parentId IS NULL AND isDeleted = 0 ORDER BY namaKategori ASC")
    fun getRootKategori(): Flow<List<Kategori>>

    /**
     * Get kategori "Tanpa Kategori" yang merupakan default
     */
    @Query("SELECT * FROM kategori WHERE namaKategori = 'Tanpa Kategori' AND isDeleted = 0 LIMIT 1")
    suspend fun getTanpaKategori(): Kategori?
}
