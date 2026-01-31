package com.example.remedialucp2_214.ui.view.viewmodel.provider

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.remedialucp2_214.AplikasiBuku
import com.example.remedialucp2_214.ui.view.viewmodel.DetailViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.EntryViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.HomeViewModel
import com.example.remedialucp2_214.ui.view.viewmodel.KategoriViewModel

/**
 * Factory untuk membuat ViewModel dengan dependency injection manual.
 * Menggunakan ViewModelProvider.Factory untuk inject repository.
 */
object ViewModelProvider {

    /**
     * Factory yang menyediakan semua ViewModel dengan repository
     */
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        // HomeViewModel
        initializer {
            HomeViewModel(
                repositoriBuku = aplikasiBuku().container.repositoriBuku,
                repositoriKategori = aplikasiBuku().container.repositoriKategori
            )
        }

        // EntryViewModel
        initializer {
            EntryViewModel(
                repositoriBuku = aplikasiBuku().container.repositoriBuku,
                repositoriKategori = aplikasiBuku().container.repositoriKategori
            )
        }

        // DetailViewModel
        initializer {
            DetailViewModel(
                repositoriBuku = aplikasiBuku().container.repositoriBuku,
                repositoriKategori = aplikasiBuku().container.repositoriKategori
            )
        }

        // KategoriViewModel
        initializer {
            KategoriViewModel(
                repositoriKategori = aplikasiBuku().container.repositoriKategori,
                repositoriBuku = aplikasiBuku().container.repositoriBuku
            )
        }
    }
}

/**
 * Extension function untuk mendapatkan instance AplikasiBuku dari CreationExtras
 */
fun CreationExtras.aplikasiBuku(): AplikasiBuku =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiBuku)
