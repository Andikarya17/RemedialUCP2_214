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

private val cardShape = RoundedCornerShape(16.dp)
private val fieldShape = RoundedCornerShape(12.dp)

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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppBar(
                titleRes = DestinasiHome.titleRes,
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = { FabColumn(navigateToKategori, navigateToEntry) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        HomeBody(
            state = state,
            onKategoriSelect = viewModel::setFilterKategori,
            onBukuClick = navigateToDetail,
            onDelete = viewModel::deleteBuku,
            modifier = Modifier.padding(padding).fillMaxSize()
        )
    }
}

@Composable
private fun FabColumn(onKategori: () -> Unit, onAdd: () -> Unit) {
    Column(horizontalAlignment = Alignment.End) {
        FloatingActionButton(
            onClick = onKategori,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.Settings, stringResource(R.string.btn_kelola_kategori))
        }
        FloatingActionButton(
            onClick = onAdd,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(Icons.Default.Add, stringResource(R.string.btn_tambah_buku))
        }
    }
}

@Composable
private fun HomeBody(
    state: HomeUiState,
    onKategoriSelect: (Int?) -> Unit,
    onBukuClick: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        FilterDropdown(
            options = state.kategoriList,
            selected = state.selectedKategoriId,
            onSelect = onKategoriSelect
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> LoadingBox()
            state.bukuList.isEmpty() -> EmptyBukuState()
            else -> BookList(state.bukuList, state.eksemplarCounts, onBukuClick, onDelete)
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
private fun EmptyBukuState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.MenuBook,
                null,
                Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.msg_buku_kosong),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun BookList(
    books: List<BukuWithKategori>,
    counts: Map<Int, Pair<Int, Int>>,
    onClick: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(books, key = { it.buku.id }) { item ->
            val (total, borrowed) = counts[item.buku.id] ?: (0 to 0)
            BookCard(item, total, borrowed, { onClick(item.buku.id) }, { onDelete(item.buku.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    options: List<Kategori>,
    selected: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.id == selected }?.namaKategori
        ?: stringResource(R.string.filter_semua_kategori)

    ExposedDropdownMenuBox(expanded, { expanded = it }, modifier) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_kategori)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = fieldShape,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_semua_kategori)) },
                onClick = { onSelect(null); expanded = false }
            )
            options.forEach { kat ->
                DropdownMenuItem(
                    text = { Text(kat.namaKategori) },
                    onClick = { onSelect(kat.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun BookCard(
    item: BukuWithKategori,
    total: Int,
    borrowed: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val available = total - borrowed

    Card(
        modifier = modifier.fillMaxWidth().clip(cardShape).clickable(onClick = onClick),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Surface(shape = fieldShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.MenuBook, null, Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Spacer(Modifier.width(16.dp))

            // Info
            Column(Modifier.weight(1f)) {
                Text(
                    item.buku.judul,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.kategori?.namaKategori ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("$available Tersedia", primary = true)
                    if (borrowed > 0) StatusChip("$borrowed Dipinjam", primary = false)
                }
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, stringResource(R.string.btn_hapus), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, primary: Boolean) {
    val bg = if (primary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val fg = if (primary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
