package com.example.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

object CommunicationManager {

    enum class IntegrationStatus(val label: String, val isConnected: Boolean, val note: String) {
        WHATSAPP_INTENT("WhatsApp Intent Dispatch", true, "Directly opens official WhatsApp app with pre-filled message"),
        SMS_INTENT("Android System SMS", true, "Launches default Android SMS app with recipient & text"),
        EMAIL_INTENT("Android Email / Gmail Client", true, "Opens installed email client (Gmail) with subject & draft"),
        GMAIL_API_SYNC("Gmail Background Sync", false, "Requires Google Cloud OAuth2 credentials to read inbox autonomously"),
        WHATSAPP_BUSINESS_API("WhatsApp Cloud API", false, "Requires Meta Business Verification token for automated background sending")
    }

    fun openWhatsApp(context: Context, phoneNumber: String? = null, text: String): Result<String> {
        return try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val uri = if (!phoneNumber.isNullOrBlank()) {
                val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
            }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("WhatsApp opened with message draft.")
        } catch (e: Exception) {
            Result.failure(Exception("Could not launch WhatsApp. Please make sure WhatsApp is installed: ${e.localizedMessage}"))
        }
    }

    fun openSms(context: Context, phoneNumber: String? = null, text: String): Result<String> {
        return try {
            val cleanPhone = phoneNumber?.replace(Regex("[^0-9+]"), "") ?: ""
            val uri = Uri.parse("smsto:$cleanPhone")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("SMS client opened with prepared message.")
        } catch (e: Exception) {
            Result.failure(Exception("Could not open SMS app: ${e.localizedMessage}"))
        }
    }

    fun openEmail(
        context: Context,
        recipient: String? = null,
        subject: String = "Regarding Inquiry",
        body: String
    ): Result<String> {
        return try {
            val emailUri = Uri.parse("mailto:${recipient ?: ""}")
            val intent = Intent(Intent.ACTION_SENDTO, emailUri).apply {
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Email client opened with draft.")
        } catch (e: Exception) {
            Result.failure(Exception("Could not open Email app: ${e.localizedMessage}"))
        }
    }
}
