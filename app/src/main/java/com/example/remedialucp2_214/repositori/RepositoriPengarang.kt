package com.example.remedialucp2_214.repositori

import com.example.remedialucp2_214.room.BukuPengarang
import com.example.remedialucp2_214.room.BukuWithPengarang
import com.example.remedialucp2_214.room.Pengarang
import com.example.remedialucp2_214.room.PengarangDao
import kotlinx.coroutines.flow.Flow

class RepositoriPengarang(private val pengarangDao: PengarangDao) {

    fun getAllPengarang(): Flow<List<Pengarang>> = pengarangDao.getAllActive()

    fun searchPengarang(query: String): Flow<List<Pengarang>> = pengarangDao.searchByName(query)

    fun getPengarangByBukuId(bukuId: Int): Flow<List<Pengarang>> = pengarangDao.getPengarangByBukuId(bukuId)

    suspend fun getPengarangById(id: Int): Pengarang? = pengarangDao.getById(id)

    suspend fun getBukuWithPengarang(bukuId: Int): BukuWithPengarang? = pengarangDao.getBukuWithPengarang(bukuId)

    suspend fun insertPengarang(pengarang: Pengarang): Result<Long> {
        return try {
            if (pengarang.nama.isBlank()) {
                return Result.failure(IllegalArgumentException("Nama pengarang tidak boleh kosong"))
            }
            val id = pengarangDao.insert(pengarang)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePengarang(pengarang: Pengarang): Result<Unit> {
        return try {
            if (pengarang.nama.isBlank()) {
                return Result.failure(IllegalArgumentException("Nama pengarang tidak boleh kosong"))
            }
            pengarangDao.update(pengarang)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePengarang(id: Int): Result<Unit> {
        return try {
            pengarangDao.softDelete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPengarangToBuku(bukuId: Int, pengarangId: Int): Result<Unit> {
        return try {
            pengarangDao.insertBukuPengarang(BukuPengarang(bukuId, pengarangId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removePengarangFromBuku(bukuId: Int, pengarangId: Int): Result<Unit> {
        return try {
            pengarangDao.deleteBukuPengarang(bukuId, pengarangId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPengarangForBuku(bukuId: Int, pengarangIds: List<Int>): Result<Unit> {
        return try {
            pengarangDao.deleteBukuPengarangByBukuId(bukuId)
            pengarangIds.forEach { pengarangId ->
                pengarangDao.insertBukuPengarang(BukuPengarang(bukuId, pengarangId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
