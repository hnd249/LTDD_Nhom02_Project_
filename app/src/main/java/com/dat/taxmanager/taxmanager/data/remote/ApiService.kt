package com.dat.taxmanager.taxmanager.data.remote

import com.dat.taxmanager.data.remote.dto.AuthResponse
import com.dat.taxmanager.data.remote.dto.LoginRequest
import com.dat.taxmanager.data.remote.dto.PriceResponse
import com.dat.taxmanager.data.remote.dto.RegisterRequest // Thêm dòng import này
import com.dat.taxmanager.data.remote.dto.TransactionSyncRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @GET("api/rates")
    suspend fun getRates(): Response<PriceResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/transactions")
    suspend fun syncTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionSyncRequest
    ): Response<Any>

    // Tải dữ liệu từ mây về
    @GET("api/transactions")
    suspend fun getMyTransactions(
        @Header("Authorization") token: String
    ): retrofit2.Response<List<com.dat.taxmanager.data.remote.dto.TransactionResponse>>
}