package com.carlos.makeupsales.ui.notifications

import android.app.NotificationChannel
import android.os.Build
import android.Manifest
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.carlos.makeupsales.R

object NotificationHelper {

    const val CHANNEL_ID = "makeupsales_reminders"
    private const val CHANNEL_NAME = "Recordatorios"
    private const val CHANNEL_DESC = "Recordatorios de órdenes pendientes y stock bajo"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        context: Context,
        pendingOrders: Int,
        lowStockCount: Int
    ) {
        // Si no hay nada que notificar, no hagas nada
        if (pendingOrders == 0 && lowStockCount == 0) return

        val title = "Resumen de MakeUpSales"
        val message = buildString {
            if (pendingOrders > 0) {
                append("Órdenes pendientes: $pendingOrders")
            }
            if (lowStockCount > 0) {
                if (isNotEmpty()) append(" • ")
                append("Productos con stock bajo: $lowStockCount")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_makeupsales_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // 👇 CHECK DE PERMISO PARA ANDROID 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                // No tenemos permiso → no intentamos mostrar la notificación
                return
            }
        }

        with(NotificationManagerCompat.from(context)) {
            notify(1001, builder.build())
        }
    }
}