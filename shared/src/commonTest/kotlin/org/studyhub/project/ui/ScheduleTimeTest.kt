package org.studyhub.project.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScheduleTimeTest {

    @Test
    fun isValidTimeAcceptsValidFormats() {
        assertEquals(true, isValidTime("08:00"))
        assertEquals(true, isValidTime("00:00"))
        assertEquals(true, isValidTime("23:59"))
        assertEquals(true, isValidTime("10:05"))
    }

    @Test
    fun isValidTimeRejectsBadInput() {
        assertEquals(false, isValidTime(""))
        assertEquals(false, isValidTime("8:00"))
        assertEquals(false, isValidTime("0800"))
        assertEquals(false, isValidTime("24:00"))
        assertEquals(false, isValidTime("12:60"))
        assertEquals(false, isValidTime("12:0"))
        assertEquals(false, isValidTime("abc"))
    }

    @Test
    fun timeToMinutesConverts() {
        assertEquals(480, timeToMinutes("08:00"))
        assertEquals(600, timeToMinutes("10:00"))
        assertEquals(0, timeToMinutes("00:00"))
        assertNull(timeToMinutes("25:00"))
    }
}
