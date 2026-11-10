package com.example.contactsv3.model

/** Validations simples des champs du formulaire (sans dépendance à Android, donc testables). */
object Validators {

    private val EMAIL = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$")

    /** Adresse e-mail plausible (un « @ », un domaine avec un point). */
    fun isValidEmail(text: String): Boolean = EMAIL.matches(text.trim())

    /** Numéro plausible : au moins 3 chiffres et uniquement chiffres, espaces et + - . ( ). */
    fun isValidPhone(text: String): Boolean {
        val t = text.trim()
        return t.count { it.isDigit() } >= 3 && t.all { it.isDigit() || it in " +-.()" }
    }
}
