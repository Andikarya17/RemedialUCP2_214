package com.example.remedialucp2_214.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedialucp2_214.repositori.RepositoriBuku
import com.example.remedialucp2_214.repositori.RepositoriKategori
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.Kategori
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * UI State untuk halaman Home
 */
data class HomeUiState(
    val bukuList: List<BukuWithKategori> = emptyList(),
    val kategoriList: List<Kategori> = emptyList(),
    val selectedKategoriId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel untuk halaman Home.
 * Mengelola:
 * - List buku dengan kategori
 * - Filter berdasarkan kategori (recursive search ke subkategori)
 * - Delete buku
 */
class HomeViewModel(
    private val repositoriBuku: RepositoriBuku,
    private val repositoriKategori: RepositoriKategori
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load semua data awal
     */
    private fun loadData() {
        loadKategori()
        loadBuku()
    }

    /**
     * Load daftar kategori untuk filter
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
     * Load buku berdasarkan filter kategori
     */
    private fun loadBuku() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val selectedId = _uiState.value.selectedKategoriId
            
            if (selectedId == null) {
                // Load semua buku
                repositoriBuku.getAllBukuWithKategori().collectLatest { bukuList ->
                    _uiState.value = _uiState.value.copy(
                        bukuList = bukuList,
                        isLoading = false
                    )
                }
            } else {
                // Load buku dengan recursive search ke subkategori
                repositoriBuku.getBukuByKategoriRecursive(selectedId).collectLatest { bukuList ->
                    _uiState.value = _uiState.value.copy(
                        bukuList = bukuList,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Set filter kategori
     * Jika kategori dipilih, akan menampilkan buku di kategori tersebut
     * DAN semua subkategorinya (recursive)
     */
    fun setFilterKategori(kategoriId: Int?) {
        _uiState.value = _uiState.value.copy(selectedKategoriId = kategoriId)
        loadBuku()
    }

    /**
     * Soft delete buku
     */
    fun deleteBuku(bukuId: Int) {
        viewModelScope.launch {
            val result = repositoriBuku.softDeleteBuku(bukuId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    successMessage = "Buku berhasil dihapus"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus buku"
                )
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
