package com.dat.taxmanager.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dat.taxmanager.data.local.entity.TransactionEntity
import com.dat.taxmanager.util.toCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(tx: TransactionEntity) {
    val localeVN = Locale("vi", "VN")

    // ===========================================================
    // BỘ THÔNG DỊCH ĐA NĂNG: CÂN MỌI LOẠI FORMAT TỪ LARAVEL
    // ===========================================================
    val dateObj: Date = remember(tx.timestamp) {
        val rawData = tx.timestamp.toString()
        var parsedDate: Date? = null

        // Danh sách các kiểu dữ liệu Laravel có thể gửi về
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", // Chuẩn JSON Laravel mới (Có millisecond 6 chữ số)
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",    // Chuẩn JSON Laravel mới (Có millisecond 3 chữ số)
            "yyyy-MM-dd'T'HH:mm:ss'Z'",        // Chuẩn JSON Laravel mới (Không millisecond)
            "yyyy-MM-dd HH:mm:ss"              // Chuẩn SQL cũ
        )

        for (format in formats) {
            try {
                val parser = SimpleDateFormat(format, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                parsedDate = parser.parse(rawData)
                if (parsedDate != null) break // Nếu dịch thành công thì thoát vòng lặp
            } catch (e: Exception) {
                // Thất bại thì thử định dạng tiếp theo
            }
        }

        // Nếu tất cả các kiểu chữ đều thất bại, thử ép sang kiểu Long
        parsedDate ?: try {
            Date(rawData.toLong())
        } catch (e: Exception) {
            Date() // Bất đắc dĩ mới lấy giờ hiện tại
        }
    }

    // ĐỊNH DẠNG HIỂN THỊ (mm viết thường là phút, HH là 24h)
    val dayStr = SimpleDateFormat("dd", localeVN).format(dateObj)
    val monthStr = SimpleDateFormat("'Thg' MM", localeVN).format(dateObj)
    val timeShort = SimpleDateFormat("HH:mm", localeVN).format(dateObj)
    val fullDateTime = SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", localeVN).format(dateObj)
    // ===========================================================

    val isExpense = tx.type == "EXPENSE"
    val amountColor = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32)
    val amountPrefix = if (isExpense) "-" else "+"

    var showDetail by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { showDetail = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(65.dp)
            ) {
                Text(text = dayStr, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(text = monthStr, fontSize = 11.sp, color = Color.Gray)
                Text(text = timeShort, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            }

            VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp).padding(horizontal = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = tx.category.uppercase(), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Text(text = "$amountPrefix${tx.amount.toCurrency()}", color = amountColor, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        }
    }

    if (showDetail) {
        ModalBottomSheet(onDismissRequest = { showDetail = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
                Text("Chi tiết lịch sử", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                DetailRow("Số tiền", "$amountPrefix${tx.amount.toCurrency()}", amountColor, isBold = true)
                DetailRow("Thời gian lập", fullDateTime)
                DetailRow("Danh mục", tx.category)
                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }
}

// HÀM DETAILROW QUAN TRỌNG (ĐỂ HẾT LỖI ĐỎ)
@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.Unspecified, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = valueColor)
    }
}