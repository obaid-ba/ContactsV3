package com.example.contactsv3.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {

    @Test
    fun fullNameAndInitials() {
        val c = Contact(1, "Ben Ali", "Ahmed", "20 111 222", "a@b.co")
        assertEquals("Ahmed Ben Ali", c.nomComplet)
        assertEquals("AB", c.initiales)
    }

    @Test
    fun emailValidation() {
        assertTrue(Validators.isValidEmail("ahmed.benali@example.com"))
        assertTrue(Validators.isValidEmail("  a+b@sub.example.tn  "))
        assertFalse(Validators.isValidEmail("ahmed"))
        assertFalse(Validators.isValidEmail("ahmed@"))
        assertFalse(Validators.isValidEmail("ahmed@example"))
        assertFalse(Validators.isValidEmail("@example.com"))
        assertFalse(Validators.isValidEmail("a b@example.com"))
    }

    @Test
    fun phoneValidation() {
        assertTrue(Validators.isValidPhone("20 111 222"))
        assertTrue(Validators.isValidPhone("+216 20 111 222"))
        assertFalse(Validators.isValidPhone("abc"))
        assertFalse(Validators.isValidPhone("12"))
        assertFalse(Validators.isValidPhone("20x111"))
    }

    @Test
    fun demoContactsHaveUniqueIdsAndValidOrAbsentPositions() {
        val contacts = DemoData.contacts()
        assertEquals(contacts.size, contacts.map { it.id }.toSet().size)
        for (c in contacts) {
            val result = Coordinates.of(c)
            assertTrue(
                "Contact ${c.id} : position ni valide ni absente ($result)",
                result is CoordinatesResult.Valid || result is CoordinatesResult.Absent
            )
            assertTrue(Validators.isValidEmail(c.email))
            assertTrue(Validators.isValidPhone(c.telephone))
        }
        assertTrue(contacts.any { Coordinates.of(it) is CoordinatesResult.Valid })
        assertTrue(contacts.any { Coordinates.of(it) is CoordinatesResult.Absent })
    }
}
