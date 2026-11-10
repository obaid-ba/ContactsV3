package com.example.contactsv3.model

/**
 * Modèle d'un contact. Toute l'interface est construite à partir de cet objet :
 * aucune donnée n'est figée dans les vues.
 *
 * @property id        identifiant stable et unique : c'est lui (et non la position dans la liste)
 *                     qui est transmis entre les fragments
 * @property photoUri  URI (texte) de la photo, ou null si le contact n'en a pas
 * @property latitude  null = position non définie
 * @property longitude null = position non définie
 */
data class Contact(
    val id: Long,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    /** « Prénom Nom », par exemple pour le titre du marqueur sur la carte. */
    val nomComplet: String
        get() = "$prenom $nom".trim()

    /** Initiales affichées si le contact n'a pas de photo (informatif, utilisé par les tests). */
    val initiales: String
        get() = listOf(prenom, nom)
            .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
            .joinToString("")
}
