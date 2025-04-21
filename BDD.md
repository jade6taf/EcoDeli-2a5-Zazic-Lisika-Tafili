# Ecodeli Base de Données

+-------------------+
| Tables_in_EcoDeli |
+-------------------+
| ANNONCE           |
| COLIS             |
| Client            |
| Commercant        |
| ENTREPOT          |
| ENTREPRISE        |
| EVALUATION        |
| FACTURE           |
| JUSTIFICATIF      |
| LIVRAISON         |
| Livreur           |
| MESSAGE           |
| PAIEMENT          |
| PRODUIT           |
| Prestataire       |
| SERVICE           |
| UTILISATEUR       |
+-------------------+

## ANNONCE

+----------------+-----------------------------------------------------------------------------------+------+-----+---------+----------------+
| Field          | Type                                                                              | Null | Key | Default | Extra          |
+----------------+-----------------------------------------------------------------------------------+------+-----+---------+----------------+
| id_annonce     | int(11)                                                                           | NO   | PRI | NULL    | auto_increment |
| id_expediteur  | int(11)                                                                           | NO   | MUL | NULL    |                |
| id_livreur     | int(11)                                                                           | YES  | MUL | NULL    |                |
| prix_unitaire  | decimal(10,2)                                                                     | NO   |     | NULL    |                |
| date_debut     | datetime(6)                                                                       | YES  |     | NULL    |                |
| date_fin       | datetime(6)                                                                       | YES  |     | NULL    |                |
| titre          | varchar(100)                                                                      | YES  |     | NULL    |                |
| adresse_depart | text                                                                              | YES  |     | NULL    |                |
| adresse_fin    | text                                                                              | YES  |     | NULL    |                |
| description    | text                                                                              | YES  |     | NULL    |                |
| statut         | enum('PUBLIEE','EN_ATTENTE_VALIDATION','VALIDEE','EN_COURS','TERMINEE','ANNULEE') | YES  |     | NULL    |                |
| type_annonce   | enum('unique','multiple')                                                         | YES  |     | NULL    |                |
+----------------+-----------------------------------------------------------------------------------+------+-----+---------+----------------+

## Colis

+-----------------+---------------+------+-----+---------+----------------+
| Field           | Type          | Null | Key | Default | Extra          |
+-----------------+---------------+------+-----+---------+----------------+
| id_colis        | int(11)       | NO   | PRI | NULL    | auto_increment |
| id_destinataire | int(11)       | NO   | MUL | NULL    |                |
| id_expediteur   | int(11)       | NO   | MUL | NULL    |                |
| poid            | decimal(10,2) | NO   |     | NULL    |                |
| taille          | int(11)       | YES  |     | NULL    |                |
| date_debut      | datetime(6)   | YES  |     | NULL    |                |
| date_fin        | datetime(6)   | YES  |     | NULL    |                |
| type            | varchar(255)  | YES  |     | NULL    |                |
+-----------------+---------------+------+-----+---------+----------------+

## Client

+----------------+---------+------+-----+---------+-------+
| Field          | Type    | Null | Key | Default | Extra |
+----------------+---------+------+-----+---------+-------+
| id_utilisateur | int(11) | NO   | PRI | NULL    |       |
+----------------+---------+------+-----+---------+-------+

## Commerçant

+----------------------+--------------+------+-----+---------+-------+
| Field                | Type         | Null | Key | Default | Extra |
+----------------------+--------------+------+-----+---------+-------+
| id_utilisateur       | int(11)      | NO   | PRI | NULL    |       |
| siret                | varchar(14)  | YES  |     | NULL    |       |
| description_commerce | text         | YES  |     | NULL    |       |
| horaires_ouverture   | text         | YES  |     | NULL    |       |
| nom_commerce         | varchar(255) | NO   |     | NULL    |       |
+----------------------+--------------+------+-----+---------+-------+

## Entrepot

+------------------+----------------------------------------------------+------+-----+---------+----------------+
| Field            | Type                                               | Null | Key | Default | Extra          |
+------------------+----------------------------------------------------+------+-----+---------+----------------+
| id_entrepot      | int(11)                                            | NO   | PRI | NULL    | auto_increment |
| nombre_de_places | int(11)                                            | YES  |     | NULL    |                |
| ville            | varchar(100)                                       | YES  |     | NULL    |                |
| statut           | enum('ACTIF','INACTIF','EN_MAINTENANCE','COMPLET') | YES  |     | NULL    |                |
+------------------+----------------------------------------------------+------+-----+---------+----------------+

## Entreprise

+-------------------+--------------+------+-----+---------+----------------+
| Field             | Type         | Null | Key | Default | Extra          |
+-------------------+--------------+------+-----+---------+----------------+
| id_entreprise     | int(11)      | NO   | PRI | NULL    | auto_increment |
| statut_juridique  | int(11)      | YES  |     | NULL    |                |
| validation_par_ad | bit(1)       | YES  |     | NULL    |                |
| date_ajout        | datetime(6)  | YES  |     | NULL    |                |
| code_postal       | varchar(10)  | YES  |     | NULL    |                |
| telephone         | varchar(30)  | YES  |     | NULL    |                |
| SIRET             | varchar(50)  | YES  |     | NULL    |                |
| pays              | varchar(50)  | YES  |     | NULL    |                |
| site_web          | varchar(50)  | YES  |     | NULL    |                |
| secteur_activite  | varchar(100) | YES  |     | NULL    |                |
| ville             | varchar(100) | YES  |     | NULL    |                |
| email             | varchar(150) | NO   | UNI | NULL    |                |
| adresse           | text         | YES  |     | NULL    |                |
+-------------------+--------------+------+-----+---------+----------------+

## Evaluation

+----------------+---------+------+-----+---------+----------------+
| Field          | Type    | Null | Key | Default | Extra          |
+----------------+---------+------+-----+---------+----------------+
| id_evaluation  | int(11) | NO   | PRI | NULL    | auto_increment |
| id_prestataire | int(11) | NO   | MUL | NULL    |                |
| id_produit     | int(11) | YES  | MUL | NULL    |                |
| id_utilisateur | int(11) | YES  | MUL | NULL    |                |
| note           | int(11) | YES  |     | NULL    |                |
| commentaire    | text    | YES  |     | NULL    |                |
+----------------+---------+------+-----+---------+----------------+

## Facture

+------------------+-----------------------------------------------------+------+-----+---------+----------------+
| Field            | Type                                                | Null | Key | Default | Extra          |
+------------------+-----------------------------------------------------+------+-----+---------+----------------+
| id_client        | int(11)                                             | NO   | MUL | NULL    |                |
| id_facture       | int(11)                                             | NO   | PRI | NULL    | auto_increment |
| id_prestataire   | int(11)                                             | NO   | MUL | NULL    |                |
| id_produit       | int(11)                                             | YES  | MUL | NULL    |                |
| prix_total       | decimal(10,2)                                       | NO   |     | NULL    |                |
| remise           | int(11)                                             | YES  |     | NULL    |                |
| tva              | int(11)                                             | YES  |     | NULL    |                |
| date_facture     | datetime(6)                                         | YES  |     | NULL    |                |
| statut           | varchar(50)                                         | YES  |     | NULL    |                |
| commentaire      | text                                                | YES  |     | NULL    |                |
| prestations      | text                                                | YES  |     | NULL    |                |
| mode_de_paiement | enum('CB','PayPal','Espèces','Virement','Chèque')   | YES  |     | NULL    |                |
+------------------+-----------------------------------------------------+------+-----+---------+----------------+

## Justificatif

+-------------------+--------------+------+-----+---------+----------------+
| Field             | Type         | Null | Key | Default | Extra          |
+-------------------+--------------+------+-----+---------+----------------+
| id_justificatif   | int(11)      | NO   | PRI | NULL    | auto_increment |
| id_utilisateur    | int(11)      | NO   | MUL | NULL    |                |
| validation_par_ad | bit(1)       | YES  |     | NULL    |                |
| date_debut        | datetime(6)  | YES  |     | NULL    |                |
| date_fin          | datetime(6)  | YES  |     | NULL    |                |
| type_justificatif | varchar(100) | YES  |     | NULL    |                |
| chemin_fichier    | varchar(255) | YES  |     | NULL    |                |
| commentaire       | text         | YES  |     | NULL    |                |
+-------------------+--------------+------+-----+---------+----------------+

## Livraison

+-----------------------+-------------------------------------------------------------------------+------+-----+---------+----------------+
| Field                 | Type                                                                    | Null | Key | Default | Extra          |
+-----------------------+-------------------------------------------------------------------------+------+-----+---------+----------------+
| id_annonce            | int(11)                                                                 | YES  | MUL | NULL    |                |
| id_colis              | int(11)                                                                 | YES  | MUL | NULL    |                |
| id_destinataire       | int(11)                                                                 | NO   | MUL | NULL    |                |
| id_expediteur         | int(11)                                                                 | NO   | MUL | NULL    |                |
| id_livraison          | int(11)                                                                 | NO   | PRI | NULL    | auto_increment |
| prix                  | int(11)                                                                 | YES  |     | NULL    |                |
| validation            | bit(1)                                                                  | YES  |     | NULL    |                |
| date_debut            | datetime(6)                                                             | YES  |     | NULL    |                |
| date_fin              | datetime(6)                                                             | YES  |     | NULL    |                |
| code_postal_envoi     | varchar(10)                                                             | YES  |     | NULL    |                |
| code_postal_livraison | varchar(10)                                                             | YES  |     | NULL    |                |
| code_validation       | varchar(10)                                                             | YES  |     | NULL    |                |
| adresse_de_livraison  | varchar(100)                                                            | YES  |     | NULL    |                |
| adresse_envoi         | varchar(100)                                                            | YES  |     | NULL    |                |
| statut                | enum('EN_ATTENTE_VALIDATION','VALIDEE','EN_COURS','TERMINEE','ANNULEE') | YES  |     | NULL    |                |
+-----------------------+-------------------------------------------------------------------------+------+-----+---------+----------------+

## Livreur

+----------------+--------------+------+-----+---------+-------+
| Field          | Type         | Null | Key | Default | Extra |
+----------------+--------------+------+-----+---------+-------+
| disponible     | bit(1)       | YES  |     | NULL    |       |
| id_utilisateur | int(11)      | NO   | PRI | NULL    |       |
| numero_permis  | varchar(9)   | YES  |     | NULL    |       |
| vehicule       | varchar(255) | YES  |     | NULL    |       |
+----------------+--------------+------+-----+---------+-------+

## Message

+-----------------+-------------+------+-----+---------+----------------+
| Field           | Type        | Null | Key | Default | Extra          |
+-----------------+-------------+------+-----+---------+----------------+
| id_destinataire | int(11)     | NO   | MUL | NULL    |                |
| id_expediteur   | int(11)     | NO   | MUL | NULL    |                |
| id_message      | int(11)     | NO   | PRI | NULL    | auto_increment |
| lu              | bit(1)      | YES  |     | NULL    |                |
| date            | datetime(6) | YES  |     | NULL    |                |
| message         | text        | NO   |     | NULL    |                |
+-----------------+-------------+------+-----+---------+----------------+

## Paiement

+------------------+-----------------------------------------------------+------+-----+---------+----------------+
| Field            | Type                                                | Null | Key | Default | Extra          |
+------------------+-----------------------------------------------------+------+-----+---------+----------------+
| commission       | decimal(10,2)                                       | YES  |     | NULL    |                |
| id_paiement      | int(11)                                             | NO   | PRI | NULL    | auto_increment |
| montant          | decimal(10,2)                                       | NO   |     | NULL    |                |
| mode_de_paiement | enum('CB','PayPal','Espèces','Virement','Chèque')   | YES  |     | NULL    |                |
+------------------+-----------------------------------------------------+------+-----+---------+----------------+

## Produit

+---------------+---------------+------+-----+---------+----------------+
| Field         | Type          | Null | Key | Default | Extra          |
+---------------+---------------+------+-----+---------+----------------+
| date_ajout    | date          | YES  |     | NULL    |                |
| id_entreprise | int(11)       | NO   | MUL | NULL    |                |
| id_produit    | int(11)       | NO   | PRI | NULL    | auto_increment |
| prix          | decimal(10,2) | NO   |     | NULL    |                |
| stock         | int(11)       | YES  |     | NULL    |                |
| categorie     | varchar(100)  | YES  |     | NULL    |                |
| nom           | varchar(100)  | NO   |     | NULL    |                |
| description   | text          | YES  |     | NULL    |                |
+---------------+---------------+------+-----+---------+----------------+

## Prestataire

+-------------------+--------------+------+-----+---------+-------+
| Field             | Type         | Null | Key | Default | Extra |
+-------------------+--------------+------+-----+---------+-------+
| disponible        | bit(1)       | YES  |     | NULL    |       |
| id_utilisateur    | int(11)      | NO   | PRI | NULL    |       |
| tarif_horaire     | double       | YES  |     | NULL    |       |
| siret             | varchar(14)  | YES  |     | NULL    |       |
| domaine_expertise | varchar(255) | YES  |     | NULL    |       |
| nom_entreprise    | varchar(255) | NO   |     | NULL    |       |
| zone_intervention | text         | YES  |     | NULL    |       |
+-------------------+--------------+------+-----+---------+-------+

## Service

+-----------------------+---------------+------+-----+---------+----------------+
| Field                 | Type          | Null | Key | Default | Extra          |
+-----------------------+---------------+------+-----+---------+----------------+
| id_client             | int(11)       | NO   | MUL | NULL    |                |
| id_prestataire        | int(11)       | NO   | MUL | NULL    |                |
| id_service            | int(11)       | NO   | PRI | NULL    | auto_increment |
| prix_unitaire         | decimal(10,2) | NO   |     | NULL    |                |
| date_debut            | datetime(6)   | YES  |     | NULL    |                |
| date_fin              | datetime(6)   | YES  |     | NULL    |                |
| code_postal_envoi     | varchar(10)   | YES  |     | NULL    |                |
| code_postal_livraison | varchar(10)   | YES  |     | NULL    |                |
| adresse_de_livraison  | varchar(100)  | YES  |     | NULL    |                |
| adresse_envoi         | varchar(100)  | YES  |     | NULL    |                |
| type_service          | varchar(100)  | YES  |     | NULL    |                |
+-----------------------+---------------+------+-----+---------+----------------+


## Utilisateur

+-------------------+--------------+------+-----+---------+----------------+
| Field             | Type         | Null | Key | Default | Extra          |
+-------------------+--------------+------+-----+---------+----------------+
| date_de_naissance | date         | YES  |     | NULL    |                |
| genre             | bit(1)       | YES  |     | NULL    |                |
| id_utilisateur    | int(11)      | NO   | PRI | NULL    | auto_increment |
| code_postal       | varchar(10)  | YES  |     | NULL    |                |
| telephone         | varchar(30)  | YES  |     | NULL    |                |
| type_utilisateur  | varchar(31)  | NO   |     | NULL    |                |
| pays              | varchar(50)  | YES  |     | NULL    |                |
| nom               | varchar(100) | NO   |     | NULL    |                |
| prenom            | varchar(100) | NO   |     | NULL    |                |
| ville             | varchar(100) | YES  |     | NULL    |                |
| email             | varchar(150) | NO   | UNI | NULL    |                |
| adresse           | text         | YES  |     | NULL    |                |
| mot_de_passe      | varchar(255) | NO   |     | NULL    |                |
+-------------------+--------------+------+-----+---------+----------------+