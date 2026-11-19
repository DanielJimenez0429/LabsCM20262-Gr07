/*
 * Copyright 2020-2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.edu.udea.compumovil.gr07_20262.lab2.ui

import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import co.edu.udea.compumovil.gr07_20262.lab2.widget.RecentOrdersWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    // Solicitar permiso para mostrar notificaciones
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Solicitar permiso de notificacion en Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            lifecycleScope.launch(Dispatchers.Default) {
                setWidgetPreviews()
            }
        }
        setContent { co.edu.udea.compumovil.gr07_20262.lab2.ui.JetsnackApp() }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    suspend fun setWidgetPreviews() {
        val receiver = RecentOrdersWidgetReceiver::class
        val installedProviders = getSystemService(AppWidgetManager::class.java).installedProviders
        val providerInfo = installedProviders.firstOrNull {
            it.provider.className ==
                receiver.qualifiedName
        }
        providerInfo?.generatedPreviewCategories.takeIf { it == 0 }?.let {
            // Set previews if this provider if unset
            GlanceAppWidgetManager(this).setWidgetPreviews(receiver)
        }
    }
}