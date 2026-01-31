package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Buku
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
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) navigateBack() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = { AppBar(titleRes = DestinasiEntry.titleRes, canNavigateBack = true, navigateUp = navigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        EntryForm(
            uiState = uiState,
            onJudulChange = viewModel::updateJudul,
            onStatusChange = viewModel::updateStatus,
            onKategoriChange = viewModel::updateKategori,
            onPengarangToggle = viewModel::togglePengarang,
            onNewPengarangNameChange = viewModel::updateNewPengarangName,
            onAddNewPengarang = viewModel::addNewPengarang,
            onSaveClick = viewModel::saveBuku,
            modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState())
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntryForm(
    uiState: EntryUiState,
    onJudulChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onKategoriChange: (Int?) -> Unit,
    onPengarangToggle: (Int) -> Unit,
    onNewPengarangNameChange: (String) -> Unit,
    onAddNewPengarang: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.btn_tambah_buku), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = uiState.judul,
                    onValueChange = onJudulChange,
                    label = { Text(stringResource(R.string.label_judul)) },
                    isError = uiState.isJudulError,
                    supportingText = { if (uiState.isJudulError) Text(uiState.judulErrorMessage) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(stringResource(R.string.label_status), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    StatusRadioGroup(selectedStatus = uiState.status, onStatusChange = onStatusChange)
                }

                KategoriDropdown(
                    kategoriList = uiState.kategoriList,
                    selectedKategoriId = uiState.selectedKategoriId,
                    onKategoriChange = onKategoriChange,
                    isError = uiState.isKategoriError,
                    errorMessage = uiState.kategoriErrorMessage
                )

                Column {
                    Text("Pengarang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = uiState.newPengarangName,
                            onValueChange = onNewPengarangNameChange,
                            label = { Text("Nama pengarang baru") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onAddNewPengarang) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah pengarang", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (uiState.pengarangList.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Pilih pengarang:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.pengarangList.forEach { p ->
                                val isSelected = uiState.selectedPengarangIds.contains(p.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onPengarangToggle(p.id) },
                                    label = { Text(p.nama) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    trailingIcon = if (isSelected) {{ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onSaveClick, enabled = !uiState.isSaving, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.isSaving) stringResource(R.string.loading) else stringResource(R.string.btn_simpan))
        }
    }
}

@Composable
private fun StatusRadioGroup(selectedStatus: String, onStatusChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf(
        Buku.STATUS_TERSEDIA to stringResource(R.string.status_tersedia),
        Buku.STATUS_DIPINJAM to stringResource(R.string.status_dipinjam)
    )
    Row(modifier = modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        options.forEach { (value, label) ->
            Row(
                modifier = Modifier.selectable(selected = selectedStatus == value, onClick = { onStatusChange(value) }, role = Role.RadioButton),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedStatus == value, onClick = null)
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriDropdown(
    kategoriList: List<Kategori>,
    selectedKategoriId: Int?,
    onKategoriChange: (Int?) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = kategoriList.find { it.id == selectedKategoriId }?.namaKategori ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_kategori)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = isError,
            supportingText = { if (isError) Text(errorMessage) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            kategoriList.forEach { kategori ->
                DropdownMenuItem(
                    text = { Text(kategori.namaKategori) },
                    onClick = { onKategoriChange(kategori.id); expanded = false }
                )
            }
        }
    }
}
