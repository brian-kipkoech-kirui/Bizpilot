package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.model.*

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ExpenseEntity::class,
        CustomerEntity::class,
        CustomerInteractionEntity::class,
        EmployeeEntity::class,
        InvoiceEntity::class,
        AiChatEntity::class,
        MarketingCampaignEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BizPilotDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerInteractionDao(): CustomerInteractionDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun marketingCampaignDao(): MarketingCampaignDao

    companion object {
        @Volatile
        private var INSTANCE: BizPilotDatabase? = null

        fun getDatabase(context: Context): BizPilotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BizPilotDatabase::class.java,
                    "bizpilot_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
