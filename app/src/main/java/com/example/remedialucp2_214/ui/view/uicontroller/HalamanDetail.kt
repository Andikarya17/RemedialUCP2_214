package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Eksemplar
import com.example.remedialucp2_214.ui.view.route.DestinasiDetailBuku
import com.example.remedialucp2_214.ui.view.viewmodel.DetailUiState
import com.example.remedialucp2_214.ui.view.viewmodel.DetailViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

private val cardShape = RoundedCornerShape(16.dp)
private val fieldShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanDetail(
    bukuId: Int,
    navigateBack: () -> Unit,
    navigateToEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(bukuId) { viewModel.loadBuku(bukuId) }
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) navigateBack() }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            onConfirm = { showDeleteConfirm = false; viewModel.deleteBuku() },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Scaffold(
        topBar = {
            AppBar(
                titleRes = DestinasiDetailBuku.titleRes,
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen(Modifier.padding(padding).fillMaxSize())
            state.bukuWithKategori == null -> EmptyScreen(Modifier.padding(padding).fillMaxSize())
            else -> ContentScreen(
                state = state,
                onDelete = { showDeleteConfirm = true },
                onJudulChange = viewModel::updateEditJudul,
                onKategoriChange = viewModel::updateEditKategori,
                onPengarangToggle = viewModel::togglePengarang,
                onNewAuthorChange = viewModel::updateNewPengarangName,
                onAddAuthor = viewModel::addNewPengarang,
                onSave = viewModel::updateBuku,
                onNewKodeChange = viewModel::updateNewEksemplarKode,
                onAddEksemplar = viewModel::addEksemplar,
                onBorrow = viewModel::pinjamEksemplar,
                onReturn = viewModel::kembalikanEksemplar,
                onDeleteEksemplar = viewModel::deleteEksemplar,
                modifier = Modifier.padding(padding).fillMaxSize()
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyScreen(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.msg_data_kosong))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContentScreen(
    state: DetailUiState,
    onDelete: () -> Unit,
    onJudulChange: (String) -> Unit,
    onKategoriChange: (Int?) -> Unit,
    onPengarangToggle: (Int) -> Unit,
    onNewAuthorChange: (String) -> Unit,
    onAddAuthor: () -> Unit,
    onSave: () -> Unit,
    onNewKodeChange: (String) -> Unit,
    onAddEksemplar: () -> Unit,
    onBorrow: (Int) -> Unit,
    onReturn: (Int) -> Unit,
    onDeleteEksemplar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EditCard(state, onJudulChange, onKategoriChange, onPengarangToggle, onNewAuthorChange, onAddAuthor) }
        item { ButtonRow(onSave, onDelete, state.isSaving) }
        item { EksemplarCard(state.eksemplarList, state.newEksemplarKode, onNewKodeChange, onAddEksemplar, onBorrow, onReturn, onDeleteEksemplar) }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun EditCard(
    state: DetailUiState,
    onJudulChange: (String) -> Unit,
    onKategoriChange: (Int?) -> Unit,
    onPengarangToggle: (Int) -> Unit,
    onNewAuthorChange: (String) -> Unit,
    onAddAuthor: () -> Unit
) {
    var dropdownOpen by remember { mutableStateOf(false) }
    val kategoriLabel = state.kategoriList.find { it.id == state.editKategoriId }?.namaKategori ?: ""

    Card(Modifier.fillMaxWidth(), shape = cardShape, elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = fieldShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.MenuBook, null, Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Text("Edit Buku", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // Judul
            OutlinedTextField(
                value = state.editJudul,
                onValueChange = onJudulChange,
                label = { Text(stringResource(R.string.label_judul)) },
                isError = state.isJudulError,
                supportingText = { if (state.isJudulError) Text(state.judulErrorMessage) },
                singleLine = true,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth()
            )

            // Kategori dropdown
            ExposedDropdownMenuBox(expanded = dropdownOpen, onExpandedChange = { dropdownOpen = it }) {
                OutlinedTextField(
                    value = kategoriLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_kategori)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownOpen) },
                    isError = state.isKategoriError,
                    supportingText = { if (state.isKategoriError) Text(state.kategoriErrorMessage) },
                    shape = fieldShape,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                    state.kategoriList.forEach { kat ->
                        DropdownMenuItem(
                            text = { Text(kat.namaKategori) },
                            onClick = { onKategoriChange(kat.id); dropdownOpen = false }
                        )
                    }
                }
            }

            // Pengarang
            Column {
                Text("Pengarang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.newPengarangName,
                        onValueChange = onNewAuthorChange,
                        label = { Text("Nama pengarang baru") },
                        singleLine = true,
                        shape = fieldShape,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onAddAuthor) {
                        Icon(Icons.Default.Add, "Tambah", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (state.pengarangList.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Pilih pengarang:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.pengarangList.forEach { author ->
                            val selected = author.id in state.selectedPengarangIds
                            FilterChip(
                                selected = selected,
                                onClick = { onPengarangToggle(author.id) },
                                label = { Text(author.nama) },
                                leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(16.dp)) },
                                trailingIcon = if (selected) { { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) } } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonRow(onSave: () -> Unit, onDelete: () -> Unit, saving: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onSave, enabled = !saving, shape = fieldShape, modifier = Modifier.weight(1f)) {
            Text(if (saving) stringResource(R.string.loading) else stringResource(R.string.btn_simpan))
        }
        Button(
            onClick = onDelete,
            shape = fieldShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Delete, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.btn_hapus))
        }
    }
}

@Composable
private fun EksemplarCard(
    items: List<Eksemplar>,
    newKode: String,
    onKodeChange: (String) -> Unit,
    onAdd: () -> Unit,
    onBorrow: (Int) -> Unit,
    onReturn: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    Card(Modifier.fillMaxWidth(), shape = cardShape, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Eksemplar Fisik (${items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newKode,
                    onValueChange = onKodeChange,
                    label = { Text("Kode Eksemplar") },
                    singleLine = true,
                    shape = fieldShape,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, "Tambah", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))

            items.forEach { item ->
                EksemplarRow(item, onBorrow, onReturn, onDelete)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EksemplarRow(
    item: Eksemplar,
    onBorrow: (Int) -> Unit,
    onReturn: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    val available = item.status == Eksemplar.STATUS_TERSEDIA

    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.kodeEksemplar, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = if (available) "Tersedia" else "Dipinjam",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            TextButton(onClick = { if (available) onBorrow(item.id) else onReturn(item.id) }) {
                Text(if (available) "Pinjam" else "Kembalikan")
            }
            IconButton(onClick = { onDelete(item.id) }) {
                Icon(Icons.Default.Delete, "Hapus", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = cardShape,
        title = { Text(stringResource(R.string.dialog_konfirmasi_hapus)) },
        text = { Text(stringResource(R.string.dialog_hapus_buku_msg)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_hapus), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_batal))
            }
        }
    )
}
