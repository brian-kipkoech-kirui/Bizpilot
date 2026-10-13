package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    /**
     * Opens WhatsApp chat directly with a given phone number and pre-filled message.
     * If phone number is provided, formats it to international digits.
     */
    fun openWhatsAppChat(context: Context, phoneNumber: String?, message: String) {
        try {
            // Clean phone number (strip spaces, dashes, +, brackets)
            val cleanPhone = phoneNumber?.replace(Regex("[^0-9]"), "") ?: ""
            // Default to Kenya international prefix 254 if number starts with 0
            val formattedPhone = if (cleanPhone.startsWith("0") && cleanPhone.length == 10) {
                "254" + cleanPhone.substring(1)
            } else {
                cleanPhone
            }

            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uriString = if (formattedPhone.isNotEmpty()) {
                "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to Android text share intent
            try {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, message)
                    type = "text/plain"
                    `package` = "com.whatsapp"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(sendIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Constructs a formatted digital sales receipt for WhatsApp sharing.
     */
    fun buildReceiptText(
        businessName: String = "BizPilot Retail & Tech Mart",
        receiptNo: String,
        itemsSummary: String,
        totalAmount: String,
        paymentMethod: String,
        paymentRef: String
    ): String {
        val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val sb = StringBuilder()
        sb.append("🧾 *OFFICIAL RECEIPT - $businessName*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📋 *Receipt No:* `$receiptNo`\n")
        sb.append("📅 *Date:* $dateStr\n\n")
        if (itemsSummary.isNotBlank()) {
            sb.append("🛒 *Items Purchased:*\n")
            sb.append(itemsSummary)
            sb.append("\n\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💰 *Total Amount Paid:* *$totalAmount*\n")
        sb.append("💳 *Payment Method:* $paymentMethod\n")
        if (paymentRef.isNotBlank()) {
            sb.append("🔑 *Transaction Ref:* `$paymentRef`\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Thank you for shopping with us! 🙏\n")
        sb.append("For inquiries, reply to this WhatsApp message.\n")
        sb.append("_Powered by BizPilot AI Smart POS_")
        return sb.toString()
    }

    /**
     * Constructs a CRM customer message template.
     */
    fun buildCrmTemplate(
        customerName: String,
        templateType: String,
        businessName: String = "BizPilot Store",
        customData: String = ""
    ): String {
        val firstName = customerName.split(" ").firstOrNull() ?: customerName
        return when (templateType) {
            "Payment Reminder" -> "Hello $firstName! 👋 This is a friendly reminder from $businessName regarding your pending invoice of $customData. Kindly let us know when settled. Thank you!"
            "Loyalty Reward" -> "Congratulations $firstName! 🎉 You have earned *${customData}* loyalty points at $businessName. Visit us today to redeem your exclusive discount voucher!"
            "Order Status Update" -> "Hi $firstName, your order ($customData) at $businessName has been processed and is ready for pickup/delivery! 🚚"
            "Feedback & Followup" -> "Hello $firstName! Thank you for visiting $businessName recently. How was your experience? We value your feedback! 😊"
            else -> "Hello $firstName! $customData"
        }
    }
}
