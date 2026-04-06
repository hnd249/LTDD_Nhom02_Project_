package com.dat.taxmanager.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import androidx.compose.runtime.saveable.rememberSaveable
import com.dat.taxmanager.data.remote.ApiService

// --- LƯU TRỮ KẾT QUẢ TÍNH TOÁN ---
data class TaxCalculationResult(
    val gross: Double = 0.0,
    val bhxh: Double = 0.0,
    val bhyt: Double = 0.0,
    val bhtn: Double = 0.0,
    val beforeTax: Double = 0.0,
    val personalDeduct: Double = 0.0,
    val dependentDeduct: Double = 0.0,
    val taxableIncome: Double = 0.0,
    val totalTax: Double = 0.0,
    val net: Double = 0.0,
    val brackets: List<BracketDetail> = emptyList()
)

data class BracketDetail(val label: String, val rate: String, val amount: Double)

@Composable
fun TaxScreen() {
    var grossIncomeStr by rememberSaveable { mutableStateOf("") }
    var dependentsStr by rememberSaveable { mutableStateOf("") }

    // Biến lưu trữ kết quả ĐỘNG thay vì gán cứng
    var taxResult by remember { mutableStateOf<TaxCalculationResult?>(null) }

    // ==========================================================
    // TÍNH NĂNG MỚI: GỌI API LARAVEL LẤY TỶ GIÁ & GIÁ VÀNG
    // ==========================================================
    var usdRate by remember { mutableStateOf("Đang tải...") }
    var goldPrice by remember { mutableStateOf("Đang tải...") }

    LaunchedEffect(Unit) {
            val retrofit = retrofit2.Retrofit.Builder()
                // ĐÃ SỬA LẠI THÀNH IP CHUẨN CỦA MÁY ẢO VÀ ĐƯỜNG DẪN GỐC
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

            val api = retrofit.create(ApiService::class.java)
            val response = api.getRates()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    usdRate = DecimalFormat("#,###").format(data.usd_vnd)
                    goldPrice = "${data.gold_sjc}M"
                }
            } else {
                usdRate = "26.362"
                goldPrice = "174.50M"
            }
    }

    // --- HÀM TÍNH TOÁN THUẾ THỰC TẾ ---
    fun calculateTax() {
        val gross = grossIncomeStr.replace("[,.]".toRegex(), "").toDoubleOrNull() ?: 0.0
        val dependents = dependentsStr.toIntOrNull() ?: 0

        // 1. Mức đóng bảo hiểm tối đa
        val maxBhxhBhyt = 46_800_000.0 // 20 lần lương cơ sở 2.34M
        val maxBhtn = 99_200_000.0 // 20 lần lương tối thiểu vùng I (4.96M)

        // 2. Tính tiền Bảo hiểm
        val bhxh = minOf(gross, maxBhxhBhyt) * 0.08
        val bhyt = minOf(gross, maxBhxhBhyt) * 0.015
        val bhtn = minOf(gross, maxBhtn) * 0.01
        val totalInsurance = bhxh + bhyt + bhtn

        val beforeTax = gross - totalInsurance

        // 3. Giảm trừ gia cảnh (Luật 2026)
        val personalDeduct = 15_500_000.0
        val dependentDeduct = dependents * 6_200_000.0
        val totalDeduct = personalDeduct + dependentDeduct

        // 4. Thu nhập chịu thuế
        val taxableIncome = maxOf(0.0, beforeTax - totalDeduct)

        // 5. Tính Thuế theo 5 Bậc Dự thảo mới
        var remaining = taxableIncome
        var totalTax = 0.0
        val brackets = mutableListOf<BracketDetail>()

        // Bậc 1: Đến 10 triệu (5%)
        if (remaining > 0) {
            val taxAmount = minOf(remaining, 10_000_000.0) * 0.05
            brackets.add(BracketDetail("Đến 10 triệu VNĐ", "5%", taxAmount))
            totalTax += taxAmount
            remaining -= 10_000_000.0
        }
        // Bậc 2: Trên 10 - 30 triệu (10%)
        if (remaining > 0) {
            val taxAmount = minOf(remaining, 20_000_000.0) * 0.10
            brackets.add(BracketDetail("Trên 10tr đến 30tr", "10%", taxAmount))
            totalTax += taxAmount
            remaining -= 20_000_000.0
        }
        // Bậc 3: Trên 30 - 60 triệu (20%)
        if (remaining > 0) {
            val taxAmount = minOf(remaining, 30_000_000.0) * 0.20
            brackets.add(BracketDetail("Trên 30tr đến 60tr", "20%", taxAmount))
            totalTax += taxAmount
            remaining -= 30_000_000.0
        }
        // Bậc 4: Trên 60 - 100 triệu (30%)
        if (remaining > 0) {
            val taxAmount = minOf(remaining, 40_000_000.0) * 0.30
            brackets.add(BracketDetail("Trên 60tr đến 100tr", "30%", taxAmount))
            totalTax += taxAmount
            remaining -= 40_000_000.0
        }
        // Bậc 5: Trên 100 triệu (35%)
        if (remaining > 0) {
            val taxAmount = remaining * 0.35
            brackets.add(BracketDetail("Trên 100 triệu VNĐ", "35%", taxAmount))
            totalTax += taxAmount
        }

        // Cập nhật lên UI
        taxResult = TaxCalculationResult(
            gross = gross, bhxh = bhxh, bhyt = bhyt, bhtn = bhtn,
            beforeTax = beforeTax, personalDeduct = personalDeduct,
            dependentDeduct = dependentDeduct, taxableIncome = taxableIncome,
            totalTax = totalTax, net = gross - totalInsurance - totalTax,
            brackets = brackets
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

        // --- THANH TỶ GIÁ ---
        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("USD/VND", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                // HIỂN THỊ DỮ LIỆU TỪ API
                Text(usdRate, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
            }
            VerticalDivider(modifier = Modifier.height(30.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Vàng SJC", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                // HIỂN THỊ DỮ LIỆU TỪ API
                Text(goldPrice, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- CÔNG CỤ TÍNH THUẾ LUẬT 2026 ---
        Text(text = "Công cụ tính Thuế TNCN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "Giảm trừ gia cảnh bản thân: 15.500.000 đ", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelMedium)
        Text(text = "Người phụ thuộc: 6.200.000 đ", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = grossIncomeStr, onValueChange = { grossIncomeStr = it }, label = { Text("Tổng thu nhập tháng (Gross) - VNĐ") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = dependentsStr, onValueChange = { dependentsStr = it }, label = { Text("Số người phụ thuộc") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Spacer(modifier = Modifier.height(32.dp))

        // NÚT TÍNH TOÁN
        Button(
            onClick = { calculateTax() }, // Gọi hàm tính toán thật
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("TÍNH THUẾ THU NHẬP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // --- KHU VỰC HIỂN THỊ KẾT QUẢ ĐỘNG ---
        taxResult?.let { result ->
            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("KẾT QUẢ TÍNH TOÁN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(16.dp))

            // Lương NET
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Lương thực nhận (NET)", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                        Text("Đã trừ thuế TNCN", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Text("${formatVND(result.net)} đ", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bảng Chi tiết 1
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ResultRow("Lương Gross", formatVND(result.gross))
                    ResultRow("Bảo hiểm xã hội (8%)", formatVND(result.bhxh))
                    ResultRow("Bảo hiểm y tế (1.5%)", formatVND(result.bhyt))
                    ResultRow("Bảo hiểm thất nghiệp (1%)", formatVND(result.bhtn))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ResultRow("Thu nhập trước thuế", formatVND(result.beforeTax), isBold = true)
                    ResultRow("Giảm trừ gia cảnh bản thân", formatVND(result.personalDeduct))
                    ResultRow("Giảm trừ người phụ thuộc", formatVND(result.dependentDeduct))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ResultRow("Thu nhập chịu thuế", formatVND(result.taxableIncome), isBold = true)
                    ResultRow("Thuế thu nhập cá nhân (*)", formatVND(result.totalTax), isBold = true, textColor = Color.Red)
                }
            }

            // Bảng Chi tiết Bậc thuế
            if (result.brackets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("(*) Chi tiết thuế thu nhập cá nhân", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mức chịu thuế", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text("Thuế suất", modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                    Text("Tiền nộp", modifier = Modifier.weight(0.5f), textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                result.brackets.forEach { bracket ->
                    BracketRow(bracket.label, bracket.rate, formatVND(bracket.amount))
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

fun formatVND(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    return if (amount == 0.0) "0" else formatter.format(amount)
}

@Composable
fun ResultRow(title: String, amount: String, isBold: Boolean = false, textColor: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, modifier = Modifier.weight(1f), style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium)
        Text(amount, color = textColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun BracketRow(range: String, rate: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(range, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(rate, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodyMedium)
        Text(amount, modifier = Modifier.weight(0.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}