package com.example.network

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TermuxConnector(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("master_agent_termux_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    var baseUrl: String
        get() = prefs.getString("termux_base_url", "http://127.0.0.1:8000") ?: "http://127.0.0.1:8000"
        set(value) = prefs.edit().putString("termux_base_url", value.trim().removeSuffix("/")).apply()

    var authToken: String
        get() = prefs.getString("termux_auth_token", "") ?: ""
        set(value) = prefs.edit().putString("termux_auth_token", value.trim()).apply()

    var isEnabled: Boolean
        get() = prefs.getBoolean("termux_enabled", false)
        set(value) = prefs.edit().putBoolean("termux_enabled", value).apply()

    suspend fun pingHealth(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/health"
            val requestBuilder = Request.Builder()
                .url(url)
                .get()

            if (authToken.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $authToken")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                Result.success("Connected to Termux server successfully! Response: $body")
            } else {
                Result.failure(Exception("Termux returned HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Termux offline or unreachable on $baseUrl. Error: ${e.localizedMessage ?: "Connection refused"}"))
        }
    }

    suspend fun sendPrompt(prompt: String): Result<TermuxResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/api/chat"
            val jsonPayload = JSONObject().apply {
                put("message", prompt)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)

            if (authToken.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $authToken")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: "{}"
                val json = JSONObject(bodyStr)
                val reply = json.optString("response", json.optString("reply", bodyStr))
                val agent = json.optString("agent", "CODING")
                Result.success(TermuxResponse(text = reply, agent = agent))
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val SAMPLE_PYTHON_SERVER = """# master_agent_server.py
# Run this inside Android Termux:
# 1. pkg update && pkg install python
# 2. pip install flask
# 3. python master_agent_server.py

from flask import Flask, request, jsonify

app = Flask(__name__)

# Optional bearer token protection
AUTH_TOKEN = ""

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "online",
        "agent": "Termux Python Master Agent",
        "environment": "Android Termux localhost"
    })

@app.route('/api/chat', methods=['POST'])
def chat():
    # Verify token if set
    auth_header = request.headers.get('Authorization', '')
    if AUTH_TOKEN and auth_header != f"Bearer {AUTH_TOKEN}":
        return jsonify({"error": "Unauthorized"}), 401

    data = request.json or {}
    user_msg = data.get('message', '')
    
    # Delegate to your existing Termux python scripts or logic:
    reply = f"Ji bhai! Termux backend se response: Aapka message mila: '{user_msg}'"
    return jsonify({
        "response": reply,
        "agent": "CODING",
        "status": "success"
    })

if __name__ == '__main__':
    print("Master Agent Termux Server starting on port 8000...")
    app.run(host='127.0.0.1', port=8000)
"""
    }
}

data class TermuxResponse(
    val text: String,
    val agent: String
)
