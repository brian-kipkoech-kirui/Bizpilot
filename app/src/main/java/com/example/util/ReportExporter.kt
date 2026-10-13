package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileNameDateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Generates a CSV file containing Sales transactions data.
     */
    fun exportSalesCsv(
        context: Context,
        sales: List<SaleEntity>,
        formatPrice: (Double) -> String
    ): File? {
        return try {
            val file = File(context.cacheDir, "BizPilot_Sales_Report_${fileNameDateFormatter.format(Date())}.csv")
            val writer = file.bufferedWriter()

            // Header
            writer.write("Receipt No,Customer Name,Date,Payment Method,Payment Ref,Discount (KES),Tax (KES),Total Amount (KES),Status\n")

            // Rows
            sales.forEach { sale ->
                val dateStr = dateFormatter.format(Date(sale.timestamp))
                val safeCustomer = sale.customerName.replace("\"", "\"\"")
                val safeMethod = sale.paymentMethod.replace("\"", "\"\"")
                val safeRef = sale.paymentReference.replace("\"", "\"\"")

                writer.write(
                    "\"${sale.receiptNo}\",\"$safeCustomer\",\"$dateStr\",\"$safeMethod\",\"$safeRef\",${sale.discount},${sale.tax},${sale.totalAmount},\"${sale.status}\"\n"
                )
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a CSV file containing Inventory & Stock audit data.
     */
    fun exportInventoryCsv(
        context: Context,
        products: List<ProductEntity>,
        formatPrice: (Double) -> String
    ): File? {
        return try {
            val file = File(context.cacheDir, "BizPilot_Inventory_Audit_${fileNameDateFormatter.format(Date())}.csv")
            val writer = file.bufferedWriter()

            // Header
            writer.write("SKU,Barcode,Product Name,Category,Cost Price (KES),Selling Price (KES),Stock Qty,Reorder Level,Total Stock Value (KES),Supplier\n")

            // Rows
            products.forEach { prod ->
                val safeName = prod.name.replace("\"", "\"\"")
                val safeCategory = prod.category.replace("\"", "\"\"")
                val safeSupplier = prod.supplierName.replace("\"", "\"\"")
                val stockValue = prod.costPrice * prod.stockQuantity

                writer.write(
                    "\"${prod.sku}\",\"${prod.barcode}\",\"$safeName\",\"$safeCategory\",${prod.costPrice},${prod.sellingPrice},${prod.stockQuantity},${prod.reorderLevel},$stockValue,\"$safeSupplier\"\n"
                )
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a combined Business Performance PDF Report using Android Native PdfDocument.
     */
    fun exportExecutivePdf(
        context: Context,
        sales: List<SaleEntity>,
        products: List<ProductEntity>,
        expenses: List<ExpenseEntity>,
        formatPrice: (Double) -> String
    ): File? {
        val pdfDocument = PdfDocument()

        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points
        val margin = 36f

        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Paints
        val paintText = Paint().apply { isAntiAlias = true }
        val paintLine = Paint().apply { isAntiAlias = true; strokeWidth = 1f; color = Color.LTGRAY }
        val paintFill = Paint().apply { isAntiAlias = true }

        var y = margin

        fun startNewPage() {
            // Draw footer on current page
            paintText.textAlign = Paint.Align.CENTER
            paintText.textSize = 9f
            paintText.color = Color.DKGRAY
            paintText.typeface = Typeface.DEFAULT
            canvas.drawText(
                "BizPilot Enterprise ERP • Executive Business Report • Page $currentPageNumber",
                pageWidth / 2f,
                pageHeight - 20f,
                paintText
            )

            pdfDocument.finishPage(page)
            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = margin
        }

        fun checkPageOverflow(neededHeight: Float) {
            if (y + neededHeight > pageHeight - 50f) {
                startNewPage()
            }
        }

        // --- DRAW HEADER ---
        // Header background banner
        paintFill.color = Color.parseColor("#0F172A") // Navy dark
        canvas.drawRect(margin, y, pageWidth - margin, y + 60f, paintFill)

        paintText.color = Color.WHITE
        paintText.textSize = 18f
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textAlign = Paint.Align.LEFT
        canvas.drawText("BizPilot Business Intelligence Report", margin + 16f, y + 26f, paintText)

        paintText.textSize = 10f
        paintText.typeface = Typeface.DEFAULT
        paintText.color = Color.parseColor("#94A3B8")
        canvas.drawText("Generated on ${dateFormatter.format(Date())} • Confidential Internal Report", margin + 16f, y + 46f, paintText)

        y += 75f

        // --- FINANCIAL SUMMARY CARDS ---
        val totalRevenue = sales.sumOf { it.totalAmount }
        val totalExpenses = expenses.sumOf { it.amount }
        val netProfit = totalRevenue - totalExpenses
        val inventoryValuation = products.sumOf { it.costPrice * it.stockQuantity }
        val lowStockCount = products.count { it.stockQuantity <= it.reorderLevel }

        paintText.textSize = 13f
        paintText.color = Color.parseColor("#0F172A")
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. Financial & Operations Summary", margin, y, paintText)
        y += 12f

        // Card box background
        paintFill.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(margin, y, pageWidth - margin, y + 70f, 8f, 8f, paintFill)
        paintLine.color = Color.parseColor("#CBD5E1")
        canvas.drawRoundRect(margin, y, pageWidth - margin, y + 70f, 8f, 8f, paintLine)

        // Column widths
        val colWidth = (pageWidth - margin * 2) / 4f

        val metrics = listOf(
            "Total Sales" to formatPrice(totalRevenue),
            "Total Expenses" to formatPrice(totalExpenses),
            "Net Profit" to formatPrice(netProfit),
            "Inventory Assets" to formatPrice(inventoryValuation)
        )

        metrics.forEachIndexed { idx, (label, value) ->
            val x = margin + (idx * colWidth) + 12f
            paintText.textSize = 9f
            paintText.color = Color.parseColor("#64748B")
            paintText.typeface = Typeface.DEFAULT
            canvas.drawText(label, x, y + 24f, paintText)

            paintText.textSize = 11f
            paintText.color = if (label == "Net Profit") Color.parseColor("#16A34A") else Color.parseColor("#0F172A")
            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(value, x, y + 46f, paintText)
        }

        y += 85f

        // --- SALES TRANSACTIONS BREAKDOWN TABLE ---
        checkPageOverflow(100f)
        paintText.textSize = 13f
        paintText.color = Color.parseColor("#0F172A")
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. Recent Sales Transactions (${sales.size} Total)", margin, y, paintText)
        y += 14f

        // Table Header
        paintFill.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(margin, y, pageWidth - margin, y + 22f, paintFill)

        paintText.textSize = 9f
        paintText.color = Color.parseColor("#334155")
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val salesCols = listOf(margin + 6f, margin + 80f, margin + 220f, margin + 340f, pageWidth - margin - 80f)
        canvas.drawText("Receipt No", salesCols[0], y + 15f, paintText)
        canvas.drawText("Customer Name", salesCols[1], y + 15f, paintText)
        canvas.drawText("Payment Method", salesCols[2], y + 15f, paintText)
        canvas.drawText("Status", salesCols[3], y + 15f, paintText)
        canvas.drawText("Amount", salesCols[4], y + 15f, paintText)

        y += 22f

        sales.forEach { sale ->
            checkPageOverflow(20f)
            paintText.typeface = Typeface.DEFAULT
            paintText.textSize = 8.5f
            paintText.color = Color.parseColor("#1E293B")

            canvas.drawText(sale.receiptNo, salesCols[0], y + 14f, paintText)
            canvas.drawText(sale.customerName.take(22), salesCols[1], y + 14f, paintText)
            canvas.drawText("${sale.paymentMethod} (${sale.paymentReference.take(8)})", salesCols[2], y + 14f, paintText)
            
            paintText.color = if (sale.status == "Completed") Color.parseColor("#16A34A") else Color.RED
            canvas.drawText(sale.status, salesCols[3], y + 14f, paintText)

            paintText.color = Color.parseColor("#0F172A")
            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(formatPrice(sale.totalAmount), salesCols[4], y + 14f, paintText)

            y += 18f
            paintLine.color = Color.parseColor("#F1F5F9")
            canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
        }

        y += 20f

        // --- INVENTORY & STOCK VALUATION TABLE ---
        checkPageOverflow(100f)
        paintText.textSize = 13f
        paintText.color = Color.parseColor("#0F172A")
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. Inventory & Stock Audit (${products.size} SKUs, $lowStockCount Low Stock)", margin, y, paintText)
        y += 14f

        // Table Header
        paintFill.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(margin, y, pageWidth - margin, y + 22f, paintFill)

        paintText.textSize = 9f
        paintText.color = Color.parseColor("#334155")
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val invCols = listOf(margin + 6f, margin + 70f, margin + 230f, margin + 310f, margin + 380f, pageWidth - margin - 80f)
        canvas.drawText("SKU", invCols[0], y + 15f, paintText)
        canvas.drawText("Product Name", invCols[1], y + 15f, paintText)
        canvas.drawText("Category", invCols[2], y + 15f, paintText)
        canvas.drawText("Sell Price", invCols[3], y + 15f, paintText)
        canvas.drawText("Stock Qty", invCols[4], y + 15f, paintText)
        canvas.drawText("Stock Value", invCols[5], y + 15f, paintText)

        y += 22f

        products.forEach { prod ->
            checkPageOverflow(20f)
            paintText.typeface = Typeface.DEFAULT
            paintText.textSize = 8.5f
            paintText.color = Color.parseColor("#1E293B")

            canvas.drawText(prod.sku, invCols[0], y + 14f, paintText)
            canvas.drawText(prod.name.take(24), invCols[1], y + 14f, paintText)
            canvas.drawText(prod.category, invCols[2], y + 14f, paintText)
            canvas.drawText(formatPrice(prod.sellingPrice), invCols[3], y + 14f, paintText)

            paintText.color = if (prod.stockQuantity <= prod.reorderLevel) Color.RED else Color.parseColor("#16A34A")
            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${prod.stockQuantity} ${if (prod.stockQuantity <= prod.reorderLevel) "⚠️" else ""}", invCols[4], y + 14f, paintText)

            paintText.color = Color.parseColor("#0F172A")
            canvas.drawText(formatPrice(prod.costPrice * prod.stockQuantity), invCols[5], y + 14f, paintText)

            y += 18f
            paintLine.color = Color.parseColor("#F1F5F9")
            canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
        }

        // Draw last page footer
        paintText.textAlign = Paint.Align.CENTER
        paintText.textSize = 9f
        paintText.color = Color.DKGRAY
        paintText.typeface = Typeface.DEFAULT
        canvas.drawText(
            "BizPilot Enterprise ERP • Executive Business Report • Page $currentPageNumber",
            pageWidth / 2f,
            pageHeight - 20f,
            paintText
        )

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "BizPilot_Executive_Report_${fileNameDateFormatter.format(Date())}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Shares or opens the exported file using FileProvider.
     */
    fun shareExportedFile(context: Context, file: File, mimeType: String, title: String = "BizPilot Business Report") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Attached is the offline business report generated by BizPilot ERP & POS.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Business Report ($title)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error launching share dialog: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
