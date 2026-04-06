package com.dat.taxmanager.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dat.taxmanager.data.local.entity.TransactionEntity
import com.dat.taxmanager.presentation.ui.components.TransactionItem
import com.dat.taxmanager.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionScreen(
    transactions: List<TransactionEntity>, // Nhận danh sách từ MainScreen truyền xuống
    onAddTransaction: (TransactionEntity) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Thêm Giao Dịch", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Số tiền (đ)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Nội dung (Lương, Ăn uống...)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !isIncome, onClick = { isIncome = false })
            Text("Chi tiêu")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = isIncome, onClick = { isIncome = true })
            Text("Thu nhập")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                if (amountVal > 0 && category.isNotBlank()) {
                    val newTx = TransactionEntity(
                        amount = amountVal,
                        type = if (isIncome) "INCOME" else "EXPENSE",
                        category = category,
                        isTaxable = false,
                        source = "Manual",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date())
                    )

                    onAddTransaction(newTx) // Gửi về MainScreen xử lý

                    NotificationHelper.showTransactionSavedNotification(context, isIncome, amountVal, category)

                    amount = ""
                    category = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu Giao Dịch")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Lịch sử gần đây", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        // UI sẽ tự động vẽ lại khi danh sách transactions truyền vào thay đổi
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions) { tx ->
                TransactionItem(tx = tx)
            }
        }
    }
}