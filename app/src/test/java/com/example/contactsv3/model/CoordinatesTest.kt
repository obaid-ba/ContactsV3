package com.example.contactsv3.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinatesTest {

    @Test
    fun bothEmptyMeansNoPosition() {
        assertEquals(CoordinatesResult.Absent, Coordinates.parse("", ""))
        assertEquals(CoordinatesResult.Absent, Coordinates.parse(null, null))
        assertEquals(CoordinatesResult.Absent, Coordinates.parse("  ", " "))
    }

    @Test
    fun validCoordinates() {
        assertEquals(
            CoordinatesResult.Valid(36.8065, 10.1815),
            Coordinates.parse("36.8065", "10.1815")
        )
    }

    @Test
    fun decimalCommaIsAccepted() {
        assertEquals(
            CoordinatesResult.Valid(36.8065, 10.1815),
            Coordinates.parse("36,8065", " 10,1815 ")
        )
    }

    @Test
    fun limitsAreIncluded() {
        assertEquals(CoordinatesResult.Valid(90.0, 180.0), Coordinates.parse("90", "180"))
        assertEquals(CoordinatesResult.Valid(-90.0, -180.0), Coordinates.parse("-90", "-180"))
    }

    @Test
    fun outOfRangeLatitude() {
        assertEquals(
            CoordinatesResult.Invalid(CoordinateError.OUT_OF_RANGE, null),
            Coordinates.parse("90.1", "10")
        )
        assertEquals(
            CoordinatesResult.Invalid(CoordinateError.OUT_OF_RANGE, null),
            Coordinates.parse("-91", "10")
        )
    }

    @Test
    fun outOfRangeLongitude() {
        assertEquals(
            CoordinatesResult.Invalid(null, CoordinateError.OUT_OF_RANGE),
            Coordinates.parse("10", "180.5")
        )
        assertEquals(
            CoordinatesResult.Invalid(null, CoordinateError.OUT_OF_RANGE),
            Coordinates.parse("10", "-181")
        )
    }

    @Test
    fun nonNumericText() {
        assertEquals(
            CoordinatesResult.Invalid(CoordinateError.NOT_A_NUMBER, null),
            Coordinates.parse("abc", "10")
        )
        assertEquals(
            CoordinatesResult.Invalid(null, CoordinateError.NOT_A_NUMBER),
            Coordinates.parse("10", "12.3.4")
        )
    }

    @Test
    fun nanAndInfinityAreRejected() {
        assertEquals(
            CoordinatesResult.Invalid(CoordinateError.NOT_A_NUMBER, CoordinateError.NOT_A_NUMBER),
            Coordinates.parse("NaN", "Infinity")
        )
    }

    @Test
    fun onlyOneFieldFilledIsInvalid() {
        assertEquals(
            CoordinatesResult.Invalid(null, CoordinateError.MISSING),
            Coordinates.parse("36.8", "")
        )
        assertEquals(
            CoordinatesResult.Invalid(CoordinateError.MISSING, null),
            Coordinates.parse("", "10.1")
        )
    }

    @Test
    fun bothInvalidReportsBothErrors() {
        assertEquals(
            CoordinatesResult.Invalid(CoordinateError.OUT_OF_RANGE, CoordinateError.NOT_A_NUMBER),
            Coordinates.parse("200", "x")
        )
    }

    @Test
    fun savedContactCoordinates() {
        val located = Contact(1, "A", "B", "123", "a@b.co", null, 36.8, 10.1)
        val notLocated = Contact(2, "C", "D", "456", "c@d.co")
        assertEquals(CoordinatesResult.Valid(36.8, 10.1), Coordinates.of(located))
        assertEquals(CoordinatesResult.Absent, Coordinates.of(notLocated))
    }
}
