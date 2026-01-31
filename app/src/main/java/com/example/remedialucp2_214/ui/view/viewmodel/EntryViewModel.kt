package com.example.remedialucp2_214.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedialucp2_214.repositori.RepositoriBuku
import com.example.remedialucp2_214.repositori.RepositoriKategori
import com.example.remedialucp2_214.repositori.RepositoriPengarang
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.room.Pengarang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class EntryUiState(
    val judul: String = "",
    val status: String = Buku.STATUS_TERSEDIA,
    val selectedKategoriId: Int? = null,
    val kategoriList: List<Kategori> = emptyList(),
    val pengarangList: List<Pengarang> = emptyList(),
    val selectedPengarangIds: Set<Int> = emptySet(),
    val newPengarangName: String = "",
    val isJudulError: Boolean = false,
    val isKategoriError: Boolean = false,
    val judulErrorMessage: String = "",
    val kategoriErrorMessage: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class EntryViewModel(
    private val repositoriBuku: RepositoriBuku,
    private val repositoriKategori: RepositoriKategori,
    private val repositoriPengarang: RepositoriPengarang
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repositoriKategori.getAllKategori().collectLatest { list ->
                _uiState.value = _uiState.value.copy(kategoriList = list)
            }
        }
        viewModelScope.launch {
            repositoriPengarang.getAllPengarang().collectLatest { list ->
                _uiState.value = _uiState.value.copy(pengarangList = list)
            }
        }
    }

    fun updateJudul(judul: String) {
        _uiState.value = _uiState.value.copy(judul = judul, isJudulError = false, judulErrorMessage = "")
    }

    fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    fun updateKategori(kategoriId: Int?) {
        _uiState.value = _uiState.value.copy(selectedKategoriId = kategoriId, isKategoriError = false, kategoriErrorMessage = "")
    }

    fun updateNewPengarangName(name: String) {
        _uiState.value = _uiState.value.copy(newPengarangName = name)
    }

    fun togglePengarang(pengarangId: Int) {
        val current = _uiState.value.selectedPengarangIds.toMutableSet()
        if (current.contains(pengarangId)) current.remove(pengarangId) else current.add(pengarangId)
        _uiState.value = _uiState.value.copy(selectedPengarangIds = current)
    }

    fun addNewPengarang() {
        val name = _uiState.value.newPengarangName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            val result = repositoriPengarang.insertPengarang(Pengarang(nama = name))
            if (result.isSuccess) {
                val newId = result.getOrNull()?.toInt() ?: 0
                val currentSelected = _uiState.value.selectedPengarangIds.toMutableSet()
                currentSelected.add(newId)
                _uiState.value = _uiState.value.copy(
                    newPengarangName = "",
                    selectedPengarangIds = currentSelected
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal menambah pengarang"
                )
            }
        }
    }

    private fun validateInput(): Boolean {
        var valid = true
        val state = _uiState.value
        if (state.judul.isBlank()) {
            _uiState.value = _uiState.value.copy(isJudulError = true, judulErrorMessage = "Judul tidak boleh kosong")
            valid = false
        }
        if (state.selectedKategoriId == null) {
            _uiState.value = _uiState.value.copy(isKategoriError = true, kategoriErrorMessage = "Kategori harus dipilih")
            valid = false
        }
        return valid
    }

    fun saveBuku() {
        if (!validateInput()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val state = _uiState.value
            val buku = Buku(judul = state.judul.trim(), status = state.status, kategoriId = state.selectedKategoriId)
            val result = repositoriBuku.insertBuku(buku)
            if (result.isSuccess) {
                val bukuId = result.getOrNull()?.toInt() ?: 0
                state.selectedPengarangIds.forEach { pengarangId ->
                    repositoriPengarang.addPengarangToBuku(bukuId, pengarangId)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.exceptionOrNull()?.message ?: "Gagal menyimpan buku")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
