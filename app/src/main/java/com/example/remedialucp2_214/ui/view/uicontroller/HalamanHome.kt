package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.Kategori
import com.example.remedialucp2_214.ui.view.route.DestinasiHome
import com.example.remedialucp2_214.ui.view.viewmodel.HomeUiState
import com.example.remedialucp2_214.ui.view.viewmodel.HomeViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

/**
 * Halaman Home - menampilkan daftar buku dengan filter kategori
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanHome(
    navigateToEntry: () -> Unit,
    navigateToDetail: (Int) -> Unit,
    navigateToKategori: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppBar(
                titleRes = DestinasiHome.titleRes,
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = navigateToKategori,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.btn_kelola_kategori)
                    )
                }
                FloatingActionButton(
                    onClick = navigateToEntry
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.btn_tambah_buku)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            onKategoriSelected = { viewModel.setFilterKategori(it) },
            onBukuClick = navigateToDetail,
            onDeleteClick = { viewModel.deleteBuku(it) },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

/**
 * Konten utama halaman home
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onKategoriSelected: (Int?) -> Unit,
    onBukuClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.padding_medium))
    ) {
        // Filter dropdown
        KategoriFilterDropdown(
            kategoriList = uiState.kategoriList,
            selectedKategoriId = uiState.selectedKategoriId,
            onKategoriSelected = onKategoriSelected,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

        // Book list
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.bukuList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.msg_buku_kosong),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                items(
                    items = uiState.bukuList,
                    key = { it.buku.id }
                ) { bukuWithKategori ->
                    BukuCard(
                        bukuWithKategori = bukuWithKategori,
                        onClick = { onBukuClick(bukuWithKategori.buku.id) },
                        onDeleteClick = { onDeleteClick(bukuWithKategori.buku.id) }
                    )
                }
            }
        }
    }
}

/**
 * Dropdown filter kategori dengan recursive search explanation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KategoriFilterDropdown(
    kategoriList: List<Kategori>,
    selectedKategoriId: Int?,
    onKategoriSelected: (Int?) -> Unit,
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
            value = selectedKategori?.namaKategori ?: stringResource(R.string.filter_semua_kategori),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_kategori)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Option untuk semua kategori
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_semua_kategori)) },
                onClick = {
                    onKategoriSelected(null)
                    expanded = false
                }
            )

            // Kategori options
            kategoriList.forEach { kategori ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = buildKategoriLabel(kategori, kategoriList)
                        )
                    },
                    onClick = {
                        onKategoriSelected(kategori.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Build label kategori dengan indentasi berdasarkan level hierarki
 */
private fun buildKategoriLabel(kategori: Kategori, allKategori: List<Kategori>): String {
    var level = 0
    var currentParentId = kategori.parentId
    
    while (currentParentId != null && level < 10) {
        val parent = allKategori.find { it.id == currentParentId }
        if (parent != null) {
            level++
            currentParentId = parent.parentId
        } else {
            break
        }
    }
    
    val indent = "  ".repeat(level)
    val prefix = if (level > 0) "└─ " else ""
    return "$indent$prefix${kategori.namaKategori}"
}

/**
 * Card untuk menampilkan buku
 */
@Composable
private fun BukuCard(
    bukuWithKategori: BukuWithKategori,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buku = bukuWithKategori.buku
    val kategori = bukuWithKategori.kategori

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = buku.judul,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    StatusChip(status = buku.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = kategori?.namaKategori ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

/**
 * Status chip untuk menampilkan status buku
 */
@Composable
private fun StatusChip(status: String) {
    val (text, color) = when (status) {
        Buku.STATUS_TERSEDIA -> stringResource(R.string.status_tersedia) to MaterialTheme.colorScheme.primary
        Buku.STATUS_DIPINJAM -> stringResource(R.string.status_dipinjam) to MaterialTheme.colorScheme.error
        else -> status to MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.SemiBold
    )
}
