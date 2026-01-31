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

object ViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                repositoriBuku = aplikasiBuku().container.repositoriBuku,
                repositoriKategori = aplikasiBuku().container.repositoriKategori,
                repositoriEksemplar = aplikasiBuku().container.repositoriEksemplar
            )
        }

        initializer {
            EntryViewModel(
                repositoriBuku = aplikasiBuku().container.repositoriBuku,
                repositoriKategori = aplikasiBuku().container.repositoriKategori,
                repositoriPengarang = aplikasiBuku().container.repositoriPengarang,
                repositoriEksemplar = aplikasiBuku().container.repositoriEksemplar
            )
        }

        initializer {
            DetailViewModel(
                repositoriBuku = aplikasiBuku().container.repositoriBuku,
                repositoriKategori = aplikasiBuku().container.repositoriKategori,
                repositoriEksemplar = aplikasiBuku().container.repositoriEksemplar,
                repositoriPengarang = aplikasiBuku().container.repositoriPengarang
            )
        }

        initializer {
            KategoriViewModel(
                repositoriKategori = aplikasiBuku().container.repositoriKategori,
                repositoriBuku = aplikasiBuku().container.repositoriBuku
            )
        }
    }
}

fun CreationExtras.aplikasiBuku(): AplikasiBuku =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplikasiBuku)
