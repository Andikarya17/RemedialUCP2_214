package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.ui.view.route.DestinasiEntry
import com.example.remedialucp2_214.ui.view.viewmodel.EntryUiState
import com.example.remedialucp2_214.ui.view.viewmodel.EntryViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

private val cardShape = RoundedCornerShape(16.dp)
private val fieldShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanEntry(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navigateBack()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                titleRes = DestinasiEntry.titleRes,
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        FormContent(
            state = state,
            onJudulChange = viewModel::updateJudul,
            onKategoriChange = viewModel::updateKategori,
            onJumlahChange = viewModel::updateJumlahEksemplar,
            onPengarangToggle = viewModel::togglePengarang,
            onNewAuthorChange = viewModel::updateNewPengarangName,
            onAddAuthor = viewModel::addNewPengarang,
            onSave = viewModel::saveBuku,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormContent(
    state: EntryUiState,
    onJudulChange: (String) -> Unit,
    onKategoriChange: (Int?) -> Unit,
    onJumlahChange: (String) -> Unit,
    onPengarangToggle: (Int) -> Unit,
    onNewAuthorChange: (String) -> Unit,
    onAddAuthor: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        Card(
            shape = cardShape,
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_tambah_buku),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Judul input
                OutlinedTextField(
                    value = state.judul,
                    onValueChange = onJudulChange,
                    label = { Text(stringResource(R.string.label_judul)) },
                    isError = state.isJudulError,
                    supportingText = { if (state.isJudulError) Text(state.judulErrorMessage) },
                    singleLine = true,
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth()
                )

                // Kategori dropdown
                KategoriPicker(
                    options = state.kategoriList,
                    selected = state.selectedKategoriId,
                    onSelect = onKategoriChange,
                    isError = state.isKategoriError,
                    errorText = state.kategoriErrorMessage
                )

                // Jumlah eksemplar
                OutlinedTextField(
                    value = state.jumlahEksemplar,
                    onValueChange = { if (it.all(Char::isDigit)) onJumlahChange(it) },
                    label = { Text("Jumlah Buku") },
                    isError = state.isJumlahError,
                    supportingText = {
                        Text(
                            if (state.isJumlahError) state.jumlahErrorMessage
                            else "Jumlah eksemplar fisik yang akan dibuat"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = fieldShape,
                    modifier = Modifier.fillMaxWidth()
                )

                // Pengarang section
                AuthorSection(
                    authors = state.pengarangList,
                    selectedIds = state.selectedPengarangIds,
                    newAuthorName = state.newPengarangName,
                    onNewAuthorChange = onNewAuthorChange,
                    onAddAuthor = onAddAuthor,
                    onToggle = onPengarangToggle
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            shape = fieldShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (state.isSaving) stringResource(R.string.loading)
                else stringResource(R.string.btn_simpan)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthorSection(
    authors: List<com.example.remedialucp2_214.room.Pengarang>,
    selectedIds: Set<Int>,
    newAuthorName: String,
    onNewAuthorChange: (String) -> Unit,
    onAddAuthor: () -> Unit,
    onToggle: (Int) -> Unit
) {
    Column {
        Text(
            text = "Pengarang",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newAuthorName,
                onValueChange = onNewAuthorChange,
                label = { Text("Nama pengarang baru") },
                singleLine = true,
                shape = fieldShape,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onAddAuthor) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah pengarang",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (authors.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Pilih pengarang:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                authors.forEach { author ->
                    val selected = author.id in selectedIds
                    FilterChip(
                        selected = selected,
                        onClick = { onToggle(author.id) },
                        label = { Text(author.nama) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, Modifier.size(16.dp))
                        },
                        trailingIcon = if (selected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriPicker(
    options: List<Kategori>,
    selected: Int?,
    onSelect: (Int?) -> Unit,
    isError: Boolean,
    errorText: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.id == selected }?.namaKategori ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_kategori)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = isError,
            supportingText = { if (isError) Text(errorText) },
            shape = fieldShape,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { kategori ->
                DropdownMenuItem(
                    text = { Text(kategori.namaKategori) },
                    onClick = {
                        onSelect(kategori.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
