# Il me faut de la place

## Phase 1

Pour cette première phase du projet, la partie fonctionnelle a été implémentée.
Elle offre les services suivants:

* Création de l'arbre (Multi-thread avec Fork/Join et WalkFileTree + Filtre + Profondeur max).
* Collecte des doublons indépendante de la construction de l'arbre (Multi-thread avec Fork/Join et WalkFileTree + Filtre + Profondeur max).
* Deux sortes de hash implémentées :
  * Indépendant de la taille du fichier prenant 3 échantillons au début, au milieu et à la fin de chaque fichier (rapide, erreurs possibles).
  * Hash du fichier complet (très lent, pas d'erreurs).
* Cache des hashs dans un fichier txt.
* Méthode pour supprimer un fichier.
* Filtrage des fichiers par expression régulière, nom, nom refusé, extentions, extensions refusées, date et poids.
* Singleton pour stocker les erreurs d'accès aux fichiers.
* Singleton pour stocker les paramètres (pour l'instant que le chemin vers le fichier cache des hashs).

Code utilisé en phase 2 par Vivien Louradour

Baptiste Vrignaud
Projet ACDC - Fil A1
