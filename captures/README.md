# Captures d'écran

L'énoncé demande des **captures réelles** de l'application lancée sur un émulateur ou un téléphone.
Déposez-les dans ce dossier (PNG) avec les noms ci-dessous, cochez la case, puis insérez-les dans le compte rendu.

Dans Android Studio : *Running Devices* → icône appareil photo, ou `adb exec-out screencap -p > captures/nom.png`.

> ⚠ **Ne montrez jamais votre clé API** sur une capture (par exemple en affichant `local.properties`).

| ✔ | Fichier à créer | Ce que la capture doit montrer |
|:-:|---|---|
| ☐ | `01_liste_complete.png` | La liste complète (8 contacts, compteur « 8 contacts ») |
| ☐ | `02_liste_filtree.png` | Le champ de recherche avec un texte saisi (ex. `ben`) et la liste réduite |
| ☐ | `03_resultat_vide.png` | Une recherche sans résultat (ex. `zzz`) avec le message « Aucun contact ne correspond… » |
| ☐ | `04_detail.png` | La fiche détail d'un contact (photo, champs, latitude et longitude) |
| ☐ | `05_modification.png` | Une modification en cours, puis la liste mise à jour après **Enregistrer** |
| ☐ | `06_erreurs_validation.png` | Champs obligatoires vides / e-mail invalide / coordonnées hors limites avec messages d'erreur |
| ☐ | `07_galerie.png` | Le sélecteur d'images ouvert via **Galerie**, puis la photo choisie dans la fiche |
| ☐ | `08_camera.png` | L'appareil photo ouvert via **Caméra**, puis la photo prise dans la fiche |
| ☐ | `09_carte_marqueur.png` | La carte Google Maps centrée sur le contact avec son marqueur nommé |
| ☐ | `10_carte_sans_position.png` | Le message explicite pour un contact sans coordonnées (ex. Inès Jaziri) |
| ☐ | `11_appel.png` | Le composeur téléphonique ouvert avec le numéro prérempli |
| ☐ | `12_email.png` | L'application de messagerie ouverte avec l'adresse préremplie |
| ☐ | `13_rotation.png` *(conseillé)* | La fiche ou la liste après rotation de l'écran, sans doublon de fragment |

Insertion dans le compte rendu : `![liste complète](../captures/01_liste_complete.png)`.
