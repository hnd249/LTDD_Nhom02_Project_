package com.dat.taxmanager.domain.usecase

import com.dat.taxmanager.data.local.entity.TransactionEntity
import javax.inject.Inject

class GetStatsUseCase @Inject constructor() {
    fun execute(transactions: List<TransactionEntity>): Map<String, Double> {
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        return mapOf(
            "income" to totalIncome,
            "expense" to totalExpense,
            "balance" to balance
        )
    }
}