# Documentation QuickRide

## Structure du Projet

### Classes Principales

1. **Main.java**
   - Point d'entrée de l'application
   - Initialise l'interface de connexion (LoginUI)

2. **LoginUI.java**
   - Interface de connexion utilisateur
   - Gère l'authentification des clients
   - Redirige vers ClientUI après connexion réussie

3. **ClientUI.java**
   - Interface utilisateur principale pour les clients
   - Permet de :
     - Commander un véhicule
     - Annuler une course
     - Voir le statut de la commande

4. **VehiculeManager.java** (Classe abstraite)
   - Gestionnaire central des véhicules
   - Gère les collections de véhicules disponibles/non disponibles
   - Implémente la logique de base pour :
     - L'attribution des véhicules
     - L'annulation des courses
     - La gestion des requêtes clients

5. **TaxiManager.java** et **BateauManager.java**
   - Héritent de VehiculeManager
   - Implémentent la logique spécifique à chaque type de véhicule

6. **ClientRequest.java**
   - Représente une demande de course
   - Stocke l'ID du client et sa localisation

7. **Vehicule.java** (Classe abstraite)
   - Classe de base pour les véhicules
   - Définit les attributs communs (ID, localisation)

8. **Taxi.java** et **Bateau.java**
   - Héritent de Vehicule
   - Implémentent les spécificités de chaque type de véhicule

### Base de Données

Structure de la base de données `quickride_db` :

1. **Table `vehicules`**
   - id (PK)
   - type
   - localisation
   - statut

2. **Table `clients`**
   - id (PK)
   - nom
   - telephone
   - pwd

3. **Table `requetes`**
   - id (PK)
   - client_id
   - localisation
   - statut
   - heure_demande

4. **Table `attributions`**
   - id (PK)
   - id_requete (FK)
   - id_vehicule (FK)
   - heure_attrib

## Interactions entre les Classes

1. **Flux de Commande de Course**
   ```
   ClientUI -> ClientRequest -> VehiculeManager
   -> TaxiManager/BateauManager -> Base de données
   ```

2. **Flux d'Annulation de Course**
   ```
   ClientUI -> VehiculeManager -> Base de données
   -> Mise à jour des collections en mémoire
   ```

3. **Flux d'Authentification**
   ```
   LoginUI -> Base de données -> ClientUI
   ```

## Gestion des Erreurs

### 1. Gestion des Erreurs de Base de Données

- Utilisation de blocs try-catch pour toutes les opérations SQL
- Gestion des transactions avec commit/rollback
- Messages d'erreur détaillés pour le débogage
- Fermeture propre des connexions dans les blocs finally

### 2. Gestion des Erreurs d'Interface

- Validation des entrées utilisateur
- Messages d'erreur clairs pour l'utilisateur
- Désactivation/activation appropriée des boutons
- Gestion des états d'erreur dans l'interface

### 3. Gestion des Erreurs Métier

- Vérification de la disponibilité des véhicules
- Validation des attributions
- Gestion des conflits de réservation
- Cohérence des données entre la mémoire et la base de données

## Points d'Attention

1. **Synchronisation**
   - Le ClientManager utilise le pattern Singleton thread-safe
   - Les collections de véhicules sont protégées contre les accès concurrents

2. **Transactions**
   - Toutes les opérations critiques sont dans des transactions
   - Rollback automatique en cas d'erreur
   - Commit explicite pour valider les opérations

3. **Cohérence des Données**
   - Mise à jour simultanée de la base de données et des collections en mémoire
   - Vérification des contraintes de clés étrangères
   - Gestion des états des véhicules et des requêtes

## Configuration et Déploiement

### Prérequis
- Java 17 ou supérieur
- MySQL 8.0 ou supérieur
- Maven pour la gestion des dépendances

### Configuration de la Base de Données
1. Créer la base de données `quickride_db`
2. Exécuter le script `quickride_v2.sql`
3. Configurer les identifiants dans les classes :
   - DB_URL: jdbc:mysql://localhost:3306/quickride_db
   - DB_USER: rider
   - DB_PASSWORD: ridepass

### Compilation et Exécution
```bash
mvn clean install
mvn exec:java
```

## Architecture Technique

### Patterns de Conception Utilisés
1. **Singleton**
   - ClientManager
   - TaxiManager
   - BateauManager

2. **Factory Method**
   - Création des véhicules dans VehiculeManager

3. **Observer**
   - Mise à jour de l'interface utilisateur lors des changements d'état

### Gestion de la Mémoire
- Collections en mémoire pour les véhicules disponibles/non disponibles
- File d'attente pour les requêtes clients
- Nettoyage automatique des ressources dans les blocs finally

### Sécurité
1. **Authentification**
   - Validation des identifiants
   - Protection contre les injections SQL

2. **Gestion des Sessions**
   - Identification unique des clients
   - Traçabilité des opérations

## Maintenance et Support

### Procédures de Maintenance
1. **Sauvegarde de la Base de Données**
   - Sauvegarde quotidienne recommandée
   - Conservation des logs de transactions

2. **Mise à Jour**
   - Procédure de mise à jour de la base de données
   - Gestion des versions

### Dépannage
1. **Problèmes Courants**
   - Erreurs de connexion à la base de données
   - Problèmes de synchronisation
   - Erreurs de contraintes de clés étrangères

2. **Solutions**
   - Vérification des logs
   - Procédures de récupération
   - Points de restauration

## Analyse des Complexités Temporelles

### Méthodes Principales de VehiculeManager

1. **loadVehiculesFromDatabase()**
   - **Fonction** : Charge tous les véhicules disponibles depuis la base de données
   - **Complexité** : O(n) où n est le nombre de véhicules disponibles
   - **Paramètres dominants** : Nombre de véhicules dans la base de données
   - **Détails** :
     - Une seule requête SQL : O(1)
     - Parcours du ResultSet : O(n)
     - Création des objets Vehicule : O(n)
     - Ajout aux collections : O(1) par véhicule

2. **addClientRequest(ClientRequest request)**
   - **Fonction** : Ajoute une nouvelle requête client à la file d'attente
   - **Complexité** : O(1)
   - **Paramètres dominants** : Aucun (opération constante)
   - **Détails** :
     - Ajout à la Queue : O(1)
     - Insertion en base de données : O(1)
     - Gestion de la transaction : O(1)

3. **assignVehiculeToNextClient()**
   - **Fonction** : Attribue un véhicule au prochain client dans la file
   - **Complexité** : O(m) où m est le nombre de véhicules disponibles
   - **Complexité moyenne** : O(m/2) si les véhicules sont uniformément distribués
   - **Complexité minimale** : O(1) si le premier véhicule correspond
   - **Paramètres dominants** : Nombre de véhicules disponibles
   - **Détails** :
     - Vérification de la file : O(1)
     - Recherche du véhicule : O(m)
     - Mise à jour des collections : O(1)
     - Sauvegarde en base de données : O(1)

4. **cancelRide(int clientId)**
   - **Fonction** : Annule une course en cours
   - **Complexité** : O(m) où m est le nombre de véhicules non disponibles
   - **Paramètres dominants** : Nombre de véhicules non disponibles
   - **Détails** :
     - Recherche en base de données : O(1)
     - Mise à jour des statuts : O(1)
     - Recherche dans notAvailableVehicules : O(m)
     - Mise à jour des collections : O(1)

5. **saveAttributionToDatabase(ClientRequest request, Vehicule vehicule)**
   - **Fonction** : Enregistre l'attribution d'un véhicule à une requête
   - **Complexité** : O(1)
   - **Paramètres dominants** : Aucun (opérations constantes)
   - **Détails** :
     - Requêtes SQL : O(1)
     - Mises à jour : O(1)
     - Gestion de la transaction : O(1)

### Optimisations Possibles

1. **Pour assignVehiculeToNextClient()**
   - Utiliser une structure de données indexée par localisation
   - Complexité réduite à O(1) pour la recherche de véhicule
   - Coût en mémoire : O(n) pour l'index

2. **Pour cancelRide()**
   - Maintenir un index des véhicules par ID
   - Complexité réduite à O(1) pour la recherche
   - Coût en mémoire : O(n) pour l'index

3. **Pour loadVehiculesFromDatabase()**
   - Chargement paginé pour de grandes quantités de véhicules
   - Complexité réduite à O(k) où k est la taille de la page
   - Meilleure gestion de la mémoire