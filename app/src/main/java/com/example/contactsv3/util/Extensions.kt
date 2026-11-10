package com.example.contactsv3.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.contactsv3.R
import com.example.contactsv3.model.CoordinateError

/** Petit message à l'écran (silencieux si le fragment n'est plus attaché). */
fun Fragment.toast(@StringRes message: Int) {
    val ctx = context ?: return
    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
}

/** Masque le clavier virtuel. */
fun Fragment.hideKeyboard() {
    val ctx = context ?: return
    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
    val token = (activity?.currentFocus ?: view)?.windowToken ?: return
    imm.hideSoftInputFromWindow(token, 0)
}

/** Message d'erreur explicite correspondant à une erreur de latitude ou de longitude. */
@StringRes
fun CoordinateError.messageRes(isLatitude: Boolean): Int = when (this) {
    CoordinateError.MISSING ->
        if (isLatitude) R.string.err_lat_manquante else R.string.err_lon_manquante
    CoordinateError.NOT_A_NUMBER ->
        if (isLatitude) R.string.err_lat_nan else R.string.err_lon_nan
    CoordinateError.OUT_OF_RANGE ->
        if (isLatitude) R.string.err_lat_hors else R.string.err_lon_hors
}
