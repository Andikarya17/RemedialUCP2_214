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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.room.Eksemplar
import com.example.remedialucp2_214.room.Pengarang
import com.example.remedialucp2_214.ui.view.route.DestinasiDetailBuku
import com.example.remedialucp2_214.ui.view.viewmodel.DetailUiState
import com.example.remedialucp2_214.ui.view.viewmodel.DetailViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalamanDetail(
    bukuId: Int,
    navigateBack: () -> Unit,
    navigateToEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bukuId) { viewModel.loadBuku(bukuId) }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) navigateBack() }
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = { showDeleteDialog = false; viewModel.deleteBuku() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = { AppBar(titleRes = DestinasiDetailBuku.titleRes, canNavigateBack = true, navigateUp = navigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        DetailContent(
            uiState = uiState,
            onEditClick = { navigateToEdit(bukuId) },
            onDeleteClick = { showDeleteDialog = true },
            onAddEksemplar = viewModel::addEksemplar,
            onNewKodeChange = viewModel::updateNewEksemplarKode,
            onPinjam = viewModel::pinjamEksemplar,
            onKembalikan = viewModel::kembalikanEksemplar,
            onDeleteEksemplar = viewModel::deleteEksemplar,
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        )
    }
}

@Composable
private fun DetailContent(
    uiState: DetailUiState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddEksemplar: () -> Unit,
    onNewKodeChange: (String) -> Unit,
    onPinjam: (Int) -> Unit,
    onKembalikan: (Int) -> Unit,
    onDeleteEksemplar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        uiState.bukuWithKategori == null -> Box(modifier, contentAlignment = Alignment.Center) { Text(stringResource(R.string.msg_data_kosong)) }
        else -> {
            LazyColumn(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { BukuDetailCard(uiState.bukuWithKategori, uiState.bukuPengarangList) }
                item { ActionButtons(onEditClick, onDeleteClick) }
                item {
                    EksemplarSection(
                        eksemplarList = uiState.eksemplarList,
                        newKode = uiState.newEksemplarKode,
                        onNewKodeChange = onNewKodeChange,
                        onAddClick = onAddEksemplar,
                        onPinjam = onPinjam,
                        onKembalikan = onKembalikan,
                        onDelete = onDeleteEksemplar
                    )
                }
                if (uiState.bukuWithKategori.buku.auditLogBefore.isNotEmpty()) {
                    item { AuditLogCard(uiState.bukuWithKategori.buku) }
                }
            }
        }
    }
}

@Composable
private fun BukuDetailCard(bukuWithKategori: BukuWithKategori, pengarangList: List<Pengarang>) {
    val buku = bukuWithKategori.buku
    val kategori = bukuWithKategori.kategori

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.padding(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(buku.judul, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(kategori?.namaKategori ?: "-", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (pengarangList.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Pengarang:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pengarangList) { p ->
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(Modifier.width(4.dp))
                                Text(p.nama, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onEditClick, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Edit, contentDescription = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.btn_edit))
        }
        Button(onClick = onDeleteClick, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Delete, contentDescription = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.btn_hapus))
        }
    }
}

@Composable
private fun EksemplarSection(
    eksemplarList: List<Eksemplar>,
    newKode: String,
    onNewKodeChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onPinjam: (Int) -> Unit,
    onKembalikan: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Eksemplar Fisik (${eksemplarList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newKode,
                    onValueChange = onNewKodeChange,
                    label = { Text("Kode Eksemplar") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))
            eksemplarList.forEach { eks ->
                EksemplarItem(eks, onPinjam = { onPinjam(eks.id) }, onKembalikan = { onKembalikan(eks.id) }, onDelete = { onDelete(eks.id) })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EksemplarItem(eksemplar: Eksemplar, onPinjam: () -> Unit, onKembalikan: () -> Unit, onDelete: () -> Unit) {
    val isTersedia = eksemplar.status == Eksemplar.STATUS_TERSEDIA
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(eksemplar.kodeEksemplar, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(if (isTersedia) "Tersedia" else "Dipinjam", style = MaterialTheme.typography.labelSmall, color = if (isTersedia) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            if (isTersedia) {
                TextButton(onClick = onPinjam) { Text("Pinjam") }
            } else {
                TextButton(onClick = onKembalikan) { Text("Kembalikan") }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AuditLogCard(buku: Buku) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Text("Audit Log", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Before:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(buku.auditLogBefore, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Text("After:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(buku.auditLogAfter, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text(stringResource(R.string.dialog_konfirmasi_hapus)) },
        text = { Text(stringResource(R.string.dialog_hapus_buku_msg)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.btn_hapus), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_batal)) } }
    )
}
