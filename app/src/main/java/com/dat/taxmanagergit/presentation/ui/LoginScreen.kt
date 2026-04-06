package com.dat.taxmanager.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dat.taxmanager.data.remote.ApiService
import com.dat.taxmanager.data.remote.dto.LoginRequest
import com.dat.taxmanager.data.remote.dto.RegisterRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    apiService: ApiService,
    // 1. SỬA CHỖ NÀY: Nhận thêm biến String thứ 2 để truyền Tên
    onLoginSuccess: (String, String) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TAX MANAGER", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            text = if (isLoginMode) "Đăng nhập để đồng bộ đám mây" else "Tạo tài khoản mới",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (!isLoginMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Họ và Tên") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
        }
        if (successMessage.isNotEmpty()) {
            Text(successMessage, color = Color(0xFF2E7D32), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || (!isLoginMode && name.isBlank())) {
                    errorMessage = "Vui lòng nhập đủ thông tin!"
                    successMessage = ""
                    return@Button
                }
                isLoading = true
                errorMessage = ""
                successMessage = ""

                coroutineScope.launch {
                    try {
                        if (isLoginMode) {
                            val response = apiService.login(LoginRequest(email, password))

                            if (response.isSuccessful) {
                                val token = response.body()?.token

                                // 2. SỬA CHỖ NÀY: Móc cái Tên từ cục JSON API trả về (nếu không có thì để mặc định)
                                val userName = response.body()?.user?.name ?: "Người Dùng"

                                if (token != null) {
                                    // Bắn cả 2 cái (Token và Tên) ra cho MainActivity chụp lấy
                                    onLoginSuccess(token, userName)
                                } else {
                                    errorMessage = "Đăng nhập được nhưng Server không phát Token!"
                                }
                            } else {
                                errorMessage = "Lỗi Server ${response.code()}: Sai thông tin hoặc sập API!"
                            }
                        } else {
                            val response = apiService.register(RegisterRequest(name, email, password))
                            if (response.isSuccessful) {
                                successMessage = "Đăng ký thành công! Đang chuyển sang đăng nhập..."
                                delay(1500)
                                isLoginMode = true
                                successMessage = ""
                                password = ""
                            } else {
                                errorMessage = "Email đã tồn tại hoặc lỗi đăng ký!"
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Lỗi kết nối máy chủ!"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isLoginMode) "ĐĂNG NHẬP" else "ĐĂNG KÝ TÀI KHOẢN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            isLoginMode = !isLoginMode
            errorMessage = ""
            successMessage = ""
        }) {
            Text(if (isLoginMode) "Chưa có tài khoản? Đăng ký ngay" else "Đã có tài khoản? Đăng nhập")
        }
    }
}