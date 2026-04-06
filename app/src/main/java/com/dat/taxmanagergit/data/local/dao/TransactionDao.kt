package com.dat.taxmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dat.taxmanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // 1. Lấy toàn bộ giao dịch (Sắp xếp mới nhất lên đầu)
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // 2. Lấy giao dịch theo Loại (Thu hoặc Chi) - Dùng để vẽ Biểu đồ Tròn
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    // 3. Lấy các khoản Thu nhập chịu thuế (Vũ khí bí mật để tính Thuế TNCN)
    @Query("SELECT * FROM transactions WHERE isTaxable = 1 AND type = 'INCOME'")
    fun getTaxableIncomes(): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}