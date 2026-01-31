package com.example.remedialucp2_214.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedialucp2_214.repositori.RepositoriBuku
import com.example.remedialucp2_214.repositori.RepositoriKategori
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.Kategori
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * UI State untuk form entry buku
 */
data class EntryUiState(
    val judul: String = "",
    val status: String = Buku.STATUS_TERSEDIA,
    val selectedKategoriId: Int? = null,
    val kategoriList: List<Kategori> = emptyList(),
    val isJudulError: Boolean = false,
    val isKategoriError: Boolean = false,
    val judulErrorMessage: String = "",
    val kategoriErrorMessage: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk halaman Entry (tambah buku baru).
 * Mengelola:
 * - Input state management
 * - Validasi input
 * - Save operation
 */
class EntryViewModel(
    private val repositoriBuku: RepositoriBuku,
    private val repositoriKategori: RepositoriKategori
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    init {
        loadKategori()
    }

    /**
     * Load daftar kategori untuk dropdown
     */
    private fun loadKategori() {
        viewModelScope.launch {
            repositoriKategori.getAllKategori().collectLatest { kategoriList ->
                _uiState.value = _uiState.value.copy(
                    kategoriList = kategoriList
                )
            }
        }
    }

    /**
     * Update judul buku
     */
    fun updateJudul(judul: String) {
        _uiState.value = _uiState.value.copy(
            judul = judul,
            isJudulError = false,
            judulErrorMessage = ""
        )
    }

    /**
     * Update status buku
     */
    fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    /**
     * Update kategori yang dipilih
     */
    fun updateKategori(kategoriId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedKategoriId = kategoriId,
            isKategoriError = false,
            kategoriErrorMessage = ""
        )
    }

    /**
     * Validasi input di ViewModel (UI validation)
     * @return true jika valid
     */
    private fun validateInput(): Boolean {
        var isValid = true
        val state = _uiState.value

        // Validasi judul
        if (state.judul.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isJudulError = true,
                judulErrorMessage = "Judul tidak boleh kosong"
            )
            isValid = false
        }

        // Validasi kategori
        if (state.selectedKategoriId == null) {
            _uiState.value = _uiState.value.copy(
                isKategoriError = true,
                kategoriErrorMessage = "Kategori harus dipilih"
            )
            isValid = false
        }

        return isValid
    }

    /**
     * Simpan buku baru
     */
    fun saveBuku() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val state = _uiState.value
            val buku = Buku(
                judul = state.judul.trim(),
                status = state.status,
                kategoriId = state.selectedKategoriId
            )

            val result = repositoriBuku.insertBuku(buku)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal menyimpan buku"
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
