package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.BizPilotViewModel
import com.example.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: BizPilotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "LOW_STOCK", "Groceries", "Electronics", "Fashion", "Hardware"
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    // Dialog state variables
    var inputName by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Electronics") }
    var inputCostPrice by remember { mutableStateOf("") }
    var inputSellingPrice by remember { mutableStateOf("") }
    var inputStockQuantity by remember { mutableStateOf("") }
    var inputReorderLevel by remember { mutableStateOf("5") }
    var inputSupplierName by remember { mutableStateOf("") }
    var inputSku by remember { mutableStateOf("") }
    var inputBarcode by remember { mutableStateOf("") }

    fun openAddDialog() {
        editingProduct = null
        inputName = ""
        inputCategory = "Electronics"
        inputCostPrice = ""
        inputSellingPrice = ""
        inputStockQuantity = ""
        inputReorderLevel = "5"
        inputSupplierName = "General Wholesalers"
        inputSku = "SKU-" + (1000..9999).random()
        inputBarcode = "BAR-" + (100000..999999).random()
        showAddOrEditDialog = true
    }

    fun openEditDialog(product: ProductEntity) {
        editingProduct = product
        inputName = product.name
        inputCategory = product.category
        inputCostPrice = product.costPrice.toString()
        inputSellingPrice = product.sellingPrice.toString()
        inputStockQuantity = product.stockQuantity.toString()
        inputReorderLevel = product.reorderLevel.toString()
        inputSupplierName = product.supplierName
        inputSku = product.sku
        inputBarcode = product.barcode
        showAddOrEditDialog = true
    }

    // Computed Inventory Metrics
    val totalValuation = remember(products) { products.sumOf { it.stockQuantity * it.sellingPrice } }
    val totalCostValuation = remember(products) { products.sumOf { it.stockQuantity * it.costPrice } }
    val totalPotentialProfit = totalValuation - totalCostValuation
    val lowStockCount = remember(products) { products.count { it.stockQuantity <= it.reorderLevel } }

    val filteredProducts = remember(products, searchQuery, selectedCategoryFilter) {
        products.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.sku.contains(searchQuery, ignoreCase = true) ||
                    item.barcode.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedCategoryFilter) {
                "ALL" -> true
                "LOW_STOCK" -> item.stockQuantity <= item.reorderLevel
                else -> item.category.equals(selectedCategoryFilter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Inventory Management",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${products.size} Products Registered • Valuation: ${viewModel.formatPrice(totalValuation)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { openAddDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_inventory_item_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Product", fontWeight = FontWeight.Bold)
            }
        }

        // Summary Statistics Cards (Stock Valuation, Items Count, Low Stock Warning)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Valuation & Profit
            GlassCard(
                modifier = Modifier
                    .weight(1.3f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(14.dp))
                        Text("Stock Valuation", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        viewModel.formatPrice(totalValuation),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Est. Margin: +${viewModel.formatPrice(totalPotentialProfit)}",
                        fontSize = 9.sp,
                        color = MpesaGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Card 2: Low Stock Warning Count
            GlassCard(
                borderColor = if (lowStockCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MpesaGreen.copy(alpha = 0.3f),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedCategoryFilter = if (selectedCategoryFilter == "LOW_STOCK") "ALL" else "LOW_STOCK" }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            if (lowStockCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MpesaGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Low Stock Alerts", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "$lowStockCount Items",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MpesaGreen
                    )
                    Text(
                        if (lowStockCount > 0) "Below threshold! Tap to view" else "All stock adequate",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Low Stock Banner Alert
        if (lowStockCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                "⚠️ $lowStockCount items at or below reorder threshold!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Reorder from suppliers to avoid stockout during peak business hours.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    AssistChip(
                        onClick = { selectedCategoryFilter = "LOW_STOCK" },
                        label = { Text("Filter Low Stock", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    )
                }
            }
        }

        // Category & Low-Stock Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            val filters = listOf(
                "ALL" to "All Items 📦",
                "LOW_STOCK" to "Low Stock Alerts ⚠️ ($lowStockCount)",
                "Electronics" to "Electronics ⚡",
                "Groceries" to "Groceries 🛒",
                "Fashion" to "Fashion 👗",
                "Hardware" to "Hardware 🛠️"
            )

            items(filters) { (code, label) ->
                FilterChip(
                    selected = selectedCategoryFilter == code,
                    onClick = { selectedCategoryFilter = code },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (code == "LOW_STOCK") MaterialTheme.colorScheme.errorContainer else MpesaGreen.copy(alpha = 0.2f),
                        selectedLabelColor = if (code == "LOW_STOCK") MaterialTheme.colorScheme.error else MpesaGreen
                    )
                )
            }
        }

        // Search Text Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by Item Name, SKU, Barcode, Supplier...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MpesaGreen) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Inventory Product List
        if (filteredProducts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Text(
                        "No inventory items found",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap '+ Add Product' above to create a new item with unit prices and stock levels.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts) { item ->
                    val isLowStock = item.stockQuantity <= item.reorderLevel
                    val unitProfit = item.sellingPrice - item.costPrice
                    val marginPercent = if (item.sellingPrice > 0) ((unitProfit / item.sellingPrice) * 100).toInt() else 0

                    GlassCard(
                        borderColor = if (isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_item_${item.id}")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Top Row: Name, Category, Stock Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                item.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "SKU: ${item.sku} • Barcode: ${item.barcode} • Supplier: ${item.supplierName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                // Current Stock Level Badge
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MpesaGreen.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isLowStock) MaterialTheme.colorScheme.error else MpesaGreen.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${item.stockQuantity} in Stock",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isLowStock) MaterialTheme.colorScheme.error else MpesaGreen
                                        )
                                        Text(
                                            text = "Threshold: ${item.reorderLevel} units",
                                            fontSize = 9.sp,
                                            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Middle Row: Individual Unit Prices (Cost Price, Selling Price, Margin)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Selling Price:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            viewModel.formatPrice(item.sellingPrice),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MpesaGreen
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Unit Cost Price:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            viewModel.formatPrice(item.costPrice),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "(Margin: +${viewModel.formatPrice(unitProfit)} / $marginPercent%)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SecondaryAmber
                                        )
                                    }
                                }

                                // Quick Stock Adjuster (+ / -)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.adjustStock(item, -1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircle, contentDescription = "Decrease Stock", tint = MaterialTheme.colorScheme.error)
                                    }

                                    Text(
                                        "${item.stockQuantity}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.adjustStock(item, 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.AddCircle, contentDescription = "Add Stock", tint = MpesaGreen)
                                    }
                                }
                            }

                            // Bottom Row: Action Buttons (Edit, Delete, WhatsApp Quote Share)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { openEditDialog(item) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Item", modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit Details", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { productToDelete = item },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Item", modifier = Modifier.size(12.dp))
                                    }
                                }

                                // Share Quote on WhatsApp
                                TextButton(
                                    onClick = {
                                        val quoteText = "📦 *PRODUCT CATALOG QUOTE - BizPilot*\n\n" +
                                                "*Item:* ${item.name}\n" +
                                                "*Category:* ${item.category}\n" +
                                                "*Unit Price:* ${viewModel.formatPrice(item.sellingPrice)}\n" +
                                                "*Stock Status:* ${if (item.stockQuantity > 0) "In Stock (${item.stockQuantity} units available)" else "Out of Stock"}\n\n" +
                                                "Contact us to place an order or inquire about wholesale!"
                                        WhatsAppHelper.openWhatsAppChat(context, null, quoteText)
                                    },
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Share Quote", tint = MpesaGreen, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("WhatsApp Quote", fontSize = 10.sp, color = MpesaGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Add or Edit Inventory Item
    if (showAddOrEditDialog) {
        val isEditing = editingProduct != null

        AlertDialog(
            onDismissRequest = { showAddOrEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (isEditing) Icons.Default.Edit else Icons.Default.AddBox,
                        contentDescription = null,
                        tint = MpesaGreen
                    )
                    Text(
                        if (isEditing) "Edit Inventory Item Details" else "Add New Inventory Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Specify item name, current stock level, low-stock threshold, and unit cost/selling prices.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Item Name
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. Solar Inverter 3.5kVA") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_item_name"),
                        singleLine = true
                    )

                    // Category
                    OutlinedTextField(
                        value = inputCategory,
                        onValueChange = { inputCategory = it },
                        label = { Text("Category") },
                        placeholder = { Text("e.g. Electronics, Groceries") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Stock Quantity & Low-Stock Threshold (Reorder Level)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputStockQuantity,
                            onValueChange = { inputStockQuantity = it },
                            label = { Text("Current Stock *") },
                            placeholder = { Text("e.g. 25") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_stock_quantity"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = inputReorderLevel,
                            onValueChange = { inputReorderLevel = it },
                            label = { Text("Low-Stock Threshold *") },
                            placeholder = { Text("e.g. 5") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_reorder_level"),
                            singleLine = true
                        )
                    }

                    // Individual Unit Prices: Cost Price & Selling Price
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputCostPrice,
                            onValueChange = { inputCostPrice = it },
                            label = { Text("Unit Cost Price *") },
                            placeholder = { Text("e.g. 1200") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_cost_price"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = inputSellingPrice,
                            onValueChange = { inputSellingPrice = it },
                            label = { Text("Unit Selling Price *") },
                            placeholder = { Text("e.g. 1800") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_selling_price"),
                            singleLine = true
                        )
                    }

                    // Supplier & SKU
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputSupplierName,
                            onValueChange = { inputSupplierName = it },
                            label = { Text("Supplier Name") },
                            placeholder = { Text("e.g. SunPower Ltd") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = inputSku,
                            onValueChange = { inputSku = it },
                            label = { Text("SKU Code") },
                            placeholder = { Text("e.g. SKU-1092") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = inputCostPrice.toDoubleOrNull() ?: 0.0
                        val price = inputSellingPrice.toDoubleOrNull() ?: 0.0
                        val stock = inputStockQuantity.toIntOrNull() ?: 0
                        val reorder = inputReorderLevel.toIntOrNull() ?: 5

                        if (inputName.isNotBlank()) {
                            if (isEditing && editingProduct != null) {
                                val updated = editingProduct!!.copy(
                                    name = inputName,
                                    category = inputCategory.ifBlank { "General" },
                                    costPrice = cost,
                                    sellingPrice = price,
                                    stockQuantity = stock,
                                    reorderLevel = reorder,
                                    supplierName = inputSupplierName.ifBlank { "General Wholesaler" },
                                    sku = inputSku.ifBlank { editingProduct!!.sku },
                                    barcode = inputBarcode.ifBlank { editingProduct!!.barcode }
                                )
                                viewModel.updateProduct(updated)
                                Toast.makeText(context, "✅ Updated '${inputName}' inventory details!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addProduct(
                                    name = inputName,
                                    category = inputCategory.ifBlank { "General" },
                                    cost = cost,
                                    price = price,
                                    stock = stock,
                                    supplier = inputSupplierName.ifBlank { "General Wholesaler" },
                                    reorderLevel = reorder,
                                    sku = inputSku,
                                    barcode = inputBarcode
                                )
                                Toast.makeText(context, "✅ Added '${inputName}' to inventory!", Toast.LENGTH_SHORT).show()
                            }
                            showAddOrEditDialog = false
                        } else {
                            Toast.makeText(context, "Please enter a valid item name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                    modifier = Modifier.testTag("save_inventory_item_button")
                ) {
                    Text(if (isEditing) "Save Changes ✓" else "Add Item to Inventory ✓", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOrEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Dialog: Delete Item Confirmation
    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text("Delete Inventory Item?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to remove '${prod.name}' (SKU: ${prod.sku}) from the inventory database? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod.id)
                        Toast.makeText(context, "Removed '${prod.name}' from inventory", Toast.LENGTH_SHORT).show()
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
