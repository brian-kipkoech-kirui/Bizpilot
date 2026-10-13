package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stockQuantity <= reorderLevel")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("UPDATE products SET stockQuantity = stockQuantity + :change WHERE id = :id")
    suspend fun adjustStock(id: Long, change: Int)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: Long)

    @Query("SELECT * FROM sales WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedSales(): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedSalesFlow(): Flow<List<SaleEntity>>

    @Query("UPDATE sales SET isSynced = 1 WHERE id = :saleId")
    suspend fun markSaleSynced(saleId: Long)

    @Query("SELECT * FROM sales WHERE timestamp >= :startTime")
    fun getSalesSince(startTime: Long): Flow<List<SaleEntity>>
}

@Dao
interface SaleItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSale(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSaleSync(saleId: Long): List<SaleItemEntity>

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteItemsForSale(saleId: Long)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("SELECT * FROM expenses WHERE timestamp >= :startTime")
    fun getExpensesSince(startTime: Long): Flow<List<ExpenseEntity>>
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY totalSpent DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: Long)

    @Query("UPDATE customers SET totalSpent = totalSpent + :amount, loyaltyPoints = loyaltyPoints + :points, purchaseHistory = :purchaseHistory WHERE id = :id")
    suspend fun updatePurchaseHistory(id: Long, amount: Double, points: Int, purchaseHistory: String)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateInvoiceStatus(id: Long, status: String)
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chats ORDER BY timestamp ASC")
    fun getAllChats(): Flow<List<AiChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: AiChatEntity): Long

    @Query("DELETE FROM ai_chats")
    suspend fun clearHistory()
}

@Dao
interface MarketingCampaignDao {
    @Query("SELECT * FROM marketing_campaigns ORDER BY id DESC")
    fun getAllCampaigns(): Flow<List<MarketingCampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: MarketingCampaignEntity): Long
}

@Dao
interface CustomerInteractionDao {
    @Query("SELECT * FROM customer_interactions ORDER BY timestamp DESC")
    fun getAllInteractions(): Flow<List<CustomerInteractionEntity>>

    @Query("SELECT * FROM customer_interactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getInteractionsForCustomer(customerId: Long): Flow<List<CustomerInteractionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: CustomerInteractionEntity): Long

    @Query("DELETE FROM customer_interactions WHERE id = :id")
    suspend fun deleteInteraction(id: Long)
}
