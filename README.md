# MyMovies - Backend

Ce Backend sert l'application **MyMovies** *(https://github.com/CharlesMassuard/MyMovies_Frontend)*.

## 1. Lancement du serveur

Le serveur utilise **SpringBoot**.

Afin de lancer le serveur, executer la commande suivante dans le repertoire courant :

```shell
./mvnw spring-boot:run
```

Afin que le projet se lance, les variables d'environnement suivante doivent être exportées :

**TMDB_API_TOKEN**, **DB_URL**, **DB_USERNAME**, **DB_PASSWORD**

## 2. Endpoints

### 2.1 Films

L'endpoint pour les films est **/api/movies**.

**GET**

- **/trending** : films populaires
- **/in-theater** : films en salles *(sortis les 40 derniers jours)*
- **/trending/day** : films populaire aujourd'hui
- **/search** : films correspondants aux paramètres de la requete
- **/{id}** : film correspondant à l'id demandé
- **/{id}/credits** : crédits du film correspondant à l'id demandé

### 2.2 Utilisateurs

L'endpoint pour les utilisateurs est **/api/auth**.

**POST**

- **/login** : permet de connecter l'utilisateur en renvoyant les datas de l'utilisateur et son token associé. Prend en parametres l'adresse mail et le mot de passe.
- **/register** : permet de créer ltuilisateur. Renvoit les datas de cet utilisateur et son token associé. Prend en parametres l'adresse mail et le mot de passe.

**PUT**

- **/update/pseudo** : permet de mettre à jour le pseudo de l'utilisateur
- **/update/mail** : permet de mettre à jour le mail de l'utilisateur
- **/update/password** : permet de mettre à jour le mot de passe de l'utilisateur

**DELETE**

- **/delete** : permet de supprimer l'utilisateur

### 2.3 Films liés à l'utilisateur *(UserMovie)*

L'endpoint est **/api/user/movies**.

**GET**

- **/** : permet de retourner la liste des films que l'utilisateur a dans sa liste de lecture. Pour chaque film, renvoit son status, sa note et son commentaire.
- **/status/{movieId}** : permet d'obtenir le status du film ayant l'id *movidId*
- **/watched-date/{movieId}** : permet d'obtenir la date de visionnage du film ayant l'id *movidId*
- **/rating/{movieId}** : permet d'obtenir la note attribué au film ayant l'id *movidId*
- **/comment/{movieId}** : permet d'obtenir le commentaire attribué au film ayant l'id *movidId*

**POST**

- **/to-watch/{movieId}** *(POST)* : permet d'ajouter le film ayant l'id *movieId* à la liste de lecture de l'utilisateur avec le status **TO_WATCH**

**PUT**

- **/status/{movieId}** : permet de mettre à jour le status du film ayant l'id *movieId*
- **/rate/{movieId}** : permet de mettre à jour la note du film ayant l'id *movieId*. Si un utilisateur note le film alors qu'il n'est pas dans la table *UserMovie*, la relation est créée avec le status à "Vu"

**DELETE**

- **/status/{movieId}** : permet de supprimer le film ayant l'id *movieId* de la liste de lecture de l'utilisateur