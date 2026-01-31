package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.ui.view.route.DestinasiEditBuku
import com.example.remedialucp2_214.ui.view.viewmodel.DetailUiState
import com.example.remedialucp2_214.ui.view.viewmodel.DetailViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

/**
 * Halaman Edit - form untuk mengedit buku
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanEdit(
    bukuId: Int,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load buku saat pertama kali
    LaunchedEffect(bukuId) {
        viewModel.loadBuku(bukuId)
    }

    // Navigate back setelah berhasil update
    LaunchedEffect(uiState.isUpdated) {
        if (uiState.isUpdated) {
            navigateBack()
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                titleRes = DestinasiEditBuku.titleRes,
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        EditContent(
            uiState = uiState,
            onJudulChange = { viewModel.updateEditJudul(it) },
            onStatusChange = { viewModel.updateEditStatus(it) },
            onKategoriChange = { viewModel.updateEditKategori(it) },
            onUpdateClick = { viewModel.updateBuku() },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

/**
 * Konten form edit
 */
@Composable
private fun EditContent(
    uiState: DetailUiState,
    onJudulChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onKategoriChange: (Int?) -> Unit,
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .padding(dimensionResource(R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
    ) {
        // Input Judul
        OutlinedTextField(
            value = uiState.editJudul,
            onValueChange = onJudulChange,
            label = { Text(stringResource(R.string.label_judul)) },
            isError = uiState.isJudulError,
            supportingText = {
                if (uiState.isJudulError) {
                    Text(uiState.judulErrorMessage)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Status Radio Group
        Text(
            text = stringResource(R.string.label_status),
            style = MaterialTheme.typography.titleSmall
        )
        EditStatusRadioGroup(
            selectedStatus = uiState.editStatus,
            onStatusChange = onStatusChange
        )

        // Kategori Dropdown
        EditKategoriDropdown(
            kategoriList = uiState.kategoriList,
            selectedKategoriId = uiState.editKategoriId,
            onKategoriChange = onKategoriChange,
            isError = uiState.isKategoriError,
            errorMessage = uiState.kategoriErrorMessage,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

        // Update Button
        Button(
            onClick = onUpdateClick,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.isSaving) {
                    stringResource(R.string.loading)
                } else {
                    stringResource(R.string.btn_update)
                }
            )
        }
    }
}

/**
 * Radio group untuk status buku (edit)
 */
@Composable
private fun EditStatusRadioGroup(
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusOptions = listOf(
        Buku.STATUS_TERSEDIA to stringResource(R.string.status_tersedia),
        Buku.STATUS_DIPINJAM to stringResource(R.string.status_dipinjam)
    )

    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
    ) {
        statusOptions.forEach { (value, label) ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = selectedStatus == value,
                        onClick = { onStatusChange(value) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedStatus == value,
                    onClick = null
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_small))
                )
            }
        }
    }
}

/**
 * Dropdown untuk memilih kategori (edit)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditKategoriDropdown(
    kategoriList: List<Kategori>,
    selectedKategoriId: Int?,
    onKategoriChange: (Int?) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedKategori = kategoriList.find { it.id == selectedKategoriId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedKategori?.namaKategori ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_kategori)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(errorMessage)
                }
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            kategoriList.forEach { kategori ->
                DropdownMenuItem(
                    text = { Text(kategori.namaKategori) },
                    onClick = {
                        onKategoriChange(kategori.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
