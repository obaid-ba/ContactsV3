package com.example.contactsv3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.contactsv3.ui.ListeContactsFragment

/**
 * Unique activité de l'application : elle héberge les fragments dans un FragmentContainerView.
 * Liste -> Détail -> Carte sont des fragments qui se remplacent (replace + addToBackStack).
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // IMPORTANT : on n'ajoute le premier fragment que lors du TOUT PREMIER lancement.
        // Après une rotation, savedInstanceState n'est pas null : le FragmentManager a déjà recréé
        // lui-même les fragments (et la pile de retour) ; en ajouter un de plus créerait des doublons.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, ListeContactsFragment())
                .commit()
        }
    }
}
