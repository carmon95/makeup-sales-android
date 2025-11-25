package com.carlos.makeupsales

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.carlos.makeupsales.data.local.MakeupDatabase
import com.carlos.makeupsales.data.repository.CustomerRepository
import com.carlos.makeupsales.data.repository.OrderRepository
import com.carlos.makeupsales.data.repository.ProductRepository
import com.carlos.makeupsales.ui.notifications.NotificationHelper
import com.carlos.makeupsales.ui.notifications.ReminderWorker
import java.util.concurrent.TimeUnit

class MakeupApplication : Application() {

    // DB y repos (como ya los tenías)
    val database by lazy { MakeupDatabase.getInstance(this) }

    val productRepository by lazy { ProductRepository(database.productDao()) }
    val customerRepository by lazy { CustomerRepository(database.customerDao()) }
    val orderRepository by lazy { OrderRepository(database.orderDao()) }

    override fun onCreate() {
        super.onCreate()

        // 1) Crear canal de notificaciones (una sola vez al iniciar la app)
        NotificationHelper.createChannel(this)

        // 2) Programar el worker periódico (por ejemplo, cada 6 horas)
        val workRequest =
            PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "makeupsales_reminder_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
