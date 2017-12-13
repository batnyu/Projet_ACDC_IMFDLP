# Il me faut de la place
>Projet ACDC - Fil A1 - Baptiste Vrignaud
>
>Code utilisé en phase 2 par Vivien Louradour

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

### Utilisation du jar

Le jar pointe par défaut sur le dossierTest disponible dans le repo.

#####Commande de base
>Affiche l'arbre et les doublons.

    java -jar Projet_ACDC_IMFDLP.jar -tree -duplicates

 #####Options utilisables
>Filtre l'arbre et la recherche de doublons avec un pattern

*-regex=pattern*

    java -jar Projet_ACDC_IMFDLP.jar -regex='jpg|txt' -tree -duplicates
>Tous les fichiers plus grand que le paramètre

*-gtWeight=nbOctets* 

    java -jar Projet_ACDC_IMFDLP.jar -gtWeight=60000 -tree -duplicates
>Tous les fichiers plus petit que le paramètre

*-lwWeight=nbOctets* 

    java -jar Projet_ACDC_IMFDLP.jar -lwWeight=60000 -tree -duplicates