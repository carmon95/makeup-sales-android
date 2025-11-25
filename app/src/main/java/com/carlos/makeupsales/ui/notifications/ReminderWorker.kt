package com.carlos.makeupsales.ui.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carlos.makeupsales.data.local.MakeupDatabase
import com.carlos.makeupsales.data.local.OrderStatus

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = MakeupDatabase.getInstance(applicationContext)
        val orderDao = db.orderDao()
        val productDao = db.productDao()

        return try {
            // 1) Contar órdenes pendientes
            val pendingOrders = orderDao.countByStatus(OrderStatus.PENDING)

            // 2) Productos con stock bajo (ej: <= 3 unidades)
            val lowStockProducts = productDao.getLowStockProducts(threshold = 3)
            val lowStockCount = lowStockProducts.size

            // 3) Crear canal y mostrar notificación
            NotificationHelper.createChannel(applicationContext)
            NotificationHelper.showReminderNotification(
                context = applicationContext,
                pendingOrders = pendingOrders,
                lowStockCount = lowStockCount
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
