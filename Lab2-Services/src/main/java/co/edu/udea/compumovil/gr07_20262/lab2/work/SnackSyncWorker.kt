package co.edu.udea.compumovil.gr07_20262.lab2.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.edu.udea.compumovil.gr07_20262.lab2.data.SnackInfoRepository
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Tarea recurrente: actualiza en segundo plano los datos de los snacks desde el servicio REST. */
class SnackSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        SnackInfoRepository.get(applicationContext).refresh()
        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.retry()
    } catch (e: Exception) {
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }
}