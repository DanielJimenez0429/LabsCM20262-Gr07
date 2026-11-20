package co.edu.udea.compumovil.gr07_20262.lab2.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OrderStatus { PENDING, CONFIRMED, FAILED }

data class Order(
    val id: Long = 0,
    val createdAt: Long = 0,
    val items: String = "",
    val itemCount: Int = 0,
    val total: Long = 0,
    val status: OrderStatus = OrderStatus.PENDING,
    val remoteId: String = "",
)

/** Historial de pedidos: lo crea el carrito, lo actualiza el CheckoutWorker y lo muestra el Perfil. */
class OrderRepository private constructor(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("lab2_orders", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _orders = MutableStateFlow(load())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    @Synchronized
    fun create(items: String, itemCount: Int, total: Long): Order {
        val now = System.currentTimeMillis()
        val order = Order(id = now, createdAt = now, items = items, itemCount = itemCount, total = total)
        _orders.value = listOf(order) + _orders.value
        save()
        return order
    }

    fun get(id: Long): Order? = _orders.value.firstOrNull { it.id == id }

    @Synchronized
    fun update(id: Long, status: OrderStatus, remoteId: String = "") {
        _orders.value = _orders.value.map {
            if (it.id == id) it.copy(status = status, remoteId = remoteId) else it
        }
        save()
    }

    private fun save() {
        prefs.edit().putString(KEY_ORDERS, gson.toJson(_orders.value)).apply()
    }

    private fun load(): List<Order> {
        val json = prefs.getString(KEY_ORDERS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Order>>() {}.type
            gson.fromJson<List<Order>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_ORDERS = "orders"

        @Volatile
        private var INSTANCE: OrderRepository? = null

        fun get(context: Context): OrderRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: OrderRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}