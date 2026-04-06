package com.dat.taxmanager.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.dat.taxmanager.data.local.entity.TransactionEntity
import com.dat.taxmanager.presentation.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.dat.taxmanager.data.remote.ApiService
import com.dat.taxmanager.data.remote.dto.TransactionSyncRequest

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("Trang chủ", Icons.Default.Home)
    object Transaction : BottomNavItem("Giao dịch", Icons.AutoMirrored.Filled.List)
    object Tax : BottomNavItem("Quản lý Thuế", Icons.Default.Edit)
    object Profile : BottomNavItem("Cá nhân", Icons.Default.AccountCircle)
}

@Composable
fun MainScreen(
    authToken: String = "",
    userName: String = "Người Dùng",
    isDarkTheme: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }

    val coroutineScope = rememberCoroutineScope()
    val apiService = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Transaction,
        BottomNavItem.Tax,
        BottomNavItem.Profile
    )

    // ===================================================================
    // MA THUẬT ĐỒNG BỘ: TỰ ĐỘNG KÉO DATA TỪ MÂY VỀ KHI MỞ APP
    // ===================================================================
    LaunchedEffect(authToken) {
        if (authToken.isNotEmpty()) {
            try {
                val response = apiService.getMyTransactions("Bearer $authToken")
                if (response.isSuccessful) {
                    val serverData = response.body()
                    if (serverData != null && serverData.isNotEmpty()) {
                        viewModel.clearData()

                        serverData.forEach { tx ->
                            viewModel.addTransaction(
                                TransactionEntity(
                                    amount = tx.amount,
                                    type = tx.type,
                                    category = tx.category,
                                    note = tx.note ?: "",
                                    source = tx.source ?: "Cloud",
                                    timestamp = tx.created_at ?: "2026-01-01 00:00:00",
                                    isTaxable = false
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
    // ===================================================================

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == item,
                        onClick = { selectedItem = item }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedItem) {

                is BottomNavItem.Home -> HomeScreen(userName = userName)

                is BottomNavItem.Transaction -> TransactionScreen(
                    transactions = transactions, // Truyền cái danh sách đang quan sát ở đầu MainScreen vào đây
                    onAddTransaction = { newTx ->
                        viewModel.addTransaction(newTx) // Lưu vào DB (Lưu xong là flow transactions ở trên sẽ tự đổi)

                        // Phần đồng bộ Laravel giữ nguyên
                        if (authToken.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    val syncReq = TransactionSyncRequest(
                                        amount = newTx.amount,
                                        type = newTx.type,
                                        category = newTx.category,
                                        source = newTx.source
                                    )
                                    apiService.syncTransaction("Bearer $authToken", syncReq)
                                } catch (e: Exception) {
                                    // Xử lý lỗi sync nếu cần
                                }
                            }
                        }
                    }
                )

                // THÊM LẠI DÒNG NÀY ĐỂ KOTLIN HẾT DỖI NÈ:
                is BottomNavItem.Tax -> TaxScreen()

                is BottomNavItem.Profile -> ProfileScreen(
                    transactions = transactions,
                    userNameFromApi = userName,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    onLogout = {
                        viewModel.clearData()
                        onLogout()
                    }
                )
            }
        }
    }
}