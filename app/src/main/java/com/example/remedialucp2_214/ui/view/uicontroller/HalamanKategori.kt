package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private val cardShape = RoundedCornerShape(16.dp)
private val fieldShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanKategori(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KategoriViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    DeleteDialogs(
        deleteState = state.deleteState,
        onDismiss = viewModel::resetDeleteState,
        onSoftDelete = { viewModel.confirmDeleteKategori(it, softDeleteBooks = true) },
        onMoveBooks = { viewModel.confirmDeleteKategori(it, softDeleteBooks = false) }
    )

    Scaffold(
        topBar = { AppBar(DestinasiKategori.titleRes, canNavigateBack = true, navigateUp = navigateBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        KategoriBody(
            state = state,
            onNameChange = viewModel::updateNamaKategori,
            onParentChange = viewModel::updateParentKategori,
            onSave = viewModel::saveKategori,
            onEdit = viewModel::startEditKategori,
            onDelete = { viewModel.initiateDeleteKategori(it.id) },
            onCancelEdit = viewModel::resetForm,
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
}

@Composable
private fun DeleteDialogs(
    deleteState: DeleteKategoriState,
    onDismiss: () -> Unit,
    onSoftDelete: (Int) -> Unit,
    onMoveBooks: (Int) -> Unit
) {
    when (deleteState) {
        is DeleteKategoriState.HasBorrowedBooks -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = cardShape,
                title = { Text(stringResource(R.string.dialog_konfirmasi_hapus)) },
                text = { Text(stringResource(R.string.msg_gagal_hapus_dipinjam)) },
                confirmButton = { TextButton(onDismiss) { Text("OK") } }
            )
        }
        is DeleteKategoriState.NeedConfirmation -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = cardShape,
                title = { Text(stringResource(R.string.dialog_pilih_aksi)) },
                text = { Text("Kategori ini memiliki ${deleteState.bookCount} buku. Pilih aksi:") },
                confirmButton = {
                    Column {
                        TextButton({ onSoftDelete(deleteState.kategoriId) }) {
                            Text(stringResource(R.string.dialog_aksi_soft_delete))
                        }
                        TextButton({ onMoveBooks(deleteState.kategoriId) }) {
                            Text(stringResource(R.string.dialog_aksi_pindah_kategori))
                        }
                    }
                },
                dismissButton = { TextButton(onDismiss) { Text(stringResource(R.string.btn_batal)) } }
            )
        }
        is DeleteKategoriState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                shape = cardShape,
                title = { Text("Error") },
                text = { Text(deleteState.message) },
                confirmButton = { TextButton(onDismiss) { Text("OK") } }
            )
        }
        else -> {}
    }
}

@Composable
private fun KategoriBody(
    state: KategoriUiState,
    onNameChange: (String) -> Unit,
    onParentChange: (Int?) -> Unit,
    onSave: () -> Unit,
    onEdit: (Kategori) -> Unit,
    onDelete: (Kategori) -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        KategoriForm(
            name = state.inputNamaKategori,
            parentId = state.selectedParentId,
            options = state.kategoriList,
            editingId = state.editingKategoriId,
            nameError = state.isNamaError,
            nameErrorMsg = state.namaErrorMessage,
            cyclicError = state.cyclicErrorMessage,
            saving = state.isSaving,
            onNameChange = onNameChange,
            onParentChange = onParentChange,
            onSave = onSave,
            onCancel = onCancelEdit
        )

        Spacer(Modifier.height(20.dp))

        when {
            state.isLoading -> LoadingBox()
            state.kategoriList.isEmpty() -> EmptyKategoriState()
            else -> KategoriList(state.kategoriList, onEdit, onDelete)
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyKategoriState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Folder, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.msg_kategori_kosong), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun KategoriList(
    items: List<Kategori>,
    onEdit: (Kategori) -> Unit,
    onDelete: (Kategori) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { it.id }) { kat ->
            val parent = items.find { it.id == kat.parentId }
            KategoriCard(kat, parent, { onEdit(kat) }, { onDelete(kat) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriForm(
    name: String,
    parentId: Int?,
    options: List<Kategori>,
    editingId: Int?,
    nameError: Boolean,
    nameErrorMsg: String,
    cyclicError: String,
    saving: Boolean,
    onNameChange: (String) -> Unit,
    onParentChange: (Int?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedParent = options.find { it.id == parentId }
    val parentOptions = if (editingId != null) options.filter { it.id != editingId } else options

    Card(Modifier.fillMaxWidth(), shape = cardShape, elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                if (editingId == null) stringResource(R.string.btn_tambah_kategori) else "Edit Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.label_nama_kategori)) },
                isError = nameError,
                supportingText = { if (nameError) Text(nameErrorMsg) },
                singleLine = true,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded, { expanded = it }) {
                OutlinedTextField(
                    value = selectedParent?.namaKategori ?: stringResource(R.string.tidak_ada_parent),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_parent_kategori)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    isError = cyclicError.isNotEmpty(),
                    supportingText = { if (cyclicError.isNotEmpty()) Text(cyclicError, color = MaterialTheme.colorScheme.error) },
                    shape = fieldShape,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.tidak_ada_parent)) },
                        onClick = { onParentChange(null); expanded = false }
                    )
                    parentOptions.forEach { kat ->
                        DropdownMenuItem(
                            text = { Text(kat.namaKategori) },
                            onClick = { onParentChange(kat.id); expanded = false }
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (editingId != null) {
                    OutlinedButton(onClick = onCancel, shape = fieldShape, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.btn_batal))
                    }
                }
                Button(
                    onClick = onSave,
                    enabled = !saving && cyclicError.isEmpty(),
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (saving) stringResource(R.string.loading) else stringResource(R.string.btn_simpan))
                }
            }
        }
    }
}

@Composable
private fun KategoriCard(
    kategori: Kategori,
    parent: Kategori?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier.fillMaxWidth(),
        shape = fieldShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Folder, null, Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    kategori.namaKategori,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (parent != null) {
                    Text(
                        "Parent: ${parent.namaKategori}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, stringResource(R.string.btn_edit), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, stringResource(R.string.btn_hapus), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
