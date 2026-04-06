package com.dat.taxmanager.util

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dat.taxmanager.data.local.entity.TransactionEntity
import com.dat.taxmanager.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : NotificationListenerService() {

    @Inject
    lateinit var repository: TransactionRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        // Chỉ đọc thông báo từ các App Ngân hàng hoặc Ví điện tử (Ví dụ: Momo, VCB)
        if (packageName.contains("momo") || packageName.contains("vcb") || packageName.contains("mbmobile")) {
            parseAndSaveTransaction(text, packageName)
        }
    }

    private fun parseAndSaveTransaction(content: String, source: String) {
        // 1. Tìm số tiền: Chấp nhận các định dạng 50.000, 1,000,000, 50000...
        val regex = "([+-]?\\d{1,3}(?:[.,]\\d{3})*)".toRegex()
        val match = regex.find(content)

        match?.let {
            var amountString = it.value.replace("[.,]".toRegex(), "")
            var amount = amountString.toDoubleOrNull() ?: 0.0

            if (amount != 0.0) {
                // 2. Logic thông minh: Đoán xem là Thu hay Chi dựa trên từ khóa tiếng Việt
                val isIncomeText = listOf("nhan tien", "chuyen den", "loi nhuan", "plus", "+", "thu nhap")
                val isExpenseText = listOf("thanh toan", "chuyen di", "tru tai khoan", "minus", "-", "rut tien")

                val isIncome = isIncomeText.any { word -> content.lowercase().contains(word) }
                        || amount > 0

                serviceScope.launch {
                    val newTx = TransactionEntity(
                        amount = Math.abs(amount),
                        type = if (isIncome) "INCOME" else "EXPENSE",
                        category = "Tự động ($source)",
                        isTaxable = content.lowercase().contains("luong"), // Nếu thấy chữ "luong" -> Tự tick chịu thuế luôn!
                        source = source,
                        note = content,
                        timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }.format(java.util.Date())
                    )
                    repository.insertTransaction(newTx)
                }
            }
        }
    }
}