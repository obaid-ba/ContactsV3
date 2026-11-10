package com.example.contactsv3.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactFilterTest {

    private val contacts = DemoData.contacts()

    private fun ids(list: List<Contact>) = list.map { it.id }

    @Test
    fun emptyQueryReturnsEveryContact() {
        assertEquals(contacts, ContactFilter.filter(contacts, ""))
        assertEquals(contacts, ContactFilter.filter(contacts, "   "))
    }

    @Test
    fun filtersByLastNameIgnoringCase() {
        assertEquals(listOf(1L), ids(ContactFilter.filter(contacts, "ben")))
        assertEquals(listOf(1L), ids(ContactFilter.filter(contacts, "BEN ALI")))
    }

    @Test
    fun filtersByFirstNameIgnoringCase() {
        assertEquals(listOf(2L), ids(ContactFilter.filter(contacts, "SARRA")))
        assertEquals(listOf(5L), ids(ContactFilter.filter(contacts, "youSSef")))
    }

    @Test
    fun filtersByFullNameInBothOrders() {
        assertEquals(listOf(1L), ids(ContactFilter.filter(contacts, "ahmed ben")))
        assertEquals(listOf(1L), ids(ContactFilter.filter(contacts, "ben ali ahmed")))
    }

    @Test
    fun filtersByPhoneWithOrWithoutSpaces() {
        assertEquals(listOf(2L), ids(ContactFilter.filter(contacts, "21 333")))
        assertEquals(listOf(2L), ids(ContactFilter.filter(contacts, "21333")))
        assertEquals(listOf(2L), ids(ContactFilter.filter(contacts, "21 333 444")))
    }

    @Test
    fun ignoresAccents() {
        assertEquals(listOf(4L), ids(ContactFilter.filter(contacts, "ines")))
        assertEquals(listOf(4L), ids(ContactFilter.filter(contacts, "INÈS")))
    }

    @Test
    fun noMatchReturnsEmptyList() {
        assertTrue(ContactFilter.filter(contacts, "zzz").isEmpty())
    }

    @Test
    fun separatorAloneDoesNotMatchEverything() {
        assertTrue(ContactFilter.filter(contacts, "+").isEmpty())
    }

    @Test
    fun filteredResultCarriesTheRightIds() {
        // L'indice dans la liste filtrée n'a rien à voir avec l'identifiant : on s'appuie sur l'id.
        val result = ContactFilter.filter(contacts, "hammami")
        assertEquals(1, result.size)
        assertEquals(8L, result[0].id)
        assertEquals("Nour", result[0].prenom)
    }

    @Test
    fun clearingTheQueryRestoresEverything() {
        val filtered = ContactFilter.filter(contacts, "ahmed")
        assertEquals(1, filtered.size)
        assertEquals(contacts.size, ContactFilter.filter(contacts, "").size)
    }

    @Test
    fun sourceListIsNeverModified() {
        val before = contacts.toList()
        ContactFilter.filter(contacts, "ahmed")
        assertEquals(before, contacts)
    }
}
