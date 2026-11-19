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
import co.edu.udea.compumovil.gr07_20262.lab2.ui.utils.formatPrice
import kotlinx.coroutines.delay

/** Procesa el pedido del carrito en segundo plano y avisa con una notificación. */
class CheckoutWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val items = inputData.getInt(KEY_ITEMS, 0)
        val total = inputData.getLong(KEY_TOTAL, 0L)

        // Simula el procesamiento del pago/envío del pedido.
        // (En el paso del servicio REST, aquí va la llamada con Retrofit.)
        delay(5_000)

        showNotification(items, total)
        return Result.success()
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
        const val KEY_ITEMS = "items"
        const val KEY_TOTAL = "total"
        private const val CHANNEL_ID = "orders"
    }
}