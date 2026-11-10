package com.example.contactsv3.ui

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactsv3.R
import com.example.contactsv3.databinding.FragmentListeContactsBinding

/**
 * Écran d'accueil : liste des contacts (RecyclerView) + champ de recherche.
 * Un clic sur une ligne ouvre [DetailContactFragment] en lui passant l'IDENTIFIANT du contact.
 */
class ListeContactsFragment : Fragment() {

    private var _binding: FragmentListeContactsBinding? = null
    private val binding get() = _binding!!

    // Même instance que dans les deux autres fragments : le ViewModel appartient à l'activité
    private val viewModel: ContactsViewModel by activityViewModels()

    private lateinit var adapter: ContactAdapter
    private var dernierClic = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactAdapter { contact -> ouvrirDetail(contact.id) }
        binding.recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerContacts.adapter = adapter

        // Réaffiche le filtre en cours (rotation, retour depuis le détail) AVANT d'écouter la saisie,
        // pour ne pas déclencher de mise à jour inutile.
        binding.etRecherche.setText(viewModel.query.value.orEmpty())
        binding.etRecherche.doAfterTextChanged { texte ->
            viewModel.setQuery(texte?.toString().orEmpty())      // filtrage immédiat, sans bouton
        }

        // La liste observe la liste FILTRÉE : elle se met à jour toute seule quand on tape
        // ou quand un contact est modifié dans le détail.
        viewModel.filtered.observe(viewLifecycleOwner) { contacts ->
            adapter.submitList(contacts)
            majCompteurEtMessage(contacts.size)
        }
    }

    private fun majCompteurEtMessage(nombre: Int) {
        binding.txtCompteur.text = resources.getQuantityString(R.plurals.nb_contacts, nombre, nombre)

        val recherche = viewModel.query.value.orEmpty().trim()
        binding.txtVide.visibility = if (nombre == 0) View.VISIBLE else View.GONE
        binding.txtVide.text =
            if (recherche.isEmpty()) getString(R.string.aucun_contact)
            else getString(R.string.aucun_resultat, recherche)
    }

    private fun ouvrirDetail(contactId: Long) {
        // Anti double-clic : évite d'empiler deux fiches si on touche deux fois la même ligne
        val maintenant = SystemClock.elapsedRealtime()
        if (maintenant - dernierClic < DELAI_ANTI_REBOND_MS) return
        dernierClic = maintenant

        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container, DetailContactFragment.newInstance(contactId))
            addToBackStack(null)            // le bouton Retour revient à la liste (et à son filtre)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null                      // évite de garder des vues détruites en mémoire
    }

    private companion object {
        const val DELAI_ANTI_REBOND_MS = 600L
    }
}
