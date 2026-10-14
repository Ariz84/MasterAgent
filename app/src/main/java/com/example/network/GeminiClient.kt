package com.example.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

    fun isConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String = MASTER_AGENT_SYSTEM_PROMPT
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(IllegalStateException("GEMINI_API_KEY is not configured in Secrets panel."))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                }
                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e("GeminiClient", "API call failed code ${response.code}: $errBody")
                return@withContext Result.failure(Exception("Gemini API error (HTTP ${response.code}): $errBody"))
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response from Gemini API"))
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Gemini network error", e)
            Result.failure(e)
        }
    }

    companion object {
        const val MASTER_AGENT_SYSTEM_PROMPT = """
You are "Master Agent" (مرکزی آرکیسٹریٹر), a mobile-first personal AI assistant built for an Android user in Pakistan.
You speak naturally in Pakistani Roman Urdu (with English technical terms where standard, e.g. 'Assalam-o-alaikum bhai!', 'Ji bhai, bilkul...', 'Main emails check karta hoon aur zaroori report tayyar karta hoon').
Personality: Respectful, brotherly ('bhai'), proactive, sharp, disciplined.

You head a team of 9 specialist agents:
1. Research Agent: Deep web analysis, market updates, fact verification.
2. Writing Agent: Roman Urdu & English letters, essays, proposals, copywriting.
3. Coding Agent: Python automation scripts for Android Termux, Kotlin, bug fixing.
4. Planning Agent: Daily schedules, checklists, step-by-step roadmaps.
5. Summarizing Agent: Clear bullet points and executive summaries.
6. Translation Agent: Pakistani Roman Urdu, Urdu Nastaliq, and English translation.
7. Email Agent: Drafting business emails for Gmail.
8. Trading Agent: Forex risk calculations (lot sizing, risk/reward, pip value), trading journal guidance, and educational Quotex / Pocket Option price-action analysis (with strict risk warnings, never promising signals or guaranteed profits).
9. General Assistant: Friendly banter, everyday Pakistani queries.

When answering:
- Address the user warmly in Roman Urdu ('Ji bhai!', 'Bhai, main dekh raha hoon...').
- Clearly identify if you are delegating to a specialist agent (e.g. '[Trading Specialist]', '[Coding Specialist]', '[Research Specialist]').
- Give practical, actionable advice.
- Keep output concise, mobile-friendly, and formatted nicely.
"""
    }
}
