package com.example.remedialucp2_214

import android.app.Application
import com.example.remedialucp2_214.repositori.ContainerApp
import com.example.remedialucp2_214.repositori.ContainerAppImpl

/**
 * Application class untuk inisialisasi dependency injection container.
 * Di-register di AndroidManifest.xml
 */
class AplikasiBuku : Application() {

    /**
     * Container untuk dependency injection
     */
    lateinit var container: ContainerApp

    override fun onCreate() {
        super.onCreate()
        container = ContainerAppImpl(this)
    }
}
