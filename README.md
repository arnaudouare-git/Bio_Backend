# BioConversion — Bio_Backend

![Statut](https://img.shields.io/badge/statut-en%20d%C3%A9veloppement-yellow)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-bioconversion__db-336791)
![Sécurité](https://img.shields.io/badge/S%C3%A9curit%C3%A9-JWT-red)
![Licence](https://img.shields.io/badge/licence-MIT-lightgrey)

> Ces badges sont statiques (générés à la main, pas par une CI) : il n'y a pas encore de pipeline d'intégration continue sur ce projet. Un badge "build passing" ne serait pas honnête tant qu'aucun test automatique ne tourne — voir [Guide de contribution](#5-guide-de-contribution).

---

## 1. Titre & badge de statut

**BioConversion** — API REST (Spring Boot) de la plateforme de gestion d'élevage de larves de mouches soldats noires (BSF) pour le Burkina Faso. Projet étudiant, **Groupe 1 Soir**, Licence 3 Réseau Informatique et Télécommunications (Institut Supérieur de Technologie). Statut actuel : **backend fonctionnellement complet** (tous les modules V1 codés et testés), sécurisé par authentification JWT ; frontend Angular pas encore commencé.

---

## 2. Description & contexte

BioConversion met en relation des **producteurs** de larves BSF et des **éleveurs (clients)** qui en achètent pour nourrir poissons ou volailles. La plateforme résout un problème concret : aujourd'hui, les producteurs et les éleveurs se trouvent difficilement (pas de marketplace centralisée), et rien ne garantit qu'un producteur a réellement été formé et vérifié avant de vendre. BioConversion apporte une marketplace géolocalisée, une vérification obligatoire (formation + pièce d'identité) avant toute vente, le paiement Orange Money (ou cash à la livraison), un suivi IoT des serres de production (température/humidité en temps réel, alertes automatiques), et une API entièrement protégée par authentification à jeton (JWT) avec contrôle d'accès par rôle.

Ce dépôt contient uniquement le **backend**. Le frontend (Angular) vivra dans un projet séparé, `Bio_Frontend`. Une maquette visuelle des écrans a déjà été validée par l'équipe avant le début du code frontend.

---

## 3. Prérequis & installation

**Versions exactes utilisées par ce projet :**

| Outil | Version |
|---|---|
| Java (JDK) | 17 |
| Spring Boot | 4.1.1 (géré par Maven, rien à installer à part le JDK) |
| PostgreSQL | 14 ou plus récent |
| Maven | aucun à installer — le wrapper `mvnw` / `mvnw.cmd` est fourni dans le dépôt |

**Étapes d'installation, sur une machine vierge :**

1. Installer le JDK 17 et PostgreSQL, vérifier qu'ils sont accessibles :
   ```
   java -version
   psql --version
   ```
2. Dans PostgreSQL, créer une base vide :
   ```sql
   CREATE DATABASE bioconversion_db;
   ```
3. Cloner/copier ce dépôt, puis éditer `src/main/resources/application.properties` :
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/bioconversion_db
   spring.datasource.username=<votre utilisateur postgres>
   spring.datasource.password=<votre mot de passe>
   jwt.secret=<une longue chaine aleatoire propre a votre machine>
   jwt.expiration-ms=86400000
   ```
   ⚠️ Le fichier de ce dépôt contient des identifiants et un secret JWT de développement local. **Ne jamais pousser de vrais mots de passe ni le vrai secret JWT dans un dépôt partagé** — en production, ces valeurs doivent venir de variables d'environnement, jamais du code source.
4. Lancer l'application depuis la racine du projet (celle qui contient `pom.xml`) :
   ```
   mvnw clean spring-boot:run
   ```
   (ou double-cliquer sur `run.bat` sous Windows, qui fait exactement ça). Le `clean` n'est pas optionnel — voir [Problèmes connus](#problèmes-connus-et-astuces).
5. L'API est prête sur **`http://localhost:8080`**. Hibernate crée automatiquement les tables au premier démarrage (`ddl-auto=update`).

### Alternative recommandée : lancer avec Docker (une seule commande)

Pour éviter à quelqu'un d'installer Java, Maven et PostgreSQL à la main, le dépôt fournit un `Dockerfile` + `docker-compose.yml` qui démarrent l'API **et** la base PostgreSQL ensemble :

```
docker compose up --build
```

Ça construit l'image du backend (build Maven multi-étapes), lance une base PostgreSQL vierge dans un conteneur séparé, attend qu'elle soit prête, puis démarre l'API — accessible sur `http://localhost:8080` comme en installation manuelle. Rien à installer à part Docker (et Docker Compose, inclus avec Docker Desktop).

Les identifiants de la base et le secret JWT utilisés par Docker sont définis directement dans `docker-compose.yml` (valeurs de développement uniquement, à changer avant toute démo publique) — `application.properties` n'a besoin d'aucune modification, les variables d'environnement le surchargent automatiquement.

Pour tout arrêter : `docker compose down` (ajouter `-v` pour aussi supprimer les données PostgreSQL du volume).

---

## 4. Utilisation & exemples

L'API n'a pas encore de frontend : elle se pilote avec `curl` ou Postman. Body en JSON pour tout `POST`/`PUT`.

**Inscription d'un Client (achète seulement, compte actif immédiatement) :**
```bash
curl -X POST http://localhost:8080/api/auth/inscription \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Ouedraogo", "prenom": "Awa",
    "email": "awa@example.com", "motDePasse": "motdepasse123",
    "telephone": "70123456", "role": "CLIENT"
  }'
```

**Inscription d'un Producteur (nécessite formation + CNIB avant activation) :**
```bash
curl -X POST http://localhost:8080/api/auth/inscription \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Kabore", "prenom": "Issa",
    "email": "issa@example.com", "motDePasse": "motdepasse123",
    "telephone": "70000099", "role": "PRODUCTEUR",
    "documentIdentiteRef": "B00000000"
  }'
```

**Connexion — renvoie désormais un token JWT :**
```bash
curl -X POST http://localhost:8080/api/auth/connexion \
  -H "Content-Type: application/json" \
  -d '{ "email": "awa@example.com", "motDePasse": "motdepasse123" }'
```
```json
{
  "id": 1, "nom": "Ouedraogo", "prenom": "Awa", "email": "awa@example.com",
  "role": "CLIENT", "statutVerificationCnib": "VERIFIE",
  "token": "eyJhbGciOiJIUzUxMiJ9......"
}
```

**Utiliser le token sur une route protégée** (ajouter l'en-tête `Authorization: Bearer <token>`) :
```bash
curl http://localhost:8080/api/commandes/acheteur/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9......"
```
Sans en-tête (ou token invalide/expiré) → `401`. Avec un token valide mais un rôle insuffisant sur une route réservée → `403`.

**Publier un produit (le producteur doit être `VERIFIE` ET authentifié, sinon `403`) :**
```bash
curl -X POST http://localhost:8080/api/produits \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token du producteur>" \
  -d '{
    "producteurId": 1, "nom": "Larves fraiches - lot A",
    "etat": "FRAICHES", "prixUnitaire": 2500, "stock": 100
  }'
```

**Consulter le catalogue public (aucune authentification requise) :**
```bash
curl http://localhost:8080/api/produits
```

**Passer une commande (l'acheteur doit être authentifié et `VERIFIE` ; stock insuffisant → `409`) :**
```bash
curl -X POST http://localhost:8080/api/commandes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token de l'acheteur>" \
  -d '{
    "acheteurId": 1, "adresseLivraison": "Secteur 15, Ouagadougou",
    "lignes": [ { "produitId": 1, "quantite": 10 } ]
  }'
```

**Initier un paiement Orange Money sur une commande :**
```bash
curl -X POST http://localhost:8080/api/paiements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{ "commandeId": 1, "methode": "ORANGE_MONEY" }'
```

**Référence complète des endpoints :**

*Authentification (publique)*

| Méthode | Endpoint | Résultat |
|---|---|---|
| POST | `/api/auth/inscription` | `201` |
| POST | `/api/auth/connexion` | `200` (+ `token`) ou `401` |

*Administration — réservé au rôle `ADMINISTRATEUR`*

| Méthode | Endpoint | Résultat |
|---|---|---|
| POST | `/api/admin/formations` | `201` |
| GET | `/api/admin/formations` | liste + nb participants |
| POST | `/api/admin/formations/{formationId}/participants/{producteurId}` | `204` |
| PUT | `/api/admin/producteurs/{producteurId}/activer` | `204` ou `400` |

*Produits*

| Méthode | Endpoint | Accès | Résultat |
|---|---|---|---|
| GET | `/api/produits` | public | catalogue (stock > 0) |
| GET | `/api/produits/producteur/{producteurId}` | public | produits d'un producteur |
| POST | `/api/produits` | PRODUCTEUR/ADMIN | `201` ou `403` |
| PUT | `/api/produits/{produitId}` | PRODUCTEUR/ADMIN | `200` |
| DELETE | `/api/produits/{produitId}` | PRODUCTEUR/ADMIN | `204` |

*Commandes — authentifié (tout rôle)*

| Méthode | Endpoint | Résultat |
|---|---|---|
| POST | `/api/commandes` | `201`, `403` ou `409` |
| GET | `/api/commandes/acheteur/{acheteurId}` | commandes de l'acheteur |
| GET | `/api/commandes/{commandeId}` | détail |
| PUT | `/api/commandes/{commandeId}/statut` | `200` |

*Paiements — authentifié (tout rôle)*

| Méthode | Endpoint | Résultat |
|---|---|---|
| POST | `/api/paiements` | `201` |
| PUT | `/api/paiements/{id}/confirmer` | `200`, `400` (cash) ou `409` (déjà traité) |
| GET | `/api/paiements/commande/{commandeId}` | liste des tentatives de paiement |

*Serres / IoT*

| Méthode | Endpoint | Accès | Résultat |
|---|---|---|---|
| POST | `/api/serres` | PRODUCTEUR/ADMIN | `201` |
| GET | `/api/serres/producteur/{producteurId}` | public (lecture) | liste des serres |
| GET | `/api/serres/{serreId}/dashboard` | public (lecture) | vue d'ensemble (mesures, alertes, capteurs silencieux) |
| POST | `/api/capteurs` | PRODUCTEUR/ADMIN | `201` |
| GET | `/api/capteurs/serre/{serreId}` | public (lecture) | liste des capteurs |
| POST | `/api/mesures` | PRODUCTEUR/ADMIN | `201` (+ alerte auto si seuil dépassé) |
| GET | `/api/mesures/capteur/{capteurId}` | public (lecture) | historique des mesures |
| GET | `/api/alertes/serre/{serreId}` | public (lecture) | alertes de la serre |
| PUT | `/api/alertes/{alerteId}/traiter` | PRODUCTEUR/ADMIN | `200` |

**Codes d'erreur renvoyés par l'API :**

| Exception | Code HTTP |
|---|---|
| `EmailDejaUtiliseException` | 409 |
| `IdentifiantsInvalidesException` | 401 |
| `RessourceIntrouvableException` | 404 |
| `CompteNonEligibleActivationException` | 400 |
| `CompteNonVerifieException` | 403 |
| `StockInsuffisantException` | 409 |
| `PaiementDejaTraiteException` | 409 |
| `IllegalArgumentException` | 400 |
| Non authentifié (token manquant/invalide/expiré) | 401 |
| Authentifié mais rôle insuffisant | 403 |

---

## 5. Guide de contribution

Projet de groupe (5 personnes) — règles à suivre pour que le dépôt reste cohérent :

- **Branches** : une branche par fonctionnalité, nommée `feature/nom-de-la-fonctionnalite` (ex. `feature/module-paiements`). Ne jamais commiter directement sur `main`.
- **Commits** : message court à l'impératif décrivant le changement (ex. `Ajoute la vérification CNIB avant activation`), pas de message générique du type `update` ou `fix`. **Committer régulièrement** (après chaque bloc de travail, pas seulement à la fin d'un module entier) — un incident survenu en septembre 2026 (un outil de migration automatique a effacé tout le travail non committé) a rappelé pourquoi c'est important.
- **Revue de code** : une Pull Request doit être relue par au moins un autre membre du groupe avant fusion dans `main` — même sur un petit projet, ça évite qu'une régression passe inaperçue.
- **Conventions de code** : respecter l'architecture en 6 couches (voir plus bas) — aucune logique métier dans un `controller/`, aucune requête SQL manuelle si Spring Data JPA peut la générer. Package racine toujours en minuscules : `com.example.bio_backend`.
- **Tests** : il n'y a pas encore de tests automatisés (dépendances de test présentes dans `pom.xml` mais aucun test écrit) — à ajouter avant d'afficher un badge de couverture honnête.
- Les règles détaillées (style de code, processus de review complet) pourront être extraites dans un fichier `CONTRIBUTING.md` séparé si l'équipe grandit.

---

## 6. Licence

Ce projet est distribué sous licence **MIT** — Groupe 1 Soir, Institut Supérieur de Technologie, 2026. Cela signifie qu'il peut être réutilisé, modifié et redistribué librement (y compris à des fins commerciales), à condition de conserver la mention de copyright originale. Choix adapté à un projet étudiant destiné à figurer dans un portfolio (GitHub) : sans licence explicite, un dépôt reste par défaut sous copyright strict, ce qui empêcherait légalement quiconque de le réutiliser même à titre d'exemple.

```
MIT License

Copyright (c) 2026 Groupe 1 Soir — BioConversion

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

Si l'établissement ou l'encadrement pédagogique impose une autre licence (ou l'absence de licence publique pour un rendu académique), remplacez cette section — mais gardez-en une : c'est un point explicitement noté dans la grille du cours.

---

## Compléments (au-delà des 6 points du cours)

### Architecture du code (6 couches)

`entity/` (tables DB) → `repository/` (accès DB via Spring Data JPA) → `dto/` (forme du JSON échangé) → `service/` (toute la logique métier) → `controller/` (HTTP uniquement) → `exception/` (erreurs métier + `GlobalExceptionHandler`), plus `config/` pour la sécurité (JWT, Spring Security).

### Modèle métier

| Rôle | Vend ? | Achète ? | Inscription |
|---|---|---|---|
| **Producteur** | Oui | Oui | Formation + vérification CNIB par un Admin avant activation (`EN_ATTENTE` → `VERIFIE`) |
| **Client** | Non | Oui | Libre et immédiate (`VERIFIE` dès la création) |
| **Administrateur** | — | — | Gère les formations et active les comptes Producteur |

Le champ `statutVerificationCnib == "VERIFIE"` est le verrou unique qui protège aussi bien la vente que l'achat, pour les deux rôles, grâce à l'héritage JPA `JOINED` (`Utilisateur` → `Producteur`/`Client`/`Administrateur`).

### Sécurité (Spring Security + JWT)

- Authentification **stateless** : aucune session côté serveur, chaque requête vers une route protégée doit porter l'en-tête `Authorization: Bearer <token>`.
- Le token est délivré à la connexion (`/api/auth/connexion`) et à l'inscription, valable 24h (`jwt.expiration-ms`).
- Les routes publiques (`/api/auth/**`, et les `GET` de consultation sur Produits/Serres/Capteurs/Mesures/Alertes) restent accessibles sans token.
- Les routes de création/modification sont réservées par rôle (`hasRole`/`hasAnyRole`) — voir le tableau des endpoints ci-dessus, colonne "Accès".
- En cas de token manquant/invalide → `401`. En cas de rôle insuffisant → `403`. Les deux réponses sont au format JSON (`timestamp`/`status`/`message`), cohérent avec le reste de l'API.

### Seuils d'alerte des serres (Module IoT)

| Condition | Seuil | Destinataires |
|---|---|---|
| Température trop basse | < 25°C | Producteur + Admin |
| Température trop élevée | > 39°C | Producteur + Admin |
| Humidité trop basse | < 50% | Producteur + Admin |
| Humidité trop élevée | > 80% | Producteur + Admin |
| Capteur silencieux | > 5 min sans donnée | Producteur + Admin |

### Structure du projet

```
Bio_Backend/
├── mvnw, mvnw.cmd, run.bat, pom.xml
└── src/main/java/com/example/bio_backend/
    ├── entity/       # Utilisateur, Producteur, Client, Administrateur, Produit, Commande,
    │                 # LigneCommande, Formation, Notification, Paiement, Serre, Capteur, Mesure, Alerte...
    ├── repository/   # une interface JpaRepository par entité
    ├── dto/          # *Request / *Response
    ├── service/      # AuthService, AdminService, ProduitService, CommandeService,
    │                 # PaiementService, SerreService, MesureService
    ├── controller/   # AuthController, AdminController, ProduitController, CommandeController,
    │                 # PaiementController, SerreController, CapteurController, MesureController, AlerteController
    ├── config/       # JwtService, JwtAuthenticationFilter, SecurityConfig, PasswordEncoderConfig,
    │                 # UtilisateurDetailsImpl/ServiceImpl
    └── exception/    # exceptions métier + GlobalExceptionHandler
```

### État d'avancement

| Module | Statut |
|---|---|
| Authentification | ✅ |
| Admin / Formation | ✅ |
| Produits | ✅ |
| Commandes | ✅ |
| Paiements (Orange Money) | ✅ |
| Serres / IoT | ✅ |
| Sécurité (Spring Security, JWT, rôles) | ✅ |
| Bean Validation, Swagger | ⏳ |
| Frontend Angular (`Bio_Frontend`) | ⏳ maquette validée, code pas commencé |

### Problèmes connus et astuces

- `mvn spring-boot:run` sans `clean` peut réutiliser d'anciennes classes compilées → toujours `mvnw clean spring-boot:run` ou `run.bat`.
- Port 8080 déjà utilisé : `netstat -ano | findstr :8080` puis `taskkill /PID <pid> /F` (Windows).
- `ddl-auto=update` ne supprime jamais une colonne obsolète — un changement de modèle peut nécessiter un `ALTER TABLE ... DROP COLUMN ...` manuel en base.
- Ne jamais taper de commande terminal directement dans un fichier source (déjà arrivé par accident).
- **Ne jamais laisser un outil externe (ex. assistant de migration Java d'un IDE) modifier le dépôt sans avoir d'abord committé son travail en cours** — un tel outil peut créer sa propre branche et réinitialiser le dossier de travail, effaçant tout ce qui n'était pas committé.
