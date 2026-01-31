package com.example.remedialucp2_214.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedialucp2_214.repositori.RepositoriBuku
import com.example.remedialucp2_214.repositori.RepositoriEksemplar
import com.example.remedialucp2_214.repositori.RepositoriKategori
import com.example.remedialucp2_214.repositori.RepositoriPengarang
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.Eksemplar
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.room.Pengarang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DetailUiState(
    val bukuWithKategori: BukuWithKategori? = null,
    val kategoriList: List<Kategori> = emptyList(),
    val pengarangList: List<Pengarang> = emptyList(),
    val bukuPengarangList: List<Pengarang> = emptyList(),
    val eksemplarList: List<Eksemplar> = emptyList(),
    val editJudul: String = "",
    val editStatus: String = Buku.STATUS_TERSEDIA,
    val editKategoriId: Int? = null,
    val isJudulError: Boolean = false,
    val isKategoriError: Boolean = false,
    val judulErrorMessage: String = "",
    val kategoriErrorMessage: String = "",
    val newEksemplarKode: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isUpdated: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class DetailViewModel(
    private val repositoriBuku: RepositoriBuku,
    private val repositoriKategori: RepositoriKategori,
    private val repositoriEksemplar: RepositoriEksemplar,
    private val repositoriPengarang: RepositoriPengarang
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadBuku(bukuId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repositoriKategori.getAllKategori().collectLatest { list ->
                _uiState.value = _uiState.value.copy(kategoriList = list)
            }
        }
        viewModelScope.launch {
            repositoriPengarang.getAllPengarang().collectLatest { list ->
                _uiState.value = _uiState.value.copy(pengarangList = list)
            }
        }
        viewModelScope.launch {
            repositoriPengarang.getPengarangByBukuId(bukuId).collectLatest { list ->
                _uiState.value = _uiState.value.copy(bukuPengarangList = list)
            }
        }
        viewModelScope.launch {
            repositoriEksemplar.getEksemplarByBukuId(bukuId).collectLatest { list ->
                _uiState.value = _uiState.value.copy(eksemplarList = list)
            }
        }
        viewModelScope.launch {
            repositoriBuku.getBukuWithKategoriById(bukuId).collectLatest { bukuWithKategori ->
                if (bukuWithKategori != null) {
                    _uiState.value = _uiState.value.copy(
                        bukuWithKategori = bukuWithKategori,
                        editJudul = bukuWithKategori.buku.judul,
                        editStatus = bukuWithKategori.buku.status,
                        editKategoriId = bukuWithKategori.buku.kategoriId,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Buku tidak ditemukan")
                }
            }
        }
    }

    fun updateEditJudul(judul: String) {
        _uiState.value = _uiState.value.copy(editJudul = judul, isJudulError = false, judulErrorMessage = "")
    }

    fun updateEditStatus(status: String) {
        _uiState.value = _uiState.value.copy(editStatus = status)
    }

    fun updateEditKategori(kategoriId: Int?) {
        _uiState.value = _uiState.value.copy(editKategoriId = kategoriId, isKategoriError = false, kategoriErrorMessage = "")
    }

    fun updateNewEksemplarKode(kode: String) {
        _uiState.value = _uiState.value.copy(newEksemplarKode = kode)
    }

    private fun validateEditInput(): Boolean {
        var valid = true
        val state = _uiState.value
        if (state.editJudul.isBlank()) {
            _uiState.value = _uiState.value.copy(isJudulError = true, judulErrorMessage = "Judul tidak boleh kosong")
            valid = false
        }
        if (state.editKategoriId == null) {
            _uiState.value = _uiState.value.copy(isKategoriError = true, kategoriErrorMessage = "Kategori harus dipilih")
            valid = false
        }
        return valid
    }

    fun updateBuku() {
        if (!validateEditInput()) return
        val currentBuku = _uiState.value.bukuWithKategori?.buku ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val state = _uiState.value
            val updatedBuku = currentBuku.copy(
                judul = state.editJudul.trim(),
                status = state.editStatus,
                kategoriId = state.editKategoriId
            )
            val result = repositoriBuku.updateBuku(updatedBuku)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isSaving = false, isUpdated = true)
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.exceptionOrNull()?.message ?: "Gagal mengupdate buku")
            }
        }
    }

    fun deleteBuku() {
        val bukuId = _uiState.value.bukuWithKategori?.buku?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            repositoriEksemplar.softDeleteByBukuId(bukuId)
            val result = repositoriBuku.softDeleteBuku(bukuId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isDeleting = false, isDeleted = true)
            } else {
                _uiState.value = _uiState.value.copy(isDeleting = false, errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus buku")
            }
        }
    }

    fun addEksemplar() {
        val bukuId = _uiState.value.bukuWithKategori?.buku?.id ?: return
        val kode = _uiState.value.newEksemplarKode.trim()
        if (kode.isBlank()) return
        viewModelScope.launch {
            val eksemplar = Eksemplar(bukuId = bukuId, kodeEksemplar = kode)
            val result = repositoriEksemplar.insertEksemplar(eksemplar)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(newEksemplarKode = "", successMessage = "Eksemplar ditambahkan")
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun pinjamEksemplar(eksemplarId: Int) {
        viewModelScope.launch {
            val result = repositoriEksemplar.pinjamEksemplar(eksemplarId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun kembalikanEksemplar(eksemplarId: Int) {
        viewModelScope.launch {
            val result = repositoriEksemplar.kembalikanEksemplar(eksemplarId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteEksemplar(eksemplarId: Int) {
        viewModelScope.launch {
            val result = repositoriEksemplar.deleteEksemplar(eksemplarId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
