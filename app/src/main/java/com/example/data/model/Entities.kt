package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String,
    val sku: String,
    val category: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val reorderLevel: Int,
    val supplierName: String,
    val imageUrl: String = ""
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNo: String,
    val customerName: String,
    val totalAmount: Double,
    val discount: Double,
    val tax: Double,
    val paymentMethod: String, // M-Pesa, Airtel Money, Card, Cash, QR
    val paymentReference: String,
    val status: String, // Completed, Refunded
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Rent, Utilities, Inventory, Salaries, Marketing, Operations
    val amount: Double,
    val currency: String,
    val description: String,
    val vendor: String,
    val paymentMethod: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val totalSpent: Double = 0.0,
    val loyaltyPoints: Int = 0,
    val tier: String = "Bronze", // Bronze, Silver, Gold, VIP
    val purchaseHistory: String = "" // Log of past purchases and transaction summary
)

@Entity(tableName = "customer_interactions")
data class CustomerInteractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val interactionType: String, // Phone Call, WhatsApp, In-Person Visit, Email, Payment Inquiry, Complaint, Quote Request, Follow-up
    val summary: String,
    val outcome: String = "Resolved", // Pending, Resolved, Follow-up Required, Closed
    val timestamp: Long = System.currentTimeMillis(),
    val loggedBy: String = "Owner"
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String, // Owner, Manager, Cashier, Accountant, Employee
    val phone: String,
    val pin: String,
    val status: String // Active, On Leave
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String,
    val totalAmount: Double,
    val dueDate: String,
    val status: String, // Paid, Pending, Overdue
    val itemsSummary: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_chats")
data class AiChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "assistant"
    val message: String,
    val actionType: String = "", // e.g. "invoice_generated", "recommendation", "expense_logged"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketing_campaigns")
data class MarketingCampaignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val channel: String, // Telegram, WhatsApp, Facebook, SMS, Email
    val message: String,
    val targetAudience: String,
    val status: String, // Active, Scheduled, Draft
    val conversionsCount: Int = 0
)
