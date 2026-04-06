package com.dat.taxmanager.taxmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String,    // 'INCOME' hoặc 'EXPENSE'
    val category: String,
    val note: String = "",
    val source: String = "",

    val timestamp: String,

    val isTaxable: Boolean = false,
    val categoryIconId: Int = 0
)