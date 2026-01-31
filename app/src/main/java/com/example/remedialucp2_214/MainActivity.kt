package com.example.remedialucp2_214

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.remedialucp2_214.ui.theme.RemedialUCP2_214Theme
import com.example.remedialucp2_214.ui.view.uicontroller.PetaNavigasi

/**
 * MainActivity - entry point aplikasi
 * Hanya bertanggung jawab untuk setup Compose dan theme
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemedialUCP2_214Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PetaNavigasi()
                }
            }
        }
    }
}