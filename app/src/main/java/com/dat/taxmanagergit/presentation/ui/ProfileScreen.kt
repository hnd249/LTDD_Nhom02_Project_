package com.dat.taxmanager.presentation.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dat.taxmanager.data.local.entity.TransactionEntity
import com.dat.taxmanager.util.ExportHelper

@Composable
fun ProfileScreen(
    transactions: List<TransactionEntity>,
    userNameFromApi: String = "Người dùng",
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE) }

    var userName by remember { mutableStateOf(userNameFromApi) }
    var isEditing by remember { mutableStateOf(false) }

    val avatarLetter = userName.trim().split(" ").lastOrNull()?.firstOrNull()?.uppercase() ?: "U"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = avatarLetter, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (isEditing) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(text = userName.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = if (isEditing) "Lưu ý: Tên sẽ cập nhật lên Avatar" else "Hội viên Basic",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (isEditing) sharedPrefs.edit().putString("USER_NAME", userName).apply()
                isEditing = !isEditing
            }) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- MENU CÀI ĐẶT ---
        Text(text = "Cài đặt & Cấu hình", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        // CÔNG TẮC DARK MODE
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Chế độ nền Tối ", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeChange(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MENU XUẤT BÁO CÁO ---
        Text(text = "Báo cáo & Thống kê", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(onClick = { ExportHelper.exportHistoryToPdf(context, transactions) }, color = Color.Transparent) {
            ProfileMenuItem(Icons.Default.List, "Xuất lịch sử giao dịch ")
        }

        Surface(onClick = { ExportHelper.exportToPdf(context, transactions) }, color = Color.Transparent) {
            ProfileMenuItem(Icons.Default.Edit, "Xuất báo cáo Thuế TNCN ")
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- NÚT ĐĂNG XUẤT ---
        Button(
            onClick = { onLogout() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text(" ĐĂNG XUẤT TÀI KHOẢN", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Hàm phụ trợ tạo menu
@Composable
fun ProfileMenuItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
    }
}