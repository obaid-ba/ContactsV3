package com.example.contactsv3.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.contactsv3.R
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Gère les fichiers photo des contacts, dans le stockage PRIVÉ de l'application (filesDir/photos).
 *
 * - Une photo choisie dans la galerie (URI temporaire) ou prise avec la caméra (fichier brut, souvent
 *   très gros) est copiée, réduite à [MAX_SIDE] pixels et redressée selon son orientation EXIF.
 * - Le contact mémorise ensuite l'URI de ce fichier : elle reste valable après rotation et ne dépend
 *   d'aucune autorisation externe.
 */
object PhotoStore {

    private const val DIR_NAME = "photos"
    private const val MAX_SIDE = 800
    private const val JPEG_QUALITY = 88

    private fun photosDir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { mkdirs() }

    /** Nouveau fichier vide (nom unique) dans le dossier des photos. */
    fun newPhotoFile(context: Context): File =
        File(photosDir(context), "photo_${UUID.randomUUID()}.jpg")

    /** URI « content:// » à donner à l'application caméra (via le FileProvider déclaré au manifeste). */
    fun cameraUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /**
     * Copie [source] (galerie ou fichier caméra) dans un nouveau fichier privé, réduit et redressé.
     * @return l'URI du fichier créé (sous forme de texte), ou null si l'image est illisible.
     */
    fun importPhoto(context: Context, source: Uri): String? {
        return try {
            val resolver = context.contentResolver

            // 1) Lire seulement les dimensions pour choisir un facteur de sous-échantillonnage
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2

            // 2) Décoder l'image réduite
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = resolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            // 3) Corriger l'orientation EXIF (photos prises en portrait par la caméra)
            val degrees = resolver.openInputStream(source)?.use { readRotation(it) } ?: 0
            val bitmap = rotate(decoded, degrees)

            // 4) Enregistrer dans un fichier privé
            val target = newPhotoFile(context)
            FileOutputStream(target).use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            Uri.fromFile(target).toString()
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    /** Affiche la photo, ou l'avatar par défaut si le contact n'en a pas / si le fichier est illisible. */
    fun display(view: ImageView, photoUri: String?) {
        if (photoUri != null) {
            try {
                view.setImageURI(Uri.parse(photoUri))
            } catch (e: Exception) {
                view.setImageDrawable(null)
            }
        } else {
            view.setImageDrawable(null)
        }
        if (view.drawable == null) view.setImageResource(R.drawable.ic_person)
    }

    /** Supprime le fichier uniquement s'il se trouve bien dans le dossier des photos de l'application. */
    fun deleteIfOwned(context: Context, photoUri: String?) {
        if (photoUri == null) return
        val path = Uri.parse(photoUri).path ?: return
        val file = File(path)
        if (file.parentFile == photosDir(context) && file.exists()) file.delete()
    }

    private fun readRotation(stream: java.io.InputStream): Int =
        when (ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
