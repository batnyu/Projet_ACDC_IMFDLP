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

#### Commandes de base
* Affiche l'arbre

    java -jar Projet_ACDC_IMFDLP.jar -tree
    
* Affiche les doublons

    java -jar Projet_ACDC_IMFDLP.jar -duplicates

#### Options utilisables
* Filtre l'arbre et la recherche de doublons avec un pattern

*-regex=pattern* (à utiliser avant les commandes tree et duplicates car elles se servent de ce filtre)

    java -jar Projet_ACDC_IMFDLP.jar -regex='jpg|txt' -tree -duplicates
    
* Accepter tous les fichiers plus grand que le paramètre

*-gtWeight=nbOctets* 

    java -jar Projet_ACDC_IMFDLP.jar -gtWeight=60000 -tree -duplicates
    
* Accepter tous les fichiers plus petit que le paramètre

*-lwWeight=nbOctets* 

    java -jar Projet_ACDC_IMFDLP.jar -lwWeight=60000 -tree -duplicates
    
* Utiliser un niveau de parallèlisme pour la création de l'arbre et la recherche de doublons 
(à placer avant -tree et -duplicates)

*-parallelism=nb* 

    java -jar Projet_ACDC_IMFDLP.jar -parallelism=2 -tree -duplicates
    
* Restreint l'arbre à une profondeur de 2.

*-tree=depth* 

    java -jar Projet_ACDC_IMFDLP.jar -tree=2
    
* Restreint la recherche des doublons à l'arbre de profondeur 2.

*-duplicates=depth* 

    java -jar Projet_ACDC_IMFDLP.jar -duplicates=2