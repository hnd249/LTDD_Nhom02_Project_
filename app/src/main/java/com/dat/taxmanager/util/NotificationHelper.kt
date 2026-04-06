package com.dat.taxmanager.util

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dat.taxmanager.MainActivity // Đảm bảo import đúng MainActivity của bạn

object NotificationHelper {
    private const val CHANNEL_ID = "transaction_channel"
    private const val CHANNEL_NAME = "Thông báo Giao dịch"

    fun showTransactionSavedNotification(context: Context, isIncome: Boolean, amount: Double, category: String) {
        createNotificationChannel(context)

        val title = "Đã lưu giao dịch!"
        val typeStr = if (isIncome) "Thu nhập" else "Chi tiêu"
        val prefix = if (isIncome) "+" else "-"
        val formattedAmount = String.format("%,.0f đ", amount)
        val contentText = "$typeStr: $prefix$formattedAmount ($category)"

        // --- BƯỚC MỚI: TẠO INTENT ĐỂ MỞ APP KHI BẤM VÀO THÔNG BÁO ---
        val intent = Intent(context, MainActivity::class.java).apply {
            // Cờ này giúp app mở lên từ đầu hoặc đưa lên nền nếu đang chạy ngầm
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            // FLAG_IMMUTABLE là bắt buộc trên Android 12 trở lên
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Gắn tấm vé vào thông báo
            .setAutoCancel(true) // Bấm vào xong là tự động tắt thông báo đi

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            } else {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "Kênh thông báo khi lưu giao dịch thành công"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}