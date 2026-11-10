package com.example.contactsv3.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.example.contactsv3.R
import com.example.contactsv3.databinding.FragmentDetailContactBinding
import com.example.contactsv3.model.Contact
import com.example.contactsv3.model.Coordinates
import com.example.contactsv3.model.CoordinatesResult
import com.example.contactsv3.model.Validators
import com.example.contactsv3.util.PhotoStore
import com.example.contactsv3.util.hideKeyboard
import com.example.contactsv3.util.messageRes
import com.example.contactsv3.util.toast
import com.google.android.material.textfield.TextInputLayout
import java.io.File

/**
 * Fiche d'un contact : consultation, modification, actions (appel, e-mail, galerie, caméra)
 * et coordonnées GPS. Le contact est retrouvé grâce à son IDENTIFIANT reçu dans le Bundle.
 */
class DetailContactFragment : Fragment() {

    private var _binding: FragmentDetailContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels()

    private val contactId: Long get() = requireArguments().getLong(ARG_CONTACT_ID)

    /** Nouvelle photo choisie mais pas encore enregistrée (perdue si on annule). */
    private var photoEnAttente: String? = null

    /** Fichier cible de l'appli caméra pendant la prise de vue. */
    private var fichierCamera: String? = null

    private var enregistre = false

    // Les « launchers » doivent être enregistrés dès la création du fragment
    private val galerieLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> onGalerieResult(uri) }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok -> onCameraResult(ok) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Survit à la rotation (le fragment est recréé)
        photoEnAttente = savedInstanceState?.getString(KEY_PHOTO_EN_ATTENTE)
        fichierCamera = savedInstanceState?.getString(KEY_FICHIER_CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contact = viewModel.getContact(contactId)
        if (contact == null) {
            toast(R.string.msg_contact_introuvable)
            parentFragmentManager.popBackStack()
            return
        }

        // On préremplit les champs depuis le modèle. Si le texte saisi a déjà été sauvegardé par
        // Android (rotation, retour depuis la carte), il est restauré juste après et prime.
        remplirChamps(contact)
        binding.txtTitreDetail.text = contact.nomComplet
        afficherPhoto()

        binding.btnGalerie.setOnClickListener { choisirDansGalerie() }
        binding.btnCamera.setOnClickListener { prendrePhoto() }
        binding.btnAppeler.setOnClickListener { appeler() }
        binding.btnEmail.setOnClickListener { envoyerEmail() }
        binding.btnVoirCarte.setOnClickListener { ouvrirCarte() }
        binding.btnAnnuler.setOnClickListener { annuler() }
        binding.btnEnregistrer.setOnClickListener { enregistrer() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PHOTO_EN_ATTENTE, photoEnAttente)
        outState.putString(KEY_FICHIER_CAMERA, fichierCamera)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Fiche quittée sans enregistrer (retour système, Annuler) : on supprime la photo temporaire.
        // isRemoving est faux pendant une rotation, la photo en attente est alors conservée.
        if (isRemoving && !enregistre) {
            context?.let { PhotoStore.deleteIfOwned(it, photoEnAttente) }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Champs
    // ---------------------------------------------------------------------------------------

    private fun remplirChamps(c: Contact) {
        binding.etPrenom.setText(c.prenom)
        binding.etNom.setText(c.nom)
        binding.etTelephone.setText(c.telephone)
        binding.etEmail.setText(c.email)
        binding.etLatitude.setText(c.latitude?.toString().orEmpty())
        binding.etLongitude.setText(c.longitude?.toString().orEmpty())
    }

    private fun texte(champ: com.google.android.material.textfield.TextInputEditText): String =
        champ.text?.toString().orEmpty().trim()

    private fun effacerErreurs() {
        listOf(
            binding.tilPrenom, binding.tilNom, binding.tilTelephone,
            binding.tilEmail, binding.tilLatitude, binding.tilLongitude
        ).forEach { it.error = null }
    }

    // ---------------------------------------------------------------------------------------
    // Enregistrer / Annuler
    // ---------------------------------------------------------------------------------------

    private fun enregistrer() {
        val original = viewModel.getContact(contactId) ?: return
        effacerErreurs()

        val prenom = texte(binding.etPrenom)
        val nom = texte(binding.etNom)
        val telephone = texte(binding.etTelephone)
        val email = texte(binding.etEmail)

        var premierChampInvalide: View? = null
        fun signaler(layout: TextInputLayout, message: String) {
            layout.error = message
            if (premierChampInvalide == null) premierChampInvalide = layout.editText
        }

        // Champs obligatoires : prénom, nom, téléphone. L'e-mail est facultatif mais doit être valide.
        if (prenom.isEmpty()) signaler(binding.tilPrenom, getString(R.string.err_obligatoire))
        if (nom.isEmpty()) signaler(binding.tilNom, getString(R.string.err_obligatoire))
        if (telephone.isEmpty()) {
            signaler(binding.tilTelephone, getString(R.string.err_obligatoire))
        } else if (!Validators.isValidPhone(telephone)) {
            signaler(binding.tilTelephone, getString(R.string.err_telephone_invalide))
        }
        if (email.isNotEmpty() && !Validators.isValidEmail(email)) {
            signaler(binding.tilEmail, getString(R.string.err_email_invalide))
        }

        // Coordonnées : vides toutes les deux (pas de position) ou valides toutes les deux
        val coordonnees = Coordinates.parse(texte(binding.etLatitude), texte(binding.etLongitude))
        if (coordonnees is CoordinatesResult.Invalid) {
            coordonnees.latitudeError?.let {
                signaler(binding.tilLatitude, getString(it.messageRes(isLatitude = true)))
            }
            coordonnees.longitudeError?.let {
                signaler(binding.tilLongitude, getString(it.messageRes(isLatitude = false)))
            }
        }

        if (premierChampInvalide != null) {
            premierChampInvalide?.requestFocus()
            toast(R.string.msg_corriger)
            return
        }

        val latitude = (coordonnees as? CoordinatesResult.Valid)?.latitude
        val longitude = (coordonnees as? CoordinatesResult.Valid)?.longitude
        val nouvellePhoto = photoEnAttente ?: original.photoUri

        viewModel.update(
            original.copy(
                nom = nom,
                prenom = prenom,
                telephone = telephone,
                email = email,
                photoUri = nouvellePhoto,
                latitude = latitude,
                longitude = longitude
            )
        )

        // L'ancienne photo n'est plus utilisée : on libère son fichier
        if (photoEnAttente != null && photoEnAttente != original.photoUri) {
            PhotoStore.deleteIfOwned(requireContext(), original.photoUri)
        }
        photoEnAttente = null
        enregistre = true

        hideKeyboard()
        toast(R.string.msg_enregistre)
        parentFragmentManager.popBackStack()     // retour à la liste, qui observe déjà le ViewModel
    }

    private fun annuler() {
        hideKeyboard()
        parentFragmentManager.popBackStack()     // rien n'est écrit dans le ViewModel : anciennes valeurs conservées
    }

    // ---------------------------------------------------------------------------------------
    // Actions : appeler, e-mail
    // ---------------------------------------------------------------------------------------

    private fun appeler() {
        val numero = texte(binding.etTelephone)
        if (numero.isEmpty()) {
            toast(R.string.msg_aucun_numero)
            return
        }
        // ACTION_DIAL ouvre le composeur avec le numéro prérempli (aucune permission nécessaire)
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(numero)))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_aucune_app_appel)
        }
    }

    private fun envoyerEmail() {
        val adresse = texte(binding.etEmail)
        if (adresse.isEmpty() || !Validators.isValidEmail(adresse)) {
            binding.tilEmail.error = getString(R.string.err_email_invalide)
            toast(R.string.msg_email_vide)
            return
        }
        binding.tilEmail.error = null
        // « mailto: » + EXTRA_EMAIL : seules les applications de messagerie répondent, adresse préremplie
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(adresse))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_aucune_app_mail)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Photo : galerie et caméra
    // ---------------------------------------------------------------------------------------

    private fun afficherPhoto() {
        val uri = photoEnAttente ?: viewModel.getContact(contactId)?.photoUri
        PhotoStore.display(binding.imgPhoto, uri)
    }

    private fun choisirDansGalerie() {
        try {
            galerieLauncher.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_galerie_indisponible)
        }
    }

    private fun onGalerieResult(uri: Uri?) {
        if (uri == null) {                        // l'utilisateur est revenu sans choisir
            toast(R.string.msg_selection_annulee)
            return
        }
        appliquerPhoto(PhotoStore.importPhoto(requireContext(), uri))
    }

    private fun prendrePhoto() {
        val fichier = PhotoStore.newPhotoFile(requireContext())
        fichierCamera = fichier.absolutePath
        try {
            cameraLauncher.launch(PhotoStore.cameraUri(requireContext(), fichier))
        } catch (e: ActivityNotFoundException) {
            fichier.delete()
            fichierCamera = null
            toast(R.string.msg_camera_indisponible)
        }
    }

    private fun onCameraResult(succes: Boolean) {
        val fichier = fichierCamera?.let { File(it) }
        fichierCamera = null
        if (fichier == null) return

        if (!succes || !fichier.exists() || fichier.length() == 0L) {   // prise de vue annulée
            fichier.delete()
            toast(R.string.msg_photo_annulee)
            return
        }
        // La photo brute de la caméra est volumineuse : on en garde une copie réduite et redressée
        val importee = PhotoStore.importPhoto(requireContext(), Uri.fromFile(fichier))
        fichier.delete()
        appliquerPhoto(importee)
    }

    private fun appliquerPhoto(nouvelleUri: String?) {
        if (nouvelleUri == null) {
            toast(R.string.msg_photo_erreur)
            return
        }
        PhotoStore.deleteIfOwned(requireContext(), photoEnAttente)   // remplace une photo en attente précédente
        photoEnAttente = nouvelleUri
        if (_binding != null) afficherPhoto()
    }

    // ---------------------------------------------------------------------------------------
    // Carte
    // ---------------------------------------------------------------------------------------

    private fun ouvrirCarte() {
        hideKeyboard()
        // On transmet les coordonnées TELLES QUE SAISIES : on peut voir une position avant de l'enregistrer
        val carte = CarteContactFragment.newInstance(
            contactId,
            texte(binding.etLatitude),
            texte(binding.etLongitude)
        )
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container, carte)
            addToBackStack(null)                 // Retour depuis la carte ramène sur cette fiche
        }
    }

    companion object {
        private const val ARG_CONTACT_ID = "contact_id"
        private const val KEY_PHOTO_EN_ATTENTE = "photo_en_attente"
        private const val KEY_FICHIER_CAMERA = "fichier_camera"

        /** Seul l'identifiant du contact est transmis, dans un Bundle. */
        fun newInstance(contactId: Long) = DetailContactFragment().apply {
            arguments = bundleOf(ARG_CONTACT_ID to contactId)
        }
    }
}
