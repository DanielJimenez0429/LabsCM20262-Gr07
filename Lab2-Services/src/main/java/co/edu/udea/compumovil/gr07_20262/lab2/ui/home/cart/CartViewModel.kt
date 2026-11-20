/*
 * Copyright 2020 The Android Open Source Project
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

package co.edu.udea.compumovil.gr07_20262.lab2.ui.home.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.edu.udea.compumovil.gr07_20262.lab2.R
import co.edu.udea.compumovil.gr07_20262.lab2.model.OrderLine
import co.edu.udea.compumovil.gr07_20262.lab2.model.SnackRepo
import co.edu.udea.compumovil.gr07_20262.lab2.model.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import co.edu.udea.compumovil.gr07_20262.lab2.data.OrderRepository
import co.edu.udea.compumovil.gr07_20262.lab2.work.WorkScheduler
import co.edu.udea.compumovil.gr07_20262.lab2.data.CartStore

/**
 * Holds the contents of the cart and allows changes to it.
 *
 * TODO: Move data to Repository so it can be displayed and changed consistently throughout the app.
 */
class CartViewModel(private val snackbarManager: SnackbarManager, snackRepository: SnackRepo) : ViewModel() {

    // Carrito compartido con el detalle del snack (CartStore)
    private val _orderLines: MutableStateFlow<List<OrderLine>> = CartStore.lines
    val orderLines: StateFlow<List<OrderLine>> get() = _orderLines

    // Logic to show errors every few requests
    private var requestCount = 0
    private fun shouldRandomlyFail(): Boolean = ++requestCount % 5 == 0

    fun increaseSnackCount(snackId: Long) {
        if (!shouldRandomlyFail()) {
            val currentCount = _orderLines.value.first { it.snack.id == snackId }.count
            updateSnackCount(snackId, currentCount + 1)
        } else {
            snackbarManager.showMessage(R.string.cart_increase_error)
        }
    }

    fun decreaseSnackCount(snackId: Long) {
        if (!shouldRandomlyFail()) {
            val currentCount = _orderLines.value.first { it.snack.id == snackId }.count
            if (currentCount == 1) {
                // remove snack from cart
                removeSnack(snackId)
            } else {
                // update quantity in cart
                updateSnackCount(snackId, currentCount - 1)
            }
        } else {
            snackbarManager.showMessage(R.string.cart_decrease_error)
        }
    }

    fun checkout(context: Context) {
        val lines = _orderLines.value
        if (lines.isEmpty()) {
            snackbarManager.showMessage(R.string.cart_empty)
            return
        }
        val summary = lines.joinToString(", ") { "${it.snack.name} x${it.count}" }
        val order = OrderRepository.get(context).create(
            items = summary,
            itemCount = lines.sumOf { it.count },
            total = lines.sumOf { it.snack.price * it.count },
        )
        WorkScheduler.enqueueCheckout(context.applicationContext, order.id)
        _orderLines.value = emptyList()
        snackbarManager.showMessage(R.string.checkout_queued)
    }
    
    fun removeSnack(snackId: Long) {
        _orderLines.value = _orderLines.value.filter { it.snack.id != snackId }
    }

    private fun updateSnackCount(snackId: Long, count: Int) {
        _orderLines.value = _orderLines.value.map {
            if (it.snack.id == snackId) {
                it.copy(count = count)
            } else {
                it
            }
        }
    }

    /**
     * Factory for CartViewModel that takes SnackbarManager as a dependency
     */
    companion object {
        fun provideFactory(
            snackbarManager: SnackbarManager = SnackbarManager,
            snackRepository: SnackRepo = SnackRepo,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CartViewModel(snackbarManager, snackRepository) as T
            }
        }
    }
}
