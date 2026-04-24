# 📱 TaxManager - Hệ thống Quản lý Tài chính & Thuế TNCN

<div align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Laravel-FF2D20?style=for-the-badge&logo=laravel&logoColor=white" />
</div>

<br>

Dự án cuối kỳ bộ môn **Lập trình thiết bị di động**. 
Hệ thống cung cấp giải pháp toàn diện giúp người dùng cá nhân ghi chép thu chi hàng ngày và tự động ước tính, tính toán Thuế Thu nhập cá nhân (TNCN) theo biểu thuế lũy tiến mới nhất của Pháp luật Việt Nam.

- **Trường:** Đại học Sư phạm - Đại học Đà Nẵng
- **Lớp:** 23CNTT2
- **Giảng viên hướng dẫn:** TS. Nguyễn Hoàng Hải

---

## 👥 Đội ngũ phát triển (Nhóm 02)

| STT | Họ và Tên | MSSV | Vai trò & Đóng góp |
| :---: | :--- | :---: | :--- |
| 1 | **Hồ Ngọc Đạt** (Leader) | 3120223027 | Thiết kế UI/UX, Android ViewModels, API Authentication |
| 2 | **Trần Hữu Đức** | 3120223034 | Thiết kế Database Schema, Room DB, Dependency Injection (Hilt) |
| 3 | **Nguyễn Thị Kim Nhung** | 3120223146 | Xây dựng thuật toán & Logic tính Thuế TNCN (TaxService) |
| 4 | **Trần Quốc Đạt** | 3120223028 | Quản lý logic Giao dịch (Transactions), Base UI & Navigation |

---

## 🔗 Liên kết hệ thống
Hệ thống được thiết kế theo kiến trúc Client-Server tách biệt:
- **Client (Android):** Repository hiện tại.
- **Server (RESTful API):** [👉 Xem mã nguồn Backend tại đây](https://github.com/hnd249/TaxManager_Backend)

---

## 🚀 Tính năng nổi bật

### 1. Quản lý Tài chính Cá nhân (Thu/Chi)
- Ghi nhận giao dịch thông minh theo từng danh mục (Lương, Thưởng, Ăn uống, Sinh hoạt...).
- Dashboard trực quan theo dõi biến động số dư và lịch sử giao dịch.

### 2. Trợ lý Thuế TNCN Tự động
- Nhập tổng thu nhập và số lượng người phụ thuộc.
- Hệ thống tự động bóc tách các khoản giảm trừ (bản thân: 11 triệu, người phụ thuộc: 4.4 triệu).
- Áp dụng thuật toán tính thuế theo **Biểu thuế lũy tiến từng phần (7 bậc)**.

### 3. Đồng bộ & Bảo mật
- Kiến trúc API xác thực an toàn bằng JWT (Sanctum).
- Dữ liệu luôn được đồng bộ hóa giữa ứng dụng di động và máy chủ đám mây.

---

## 🛠 Công nghệ & Kiến trúc

### Tầng Client (Android App)
- **Ngôn ngữ:** Kotlin
- **Giao diện:** Jetpack Compose (100% Declarative UI)
- **Kiến trúc:** Clean Architecture kết hợp MVVM (Model-View-ViewModel)
- **Cơ sở dữ liệu cục bộ:** Room Database (Offline support)
- **Mạng & API:** Retrofit2 + OkHttp
- **Dependency Injection:** Dagger Hilt

### Tầng Server (Backend & Database)
- **Framework:** Laravel 11 (PHP)
- **Cơ sở dữ liệu:** MySQL / MariaDB
- **Mô hình xử lý:** Service Repository Pattern

---

## 📂 Cấu trúc thư mục cốt lõi (Android)

```text
app/src/main/java/com/dat/taxmanagergit/
├── data/           # Triển khai Repository, Room DAO, API Services
├── di/             # Các Module cung cấp Dependency (Hilt)
├── domain/         # Các UseCase và Interface Repository (Business Logic)
├── presentation/   # Jetpack Compose Screens, Navigation và ViewModels
└── util/           # Extension functions, hằng số và formatters
```
### ⚙️ Hướng dẫn cài đặt & Khởi chạy
Bước 1: Khởi động Backend
Vui lòng clone và chạy Server Laravel từ Repository Backend trước. Đảm bảo server đang chạy ở mạng LAN (ví dụ: php artisan serve --host=0.0.0.0).

Bước 2: Cấu hình Client
1. Clone dự án này về máy:
  git clone [https://github.com/hnd249/LTDD_Nhom02_Project_.git](https://github.com/hnd249/LTDD_Nhom02_Project_.git)
2. Mở dự án bằng Android Studio.
3. Tìm đến file chứa cấu hình mạng (thường là trong module di/NetworkModule.kt hoặc util/Constants.kt).
4. Thay đổi biến BASE_URL thành địa chỉ IP IPv4 của máy tính đang chạy Backend:
  const val BASE_URL = "http://[YOUR_IPV4_ADDRESS]:8000/api/"
5. Nhấn Sync Project with Gradle Files và chạy ứng dụng trên Emulator hoặc thiết bị Android vật lý.
