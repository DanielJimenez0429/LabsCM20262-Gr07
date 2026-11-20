package co.edu.udea.compumovil.gr07_20262.lab2.data

import co.edu.udea.compumovil.gr07_20262.lab2.model.OrderLine
import co.edu.udea.compumovil.gr07_20262.lab2.model.Snack
import kotlinx.coroutines.flow.MutableStateFlow

/** Carrito compartido entre el detalle del snack y la pantalla del carrito. */
object CartStore {
    // Empieza vacío. Para volver a los productos de ejemplo de Jetsnack usa:
    // MutableStateFlow(SnackRepo.getCart())
    val lines: MutableStateFlow<List<OrderLine>> = MutableStateFlow(emptyList())

    fun add(snack: Snack, quantity: Int) {
        if (quantity <= 0) return
        val current = lines.value
        lines.value = if (current.any { it.snack.id == snack.id }) {
            current.map {
                if (it.snack.id == snack.id) it.copy(count = it.count + quantity) else it
            }
        } else {
            current + OrderLine(snack, quantity)
        }
    }
}