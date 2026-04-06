package com.dat.taxmanager.domain.repository

import com.dat.taxmanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>
    fun getTaxableIncomes(): Flow<List<TransactionEntity>>

    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)

    suspend fun clearAllTransactions()
}