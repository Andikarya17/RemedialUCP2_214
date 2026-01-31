package com.example.remedialucp2_214.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PengarangDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pengarang: Pengarang): Long

    @Update
    suspend fun update(pengarang: Pengarang)

    @Query("UPDATE pengarang SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    @Query("SELECT * FROM pengarang WHERE isDeleted = 0 ORDER BY nama ASC")
    fun getAllActive(): Flow<List<Pengarang>>

    @Query("SELECT * FROM pengarang WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: Int): Pengarang?

    @Query("SELECT * FROM pengarang WHERE nama LIKE '%' || :query || '%' AND isDeleted = 0")
    fun searchByName(query: String): Flow<List<Pengarang>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBukuPengarang(bukuPengarang: BukuPengarang)

    @Query("DELETE FROM buku_pengarang WHERE bukuId = :bukuId")
    suspend fun deleteBukuPengarangByBukuId(bukuId: Int)

    @Query("DELETE FROM buku_pengarang WHERE bukuId = :bukuId AND pengarangId = :pengarangId")
    suspend fun deleteBukuPengarang(bukuId: Int, pengarangId: Int)

    @Transaction
    @Query("SELECT * FROM buku WHERE id = :bukuId AND isDeleted = 0")
    suspend fun getBukuWithPengarang(bukuId: Int): BukuWithPengarang?

    @Query("""
        SELECT p.* FROM pengarang p
        INNER JOIN buku_pengarang bp ON p.id = bp.pengarangId
        WHERE bp.bukuId = :bukuId AND p.isDeleted = 0
    """)
    fun getPengarangByBukuId(bukuId: Int): Flow<List<Pengarang>>
}
