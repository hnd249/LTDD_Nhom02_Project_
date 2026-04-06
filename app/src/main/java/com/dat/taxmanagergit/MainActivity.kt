package com.dat.taxmanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dat.taxmanager.presentation.ui.LoginScreen
import com.dat.taxmanager.presentation.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE) }

            // 1. TẠO BIẾN LƯU TRỮ CHẾ ĐỘ SÁNG TỐI (Lấy từ bộ nhớ, mặc định là False - Sáng)
            var isDarkTheme by remember { mutableStateOf(sharedPrefs.getBoolean("DARK_MODE", false)) }

            // 2. CHỌN BẢNG MÀU TƯƠNG ỨNG VỚI CÔNG TẮC
            val colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()

            // 3. ÉP TOÀN BỘ APP DÙNG BẢNG MÀU NÀY
            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var authToken by remember { mutableStateOf(sharedPrefs.getString("TOKEN", "") ?: "") }
                    var userName by remember { mutableStateOf(sharedPrefs.getString("USER_NAME", "Người Dùng") ?: "Người Dùng") }

                    val apiService = remember {
                        Retrofit.Builder()
                            .baseUrl("http://10.0.2.2:8000/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                            .create(com.dat.taxmanager.data.remote.ApiService::class.java)
                    }

                    if (authToken.isEmpty()) {
                        LoginScreen(
                            apiService = apiService,
                            onLoginSuccess = { token, name ->
                                sharedPrefs.edit()
                                    .putString("TOKEN", token)
                                    .putString("USER_NAME", name)
                                    .apply()

                                authToken = token
                                userName = name
                            }
                        )
                    } else {
                        MainScreen(
                            authToken = authToken,
                            userName = userName,
                            // 4. CHUYỀN DÂY ĐIỆN (TRẠNG THÁI & LỆNH BẬT TẮT) XUỐNG CHO MAIN SCREEN
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { isDark ->
                                isDarkTheme = isDark
                                // Lưu vĩnh viễn vào điện thoại để lần sau mở app vẫn giữ nguyên màu
                                sharedPrefs.edit().putBoolean("DARK_MODE", isDark).apply()
                            },
                            onLogout = {
                                sharedPrefs.edit().clear().apply()
                                authToken = ""
                                userName = ""
                            }
                        )
                    }
                }
            }
        }
    }
}