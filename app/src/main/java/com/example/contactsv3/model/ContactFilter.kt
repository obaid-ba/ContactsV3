package com.example.contactsv3.model

import java.text.Normalizer
import java.util.Locale

/**
 * Stratégie de filtrage de la liste.
 *
 * - Le filtre est une **fonction pure** : elle reçoit la liste complète + le texte saisi et renvoie
 *   une NOUVELLE liste. La liste d'origine n'est jamais modifiée, effacer le texte rend donc toujours
 *   tous les contacts.
 * - La recherche porte sur le nom, le prénom, « prénom nom » / « nom prénom » et le téléphone.
 * - Elle ne tient compte ni de la casse ni des accents (« ines » trouve « Inès »).
 * - Pour le téléphone, espaces et séparateurs sont ignorés (« 20 111 » trouve « 20111222 »).
 */
object ContactFilter {

    fun filter(contacts: List<Contact>, query: String): List<Contact> {
        val q = fold(query.trim())
        if (q.isEmpty()) return contacts

        val queryLooksLikePhone = q.all { it.isDigit() || it in PHONE_SEPARATORS }
        val queryDigits = q.filter { it.isDigit() }

        return contacts.filter { contact ->
            val nom = fold(contact.nom)
            val prenom = fold(contact.prenom)
            val telephone = fold(contact.telephone)

            nom.contains(q) ||
                prenom.contains(q) ||
                "$prenom $nom".contains(q) ||
                "$nom $prenom".contains(q) ||
                telephone.contains(q) ||
                (queryLooksLikePhone && queryDigits.isNotEmpty() &&
                    contact.telephone.filter { it.isDigit() }.contains(queryDigits))
        }
    }

    /** Minuscules + suppression des accents, pour comparer sans tenir compte de la casse ni des accents. */
    internal fun fold(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase(Locale.ROOT)

    private val DIACRITICS = Regex("\\p{Mn}+")
    private const val PHONE_SEPARATORS = " +-.()"
}
