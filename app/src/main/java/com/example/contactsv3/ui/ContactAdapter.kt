package com.example.contactsv3.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsv3.databinding.ItemContactBinding
import com.example.contactsv3.model.Contact
import com.example.contactsv3.util.PhotoStore

/**
 * Adapter de la RecyclerView.
 *
 * - [ViewHolder] garde les références des vues d'une ligne (pas de findViewById à chaque défilement).
 * - [onBindViewHolder] remplit la ligne À PARTIR DU MODÈLE [Contact] : rien n'est figé dans le layout.
 * - [ListAdapter] + [DiffCallback] calculent automatiquement les différences entre deux listes
 *   (filtrage, modification d'un contact) et animent seulement ce qui change.
 * - Le clic transmet le [Contact] lui-même (donc son id), jamais l'indice de la ligne :
 *   après un filtrage, l'indice 0 n'est plus forcément le premier contact de la liste complète.
 */
class ContactAdapter(
    private val onContactClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        with(holder.binding) {
            txtNomComplet.text = contact.nomComplet
            txtTelephone.text = contact.telephone
            PhotoStore.display(imgPhoto, contact.photoUri)
            imgPosition.visibility =
                if (contact.latitude != null && contact.longitude != null) View.VISIBLE else View.GONE
            root.setOnClickListener { onContactClick(contact) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem
    }
}
