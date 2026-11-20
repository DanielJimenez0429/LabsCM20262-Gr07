package co.edu.udea.compumovil.gr07_20262.lab2.data

import android.content.Context
import co.edu.udea.compumovil.gr07_20262.lab2.data.remote.ApiClient
import co.edu.udea.compumovil.gr07_20262.lab2.data.remote.SnackInfoDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Estados que puede mostrar la pantalla de detalle. */
sealed interface SnackInfoUiState {
    data object Loading : SnackInfoUiState
    data class Success(val info: SnackInfoDto) : SnackInfoUiState
    data object NotFound : SnackInfoUiState
    data object Error : SnackInfoUiState
}

/** Datos del servicio REST (mockapi.io) guardados también en el celular. */
class SnackInfoRepository private constructor(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("lab2_snacks", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val items = MutableStateFlow(loadCache())
    private val _lastSync = MutableStateFlow(prefs.getLong(KEY_SYNC, 0L))
    val lastSync: StateFlow<Long> = _lastSync.asStateFlow()

    /** Consume el servicio REST y guarda la copia local. Devuelve cuántos snacks bajó. */
    suspend fun refresh(): Int {
        val list = ApiClient.api.getSnacks()
        val now = System.currentTimeMillis()
        prefs.edit()
            .putString(KEY_ITEMS, gson.toJson(list))
            .putLong(KEY_SYNC, now)
            .apply()
        items.value = list
        _lastSync.value = now
        return list.size
    }

    private fun find(name: String): SnackInfoDto? =
        items.value.firstOrNull { it.name.equals(name, ignoreCase = true) }

    /** Primero busca en la copia local; si no está, consulta el servidor. */
    suspend fun load(name: String): SnackInfoUiState {
        find(name)?.let { return SnackInfoUiState.Success(it) }
        return try {
            refresh()
            find(name)?.let { SnackInfoUiState.Success(it) } ?: SnackInfoUiState.NotFound
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SnackInfoUiState.Error
        }
    }

    private fun loadCache(): List<SnackInfoDto> {
        val json = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SnackInfoDto>>() {}.type
            gson.fromJson<List<SnackInfoDto>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_ITEMS = "items"
        private const val KEY_SYNC = "last_sync"

        @Volatile
        private var INSTANCE: SnackInfoRepository? = null

        fun get(context: Context): SnackInfoRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SnackInfoRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}