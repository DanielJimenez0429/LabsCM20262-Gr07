package co.edu.udea.compumovil.gr07_20262.lab2.work

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.edu.udea.compumovil.gr07_20262.lab2.R
import co.edu.udea.compumovil.gr07_20262.lab2.data.OrderRepository
import co.edu.udea.compumovil.gr07_20262.lab2.data.OrderStatus
import co.edu.udea.compumovil.gr07_20262.lab2.data.remote.ApiClient
import co.edu.udea.compumovil.gr07_20262.lab2.data.remote.OrderRequest
import co.edu.udea.compumovil.gr07_20262.lab2.ui.utils.formatPrice
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/**
 * Envía el pedido al servicio REST (POST /orders) en segundo plano.
 * Sin red, WorkManager lo deja en espera y lo reintenta con backoff exponencial.
 */
class CheckoutWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repo = OrderRepository.get(applicationContext)
        val orderId = inputData.getLong(KEY_ORDER_ID, -1L)
        val order = repo.get(orderId) ?: return Result.failure()

        return try {
            val response = ApiClient.api.createOrder(
                OrderRequest(
                    items = order.items,
                    itemCount = order.itemCount,
                    total = order.total,
                    status = "confirmed",
                ),
            )
            repo.update(orderId, OrderStatus.CONFIRMED, response.id)
            showNotification(order.itemCount, order.total)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else fail(repo, orderId)
        } catch (e: HttpException) {
            if (e.code() >= 500 && runAttemptCount < MAX_ATTEMPTS) Result.retry() else fail(repo, orderId)
        } catch (e: Exception) {
            fail(repo, orderId)
        }
    }

    private fun fail(repo: OrderRepository, orderId: Long): Result {
        repo.update(orderId, OrderStatus.FAILED)
        return Result.failure()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(items: Int, total: Long) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(context.getString(R.string.notif_order_title))
            .setContentText(context.getString(R.string.notif_order_text, items, formatPrice(total)))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val KEY_ORDER_ID = "order_id"
        private const val CHANNEL_ID = "orders"
        private const val MAX_ATTEMPTS = 5
    }
}