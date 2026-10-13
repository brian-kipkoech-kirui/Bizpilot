package com.example.repository

import com.example.data.database.BizPilotDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class BizPilotRepository(private val db: BizPilotDatabase) {
    val products: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = db.productDao().getLowStockProducts()
    val sales: Flow<List<SaleEntity>> = db.saleDao().getAllSales()
    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val customers: Flow<List<CustomerEntity>> = db.customerDao().getAllCustomers()
    val customerInteractions: Flow<List<CustomerInteractionEntity>> = db.customerInteractionDao().getAllInteractions()
    val employees: Flow<List<EmployeeEntity>> = db.employeeDao().getAllEmployees()
    val invoices: Flow<List<InvoiceEntity>> = db.invoiceDao().getAllInvoices()
    val aiChats: Flow<List<AiChatEntity>> = db.aiChatDao().getAllChats()
    val campaigns: Flow<List<MarketingCampaignEntity>> = db.marketingCampaignDao().getAllCampaigns()

    suspend fun insertProduct(product: ProductEntity) = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(id: Long) = db.productDao().deleteProduct(id)
    suspend fun adjustStock(id: Long, delta: Int) = db.productDao().adjustStock(id, delta)

    suspend fun recordSale(
        customerName: String,
        totalAmount: Double,
        discount: Double,
        tax: Double,
        paymentMethod: String,
        paymentRef: String,
        cartItems: List<Pair<ProductEntity, Int>>
    ) = withContext(Dispatchers.IO) {
        val receiptNo = "REC-" + (1000..9999).random()
        val sale = SaleEntity(
            receiptNo = receiptNo,
            customerName = customerName.ifBlank { "Walk-in Customer" },
            totalAmount = totalAmount,
            discount = discount,
            tax = tax,
            paymentMethod = paymentMethod,
            paymentReference = paymentRef.ifBlank { "MP-" + (10000..99999).random() },
            status = "Completed"
        )
        val saleId = db.saleDao().insertSale(sale)

        val saleItems = cartItems.map { (product, qty) ->
            // Deduct stock quantity
            db.productDao().adjustStock(product.id, -qty)
            SaleItemEntity(
                saleId = saleId,
                productId = product.id,
                productName = product.name,
                quantity = qty,
                unitPrice = product.sellingPrice
            )
        }
        db.saleItemDao().insertSaleItems(saleItems)
    }

    suspend fun addExpense(
        category: String,
        amount: Double,
        description: String,
        vendor: String,
        paymentMethod: String,
        currency: String = "KES"
    ) = db.expenseDao().insertExpense(
        ExpenseEntity(
            category = category,
            amount = amount,
            currency = currency,
            description = description,
            vendor = vendor,
            paymentMethod = paymentMethod
        )
    )

    suspend fun addCustomer(
        name: String,
        phone: String,
        email: String = "",
        address: String = "",
        notes: String = "",
        tier: String = "Bronze"
    ) = db.customerDao().insertCustomer(
        CustomerEntity(
            name = name,
            phone = phone,
            email = email,
            address = address,
            notes = notes,
            totalSpent = 0.0,
            loyaltyPoints = 10,
            tier = tier
        )
    )

    suspend fun updateCustomer(customer: CustomerEntity) = db.customerDao().updateCustomer(customer)

    suspend fun deleteCustomer(id: Long) = db.customerDao().deleteCustomer(id)

    suspend fun addCustomerInteraction(
        customerId: Long,
        customerName: String,
        type: String,
        summary: String,
        outcome: String = "Resolved"
    ) = db.customerInteractionDao().insertInteraction(
        CustomerInteractionEntity(
            customerId = customerId,
            customerName = customerName,
            interactionType = type,
            summary = summary,
            outcome = outcome
        )
    )

    suspend fun deleteCustomerInteraction(id: Long) = db.customerInteractionDao().deleteInteraction(id)

    suspend fun addInvoice(customerName: String, customerPhone: String, amount: Double, dueDate: String, itemsSummary: String) =
        db.invoiceDao().insertInvoice(
            InvoiceEntity(
                invoiceNumber = "INV-" + (2000..9999).random(),
                customerName = customerName,
                customerPhone = customerPhone,
                totalAmount = amount,
                dueDate = dueDate,
                status = "Pending",
                itemsSummary = itemsSummary
            )
        )

    suspend fun addAiChat(sender: String, message: String, actionType: String = "") =
        db.aiChatDao().insertChat(
            AiChatEntity(sender = sender, message = message, actionType = actionType)
        )

    suspend fun addCampaign(title: String, channel: String, message: String, targetAudience: String) =
        db.marketingCampaignDao().insertCampaign(
            MarketingCampaignEntity(
                title = title,
                channel = channel,
                message = message,
                targetAudience = targetAudience,
                status = "Active",
                conversionsCount = (5..32).random()
            )
        )

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingProducts = db.productDao().getAllProducts().first()
        if (existingProducts.isEmpty()) {
            // Seed products
            val initialProducts = listOf(
                ProductEntity(name = "Solar Inverter 1.5kVA", barcode = "6001001", sku = "SLR-150", category = "Electronics", costPrice = 18000.0, sellingPrice = 24500.0, stockQuantity = 8, reorderLevel = 3, supplierName = "SunPower Africa"),
                ProductEntity(name = "Organic Maize Flour 2kg", barcode = "6001002", sku = "MAI-002", category = "Groceries", costPrice = 140.0, sellingPrice = 195.0, stockQuantity = 4, reorderLevel = 10, supplierName = "Unga Millers"),
                ProductEntity(name = "Smart POS Terminal V2", barcode = "6001003", sku = "POS-V2", category = "Tech", costPrice = 12000.0, sellingPrice = 16500.0, stockQuantity = 15, reorderLevel = 5, supplierName = "PayTech Global"),
                ProductEntity(name = "Premium Kenyan Coffee 500g", barcode = "6001004", sku = "COF-500", category = "Groceries", costPrice = 650.0, sellingPrice = 980.0, stockQuantity = 25, reorderLevel = 8, supplierName = "Nairobi Coffee Exchange"),
                ProductEntity(name = "Fast Charge Type-C Cable", barcode = "6001005", sku = "CAB-USB", category = "Accessories", costPrice = 200.0, sellingPrice = 500.0, stockQuantity = 42, reorderLevel = 12, supplierName = "Anker Wholesale")
            )
            initialProducts.forEach { db.productDao().insertProduct(it) }

            // Seed Sales
            db.saleDao().insertSale(SaleEntity(receiptNo = "REC-8842", customerName = "Amina Mohamed", totalAmount = 24500.0, discount = 500.0, tax = 3840.0, paymentMethod = "M-Pesa", paymentReference = "QK89210X", status = "Completed"))
            db.saleDao().insertSale(SaleEntity(receiptNo = "REC-8843", customerName = "David Ochieng", totalAmount = 1960.0, discount = 0.0, tax = 270.0, paymentMethod = "M-Pesa", paymentReference = "QK89211Y", status = "Completed"))
            db.saleDao().insertSale(SaleEntity(receiptNo = "REC-8844", customerName = "Walk-in Customer", totalAmount = 1500.0, discount = 0.0, tax = 200.0, paymentMethod = "Cash", paymentReference = "CASH-101", status = "Completed"))

            // Seed Expenses
            db.expenseDao().insertExpense(ExpenseEntity(category = "Rent", amount = 35000.0, currency = "KES", description = "Monthly Shop Lease Plaza Ground Floor", vendor = "Westlands Realty", paymentMethod = "Bank Transfer"))
            db.expenseDao().insertExpense(ExpenseEntity(category = "Utilities", amount = 4200.0, currency = "KES", description = "KPLC Power Bill & High Speed Internet", vendor = "Kenya Power & Safaricom", paymentMethod = "M-Pesa Paybill"))
            db.expenseDao().insertExpense(ExpenseEntity(category = "Inventory", amount = 48000.0, currency = "KES", description = "Restock Solar Inverters & Cables", vendor = "SunPower Africa", paymentMethod = "M-Pesa"))

            // Seed Customers
            val c1Id = db.customerDao().insertCustomer(CustomerEntity(name = "Amina Mohamed", phone = "+254 712 345678", email = "amina@example.com", address = "Kilimani, Nairobi", notes = "Prefers M-Pesa receipt, regular tech buyer", totalSpent = 84500.0, loyaltyPoints = 420, tier = "Gold", purchaseHistory = "2x Solar Inverter 1.5kVA (REC-8842)"))
            val c2Id = db.customerDao().insertCustomer(CustomerEntity(name = "David Ochieng", phone = "+254 722 987654", email = "david.o@example.com", address = "Westlands, Nairobi", notes = "Restaurant owner, buys coffee in bulk", totalSpent = 32100.0, loyaltyPoints = 160, tier = "Silver", purchaseHistory = "20x Premium Kenyan Coffee 500g (REC-8843)"))
            val c3Id = db.customerDao().insertCustomer(CustomerEntity(name = "Grace Wanjiku", phone = "+254 733 112233", email = "grace@example.com", address = "Upper Hill, Nairobi", notes = "Inquired about solar generator installation", totalSpent = 12000.0, loyaltyPoints = 60, tier = "Bronze", purchaseHistory = "1x Fast Charge Type-C Cable"))

            // Seed Customer Interactions
            db.customerInteractionDao().insertInteraction(CustomerInteractionEntity(customerId = c1Id, customerName = "Amina Mohamed", interactionType = "WhatsApp", summary = "Sent invoice quotation INV-2026-001 for 4x Solar Inverters and 10x Cables.", outcome = "Resolved"))
            db.customerInteractionDao().insertInteraction(CustomerInteractionEntity(customerId = c1Id, customerName = "Amina Mohamed", interactionType = "Phone Call", summary = "Customer confirmed order receipt and inquired about warranty policy.", outcome = "Resolved"))
            db.customerInteractionDao().insertInteraction(CustomerInteractionEntity(customerId = c2Id, customerName = "David Ochieng", interactionType = "In-Person Visit", summary = "Visited shop to negotiate bulk coffee discount for August supply.", outcome = "Follow-up Required"))
            db.customerInteractionDao().insertInteraction(CustomerInteractionEntity(customerId = c3Id, customerName = "Grace Wanjiku", interactionType = "Quote Request", summary = "Requested price list for 3.5kVA solar inverter setup for home office.", outcome = "Pending"))

            // Seed Employees
            db.employeeDao().insertEmployee(EmployeeEntity(name = "Kipkirui Peter (You)", role = "Owner", phone = "+254 700 000000", pin = "1234", status = "Active"))
            db.employeeDao().insertEmployee(EmployeeEntity(name = "Sarah Kamau", role = "Manager", phone = "+254 711 223344", pin = "5678", status = "Active"))
            db.employeeDao().insertEmployee(EmployeeEntity(name = "Brian Kiprono", role = "Cashier", phone = "+254 722 334455", pin = "9999", status = "Active"))

            // Seed Invoices
            db.invoiceDao().insertInvoice(InvoiceEntity(invoiceNumber = "INV-2026-001", customerName = "Kilimani Tech Hub", customerPhone = "+254 701 555888", totalAmount = 98000.0, dueDate = "2026-08-05", status = "Pending", itemsSummary = "4x Solar Inverters, 10x Type-C Cables"))
            db.invoiceDao().insertInvoice(InvoiceEntity(invoiceNumber = "INV-2026-002", customerName = "Nairobi Coffee Club", customerPhone = "+254 702 444111", totalAmount = 19600.0, dueDate = "2026-07-28", status = "Paid", itemsSummary = "20x Premium Coffee Bags"))

            // Seed AI Welcome Chat
            db.aiChatDao().insertChat(AiChatEntity(sender = "assistant", message = "👋 Jambo! I am BizPilot AI — your 24/7 AI CEO & Business Copilot. How can I assist your business today? Ask me to analyze profits, generate a quotation, scan a receipt, or draft a Telegram promo!", actionType = "welcome"))

            // Seed Marketing
            db.marketingCampaignDao().insertCampaign(MarketingCampaignEntity(title = "M-Pesa Cashback Promo", channel = "Telegram", message = "Get 5% instant cashback on all orders made via Telegram Mini App today!", targetAudience = "All Customers", status = "Active", conversionsCount = 28))
        }
    }
}
