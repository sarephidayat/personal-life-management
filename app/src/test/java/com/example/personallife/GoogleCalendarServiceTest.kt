package com.example.personallife

import com.example.personallife.data.google.CalendarConnectionState
import com.example.personallife.data.google.GoogleCalendarService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleCalendarServiceTest {

    @Test
    fun testBuildOAuthAuthorizationUrl_containsRequiredParameters() {
        val service = GoogleCalendarService()
        val url = service.buildOAuthAuthorizationUrl()

        assertTrue("URL must start with Google OAuth endpoint", url.startsWith("https://accounts.google.com/o/oauth2/v2/auth"))
        assertTrue("URL must contain client_id", url.contains("client_id=${GoogleCalendarService.OAUTH_CLIENT_ID}"))
        assertTrue("URL must contain response_type=token", url.contains("response_type=token"))
        assertTrue("URL must request readonly calendar scope", url.contains("calendar.events.readonly"))
        assertTrue("URL must contain target account login_hint", java.net.URLDecoder.decode(url, "UTF-8").contains(GoogleCalendarService.TARGET_ACCOUNT))
    }

    @Test
    fun testParseRedirectUrl_extractsAccessTokenFromFragment() {
        val service = GoogleCalendarService()
        val redirectUrl = "com.aistudio.personallife://oauth2redirect#access_token=ya29.test_token_12345&token_type=Bearer&expires_in=3599"

        val (token, error) = service.parseRedirectUrl(redirectUrl)

        assertEquals("ya29.test_token_12345", token)
        assertNull(error)
    }

    @Test
    fun testParseRedirectUrl_extractsErrorWhenUserCancelsOrDenies() {
        val service = GoogleCalendarService()
        val redirectUrl = "com.aistudio.personallife://oauth2redirect#error=access_denied&error_description=User+denied+consent"

        val (token, error) = service.parseRedirectUrl(redirectUrl)

        assertNull(token)
        assertEquals("access_denied", error)
    }

    @Test
    fun testParseRedirectUrl_extractsTokenFromQueryParameters() {
        val service = GoogleCalendarService()
        val redirectUrl = "com.aistudio.personallife://oauth2redirect?access_token=ya29.query_token_abc"

        val (token, error) = service.parseRedirectUrl(redirectUrl)

        assertEquals("ya29.query_token_abc", token)
        assertNull(error)
    }

    @Test
    fun testTokenMemorySafety_storedOnlyInMemoryAndCleared() {
        val service = GoogleCalendarService()
        assertNull(service.getStoredToken())

        service.saveToken("sample_token_temp")
        assertEquals("sample_token_temp", service.getStoredToken())

        service.clearToken()
        assertNull(service.getStoredToken())
    }

    @Test
    fun testTargetAccountVerification_matchesExpectedTarget() {
        assertEquals("hidayatsyarifudin903@gmail.com", GoogleCalendarService.TARGET_ACCOUNT)
    }

    @Test
    fun testCalendarConnectionState_dataClasses() {
        val connected = CalendarConnectionState.Connected(
            email = "hidayatsyarifudin903@gmail.com",
            calendarSummary = "Primary Calendar",
            timeZone = "Asia/Jakarta",
            eventCount = 3,
            lastVerifiedAt = "14/10/2026 12:00:00"
        )
        assertEquals("hidayatsyarifudin903@gmail.com", connected.email)
        assertEquals(3, connected.eventCount)

        val error = CalendarConnectionState.Error(
            message = "Akun tidak cocok",
            details = "Detail error"
        )
        assertEquals("Akun tidak cocok", error.message)
        assertEquals("Detail error", error.details)
    }
}
