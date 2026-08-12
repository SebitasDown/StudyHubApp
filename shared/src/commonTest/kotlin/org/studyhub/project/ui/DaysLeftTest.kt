package org.studyhub.project.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DaysLeftTest {

    @Test
    fun epochDayKnownDates() {
        assertEquals(0L, dateToEpochDay(1970, 1, 1))
        assertEquals(19723L, dateToEpochDay(2024, 1, 1))
        // 2026-01-01 = 19723 + 366 (2024 bisiesto) + 365 (2025) = 20454
        assertEquals(20454L, dateToEpochDay(2026, 1, 1))
        // 2026-08-15 = 20454 + 226 días transcurridos
        assertEquals(20680L, dateToEpochDay(2026, 8, 15))
    }

    @Test
    fun epochDayRejectsInvalidDates() {
        assertNull(dateToEpochDay(2026, 0, 15))
        assertNull(dateToEpochDay(2026, 13, 1))
        assertNull(dateToEpochDay(2026, 8, 0))
    }

    @Test
    fun daysUntilDueParsesIso() {
        // Fechas lejanas en el futuro/pasado: solo verificamos signo y coherencia,
        // el cálculo exacto depende del reloj actual.
        val past = daysUntilDue("2020-01-01T00:00:00.000Z")
        assertEquals(true, past != null && past < 0)
        val future = daysUntilDue("2099-01-01T00:00:00.000Z")
        assertEquals(true, future != null && future > 0)
    }

    @Test
    fun daysUntilDueRejectsBadInput() {
        assertNull(daysUntilDue(""))
        assertNull(daysUntilDue("sin fecha"))
        assertNull(daysUntilDue("2026-13-40T00:00:00.000Z"))
    }

    @Test
    fun isValidIsoDateValidatesCalendar() {
        assertEquals(true, isValidIsoDate("2026-08-15"))
        assertEquals(true, isValidIsoDate("2024-02-29")) // bisiesto
        assertEquals(false, isValidIsoDate("2023-02-29")) // no bisiesto
        assertEquals(false, isValidIsoDate("2026-02-30"))
        assertEquals(false, isValidIsoDate("2026-13-01"))
        assertEquals(false, isValidIsoDate("2026-8-1"))
        assertEquals(false, isValidIsoDate(""))
        assertEquals(false, isValidIsoDate("15-08-2026"))
    }

    @Test
    fun todayIsoHasValidFormat() {
        val today = todayIso()
        assertEquals(true, isValidIsoDate(today))
        assertEquals(10, today.length)
    }
}
