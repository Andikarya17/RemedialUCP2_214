package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.remedialucp2_214.R

/**
 * Reusable TopAppBar component dengan dukungan navigasi kembali.
 *
 * @param title Judul yang ditampilkan
 * @param canNavigateBack Apakah menampilkan tombol back
 * @param navigateUp Callback saat tombol back ditekan
 * @param scrollBehavior Scroll behavior untuk collapse effect
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.kembali)
                    )
                }
            }
        }
    )
}

/**
 * Overload dengan titleRes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    titleRes: Int,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    AppBar(
        title = stringResource(titleRes),
        canNavigateBack = canNavigateBack,
        modifier = modifier,
        navigateUp = navigateUp,
        scrollBehavior = scrollBehavior
    )
}
