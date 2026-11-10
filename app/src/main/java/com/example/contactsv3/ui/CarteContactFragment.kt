package com.example.contactsv3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.contactsv3.R
import com.example.contactsv3.databinding.FragmentCarteContactBinding
import com.example.contactsv3.model.Coordinates
import com.example.contactsv3.model.CoordinatesResult
import com.example.contactsv3.util.messageRes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Affiche la position GPS d'un contact sur Google Maps : carte centrée sur ses coordonnées,
 * avec un marqueur portant son nom. Aucune permission de localisation n'est nécessaire :
 * on n'affiche que des coordonnées déjà saisies.
 */
class CarteContactFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarteContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels()

    private var position: LatLng? = null
    private var titreMarqueur: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarteContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val nom = viewModel.getContact(args.getLong(ARG_CONTACT_ID))?.nomComplet.orEmpty()
        titreMarqueur = nom
        binding.txtCarteTitre.text = nom.ifEmpty { getString(R.string.titre_carte) }

        // Retour à la fiche du contact (identique au bouton Retour du système : on dépile la carte)
        binding.btnRetour.setOnClickListener { parentFragmentManager.popBackStack() }

        when (val resultat = Coordinates.parse(args.getString(ARG_LATITUDE), args.getString(ARG_LONGITUDE))) {
            CoordinatesResult.Absent ->
                afficherMessage(getString(R.string.carte_absente))

            is CoordinatesResult.Invalid -> {
                val details = listOfNotNull(
                    resultat.latitudeError?.messageRes(isLatitude = true),
                    resultat.longitudeError?.messageRes(isLatitude = false)
                ).joinToString("\n") { getString(it) }
                afficherMessage(getString(R.string.carte_invalide, details))
            }

            is CoordinatesResult.Valid -> {
                position = LatLng(resultat.latitude, resultat.longitude)
                binding.txtCarteMessage.visibility = View.GONE
                binding.map.visibility = View.VISIBLE
                (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)
                    ?.getMapAsync(this)
            }
        }
    }

    private fun afficherMessage(message: String) {
        binding.map.visibility = View.GONE
        binding.txtCarteMessage.text = message
        binding.txtCarteMessage.visibility = View.VISIBLE
    }

    /** Appelé quand la carte est prête : centrage + marqueur au nom du contact. */
    override fun onMapReady(googleMap: GoogleMap) {
        val cible = position ?: return
        googleMap.uiSettings.isZoomControlsEnabled = true
        val marqueur = googleMap.addMarker(MarkerOptions().position(cible).title(titreMarqueur))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cible, ZOOM_VILLE))
        marqueur?.showInfoWindow()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONTACT_ID = "contact_id"
        private const val ARG_LATITUDE = "latitude_texte"
        private const val ARG_LONGITUDE = "longitude_texte"
        private const val ZOOM_VILLE = 14f

        fun newInstance(contactId: Long, latitudeTexte: String, longitudeTexte: String) =
            CarteContactFragment().apply {
                arguments = bundleOf(
                    ARG_CONTACT_ID to contactId,
                    ARG_LATITUDE to latitudeTexte,
                    ARG_LONGITUDE to longitudeTexte
                )
            }
    }
}
