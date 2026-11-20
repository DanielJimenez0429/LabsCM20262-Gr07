package co.edu.udea.compumovil.gr07_20262.lab2.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object WorkScheduler {
    private const val PERIODIC_SNACKS = "snacks_periodic_sync"
    private const val ONE_TIME_SNACKS = "snacks_sync_now"

    private val needsNetwork = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Worker recurrente (15 min es el mínimo que permite WorkManager). */
    fun schedulePeriodicSnackSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SnackSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(needsNetwork)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SNACKS,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /** Sincronización inmediata (botón del Perfil). */
    fun syncSnacksNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SnackSyncWorker>()
            .setConstraints(needsNetwork)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SNACKS,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /** Encola el envío de un pedido; se ejecuta cuando haya red. */
    fun enqueueCheckout(context: Context, orderId: Long) {
        val request = OneTimeWorkRequestBuilder<CheckoutWorker>()
            .setConstraints(needsNetwork)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .setInputData(workDataOf(CheckoutWorker.KEY_ORDER_ID to orderId))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "checkout_$orderId",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun snackSyncState(context: Context): Flow<WorkInfo.State?> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(ONE_TIME_SNACKS)
            .map { it.firstOrNull()?.state }
}