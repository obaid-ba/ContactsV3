package com.example.contactsv3.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.contactsv3.model.Contact
import com.example.contactsv3.model.ContactFilter
import com.example.contactsv3.model.DemoData

/**
 * ViewModel PARTAGÉ par les trois fragments (il appartient à l'activité : `activityViewModels()`).
 *
 * Il est la source de vérité de l'application :
 * - la collection de contacts en mémoire (aucun stockage permanent n'est exigé) ;
 * - le texte de recherche en cours ;
 * - la liste filtrée, recalculée automatiquement quand l'un des deux change.
 *
 * Choix de communication entre fragments (voir le compte rendu) :
 * le détail enregistre via [update] ; la liste observe [filtered] et se rafraîchit toute seule,
 * même après rotation de l'écran. Le Bundle ne sert qu'à transmettre l'identifiant du contact.
 */
class ContactsViewModel : ViewModel() {

    private val _contacts = MutableLiveData(DemoData.contacts())
    val contacts: LiveData<List<Contact>> = _contacts

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    /** Contacts correspondant au texte de recherche (tous les contacts si le texte est vide). */
    val filtered: LiveData<List<Contact>> = MediatorLiveData<List<Contact>>().apply {
        val recompute = {
            value = ContactFilter.filter(_contacts.value.orEmpty(), _query.value.orEmpty())
        }
        addSource(_contacts) { recompute() }
        addSource(_query) { recompute() }
    }

    fun setQuery(text: String) {
        if (_query.value != text) _query.value = text
    }

    /** Retrouve un contact par son IDENTIFIANT (jamais par sa position dans la liste affichée). */
    fun getContact(id: Long): Contact? = _contacts.value.orEmpty().firstOrNull { it.id == id }

    /** Remplace le contact de même identifiant ; la liste et le filtre sont recalculés. */
    fun update(updated: Contact) {
        _contacts.value = _contacts.value.orEmpty().map { if (it.id == updated.id) updated else it }
    }
}
