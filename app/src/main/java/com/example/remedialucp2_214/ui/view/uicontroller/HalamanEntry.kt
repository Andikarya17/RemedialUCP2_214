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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanEntry(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.isSaved) { if (state.isSaved) navigateBack() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = { AppBar(DestinasiEntry.titleRes, canNavigateBack = true, navigateUp = navigateBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        FormBody(
            state = state,
            onJudulChange = viewModel::updateJudul,
            onKategoriChange = viewModel::updateKategori,
            onJumlahChange = viewModel::updateJumlahEksemplar,
            onPengarangToggle = viewModel::togglePengarang,
            onNewAuthorChange = viewModel::updateNewPengarangName,
            onAddAuthor = viewModel::addNewPengarang,
            onSave = viewModel::saveBuku,
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FormBody(
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
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.btn_tambah_buku), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = state.judul, onValueChange = onJudulChange,
                    label = { Text(stringResource(R.string.label_judul)) },
                    isError = state.isJudulError,
                    supportingText = { if (state.isJudulError) Text(state.judulErrorMessage) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                KategoriPicker(state.kategoriList, state.selectedKategoriId, onKategoriChange, state.isKategoriError, state.kategoriErrorMessage)

                OutlinedTextField(
                    value = state.jumlahEksemplar,
                    onValueChange = { if (it.all(Char::isDigit)) onJumlahChange(it) },
                    label = { Text("Jumlah Buku") },
                    isError = state.isJumlahError,
                    supportingText = { Text(if (state.isJumlahError) state.jumlahErrorMessage else "Jumlah eksemplar fisik") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Pengarang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.newPengarangName, onValueChange = onNewAuthorChange,
                            label = { Text("Nama pengarang baru") },
                            singleLine = true, shape = RoundedCornerShape(12.dp),
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
                                    selected = selected, onClick = { onPengarangToggle(author.id) },
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

        Spacer(Modifier.height(24.dp))

        Button(onClick = onSave, enabled = !state.isSaving, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text(if (state.isSaving) stringResource(R.string.loading) else stringResource(R.string.btn_simpan))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriPicker(
    options: List<Kategori>, selected: Int?, onSelect: (Int?), isError: Boolean, errorText: String
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.id == selected }?.namaKategori ?: ""

    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = label, onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.label_kategori)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = isError, supportingText = { if (isError) Text(errorText) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { kat ->
                DropdownMenuItem(text = { Text(kat.namaKategori) }, onClick = { onSelect(kat.id); expanded = false })
            }
        }
    }
}
