package com.example.remedialucp2_214.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedialucp2_214.repositori.RepositoriBuku
import com.example.remedialucp2_214.repositori.RepositoriKategori
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.Kategori
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * UI State untuk halaman Detail dan Edit
 */
data class DetailUiState(
    val bukuWithKategori: BukuWithKategori? = null,
    val kategoriList: List<Kategori> = emptyList(),
    // Edit form state
    val editJudul: String = "",
    val editStatus: String = Buku.STATUS_TERSEDIA,
    val editKategoriId: Int? = null,
    // Validation state
    val isJudulError: Boolean = false,
    val isKategoriError: Boolean = false,
    val judulErrorMessage: String = "",
    val kategoriErrorMessage: String = "",
    // Operation state
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isUpdated: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk halaman Detail dan Edit buku.
 * Mengelola:
 * - Load detail buku dengan kategori
 * - Edit form state
 * - Update dengan audit log
 * - Delete buku
 */
class DetailViewModel(
    private val repositoriBuku: RepositoriBuku,
    private val repositoriKategori: RepositoriKategori
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Load detail buku berdasarkan ID
     */
    fun loadBuku(bukuId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load kategori list
            repositoriKategori.getAllKategori().collectLatest { kategoriList ->
                _uiState.value = _uiState.value.copy(kategoriList = kategoriList)
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Buku tidak ditemukan"
                    )
                }
            }
        }
    }

    /**
     * Update edit judul
     */
    fun updateEditJudul(judul: String) {
        _uiState.value = _uiState.value.copy(
            editJudul = judul,
            isJudulError = false,
            judulErrorMessage = ""
        )
    }

    /**
     * Update edit status
     */
    fun updateEditStatus(status: String) {
        _uiState.value = _uiState.value.copy(editStatus = status)
    }

    /**
     * Update edit kategori
     */
    fun updateEditKategori(kategoriId: Int?) {
        _uiState.value = _uiState.value.copy(
            editKategoriId = kategoriId,
            isKategoriError = false,
            kategoriErrorMessage = ""
        )
    }

    /**
     * Validasi input edit
     */
    private fun validateEditInput(): Boolean {
        var isValid = true
        val state = _uiState.value

        if (state.editJudul.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isJudulError = true,
                judulErrorMessage = "Judul tidak boleh kosong"
            )
            isValid = false
        }

        if (state.editKategoriId == null) {
            _uiState.value = _uiState.value.copy(
                isKategoriError = true,
                kategoriErrorMessage = "Kategori harus dipilih"
            )
            isValid = false
        }

        return isValid
    }

    /**
     * Update buku dengan audit log
     */
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
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isUpdated = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal mengupdate buku"
                )
            }
        }
    }

    /**
     * Soft delete buku
     */
    fun deleteBuku() {
        val bukuId = _uiState.value.bukuWithKategori?.buku?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            val result = repositoriBuku.softDeleteBuku(bukuId)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    isDeleted = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus buku"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
