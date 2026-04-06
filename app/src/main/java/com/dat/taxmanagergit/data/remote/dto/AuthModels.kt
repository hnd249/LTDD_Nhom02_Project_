package com.dat.taxmanager.data.remote.dto

// 1. Khuôn gửi Email/Mật khẩu lên Server
data class LoginRequest(
    val email: String,
    val password: String
)

// 2. Khuôn nhận Thẻ thông hành (Token) từ Server về
data class AuthResponse(
    val token: String?,
    val message: String?,
    val user: UserDto? // Bổ sung giỏ đựng thông tin User
)

// Tạo thêm khuôn UserDto
data class UserDto(
    val id: Int,
    val name: String,
    val email: String
)

// 3. Khuôn gửi Giao dịch lên Server để Đồng bộ
data class TransactionSyncRequest(
    val amount: Double,
    val type: String,
    val category: String,
    val source: String
)

// Khuôn gửi thông tin Đăng ký lên Server
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Khuôn để tải dữ liệu từ Server về
data class TransactionResponse(
    val amount: Double,
    val type: String,
    val category: String,
    val note: String?,
    val source: String?,

    // ✅ ĐÃ THÊM DÒNG NÀY ĐỂ HẾT BÁO ĐỎ Ở MAINSCREEN
    val created_at: String? = null
)