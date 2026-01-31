package com.example.remedialucp2_214.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedialucp2_214.repositori.RepositoriBuku
import com.example.remedialucp2_214.repositori.RepositoriEksemplar
import com.example.remedialucp2_214.repositori.RepositoriKategori
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.Kategori
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val bukuList: List<BukuWithKategori> = emptyList(),
    val eksemplarCounts: Map<Int, Pair<Int, Int>> = emptyMap(), // bukuId -> (total, dipinjam)
    val kategoriList: List<Kategori> = emptyList(),
    val selectedKategoriId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class HomeViewModel(
    private val repositoriBuku: RepositoriBuku,
    private val repositoriKategori: RepositoriKategori,
    private val repositoriEksemplar: RepositoriEksemplar
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadKategori()
        loadBuku()
    }

    private fun loadKategori() {
        viewModelScope.launch {
            repositoriKategori.getAllKategori().collectLatest { kategoriList ->
                _uiState.value = _uiState.value.copy(kategoriList = kategoriList)
            }
        }
    }

    private fun loadBuku() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val selectedId = _uiState.value.selectedKategoriId

            if (selectedId == null) {
                repositoriBuku.getAllBukuWithKategori().collectLatest { bukuList ->
                    loadEksemplarCounts(bukuList)
                }
            } else {
                repositoriBuku.getBukuByKategoriRecursive(selectedId).collectLatest { bukuList ->
                    loadEksemplarCounts(bukuList)
                }
            }
        }
    }

    private suspend fun loadEksemplarCounts(bukuList: List<BukuWithKategori>) {
        val counts = mutableMapOf<Int, Pair<Int, Int>>()
        bukuList.forEach { item ->
            val total = repositoriEksemplar.countEksemplarByBukuId(item.buku.id)
            val dipinjam = repositoriEksemplar.countDipinjamByBukuId(item.buku.id)
            counts[item.buku.id] = Pair(total, dipinjam)
        }
        _uiState.value = _uiState.value.copy(
            bukuList = bukuList,
            eksemplarCounts = counts,
            isLoading = false
        )
    }

    fun setFilterKategori(kategoriId: Int?) {
        _uiState.value = _uiState.value.copy(selectedKategoriId = kategoriId)
        loadBuku()
    }

    fun deleteBuku(bukuId: Int) {
        viewModelScope.launch {
            val result = repositoriBuku.softDeleteBuku(bukuId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(successMessage = "Buku berhasil dihapus")
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus buku"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
