# Documentation Backend EcoDeli

## Présentation

Le backend d'EcoDeli est une application Spring Boot qui fournit une API REST pour l'application web EcoDeli. Cette API permet de gérer les utilisateurs, les prestations, les livraisons, les paiements et toutes les autres fonctionnalités nécessaires au bon fonctionnement de la plateforme.

## Technologies utilisées

- **Java 17/21** : Langage de programmation
- **Spring Boot 3.4.x** : Framework Java pour la création d'applications web
- **Spring Data JPA** : Pour la persistance des données
- **MariaDB** : Base de données relationnelle
- **Maven** : Outil de build et de gestion des dépendances

## Installation et configuration

### Prérequis

- JDK 17 ou supérieur
- Maven 3.6 ou supérieur
- MariaDB 10.x

### Exécution de l'application

1/ Cloner le dépôt
2/ Se placer dans le répertoire ecodeli-backend
3/ Exécuter la commande suivante:

./mvnw spring-boot:run

L'application sera accessible à l'adresse: http://localhost:8080

### API REST

- GET /api/utilisateurs : Récupérer tous les utilisateurs
- GET /api/utilisateurs/{id} : Récupérer un utilisateur par son ID
- POST /api/utilisateurs : Créer un nouvel utilisateur
- PUT /api/utilisateurs/{id} : Mettre à jour un utilisateur
- DELETE /api/utilisateurs/{id} : Supprimer un utilisateur