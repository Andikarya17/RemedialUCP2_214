package com.example.remedialucp2_214.ui.view.uicontroller

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.remedialucp2_214.ui.view.route.DestinasiDetailBuku
import com.example.remedialucp2_214.ui.view.route.DestinasiEditBuku
import com.example.remedialucp2_214.ui.view.route.DestinasiEntry
import com.example.remedialucp2_214.ui.view.route.DestinasiHome
import com.example.remedialucp2_214.ui.view.route.DestinasiKategori

/**
 * Navigation graph untuk aplikasi perpustakaan.
 * Mengatur semua route dan navigasi antar halaman.
 */
@Composable
fun PetaNavigasi(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DestinasiHome.route,
        modifier = modifier
    ) {
        // Home - Daftar Buku
        composable(route = DestinasiHome.route) {
            HalamanHome(
                navigateToEntry = {
                    navController.navigate(DestinasiEntry.route)
                },
                navigateToDetail = { bukuId ->
                    navController.navigate(DestinasiDetailBuku.createRoute(bukuId))
                },
                navigateToKategori = {
                    navController.navigate(DestinasiKategori.route)
                }
            )
        }

        // Entry - Tambah Buku Baru
        composable(route = DestinasiEntry.route) {
            HalamanEntry(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Detail Buku
        composable(
            route = DestinasiDetailBuku.route,
            arguments = listOf(
                navArgument(DestinasiDetailBuku.BUKU_ID_ARG) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val bukuId = backStackEntry.arguments?.getInt(DestinasiDetailBuku.BUKU_ID_ARG) ?: 0
            HalamanDetail(
                bukuId = bukuId,
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToEdit = { id ->
                    navController.navigate(DestinasiEditBuku.createRoute(id))
                }
            )
        }

        // Edit Buku
        composable(
            route = DestinasiEditBuku.route,
            arguments = listOf(
                navArgument(DestinasiEditBuku.BUKU_ID_ARG) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val bukuId = backStackEntry.arguments?.getInt(DestinasiEditBuku.BUKU_ID_ARG) ?: 0
            HalamanEdit(
                bukuId = bukuId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Kelola Kategori
        composable(route = DestinasiKategori.route) {
            HalamanKategori(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
