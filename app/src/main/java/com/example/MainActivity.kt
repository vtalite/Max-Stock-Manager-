package com.example

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import coil.ImageLoader
import coil.memory.MemoryCache
import com.example.ui.MainScreen
import com.example.ui.WineViewModel
import com.example.ui.theme.AdegaTheme
import com.example.ui.theme.AppThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Standard 8-bit RGBA_8888 window format
        window.setFormat(PixelFormat.RGBA_8888)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
        }

        // Configure modern Coil ImageLoader with ARGB_8888 and software bitmaps
        // allowHardware(false) prevents repetitive W/HWUI: Image decoding logging dropped! warnings in virtualized emulators
        val imageLoader = ImageLoader.Builder(applicationContext)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .allowHardware(false)
            .allowRgb565(false)
            .memoryCache {
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.25)
                    .build()
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        enableEdgeToEdge()
        setContent {
            val viewModel: WineViewModel = viewModel()
            val themeMode by viewModel.appThemeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemDark
            }

            AdegaTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            Coil.imageLoader(this).memoryCache?.trimMemory(level)
        } catch (_: Exception) {
        }
    }
}
