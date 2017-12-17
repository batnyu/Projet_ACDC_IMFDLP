# Compte-rendu Partie Backend

## 1 - Utilisation du jar

Le jar pointe par défaut sur le dossierTest disponible dans le repo.

### Commandes de base

* Affiche l'arbre

        java -jar Projet_ACDC_IMFDLP.jar -tree
    
* Affiche les doublons

        java -jar Projet_ACDC_IMFDLP.jar -duplicates
        
* Change le dossier cible (à mettre avant -tree et -duplicates, ne pas oublier les doubles quotes)

        java -jar Projet_ACDC_IMFDLP.jar -dir="C:\Users\Roger\Desktop\Nouveau dossier" -tree

### Options utilisables
* Filtre l'arbre et la recherche de doublons avec un pattern

    *-regex=pattern* (à utiliser avant les commandes tree et duplicates car elles se servent de ce filtre)

        java -jar Projet_ACDC_IMFDLP.jar -regex='jpg|txt' -tree -duplicates
        java -jar Projet_ACDC_IMFDLP.jar -regex=canelle -tree -duplicates
    
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
        
* Afficher les erreurs parvenu lors de la construction de l'arbre et de la recherche des doublons
(exemple: Accès non autorisé à un fichier)

    *-errors* 

        java -jar Projet_ACDC_IMFDLP.jar -tree -duplicates -errors
        
## 2 - Organisation du projet

La documentation est disponible dans le dossier /javadoc.

### 2.1 - Recherche en amont

Mon but était de trouver le meilleur moyen pour parcourir un système de fichier. J'ai trouvé le lien suivant :
https://github.com/brettryan/io-recurse-tests. J'ai donc tout d'abord essayé avec les streams de Java 8
 mais je n'ai pas réussi à séparer les fichiers et dossiers pour créer ma structure. Donc, je suis passé à la librairie Java(nio) Walk File Tree
 qui a plusieurs avantages :
 - Séparation du parcours des dossiers et des fichiers.
 - Méthode avant le parcours d'un répertoire et après.
 - Gestion des erreurs séparée.
 - Créer la structure en parallèle du parcours de l'arborescence de fichiers.
 
Pour ma structure de données, j'avais deux choix :
- Utiliser DefaultMutableTreeNode, un objet Java avec déjà toutes les fonctionnalités nécessaire et compatible avec un JTree.
- Créer ma propre structure de données avec le pattern Composite.

J'ai choisi d'utiliser DefaultMutableTreeNode car je n'avais pas d'intérêt à créer ma propre structure. 
Mais par la suite, j'ai implémenté ma propre structure pour le cache (voir les difficultés rencontrées pour le cache).

En ce qui concerne le multi-threading, j'ai fait quelques recherches et j'ai trouvé le framework Fork/Join qui m'a paru être une bonne solution pour cette application.
Il permet de paralléliser des opérations récursives.

### 2.2 - Architecture

* **Structure de données**

    Ma structure est composée d'une classe File1 qui possède un vecteur de File1 (Pattern Composite).
    File1 possède tous les attributs caractérisant un répertoire/fichier (nom, path, poids, dernier temps de modif, dossier ou non, enfants). J'ai récupéré toutes les méthodes de DefaultMutableTreeNode au cas où celui qui reprendrait mon code en aurait besoin.
    
* **Modèle d'arbre**

    Comme le tutoriel de JTree indique pour créer sa propre structure de données et se passer de DefaultMutableTreeNode, j'ai créé 
    mon propre FileTreeModel qui agrège ma structure de données File1 et implémente TreeModel.
    
* **Core**

   * FileTree
   
       C'est la classe qui possède la logique de base de l'application. Elle utilise les classes
       RecursiveCollectDuplicates et RecursiveCreateTree.
       
   * RecursiveCollectDuplicates
   
       Cette classe utilise WalkFileTree et Fork/Join pour collecter les doublons et les stocker dans la ConcurrentHashMap de FileTree.
       
   * RecursiveCreateTree
   
       Cette classe utilise WalkFileTree et Fork/Join pour parcourir le système de fichiers et créer une structure de données File1.
       J'ai choisi de calculer les poids des répertoires à la construction car le but du projet est d'avoir une vision claire de ce qui prend le plus de place.
       L'arbre construit aura donc toutes les bonnes tailles de répertoire.
    
* **Utils**

    * Filter

        Classe qui permet de filtrer la construction d'un arbre et la recherche des doublons
        
    * Hash
    
        Classe qui possède deux hashs
        
        * Hash partiel
        
            Prend trois échantillons au début, au milieu et à la fin du fichier et est donc indépendant de la taille du fichier.
            
        * Hash complet
        
            L'intégralité du fichier est hashé.
    
* **Services**

    * Settings
    
        Ce singleton permet de centraliser tous les paramètres de l'application pour pouvoir y accéder en lecture et écriture de n'importe où.
    * ErrorLogging
    
        Ce singleton permet de centraliser les erreurs liés au I/O quand on parcourt le système de fichiers.
    
* **Renderer du JTree**

    J'ai du implémenté mon propre FileTreeCellRenderer qui hérite de DefaultTreeCellRenderer afin de
    corriger les icônes pour les dossiers vides qui étaient par défaut des feuilles.

## 3 - Difficultés rencontrés

* **Framework multi-thread Fork/Join**

    Je n'ai pas réussi à optimiser le fonctionnement du framework. A la base, il est optimisé pour un arbre binaire équilibré
    où chaque tâche élémentaire est la même. Or pour un système de fichiers, l'arbre n'est pas équilibré.
    Il faut donc déterminer des conditions pour la tâche élémentaire. Mais je n'ai pas eu le temps et l'inspiration pour le faire.
    La tâche élémentaire de mon projet est le répertoire. A chaque répertoire, je crée une nouvelle instance de la classe Recursive.
    
    La doc en parle ici : http://www.oracle.com/technetwork/articles/java/fork-join-422606.html
    
    >In particular, the “map” phase that identifies chunks of data “small enough” to be processed 
    independently in an efficient manner does not know the data space topology in advance. 
    This is especially true for graph-based and tree-based data structures. 
    In those cases, algorithms should create hierarchies of “divisions,” 
    waiting for subtasks to complete before returning a partial result.
    
* **Collecter les doublons en multi-thread**

    J'ai du utiliser une ConcurrentHashMap avec une ConcurrentLinkedQueue pour que les threads en parallèle puissent écrire dans la hashmap en même temps.

* **Limiter la profondeur en multi-thread**

    Sachant que j'utilise une instance de WalkFileTree par répertoire, je ne peux pas utiliser sa fonctionnalité MAX_DEPTH qui permet de limiter le parcours en profondeur.
    J'ai donc utilisé pathNameCount qui me permet de vérifier à combien de niveau de profondeur je me trouve par rapport à mon chemin de base.
    Je n'ajoute pas les répertoires et les fichiers qui ont un chemin supérieur à ma limite. Mais j'ai choisi de les parcourir quand même afin de calculer la taille de tous les dossiers.

* **Effacer les dossiers vides quand on filtre**

    Au début, pour régler ce problème, après la construction de l'arbre, si le filtre était activé, je parcourais les noeuds de l'arbre que j'avais construit
    afin de supprimer les dossiers vides.
    
    Je teste maintenant à la construction. Je n'ajoute pas à mon arbre les dossiers vides si un filtre est activé.

* **Cache**

    Au début, j'ai voulu utiliser le json pour le cache. J'ai essayé avec JsonPath pour extraire facilement les données,
    ensuite avec JsonSurf pour utiliser un streaming. Avec ses deux libraires, je n'ai pas réussi. 
    
    Je me suis ensuite servi de la libraire gson de Google. Cette libraire a l'avantage de pouvoir convertir un json en ma structure et inversément
    avec simplement la commande  
     C'est pourquoi, j'ai abandonné DefaultMutableTreeNode pour ma propre structure. En effet, DefaultMutableTreeNode sépare 
     la structure des données ce qui est incompatible avec la conversion en json. 
     
     J'ai réussi à implémenter un peu près la lecture du json qu'on peut voir dans CacheUpdate.json. Mais c'était très laborieux et je n'ai pas eu le temps de finir.

    Je me suis donc rabattu vers un simple fichier pour stocker les hashs de mes fichiers avec un timestamp.
    Je mets en cache juste les hashs de mes fichiers sachant qu'on sera obligé de toute façon de reparcourir le système de fichier
    à chaque démarrage de l'application car je n'ai pas implémenté de hooks qui aurait permis de surveiller le système de fichiers
    et de faire un cache optimal.
    
    Sachant qu'avec le hash qui prend des échantillons, ce sera surement plus lent de chercher le hash enregistré dans tous le fichier que de le recalculer.
    Le cache est donc plutôt utilisable avec le hash lent de l'intégralité des fichiers.
