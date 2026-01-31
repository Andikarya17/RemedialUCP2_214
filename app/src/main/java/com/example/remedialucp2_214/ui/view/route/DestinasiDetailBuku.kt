package com.example.remedialucp2_214.ui.view.route

import com.example.remedialucp2_214.R

/**
 * Destinasi untuk halaman Detail Buku dengan parameter bukuId
 */
object DestinasiDetailBuku : DestinasiNavigasi {
    override val route = "detail/{bukuId}"
    override val titleRes = R.string.halaman_detail
    const val BUKU_ID_ARG = "bukuId"
    
    /**
     * Helper untuk membuat route dengan bukuId tertentu
     */
    fun createRoute(bukuId: Int): String = "detail/$bukuId"
}
