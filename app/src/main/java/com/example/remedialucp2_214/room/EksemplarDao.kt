package com.example.remedialucp2_214.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EksemplarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eksemplar: Eksemplar): Long

    @Update
    suspend fun update(eksemplar: Eksemplar)

    @Query("UPDATE eksemplar SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    @Query("UPDATE eksemplar SET isDeleted = 1 WHERE bukuId = :bukuId")
    suspend fun softDeleteByBukuId(bukuId: Int)

    @Query("SELECT * FROM eksemplar WHERE bukuId = :bukuId AND isDeleted = 0")
    fun getByBukuId(bukuId: Int): Flow<List<Eksemplar>>

    @Query("SELECT * FROM eksemplar WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: Int): Eksemplar?

    @Query("SELECT * FROM eksemplar WHERE kodeEksemplar = :kode AND isDeleted = 0 LIMIT 1")
    suspend fun getByKode(kode: String): Eksemplar?

    @Query("UPDATE eksemplar SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Transaction
    @Query("SELECT * FROM buku WHERE id = :bukuId AND isDeleted = 0")
    suspend fun getBukuWithEksemplar(bukuId: Int): BukuWithEksemplar?

    @Transaction
    @Query("SELECT * FROM buku WHERE isDeleted = 0")
    fun getAllBukuWithEksemplar(): Flow<List<BukuWithEksemplar>>

    @Query("SELECT COUNT(*) FROM eksemplar WHERE bukuId = :bukuId AND isDeleted = 0")
    suspend fun countByBukuId(bukuId: Int): Int

    @Query("SELECT COUNT(*) FROM eksemplar WHERE bukuId = :bukuId AND status = :status AND isDeleted = 0")
    suspend fun countByBukuIdAndStatus(bukuId: Int, status: String): Int

    @Query("""
        SELECT COUNT(*) FROM eksemplar e
        INNER JOIN buku b ON e.bukuId = b.id
        WHERE b.kategoriId IN (
            WITH RECURSIVE kategori_tree AS (
                SELECT id FROM kategori WHERE id = :kategoriId AND isDeleted = 0
                UNION ALL
                SELECT k.id FROM kategori k
                INNER JOIN kategori_tree kt ON k.parentId = kt.id
                WHERE k.isDeleted = 0
            )
            SELECT id FROM kategori_tree
        )
        AND e.status = 'dipinjam'
        AND e.isDeleted = 0
        AND b.isDeleted = 0
    """)
    suspend fun countDipinjamInKategoriSubtree(kategoriId: Int): Int
}
