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

### Base de données EcoDeli :

1/ CREATE DATABASE EcoDeli;

2/ USE EcoDeli;

3/ -> CREATE TABLE ENTREPRISE (id_entreprise INT AUTO_INCREMENT PRIMARY KEY, SIRET VARCHAR(50), statut_juridique INT, secteur_activite VARCHAR(100), email VARCHAR(150) UNIQUE NOT NULL, telephone VARCHAR(30), adresse TEXT, ville VARCHAR(100), code_postal VARCHAR(10), pays VARCHAR(50), site_web VARCHAR(50), date_ajout DATETIME, validation_par_ad BOOLEAN DEFAULT 0);

-> CREATE TABLE UTILISATEUR (id_utilisateur INT AUTO_INCREMENT PRIMARY KEY, nom VARCHAR(100) NOT NULL, prenom VARCHAR(100) NOT NULL, genre BOOLEAN, date_de_naissance DATE, email VARCHAR(150) UNIQUE NOT NULL, mot_de_passe VARCHAR(255) NOT NULL, telephone VARCHAR(30), adresse TEXT, ville VARCHAR(100), code_postal VARCHAR(10), pays VARCHAR(50), type VARCHAR(50), abonnement VARCHAR(10), validation_par_ad BOOLEAN DEFAULT 0, id_entreprise INT NULL, FOREIGN KEY (id_entreprise) REFERENCES ENTREPRISE(id_entreprise) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE MESSAGE (id_message INT AUTO_INCREMENT PRIMARY KEY, message TEXT NOT NULL, date DATETIME DEFAULT CURRENT_TIMESTAMP, id_expediteur INT NOT NULL, id_destinataire INT NOT NULL, lu BOOLEAN DEFAULT 0, FOREIGN KEY (id_expediteur) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_destinataire) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE COLIS (id_colis INT AUTO_INCREMENT PRIMARY KEY, date_debut DATETIME, date_fin DATETIME, poid DECIMAL(10,2), taille INT, type VARCHAR(255), id_expediteur INT NOT NULL, id_destinataire INT NOT NULL, FOREIGN KEY (id_expediteur) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_destinataire) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE LIVRAISON (id_livraison INT AUTO_INCREMENT PRIMARY KEY, date_debut DATETIME, date_fin DATETIME, statut VARCHAR(50), adresse_de_livraison VARCHAR(100), code_postal_livraison VARCHAR(10), adresse_envoi VARCHAR(100), code_postal_envoi VARCHAR(10), prix INT, code_validation VARCHAR(10), validation BOOLEAN DEFAULT FALSE, id_expediteur INT NOT NULL, id_destinataire INT NOT NULL, id_colis INT NULL, FOREIGN KEY (id_expediteur) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_destinataire) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_colis) REFERENCES COLIS(id_colis) ON DELETE SET NULL ON UPDATE CASCADE);

-> CREATE TABLE ANNONCE (id_annonce INT AUTO_INCREMENT PRIMARY KEY, titre VARCHAR(100), date_debut DATETIME, date_fin DATETIME, description TEXT, prix_unitaire DECIMAL(10,2), type_annonce ENUM('unique', 'multiple') DEFAULT 'unique', statut ENUM('active', 'expirée', 'annulée') DEFAULT 'active', adresse_depart TEXT, adresse_fin TEXT, id_expediteur INT NOT NULL, FOREIGN KEY (id_expediteur) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE JUSTIFICATIF (id_justificatif INT AUTO_INCREMENT PRIMARY KEY, type_justificatif VARCHAR(100), date_debut DATETIME, date_fin DATETIME, commentaire TEXT, validation_par_ad BOOLEAN DEFAULT 0, chemin_fichier VARCHAR(255), id_utilisateur INT NOT NULL, FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE SERVICE (id_service INT AUTO_INCREMENT PRIMARY KEY, date_debut DATETIME, date_fin DATETIME, prix_unitaire DECIMAL(10,2), adresse_de_livraison VARCHAR(100), code_postal_livraison VARCHAR(10), adresse_envoi VARCHAR(100), code_postal_envoi VARCHAR(10), type_service VARCHAR(100), id_prestataire INT NOT NULL, id_client INT NOT NULL, FOREIGN KEY (id_prestataire) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_client) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE PAIEMENT (id_paiement INT AUTO_INCREMENT PRIMARY KEY, montant DECIMAL(10,2), commission DECIMAL(10,2), mode_de_paiement ENUM('CB', 'PayPal', 'Espèces', 'Virement', 'Chèque'));

-> CREATE TABLE ENTREPOT (id_entrepot INT AUTO_INCREMENT PRIMARY KEY, statut VARCHAR(50), nombre_de_places INT, ville VARCHAR(100));

-> CREATE TABLE PRODUIT (id_produit INT PRIMARY KEY AUTO_INCREMENT, nom VARCHAR(100) NOT NULL, description TEXT, prix DECIMAL(10,2) NOT NULL, stock INT DEFAULT 0, categorie VARCHAR(100), id_entreprise INT NOT NULL, date_ajout DATE DEFAULT CURRENT_DATE, FOREIGN KEY (id_entreprise) REFERENCES ENTREPRISE(id_entreprise) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE FACTURE (id_facture INT AUTO_INCREMENT PRIMARY KEY, prestations TEXT, prix_total DECIMAL(10,2), date_facture DATETIME, statut VARCHAR(50), mode_de_paiement ENUM('CB', 'PayPal', 'Espèces', 'Virement', 'Chèque'), tva INT, remise INT, commentaire TEXT, id_produit INT, id_prestataire INT NOT NULL, id_client INT NOT NULL, FOREIGN KEY (id_produit) REFERENCES PRODUIT(id_produit) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_prestataire) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_client) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE);

-> CREATE TABLE EVALUATION (id_evaluation INT AUTO_INCREMENT PRIMARY KEY, note INT CHECK (note BETWEEN 1 AND 5), commentaire TEXT, id_utilisateur INT, id_prestataire INT NOT NULL, id_produit INT, FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE SET NULL ON UPDATE CASCADE, FOREIGN KEY (id_prestataire) REFERENCES UTILISATEUR(id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (id_produit) REFERENCES PRODUIT(id_produit) ON DELETE CASCADE ON UPDATE CASCADE);

## Résumé de la database EcoDeli :
+-------------------+
| Tables_in_EcoDeli |
+-------------------+
| ANNONCE           |
| COLIS             |
| ENTREPOT          |
| ENTREPRISE        |
| EVALUATION        |
| FACTURE           |
| JUSTIFICATIF      |
| LIVRAISON         |
| MESSAGE           |
| PAIEMENT          |
| PRODUIT           |
| SERVICE           |
| UTILISATEUR       |
+-------------------+

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