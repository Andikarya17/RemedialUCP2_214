package com.example.remedialucp2_214.ui.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remedialucp2_214.repositori.RepositoriBuku
import com.example.remedialucp2_214.repositori.RepositoriKategori
import com.example.remedialucp2_214.room.Kategori
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Sealed class untuk hasil delete kategori
 */
sealed class DeleteKategoriState {
    object Idle : DeleteKategoriState()
    object Success : DeleteKategoriState()
    data class HasBorrowedBooks(val count: Int) : DeleteKategoriState()
    data class NeedConfirmation(val kategoriId: Int, val bookCount: Int) : DeleteKategoriState()
    data class Error(val message: String) : DeleteKategoriState()
}

/**
 * UI State untuk halaman Kategori
 */
data class KategoriUiState(
    val kategoriList: List<Kategori> = emptyList(),
    // Form state
    val inputNamaKategori: String = "",
    val selectedParentId: Int? = null,
    val editingKategoriId: Int? = null, // null = mode tambah, not null = mode edit
    // Validation
    val isNamaError: Boolean = false,
    val namaErrorMessage: String = "",
    val cyclicErrorMessage: String = "",
    // Operation state
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val deleteState: DeleteKategoriState = DeleteKategoriState.Idle,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel untuk halaman Kelola Kategori.
 * Mengelola:
 * - CRUD kategori
 * - Cyclic reference prevention
 * - Delete dengan transaction dan opsi untuk buku
 */
class KategoriViewModel(
    private val repositoriKategori: RepositoriKategori,
    private val repositoriBuku: RepositoriBuku
) : ViewModel() {

    private val _uiState = MutableStateFlow(KategoriUiState())
    val uiState: StateFlow<KategoriUiState> = _uiState.asStateFlow()

    init {
        loadKategori()
    }

    /**
     * Load semua kategori
     */
    private fun loadKategori() {
        viewModelScope.launch {
            repositoriKategori.getAllKategori().collectLatest { kategoriList ->
                _uiState.value = _uiState.value.copy(
                    kategoriList = kategoriList,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Update nama kategori input
     */
    fun updateNamaKategori(nama: String) {
        _uiState.value = _uiState.value.copy(
            inputNamaKategori = nama,
            isNamaError = false,
            namaErrorMessage = ""
        )
    }

    /**
     * Update parent kategori yang dipilih
     */
    fun updateParentKategori(parentId: Int?) {
        viewModelScope.launch {
            val editingId = _uiState.value.editingKategoriId

            // Cek cyclic reference jika sedang edit
            if (editingId != null && parentId != null) {
                val wouldCycle = repositoriKategori.wouldCreateCycle(editingId, parentId)
                if (wouldCycle) {
                    _uiState.value = _uiState.value.copy(
                        cyclicErrorMessage = "Tidak dapat memilih kategori ini sebagai parent (akan membentuk loop)"
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(
                selectedParentId = parentId,
                cyclicErrorMessage = ""
            )
        }
    }

    /**
     * Set mode edit untuk kategori tertentu
     */
    fun startEditKategori(kategori: Kategori) {
        _uiState.value = _uiState.value.copy(
            editingKategoriId = kategori.id,
            inputNamaKategori = kategori.namaKategori,
            selectedParentId = kategori.parentId,
            isNamaError = false,
            namaErrorMessage = "",
            cyclicErrorMessage = ""
        )
    }

    /**
     * Reset form ke mode tambah
     */
    fun resetForm() {
        _uiState.value = _uiState.value.copy(
            editingKategoriId = null,
            inputNamaKategori = "",
            selectedParentId = null,
            isNamaError = false,
            namaErrorMessage = "",
            cyclicErrorMessage = ""
        )
    }

    /**
     * Validasi input
     */
    private fun validateInput(): Boolean {
        if (_uiState.value.inputNamaKategori.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isNamaError = true,
                namaErrorMessage = "Nama kategori tidak boleh kosong"
            )
            return false
        }
        return true
    }

    /**
     * Simpan kategori (tambah atau update)
     */
    fun saveKategori() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val state = _uiState.value
            val editingId = state.editingKategoriId

            val result = if (editingId == null) {
                // Mode tambah
                val kategori = Kategori(
                    namaKategori = state.inputNamaKategori.trim(),
                    parentId = state.selectedParentId
                )
                repositoriKategori.insertKategori(kategori)
                    .map { "Kategori berhasil ditambahkan" }
            } else {
                // Mode edit
                val kategori = Kategori(
                    id = editingId,
                    namaKategori = state.inputNamaKategori.trim(),
                    parentId = state.selectedParentId
                )
                repositoriKategori.updateKategori(kategori)
                    .map { "Kategori berhasil diupdate" }
            }

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = result.getOrNull(),
                    editingKategoriId = null,
                    inputNamaKategori = "",
                    selectedParentId = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal menyimpan kategori"
                )
            }
        }
    }

    /**
     * Mulai proses hapus kategori - cek kondisi terlebih dahulu
     */
    fun initiateDeleteKategori(kategoriId: Int) {
        viewModelScope.launch {
            val checkResult = repositoriBuku.checkDeleteKategori(kategoriId)

            when (checkResult) {
                is RepositoriBuku.DeleteKategoriResult.Success -> {
                    // Langsung hapus karena tidak ada buku
                    deleteEmptyKategori(kategoriId)
                }
                is RepositoriBuku.DeleteKategoriResult.HasBorrowedBooks -> {
                    // Ada buku dipinjam - tidak bisa hapus
                    _uiState.value = _uiState.value.copy(
                        deleteState = DeleteKategoriState.HasBorrowedBooks(checkResult.count)
                    )
                }
                is RepositoriBuku.DeleteKategoriResult.HasAvailableBooks -> {
                    // Ada buku tersedia - perlu konfirmasi opsi
                    _uiState.value = _uiState.value.copy(
                        deleteState = DeleteKategoriState.NeedConfirmation(
                            kategoriId = kategoriId,
                            bookCount = checkResult.count
                        )
                    )
                }
                is RepositoriBuku.DeleteKategoriResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        deleteState = DeleteKategoriState.Error(checkResult.message)
                    )
                }
            }
        }
    }

    /**
     * Hapus kategori kosong (tanpa buku)
     */
    private suspend fun deleteEmptyKategori(kategoriId: Int) {
        val result = repositoriBuku.deleteEmptyKategori(kategoriId)
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(
                deleteState = DeleteKategoriState.Success,
                successMessage = "Kategori berhasil dihapus"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                deleteState = DeleteKategoriState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal menghapus kategori"
                )
            )
        }
    }

    /**
     * Konfirmasi hapus kategori dengan opsi untuk buku
     * @param softDeleteBooks true = soft delete buku, false = pindahkan ke Tanpa Kategori
     */
    fun confirmDeleteKategori(kategoriId: Int, softDeleteBooks: Boolean) {
        viewModelScope.launch {
            val result = repositoriBuku.deleteKategoriWithBooks(kategoriId, softDeleteBooks)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    deleteState = DeleteKategoriState.Success,
                    successMessage = "Kategori berhasil dihapus"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    deleteState = DeleteKategoriState.Error(
                        result.exceptionOrNull()?.message ?: "Gagal menghapus kategori"
                    )
                )
            }
        }
    }

    /**
     * Reset delete state
     */
    fun resetDeleteState() {
        _uiState.value = _uiState.value.copy(deleteState = DeleteKategoriState.Idle)
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}
