package com.example.ingilizcekelimekasasi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ingilizcekelimekasasi.ui.theme.IngilizceKelimeKasasiTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Uygulamanın ana Activity'si.
 *
 * `@AndroidEntryPoint` anotasyonu, Hilt'in bu Activity'ye ve
 * içindeki Composable ağacına bağımlılık enjekte edebilmesini sağlar.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IngilizceKelimeKasasiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IngilizceKelimeKasasiTheme {
        Greeting("Android")
    }
}