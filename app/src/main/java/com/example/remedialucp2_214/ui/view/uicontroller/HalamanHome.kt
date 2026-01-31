package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.ui.view.route.DestinasiHome
import com.example.remedialucp2_214.ui.view.viewmodel.HomeUiState
import com.example.remedialucp2_214.ui.view.viewmodel.HomeViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanHome(
    navigateToEntry: () -> Unit,
    navigateToDetail: (Int) -> Unit,
    navigateToKategori: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { AppBar(DestinasiHome.titleRes, canNavigateBack = false, scrollBehavior = scroll) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = navigateToKategori, containerColor = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(Icons.Default.Settings, stringResource(R.string.btn_kelola_kategori))
                }
                FloatingActionButton(onClick = navigateToEntry, containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(Icons.Default.Add, stringResource(R.string.btn_tambah_buku))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        HomeBody(state, viewModel::setFilterKategori, navigateToDetail, viewModel::deleteBuku, Modifier.padding(padding).fillMaxSize())
    }
}

@Composable
private fun HomeBody(
    state: HomeUiState, onFilter: (Int?) -> Unit, onClick: (Int) -> Unit, onDelete: (Int) -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        FilterDropdown(state.kategoriList, state.selectedKategoriId, onFilter)
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.bukuList.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MenuBook, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.msg_buku_kosong), color = MaterialTheme.colorScheme.outline)
                }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.bukuList, key = { it.buku.id }) { item ->
                    val (total, borrowed) = state.eksemplarCounts[item.buku.id] ?: (0 to 0)
                    BookRow(item, total - borrowed, borrowed, { onClick(item.buku.id) }, { onDelete(item.buku.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(options: List<Kategori>, selected: Int?, onSelect: (Int?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.id == selected }?.namaKategori ?: stringResource(R.string.filter_semua_kategori)

    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = label, onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.label_kategori)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.filter_semua_kategori)) }, onClick = { onSelect(null); expanded = false })
            options.forEach { kat ->
                DropdownMenuItem(text = { Text(kat.namaKategori) }, onClick = { onSelect(kat.id); expanded = false })
            }
        }
    }
}

@Composable
private fun BookRow(
    item: BukuWithKategori, available: Int, borrowed: Int, onClick: () -> Unit, onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.MenuBook, null, Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.buku.judul, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(item.kategori?.namaKategori ?: "-", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("$available Tersedia", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    if (borrowed > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Text("$borrowed Dipinjam", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, stringResource(R.string.btn_hapus), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
