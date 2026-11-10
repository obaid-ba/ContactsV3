package com.example.contactsv3.model

/** Types d'erreur de saisie d'une coordonnée (le texte affiché est choisi par l'interface). */
enum class CoordinateError {
    /** Un seul des deux champs est rempli. */
    MISSING,

    /** Le texte n'est pas un nombre (lettres, « NaN », « Infinity »…). */
    NOT_A_NUMBER,

    /** Latitude hors de −90…90 ou longitude hors de −180…180. */
    OUT_OF_RANGE
}

/** Résultat de la validation d'un couple latitude / longitude saisi sous forme de texte. */
sealed interface CoordinatesResult {

    /** Les deux champs sont vides : le contact n'a pas de position (cas valide). */
    data object Absent : CoordinatesResult

    /** Au moins un champ est incorrect ; l'erreur de chaque champ est précisée (null = champ correct). */
    data class Invalid(
        val latitudeError: CoordinateError?,
        val longitudeError: CoordinateError?
    ) : CoordinatesResult

    /** Position valide. */
    data class Valid(val latitude: Double, val longitude: Double) : CoordinatesResult
}

object Coordinates {

    const val LATITUDE_MAX = 90.0
    const val LONGITUDE_MAX = 180.0

    /**
     * Valide un couple saisi comme texte.
     *
     * - deux champs vides            -> [CoordinatesResult.Absent]
     * - un seul champ vide           -> [CoordinatesResult.Invalid] (MISSING sur le champ vide)
     * - texte non numérique          -> NOT_A_NUMBER (la virgule décimale « 36,8 » est acceptée)
     * - hors limites                 -> OUT_OF_RANGE (latitude −90…90, longitude −180…180)
     */
    fun parse(latitudeText: String?, longitudeText: String?): CoordinatesResult {
        val latText = latitudeText.orEmpty().trim()
        val lonText = longitudeText.orEmpty().trim()

        if (latText.isEmpty() && lonText.isEmpty()) return CoordinatesResult.Absent

        val latError: CoordinateError?
        val lonError: CoordinateError?
        var latitude: Double? = null
        var longitude: Double? = null

        if (latText.isEmpty()) {
            latError = CoordinateError.MISSING
        } else {
            latitude = toFiniteDouble(latText)
            latError = when {
                latitude == null -> CoordinateError.NOT_A_NUMBER
                latitude < -LATITUDE_MAX || latitude > LATITUDE_MAX -> CoordinateError.OUT_OF_RANGE
                else -> null
            }
        }

        if (lonText.isEmpty()) {
            lonError = CoordinateError.MISSING
        } else {
            longitude = toFiniteDouble(lonText)
            lonError = when {
                longitude == null -> CoordinateError.NOT_A_NUMBER
                longitude < -LONGITUDE_MAX || longitude > LONGITUDE_MAX -> CoordinateError.OUT_OF_RANGE
                else -> null
            }
        }

        return if (latError == null && lonError == null && latitude != null && longitude != null) {
            CoordinatesResult.Valid(latitude, longitude)
        } else {
            CoordinatesResult.Invalid(latError, lonError)
        }
    }

    /** Version pratique pour un contact déjà enregistré (coordonnées stockées en Double?). */
    fun of(contact: Contact): CoordinatesResult =
        parse(contact.latitude?.toString(), contact.longitude?.toString())

    private fun toFiniteDouble(text: String): Double? =
        text.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
}
