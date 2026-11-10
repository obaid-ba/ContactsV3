package com.example.contactsv3.model

/**
 * Contacts de démonstration (stockage en mémoire uniquement, comme l'autorise l'énoncé).
 * Cinq contacts ont des coordonnées valides, deux n'ont aucune position définie.
 */
object DemoData {

    fun contacts(): List<Contact> = listOf(
        Contact(1, "Ben Ali", "Ahmed", "20 111 222", "ahmed.benali@example.com", null, 36.8065, 10.1815),
        Contact(2, "Trabelsi", "Sarra", "21 333 444", "sarra.trabelsi@example.com", null, 35.8256, 10.6084),
        Contact(3, "Gharbi", "Mohamed", "22 555 666", "mohamed.gharbi@example.com", null, 37.2744, 9.8739),
        Contact(4, "Jaziri", "Inès", "23 777 888", "ines.jaziri@example.com"),
        Contact(5, "Mansouri", "Youssef", "24 999 000", "youssef.mansouri@example.com", null, 33.8760, 10.8575),
        Contact(6, "Chaabane", "Amira", "25 121 212", "amira.chaabane@example.com", null, 34.7406, 10.7603),
        Contact(7, "Bouazizi", "Karim", "26 343 434", "karim.bouazizi@example.com"),
        Contact(8, "Hammami", "Nour", "27 565 656", "nour.hammami@example.com", null, 35.6781, 10.0963)
    )
}
