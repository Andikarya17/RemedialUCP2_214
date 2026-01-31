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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.ui.view.route.DestinasiKategori
import com.example.remedialucp2_214.ui.view.viewmodel.DeleteKategoriState
import com.example.remedialucp2_214.ui.view.viewmodel.KategoriUiState
import com.example.remedialucp2_214.ui.view.viewmodel.KategoriViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

/**
 * Halaman Kelola Kategori - CRUD kategori dengan hierarki
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanKategori(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KategoriViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for messages
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Handle delete state dialogs
    HandleDeleteState(
        deleteState = uiState.deleteState,
        onDismiss = { viewModel.resetDeleteState() },
        onConfirmSoftDelete = { kategoriId ->
            viewModel.confirmDeleteKategori(kategoriId, softDeleteBooks = true)
        },
        onConfirmMoveToTanpaKategori = { kategoriId ->
            viewModel.confirmDeleteKategori(kategoriId, softDeleteBooks = false)
        }
    )

    Scaffold(
        topBar = {
            AppBar(
                titleRes = DestinasiKategori.titleRes,
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        KategoriContent(
            uiState = uiState,
            onNamaChange = { viewModel.updateNamaKategori(it) },
            onParentChange = { viewModel.updateParentKategori(it) },
            onSaveClick = { viewModel.saveKategori() },
            onEditClick = { viewModel.startEditKategori(it) },
            onDeleteClick = { viewModel.initiateDeleteKategori(it.id) },
            onCancelEdit = { viewModel.resetForm() },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

/**
 * Handle delete state dan tampilkan dialog yang sesuai
 */
@Composable
private fun HandleDeleteState(
    deleteState: DeleteKategoriState,
    onDismiss: () -> Unit,
    onConfirmSoftDelete: (Int) -> Unit,
    onConfirmMoveToTanpaKategori: (Int) -> Unit
) {
    when (deleteState) {
        is DeleteKategoriState.HasBorrowedBooks -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.dialog_konfirmasi_hapus)) },
                text = {
                    Text(stringResource(R.string.msg_gagal_hapus_dipinjam))
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            )
        }

        is DeleteKategoriState.NeedConfirmation -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.dialog_pilih_aksi)) },
                text = {
                    Column {
                        Text("Kategori ini memiliki ${deleteState.bookCount} buku. Pilih aksi:")
                    }
                },
                confirmButton = {
                    Column {
                        TextButton(
                            onClick = { onConfirmSoftDelete(deleteState.kategoriId) }
                        ) {
                            Text(stringResource(R.string.dialog_aksi_soft_delete))
                        }
                        TextButton(
                            onClick = { onConfirmMoveToTanpaKategori(deleteState.kategoriId) }
                        ) {
                            Text(stringResource(R.string.dialog_aksi_pindah_kategori))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_batal))
                    }
                }
            )
        }

        is DeleteKategoriState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Error") },
                text = { Text(deleteState.message) },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            )
        }

        else -> { /* No dialog needed */ }
    }
}

/**
 * Konten halaman kategori
 */
@Composable
private fun KategoriContent(
    uiState: KategoriUiState,
    onNamaChange: (String) -> Unit,
    onParentChange: (Int?) -> Unit,
    onSaveClick: () -> Unit,
    onEditClick: (Kategori) -> Unit,
    onDeleteClick: (Kategori) -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.padding_medium))
    ) {
        // Form tambah/edit kategori
        KategoriForm(
            namaKategori = uiState.inputNamaKategori,
            selectedParentId = uiState.selectedParentId,
            kategoriList = uiState.kategoriList,
            editingKategoriId = uiState.editingKategoriId,
            isNamaError = uiState.isNamaError,
            namaErrorMessage = uiState.namaErrorMessage,
            cyclicErrorMessage = uiState.cyclicErrorMessage,
            isSaving = uiState.isSaving,
            onNamaChange = onNamaChange,
            onParentChange = onParentChange,
            onSaveClick = onSaveClick,
            onCancelEdit = onCancelEdit
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

        // List kategori
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.kategoriList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.msg_kategori_kosong),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                items(
                    items = uiState.kategoriList,
                    key = { it.id }
                ) { kategori ->
                    KategoriCard(
                        kategori = kategori,
                        allKategori = uiState.kategoriList,
                        onEditClick = { onEditClick(kategori) },
                        onDeleteClick = { onDeleteClick(kategori) }
                    )
                }
            }
        }
    }
}

/**
 * Form untuk tambah/edit kategori
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriForm(
    namaKategori: String,
    selectedParentId: Int?,
    kategoriList: List<Kategori>,
    editingKategoriId: Int?,
    isNamaError: Boolean,
    namaErrorMessage: String,
    cyclicErrorMessage: String,
    isSaving: Boolean,
    onNamaChange: (String) -> Unit,
    onParentChange: (Int?) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedParent = kategoriList.find { it.id == selectedParentId }
    
    // Filter out current kategori dari parent options jika sedang edit
    val parentOptions = if (editingKategoriId != null) {
        kategoriList.filter { it.id != editingKategoriId }
    } else {
        kategoriList
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = if (editingKategoriId == null) {
                    stringResource(R.string.btn_tambah_kategori)
                } else {
                    "Edit Kategori"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Input nama kategori
            OutlinedTextField(
                value = namaKategori,
                onValueChange = onNamaChange,
                label = { Text(stringResource(R.string.label_nama_kategori)) },
                isError = isNamaError,
                supportingText = {
                    if (isNamaError) {
                        Text(namaErrorMessage)
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Parent kategori dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedParent?.namaKategori ?: stringResource(R.string.tidak_ada_parent),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_parent_kategori)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    isError = cyclicErrorMessage.isNotEmpty(),
                    supportingText = {
                        if (cyclicErrorMessage.isNotEmpty()) {
                            Text(cyclicErrorMessage, color = MaterialTheme.colorScheme.error)
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
                    // Option untuk tidak ada parent
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.tidak_ada_parent)) },
                        onClick = {
                            onParentChange(null)
                            expanded = false
                        }
                    )

                    // Parent options
                    parentOptions.forEach { kategori ->
                        DropdownMenuItem(
                            text = { Text(kategori.namaKategori) },
                            onClick = {
                                onParentChange(kategori.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                if (editingKategoriId != null) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_batal))
                    }
                }

                Button(
                    onClick = onSaveClick,
                    enabled = !isSaving && cyclicErrorMessage.isEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isSaving) {
                            stringResource(R.string.loading)
                        } else {
                            stringResource(R.string.btn_simpan)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Card untuk menampilkan kategori
 */
@Composable
private fun KategoriCard(
    kategori: Kategori,
    allKategori: List<Kategori>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parent = allKategori.find { it.id == kategori.parentId }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.padding_medium))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kategori.namaKategori,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (parent != null) {
                    Text(
                        text = "Parent: ${parent.namaKategori}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.btn_edit)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.btn_hapus),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
