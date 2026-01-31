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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remedialucp2_214.R
import com.example.remedialucp2_214.room.Buku
import com.example.remedialucp2_214.room.BukuWithKategori
import com.example.remedialucp2_214.ui.view.route.DestinasiDetailBuku
import com.example.remedialucp2_214.ui.view.viewmodel.DetailUiState
import com.example.remedialucp2_214.ui.view.viewmodel.DetailViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.provider.ViewModelProvider
import androidx.compose.ui.unit.dp

/**
 * Halaman Detail - menampilkan detail buku
 */
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

    // Load buku saat pertama kali
    LaunchedEffect(bukuId) {
        viewModel.loadBuku(bukuId)
    }

    // Navigate back setelah delete
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            navigateBack()
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteBuku()
            },
            onDismiss = { showDeleteDialog = false }
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        DetailContent(
            uiState = uiState,
            onEditClick = { navigateToEdit(bukuId) },
            onDeleteClick = { showDeleteDialog = true },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

/**
 * Konten detail buku
 */
@Composable
private fun DetailContent(
    uiState: DetailUiState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val bukuWithKategori = uiState.bukuWithKategori
    if (bukuWithKategori == null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.msg_data_kosong),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    Column(
        modifier = modifier
            .padding(dimensionResource(R.dimen.padding_medium))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
    ) {
        // Card detail buku
        BukuDetailCard(bukuWithKategori = bukuWithKategori)

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_edit))
            }

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.btn_hapus))
            }
        }

        // Audit log (jika ada)
        if (bukuWithKategori.buku.auditLogBefore.isNotEmpty()) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            AuditLogSection(buku = bukuWithKategori.buku)
        }
    }
}

/**
 * Card menampilkan detail buku
 */
@Composable
private fun BukuDetailCard(
    bukuWithKategori: BukuWithKategori,
    modifier: Modifier = Modifier
) {
    val buku = bukuWithKategori.buku
    val kategori = bukuWithKategori.kategori

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            DetailRow(
                label = stringResource(R.string.label_judul),
                value = buku.judul
            )

            DetailRow(
                label = stringResource(R.string.label_status),
                value = if (buku.status == Buku.STATUS_TERSEDIA) {
                    stringResource(R.string.status_tersedia)
                } else {
                    stringResource(R.string.status_dipinjam)
                },
                valueColor = if (buku.status == Buku.STATUS_TERSEDIA) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            DetailRow(
                label = stringResource(R.string.label_kategori),
                value = kategori?.namaKategori ?: "-"
            )
        }
    }
}

/**
 * Row untuk menampilkan label dan value
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

/**
 * Section untuk menampilkan audit log
 */
@Composable
private fun AuditLogSection(
    buku: Buku,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(
                text = "Audit Log",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Before:",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = buku.auditLogBefore,
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "After:",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = buku.auditLogAfter,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Dialog konfirmasi hapus
 */
@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_konfirmasi_hapus)) },
        text = { Text(stringResource(R.string.dialog_hapus_buku_msg)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.btn_hapus))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_batal))
            }
        }
    )
}
