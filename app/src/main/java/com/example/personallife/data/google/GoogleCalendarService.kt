package com.example.personallife.data.google

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class CalendarConnectionState {
    object NotConnected : CalendarConnectionState()
    object Connecting : CalendarConnectionState()
    data class Connected(
        val email: String,
        val calendarSummary: String,
        val timeZone: String,
        val eventCount: Int,
        val lastVerifiedAt: String
    ) : CalendarConnectionState()
    data class Error(val message: String, val details: String? = null) : CalendarConnectionState()
}

class GoogleCalendarService(private val context: Context? = null) {

    companion object {
        const val TARGET_ACCOUNT = "hidayatsyarifudin903@gmail.com"
        const val OAUTH_CLIENT_ID = "953959207754-hl14rig9jpg4lna4tq7der0uuscvltuc.apps.googleusercontent.com"
        const val REDIRECT_URI = "com.aistudio.personallife://oauth2redirect"
        const val SCOPE_CALENDAR_READONLY = "https://www.googleapis.com/auth/calendar.events.readonly"
        const val SCOPE_USERINFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email"
    }

    // In-memory token storage (never logged, never stored in plaintext files)
    @Volatile
    private var inMemoryAccessToken: String? = null

    fun getStoredToken(): String? = inMemoryAccessToken

    fun saveToken(token: String) {
        inMemoryAccessToken = token
    }

    fun clearToken() {
        inMemoryAccessToken = null
    }

    /**
     * Builds standard OAuth 2.0 authorization URL for Google Identity
     */
    fun buildOAuthAuthorizationUrl(): String {
        val scopes = "$SCOPE_CALENDAR_READONLY $SCOPE_USERINFO_EMAIL"
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=${URLEncoder.encode(OAUTH_CLIENT_ID, "UTF-8")}" +
                "&redirect_uri=${URLEncoder.encode(REDIRECT_URI, "UTF-8")}" +
                "&response_type=token" +
                "&scope=${URLEncoder.encode(scopes, "UTF-8")}" +
                "&login_hint=${URLEncoder.encode(TARGET_ACCOUNT, "UTF-8")}" +
                "&prompt=select_account%20consent"
    }

    /**
     * Extracts access_token, auth code, or error from OAuth redirect URL string
     */
    fun parseRedirectUrl(url: String): Pair<String?, String?> {
        val fragmentIndex = url.indexOf('#')
        if (fragmentIndex != -1) {
            val fragment = url.substring(fragmentIndex + 1)
            val params = fragment.split("&").associate {
                val parts = it.split("=")
                if (parts.size >= 2) parts[0] to parts[1] else parts[0] to ""
            }
            if (params.containsKey("access_token")) {
                return Pair(params["access_token"], null)
            }
            if (params.containsKey("error")) {
                return Pair(null, params["error"])
            }
        }
        val queryIndex = url.indexOf('?')
        if (queryIndex != -1) {
            val queryEnd = if (fragmentIndex != -1 && fragmentIndex > queryIndex) fragmentIndex else url.length
            val query = url.substring(queryIndex + 1, queryEnd)
            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size >= 2) parts[0] to parts[1] else parts[0] to ""
            }
            if (params.containsKey("access_token")) {
                return Pair(params["access_token"], null)
            }
            if (params.containsKey("error")) {
                return Pair(null, params["error"])
            }
            if (params.containsKey("code")) {
                return Pair(params["code"], null)
            }
        }
        return Pair(null, null)
    }

    /**
     * Extracts access_token or error from OAuth redirect URI fragment/query
     */
    fun parseRedirectUri(uri: Uri): Pair<String?, String?> {
        return parseRedirectUrl(uri.toString())
    }

    /**
     * Performs real authenticated GET request to Google Calendar API
     * and userinfo endpoint to verify read-only access and account email.
     */
    suspend fun verifyPrimaryCalendarAccess(token: String): CalendarConnectionState = withContext(Dispatchers.IO) {
        try {
            // 1. Perform GET request to Primary Calendar metadata
            val calendarInfo = fetchPrimaryCalendar(token)
            val primaryCalendarId = calendarInfo.optString("id", "")

            // 2. Verify authenticated user identity
            val userInfoEmail = fetchUserEmail(token) ?: if (primaryCalendarId.contains("@")) primaryCalendarId else null

            if (userInfoEmail != null && !userInfoEmail.equals(TARGET_ACCOUNT, ignoreCase = true)) {
                return@withContext CalendarConnectionState.Error(
                    message = "Akun tidak cocok",
                    details = "Akun terautentikasi adalah '$userInfoEmail', sedangkan akun target adalah '$TARGET_ACCOUNT'."
                )
            }

            // 3. Perform GET request to Primary Calendar events (maxResults=10, read-only)
            val eventCount = fetchPrimaryCalendarEventsCount(token)

            val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            inMemoryAccessToken = token

            CalendarConnectionState.Connected(
                email = userInfoEmail ?: TARGET_ACCOUNT,
                calendarSummary = calendarInfo.optString("summary", "Primary Calendar"),
                timeZone = calendarInfo.optString("timeZone", "Asia/Jakarta"),
                eventCount = eventCount,
                lastVerifiedAt = now
            )
        } catch (e: GoogleApiException) {
            CalendarConnectionState.Error(
                message = "Gagal mengakses Google Calendar API",
                details = "HTTP ${e.statusCode}: ${e.responseMessage}\n${e.errorBody}"
            )
        } catch (e: Exception) {
            CalendarConnectionState.Error(
                message = "Kesalahan koneksi autentikasi",
                details = e.localizedMessage ?: e.message ?: "Koneksi jaringan terputus"
            )
        }
    }

    private fun fetchUserEmail(token: String): String? {
        // Try tokeninfo endpoint
        try {
            val url = URL("https://oauth2.googleapis.com/tokeninfo?access_token=$token")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            if (conn.responseCode == 200) {
                val json = JSONObject(readStream(conn.inputStream))
                if (json.has("email")) return json.getString("email")
            }
        } catch (_: Exception) {}

        // Fallback to userinfo endpoint
        try {
            val url = URL("https://www.googleapis.com/oauth2/v2/userinfo")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val code = conn.responseCode
            if (code == 200) {
                val response = readStream(conn.inputStream)
                val json = JSONObject(response)
                return if (json.has("email")) json.getString("email") else null
            }
        } catch (_: Exception) {}

        return null
    }

    private fun fetchPrimaryCalendar(token: String): JSONObject {
        val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val code = conn.responseCode
        if (code in 200..299) {
            val response = readStream(conn.inputStream)
            return JSONObject(response)
        } else {
            val err = readStream(conn.errorStream ?: conn.inputStream)
            throw GoogleApiException(code, conn.responseMessage, err)
        }
    }

    private fun fetchPrimaryCalendarEventsCount(token: String): Int {
        val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events?maxResults=10&singleEvents=true")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Accept", "application/json")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        val code = conn.responseCode
        if (code in 200..299) {
            val response = readStream(conn.inputStream)
            val json = JSONObject(response)
            val items = json.optJSONArray("items")
            return items?.length() ?: 0
        } else {
            val err = readStream(conn.errorStream ?: conn.inputStream)
            throw GoogleApiException(code, conn.responseMessage, err)
        }
    }

    private fun readStream(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }
}

class GoogleApiException(
    val statusCode: Int,
    val responseMessage: String,
    val errorBody: String
) : Exception("Google API error: $statusCode $responseMessage - $errorBody")
