package com.dat.taxmanager.taxmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dat.taxmanager.data.local.dao.TransactionDao
import com.dat.taxmanager.data.local.entity.TransactionEntity

// version = 1 vì chúng ta sẽ cài lại app mới hoàn toàn
@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val transactionDao: TransactionDao

    companion object {
        const val DATABASE_NAME = "tax_manager_db"
    }
}