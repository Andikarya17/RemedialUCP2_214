package com.example.remedialucp2_214.ui.view.route

import com.example.remedialucp2_214.R

/**
 * Destinasi untuk halaman Edit Buku dengan parameter bukuId
 */
object DestinasiEditBuku : DestinasiNavigasi {
    override val route = "edit/{bukuId}"
    override val titleRes = R.string.halaman_edit
    const val BUKU_ID_ARG = "bukuId"
    
    /**
     * Helper untuk membuat route dengan bukuId tertentu
     */
    fun createRoute(bukuId: Int): String = "edit/$bukuId"
}
