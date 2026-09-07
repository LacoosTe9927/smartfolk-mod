# Smart Folk — mod Fabric pour Minecraft 1.20.1

Ce mod ajoute les **Smart Folk** : de petits personnages autonomes qui :

- **explorent et construisent** tout seuls (ils posent des blocs pour ériger
  de petites tours, `BuildGoal`) ;
- **évoluent** : chaque action réussie (bloc posé, interaction sociale)
  donne de l'expérience, fait monter leur **niveau** et améliore leurs
  statistiques (vie, vitesse) ainsi que leurs compétences de construction
  et de socialisation ;
- existent en **8 couleurs** (rouge, bleu, vert, jaune, violet, orange,
  cyan, rose), visibles via un plastron en cuir teinté automatiquement
  à l'apparition ;
- **nouent des liens** entre eux : une "affinité" (-100 à +100) évolue à
  chaque rencontre (`SocializeGoal`). Au-delà d'un seuil ils deviennent
  amis et se suivent ; en dessous d'un autre seuil ils deviennent
  rivaux et **se battent** (`RivalryGoal`), ce qui dégrade encore la
  relation à chaque coup. Deux Smart Folk de la même couleur démarrent
  avec un petit a priori positif, mais rien ne les empêche de finir
  rivaux, ni deux couleurs différentes de devenir amies.

Contrairement à une tentative précédente sur Minecraft 26.2 (trop récente
pour que je puisse la vérifier), cette version cible **1.20.1**, une
version que je connais très bien : le code utilise des noms de classes
et méthodes que je suis en mesure de vérifier avec un bon niveau de
confiance, et inclut un **vrai rendu visuel** (le personnage réutilise
la silhouette du villageois vanilla, réduite, avec son plastron coloré
par-dessus).

## Installation — utilisation directe (sans coder)

1. Installe le **Fabric Loader** pour Minecraft 1.20.1 :
   https://fabricmc.net/use/installer/
2. Télécharge **Fabric API** pour 1.20.1 (obligatoire, ce mod en dépend) :
   https://modrinth.com/mod/fabric-api/versions
3. Compile ce projet en `.jar` (voir section suivante).
4. Place le `.jar` obtenu, ainsi que Fabric API, dans le dossier `mods`
   de ton installation Minecraft 1.20.1 avec le profil Fabric.

## Compiler le `.jar` toi-même (2 commandes)

Je n'ai pas pu compiler ce mod moi-même : mon environnement de travail
n'a pas accès à Internet pour télécharger Minecraft/Fabric, ce qui est
nécessaire au premier lancement de Gradle. Voici comment le faire
chez toi, en quelques minutes :

1. Installe le **JDK 17** (Temurin/Adoptium recommandé).
2. Ouvre un terminal dans ce dossier et lance :
   - Windows : `gradlew.bat build`
   - Mac/Linux : `./gradlew build`
3. Le fichier prêt à l'emploi apparaît dans
   `build/libs/smartfolk-mod-1.0.0.jar`.

(Aucun fichier `gradlew`/`gradle-wrapper.jar` n'est inclus dans ce zip
pour rester léger — si `./gradlew` ne fonctionne pas chez toi, installe
Gradle 8.x et lance simplement `gradle build` à la place, ou ouvre le
dossier dans IntelliJ IDEA qui proposera de générer le wrapper.)

Tester rapidement sans construire de monde : `./gradlew runClient` lance
directement une instance de jeu avec le mod chargé.

## Utilisation en jeu

- Ouvre l'inventaire créatif, onglet **Outils et utilitaires**, cherche
  l'oeuf "Smart Folk", et utilise-le sur le sol.
- Ou tape `/summon smartfolk:smart_folk` en jeu.
- Fais apparaître plusieurs individus et observe-les : ils construisent,
  se rencontrent, deviennent amis ou rivaux, et évoluent avec le temps.

## Structure du code

```
src/main/java/fr/smartfolk/
├── SmartFolkMod.java              → enregistrement de l'entité, de l'oeuf, du groupe créatif
├── client/
│   └── SmartFolkModClient.java    → rendu visuel (modèle + texture)
└── entity/
    ├── ColorVariant.java          → les 8 couleurs possibles
    ├── SmartFolkEntity.java       → logique principale : niveau, compétences, relations, NBT
    └── ai/
        ├── BuildGoal.java         → construction autonome de petites structures
        ├── SocializeGoal.java     → rencontres et évolution de l'affinité
        └── RivalryGoal.java       → combats entre rivaux
```

## Pistes d'amélioration faciles

- Un modèle 3D 100% custom via Blockbench, plutôt que la silhouette du
  villageois (voir le commentaire dans `SmartFolkModClient.java`).
- Varier les matériaux de construction selon la couleur ou le niveau
  (`BuildGoal.BUILD_MATERIAL`).
- Ajouter une apparition naturelle dans le monde (actuellement, seuls
  l'oeuf d'apparition et `/summon` fonctionnent, pour rester simple).
- Une commande `/smartfolk info` affichant le niveau et les relations
  d'un Smart Folk ciblé.
- Faire des amis un vrai "groupe" qui construit une structure commune.

## Si une erreur de compilation apparaît malgré tout

Le code a été écrit avec les mappings Yarn `1.20.1+build.10`. Si une
version un peu différente de Fabric API/Loader est déjà installée sur
ta machine et provoque une erreur, le plus simple est d'ouvrir le
message d'erreur dans IntelliJ IDEA : un clic sur le nom de méthode en
rouge propose en général directement le bon renommage.
