package com.example.remedialucp2_214.repositori

import com.example.remedialucp2_214.room.BukuWithEksemplar
import com.example.remedialucp2_214.room.Eksemplar
import com.example.remedialucp2_214.room.EksemplarDao
import kotlinx.coroutines.flow.Flow

class RepositoriEksemplar(private val eksemplarDao: EksemplarDao) {

    fun getEksemplarByBukuId(bukuId: Int): Flow<List<Eksemplar>> = eksemplarDao.getByBukuId(bukuId)

    fun getAllBukuWithEksemplar(): Flow<List<BukuWithEksemplar>> = eksemplarDao.getAllBukuWithEksemplar()

    suspend fun getBukuWithEksemplar(bukuId: Int): BukuWithEksemplar? = eksemplarDao.getBukuWithEksemplar(bukuId)

    suspend fun getEksemplarById(id: Int): Eksemplar? = eksemplarDao.getById(id)

    suspend fun getEksemplarByKode(kode: String): Eksemplar? = eksemplarDao.getByKode(kode)

    suspend fun countEksemplarByBukuId(bukuId: Int): Int = eksemplarDao.countByBukuId(bukuId)

    suspend fun countDipinjamByBukuId(bukuId: Int): Int =
        eksemplarDao.countByBukuIdAndStatus(bukuId, Eksemplar.STATUS_DIPINJAM)

    suspend fun countDipinjamInKategoriSubtree(kategoriId: Int): Int =
        eksemplarDao.countDipinjamInKategoriSubtree(kategoriId)

    suspend fun insertEksemplar(eksemplar: Eksemplar): Result<Long> {
        return try {
            if (eksemplar.kodeEksemplar.isBlank()) {
                return Result.failure(IllegalArgumentException("Kode eksemplar tidak boleh kosong"))
            }
            val existing = eksemplarDao.getByKode(eksemplar.kodeEksemplar)
            if (existing != null) {
                return Result.failure(IllegalArgumentException("Kode eksemplar sudah digunakan"))
            }
            val id = eksemplarDao.insert(eksemplar)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEksemplar(eksemplar: Eksemplar): Result<Unit> {
        return try {
            if (eksemplar.kodeEksemplar.isBlank()) {
                return Result.failure(IllegalArgumentException("Kode eksemplar tidak boleh kosong"))
            }
            val existing = eksemplarDao.getByKode(eksemplar.kodeEksemplar)
            if (existing != null && existing.id != eksemplar.id) {
                return Result.failure(IllegalArgumentException("Kode eksemplar sudah digunakan"))
            }
            eksemplarDao.update(eksemplar)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEksemplar(id: Int): Result<Unit> {
        return try {
            val eksemplar = eksemplarDao.getById(id)
            if (eksemplar?.status == Eksemplar.STATUS_DIPINJAM) {
                return Result.failure(IllegalStateException("Tidak dapat menghapus eksemplar yang sedang dipinjam"))
            }
            eksemplarDao.softDelete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun softDeleteByBukuId(bukuId: Int): Result<Unit> {
        return try {
            eksemplarDao.softDeleteByBukuId(bukuId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pinjamEksemplar(id: Int): Result<Unit> {
        return try {
            val eksemplar = eksemplarDao.getById(id)
                ?: return Result.failure(IllegalArgumentException("Eksemplar tidak ditemukan"))
            if (eksemplar.status == Eksemplar.STATUS_DIPINJAM) {
                return Result.failure(IllegalStateException("Eksemplar sudah dipinjam"))
            }
            eksemplarDao.updateStatus(id, Eksemplar.STATUS_DIPINJAM)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun kembalikanEksemplar(id: Int): Result<Unit> {
        return try {
            val eksemplar = eksemplarDao.getById(id)
                ?: return Result.failure(IllegalArgumentException("Eksemplar tidak ditemukan"))
            if (eksemplar.status == Eksemplar.STATUS_TERSEDIA) {
                return Result.failure(IllegalStateException("Eksemplar tidak sedang dipinjam"))
            }
            eksemplarDao.updateStatus(id, Eksemplar.STATUS_TERSEDIA)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
