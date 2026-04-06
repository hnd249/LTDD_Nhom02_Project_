package com.dat.taxmanager.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dat.taxmanager.domain.usecase.GetStatsUseCase
import com.dat.taxmanager.presentation.ui.components.TransactionItem
import com.dat.taxmanager.presentation.viewmodel.TransactionViewModel
import com.dat.taxmanager.util.toCurrency

@Composable
fun HomeScreen(
    userName: String = "Người Dùng",
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val stats = GetStatsUseCase().execute(transactions)
    val balance = stats["balance"] ?: 0.0
    val income = stats["income"] ?: 0.0
    val expense = stats["expense"] ?: 0.0

    val avatarLetter = userName.trim().split(" ").lastOrNull()?.firstOrNull()?.uppercase() ?: "U"

    // --- BIẾN CHO BIỂU ĐỒ ---
    var isExpenseMode by remember { mutableStateOf(true) } // true = Chi tiêu, false = Thu nhập

    // Gom nhóm dữ liệu theo Danh mục
    val chartData = transactions
        .filter { if (isExpenseMode) it.type == "EXPENSE" else it.type == "INCOME" }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second } // Sắp xếp từ lớn đến bé

    val totalChartAmount = chartData.sumOf { it.second }

    // Bảng màu cho biểu đồ (Bắt mắt như ShopeePay)
    val chartColors = listOf(
        Color(0xFF64B5F6), // Xanh dương nhạt (Di chuyển)
        Color(0xFFF06292), // Hồng (Giải trí)
        Color(0xFFFFD54F), // Vàng
        Color(0xFF81C784), // Xanh lá
        Color(0xFFBA68C8), // Tím
        Color(0xFFFF8A65), // Cam
        Color(0xFF4DB6AC)  // Xanh ngọc
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // --- HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                Text(avatarLetter, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Xin chào,", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = userName.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 1. KHUNG SỐ DƯ ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tổng số dư khả dụng", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = balance.toCurrency(), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. KHUNG BIỂU ĐỒ PHÂN BỔ (TÍNH NĂNG MỚI) ---
        Text(text = "Thống kê phân bổ tháng này", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Nút gạt Thu / Chi
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(4.dp)
                ) {
                    // Nút Chi tiêu
                    Box(
                        modifier = Modifier.weight(1f).background(if (isExpenseMode) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { isExpenseMode = true }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chi tiêu", fontWeight = FontWeight.Bold, color = if (isExpenseMode) Color(0xFFC62828) else Color.Gray)
                    }
                    // Nút Thu nhập
                    Box(
                        modifier = Modifier.weight(1f).background(if (!isExpenseMode) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { isExpenseMode = false }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Thu nhập", fontWeight = FontWeight.Bold, color = if (!isExpenseMode) Color(0xFF2E7D32) else Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // VẼ BIỂU ĐỒ DONUT BẰNG CANVAS
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        if (totalChartAmount == 0.0) {
                            // Nếu không có dữ liệu -> Vẽ vòng tròn xám
                            drawArc(color = Color.LightGray.copy(alpha = 0.3f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 60f, cap = StrokeCap.Butt))
                        } else {
                            var startAngle = -90f // Bắt đầu vẽ từ đỉnh (hướng 12 giờ)
                            chartData.forEachIndexed { index, pair ->
                                val sweepAngle = (pair.second.toFloat() / totalChartAmount.toFloat()) * 360f
                                drawArc(
                                    color = chartColors[index % chartColors.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 60f, cap = StrokeCap.Butt)
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }

                    // Chữ ở giữa biểu đồ
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isExpenseMode) "Tổng chi" else "Tổng thu", color = Color.Gray, fontSize = 12.sp)
                        Text(totalChartAmount.toCurrency(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // DANH SÁCH CHÚ THÍCH (LEGEND)
                if (chartData.isEmpty()) {
                    Text("Chưa có dữ liệu", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
                } else {
                    chartData.forEachIndexed { index, pair ->
                        val percentage = (pair.second / totalChartAmount) * 100
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Chấm màu
                            Box(modifier = Modifier.size(12.dp).background(chartColors[index % chartColors.size], CircleShape))
                            Spacer(modifier = Modifier.width(12.dp))
                            // Tên danh mục
                            Text(pair.first, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            // Phần trăm
                            Text(String.format("%.1f%%", percentage), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                            // Số tiền
                            Text(pair.second.toCurrency(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. GIAO DỊCH GẦN NHẤT ---
        Text(text = "Giao dịch gần nhất", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // 1. LỌC DANH SÁCH THEO TAB ĐANG CHỌN (Thu hoặc Chi)
        val filteredRecentTransactions = transactions.filter { tx ->
            if (isExpenseMode) {
                tx.type == "EXPENSE" // Đang ở tab Chi -> Lọc Chi
            } else {
                tx.type == "INCOME"  // Đang ở tab Thu -> Lọc Thu
            }
        }.take(3) // Lấy 3 giao dịch gần nhất cho gọn màn hình

        // 2. HIỂN THỊ DANH SÁCH HOẶC BÁO TRỐNG
        if (filteredRecentTransactions.isEmpty()) {
            Text(
                text = "Chưa có giao dịch nào.",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            filteredRecentTransactions.forEach { tx ->
                TransactionItem(tx = tx)
            }
        }
    } // Đóng ngoặc của Column bọc ngoài cùng
} // Đóng ngoặc của HomeScreen