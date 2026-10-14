package com.example.personallife

import com.example.personallife.data.models.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateFormattingTest {

    @Test
    fun testFormatDateDisplay_convertsIsoToIndonesianFormat() {
        val isoDate = "2026-10-14"
        val display = FormatUtils.formatDateDisplay(isoDate)
        assertEquals("14/10/2026", display)
    }

    @Test
    fun testFormatDateDisplay_handlesEmptyAndNull() {
        assertEquals("-", FormatUtils.formatDateDisplay(null))
        assertEquals("-", FormatUtils.formatDateDisplay(""))
    }

    @Test
    fun testLocalDateRoundTrip_dbFormatPreserved() {
        // User selects date in UI
        val selectedDate = LocalDate.of(2026, 10, 14)

        // Stored in database as YYYY-MM-DD
        val dbValue = selectedDate.toString()
        assertEquals("2026-10-14", dbValue)

        // Displayed in UI as DD/MM/YYYY
        val uiDisplay = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        assertEquals("14/10/2026", uiDisplay)

        // Reparsing from db value produces the exact same LocalDate
        val reparsed = LocalDate.parse(dbValue)
        assertEquals(selectedDate, reparsed)
    }

    @Test
    fun testFormatMonthYear_indonesianLocalization() {
        val monthYear = FormatUtils.formatMonthYear(2026, 10)
        // In Indonesian locale, October is Oktober
        assertEquals("Oktober 2026", monthYear)
    }
}
