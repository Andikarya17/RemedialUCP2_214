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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.ui.view.route.DestinasiKategori
import com.example.remedialucp2_214.ui.view.viewmodel.DeleteKategoriState
import com.example.remedialucp2_214.ui.view.viewmodel.KategoriUiState
import com.example.remedialucp2_214.ui.view.viewmodel.KategoriViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanKategori(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KategoriViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    DeleteStateDialogs(
        deleteState = uiState.deleteState,
        onDismiss = viewModel::resetDeleteState,
        onSoftDelete = { viewModel.confirmDeleteKategori(it, softDeleteBooks = true) },
        onMoveBooks = { viewModel.confirmDeleteKategori(it, softDeleteBooks = false) }
    )

    Scaffold(
        topBar = {
            AppBar(
                titleRes = DestinasiKategori.titleRes,
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        KategoriContent(
            uiState = uiState,
            onNamaChange = viewModel::updateNamaKategori,
            onParentChange = viewModel::updateParentKategori,
            onSaveClick = viewModel::saveKategori,
            onEditClick = viewModel::startEditKategori,
            onDeleteClick = { viewModel.initiateDeleteKategori(it.id) },
            onCancelEdit = viewModel::resetForm,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@Composable
private fun DeleteStateDialogs(
    deleteState: DeleteKategoriState,
    onDismiss: () -> Unit,
    onSoftDelete: (Int) -> Unit,
    onMoveBooks: (Int) -> Unit
) {
    when (deleteState) {
        is DeleteKategoriState.HasBorrowedBooks -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(16.dp),
                title = { Text(stringResource(R.string.dialog_konfirmasi_hapus)) },
                text = { Text(stringResource(R.string.msg_gagal_hapus_dipinjam)) },
                confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
            )
        }
        is DeleteKategoriState.NeedConfirmation -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(16.dp),
                title = { Text(stringResource(R.string.dialog_pilih_aksi)) },
                text = { Text("Kategori ini memiliki ${deleteState.bookCount} buku. Pilih aksi:") },
                confirmButton = {
                    Column {
                        TextButton(onClick = { onSoftDelete(deleteState.kategoriId) }) {
                            Text(stringResource(R.string.dialog_aksi_soft_delete))
                        }
                        TextButton(onClick = { onMoveBooks(deleteState.kategoriId) }) {
                            Text(stringResource(R.string.dialog_aksi_pindah_kategori))
                        }
                    }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_batal)) } }
            )
        }
        is DeleteKategoriState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(16.dp),
                title = { Text("Error") },
                text = { Text(deleteState.message) },
                confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
            )
        }
        else -> {}
    }
}

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
    Column(modifier = modifier.padding(16.dp)) {
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

        Spacer(Modifier.height(20.dp))

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.kategoriList.isEmpty() -> {
                EmptyKategoriState()
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.kategoriList, key = { it.id }) { kategori ->
                        KategoriCard(
                            kategori = kategori,
                            parentKategori = uiState.kategoriList.find { it.id == kategori.parentId },
                            onEditClick = { onEditClick(kategori) },
                            onDeleteClick = { onDeleteClick(kategori) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyKategoriState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.msg_kategori_kosong),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

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
    onCancelEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedParent = kategoriList.find { it.id == selectedParentId }
    val parentOptions = if (editingKategoriId != null) {
        kategoriList.filter { it.id != editingKategoriId }
    } else kategoriList

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (editingKategoriId == null) stringResource(R.string.btn_tambah_kategori) else "Edit Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = namaKategori,
                onValueChange = onNamaChange,
                label = { Text(stringResource(R.string.label_nama_kategori)) },
                isError = isNamaError,
                supportingText = { if (isNamaError) Text(namaErrorMessage) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedParent?.namaKategori ?: stringResource(R.string.tidak_ada_parent),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_parent_kategori)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    isError = cyclicErrorMessage.isNotEmpty(),
                    supportingText = {
                        if (cyclicErrorMessage.isNotEmpty()) {
                            Text(cyclicErrorMessage, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.tidak_ada_parent)) },
                        onClick = {
                            onParentChange(null)
                            expanded = false
                        }
                    )
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (editingKategoriId != null) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_batal))
                    }
                }

                Button(
                    onClick = onSaveClick,
                    enabled = !isSaving && cyclicErrorMessage.isEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSaving) stringResource(R.string.loading) else stringResource(R.string.btn_simpan))
                }
            }
        }
    }
}

@Composable
private fun KategoriCard(
    kategori: Kategori,
    parentKategori: Kategori?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = kategori.namaKategori,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (parentKategori != null) {
                    Text(
                        text = "Parent: ${parentKategori.namaKategori}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.btn_edit),
                    tint = MaterialTheme.colorScheme.primary
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
