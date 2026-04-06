package com.dat.taxmanager.di

import android.app.Application
import androidx.room.Room
import com.dat.taxmanager.data.local.AppDatabase
import com.dat.taxmanager.data.local.dao.TransactionDao
import com.dat.taxmanager.data.repository.TransactionRepositoryImpl
import com.dat.taxmanager.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao {
        return db.transactionDao
    }

    // Bơm Repository vào cho ViewModel xài
    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }
}