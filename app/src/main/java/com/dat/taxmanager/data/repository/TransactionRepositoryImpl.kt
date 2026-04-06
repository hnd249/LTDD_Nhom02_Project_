package com.dat.taxmanager.data.repository

import com.dat.taxmanager.data.local.dao.TransactionDao
import com.dat.taxmanager.data.local.entity.TransactionEntity
import com.dat.taxmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override suspend fun clearAllTransactions() { dao.clearAllTransactions()}

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    override fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> = dao.getTransactionsByType(type)

    override fun getTaxableIncomes(): Flow<List<TransactionEntity>> = dao.getTaxableIncomes()

    override suspend fun insertTransaction(transaction: TransactionEntity) = dao.insertTransaction(transaction)

    override suspend fun updateTransaction(transaction: TransactionEntity) = dao.updateTransaction(transaction)

    override suspend fun deleteTransaction(transaction: TransactionEntity) = dao.deleteTransaction(transaction)


}