# Documentation API REST EcoDeli

## Vue d'ensemble

L'API REST EcoDeli fournit un accès programmatique à toutes les fonctionnalités de la plateforme. Cette documentation détaille l'ensemble des endpoints disponibles, leurs paramètres, et les formats de réponse.

## Informations Générales

### URL de Base

- **Développement** : `http://localhost:8080`
- **Production** : `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app`

### Format des Données

- **Content-Type** : `application/json`
- **Encodage** : UTF-8
- **Format de date** : ISO 8601 (`YYYY-MM-DDTHH:mm:ssZ`)

### Authentification

L'API utilise l'authentification JWT (JSON Web Token). Après connexion réussie, incluez le token dans l'en-tête :

```
Authorization: Bearer <votre_token_jwt>
```

### Codes de Statut HTTP

| Code | Description |
|------|-------------|
| 200 | Succès |
| 201 | Créé avec succès |
| 400 | Requête invalide |
| 401 | Non autorisé |
| 403 | Accès interdit |
| 404 | Ressource non trouvée |
| 409 | Conflit (ex: email déjà utilisé) |
| 422 | Données non valides |
| 500 | Erreur interne du serveur |

---

## Authentification

### POST /api/auth/register

Inscription d'un nouvel utilisateur.

#### Requête

```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "motDePasse": "motdepasse123",
  "telephone": "0123456789",
  "adresse": "123 Rue de la Paix, 75001 Paris",
  "dateDeNaissance": "1990-01-15",
  "genre": true,
  "type": "CLIENT"
}
```

#### Réponse (201)

```json
{
  "message": "Utilisateur créé avec succès",
  "utilisateur": {
    "idUtilisateur": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com",
    "type": "CLIENT",
    "statut": "ACTIVE"
  }
}
```

#### Paramètres Spécifiques par Type

**COMMERCANT :**
```json
{
  "nomCommerce": "Boulangerie du Coin",
  "siret": "12345678901234",
  "descriptionCommerce": "Boulangerie artisanale",
  "horairesOuverture": "7h-19h du lundi au samedi"
}
```

**LIVREUR :**
```json
{
  "typeVehicule": "VELO",
  "numeroPermis": "123456789",
  "zoneActivite": "Paris Centre",
  "capaciteTransport": 15.5
}
```

**PRESTATAIRE :**
```json
{
  "nomEntreprise": "Services Pro",
  "typeServices": ["MONTAGE", "REPARATION"],
  "zoneIntervention": "Ile-de-France"
}
```

### POST /api/auth/login

Connexion d'un utilisateur existant.

#### Requête

```json
{
  "email": "jean.dupont@example.com",
  "motDePasse": "motdepasse123"
}
```

#### Réponse (200)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400,
  "utilisateur": {
    "idUtilisateur": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com",
    "type": "CLIENT",
    "statut": "ACTIVE"
  }
}
```

### POST /api/auth/refresh

Renouvellement du token JWT.

#### Requête

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Réponse (200)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

### POST /api/auth/logout

Déconnexion de l'utilisateur.

#### Requête

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Réponse (200)

```json
{
  "message": "Déconnexion réussie"
}
```

---

## Gestion des Utilisateurs

### GET /api/users/profile

Récupération du profil de l'utilisateur connecté.

#### Réponse (200)

```json
{
  "idUtilisateur": 123,
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "telephone": "0123456789",
  "adresse": "123 Rue de la Paix, 75001 Paris",
  "dateDeNaissance": "1990-01-15",
  "dateInscription": "2024-01-15T10:30:00Z",
  "statut": "ACTIVE",
  "noteGlobale": 4.8,
  "nombreEvaluations": 156
}
```

### PUT /api/users/profile

Mise à jour du profil utilisateur.

#### Requête

```json
{
  "nom": "Dupont",
  "prenom": "Jean-Pierre",
  "telephone": "0123456789",
  "adresse": "456 Avenue des Champs, 75008 Paris"
}
```

#### Réponse (200)

```json
{
  "message": "Profil mis à jour avec succès",
  "utilisateur": {
    "idUtilisateur": 123,
    "nom": "Dupont",
    "prenom": "Jean-Pierre",
    "email": "jean.dupont@example.com",
    "telephone": "0123456789",
    "adresse": "456 Avenue des Champs, 75008 Paris"
  }
}
```

### DELETE /api/users/account

Suppression du compte utilisateur.

#### Réponse (200)

```json
{
  "message": "Compte supprimé avec succès"
}
```

---

## Gestion des Annonces

### GET /api/annonces

Liste des annonces avec pagination et filtres.

#### Paramètres de Requête

| Paramètre | Type | Description | Défaut |
|-----------|------|-------------|--------|
| page | int | Numéro de page | 0 |
| size | int | Nombre d'éléments par page | 20 |
| sort | string | Champ de tri | dateCreation |
| direction | string | Direction du tri (asc/desc) | desc |
| categorie | int | ID de la catégorie | - |
| ville | string | Ville de livraison | - |
| prixMin | decimal | Prix minimum | - |
| prixMax | decimal | Prix maximum | - |
| statut | string | Statut de l'annonce | ACTIVE |

#### Exemple de Requête

```
GET /api/annonces?page=0&size=10&categorie=1&ville=Paris&prixMin=10&prixMax=50
```

#### Réponse (200)

```json
{
  "content": [
    {
      "idAnnonce": 456,
      "titre": "Livraison courses urgente",
      "description": "Besoin de livrer des courses alimentaires",
      "prix": 25.50,
      "dateCreation": "2024-12-01T14:30:00Z",
      "dateExpiration": "2024-12-02T18:00:00Z",
      "adresseDepart": "123 Rue du Commerce, 75015 Paris",
      "adresseArrivee": "456 Avenue Victor Hugo, 75016 Paris",
      "estActive": true,
      "client": {
        "idUtilisateur": 123,
        "nom": "Dupont",
        "prenom": "Jean",
        "noteGlobale": 4.8
      },
      "categorie": {
        "idCategorie": 1,
        "nom": "Alimentaire",
        "icone": "🍎"
      },
      "nombreCandidatures": 3
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 125,
  "totalPages": 13,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "empty": false
}
```

### POST /api/annonces

Création d'une nouvelle annonce.

#### Requête

```json
{
  "titre": "Livraison documents urgents",
  "description": "Livraison de documents administratifs importants",
  "prix": 35.00,
  "dateExpiration": "2024-12-03T17:00:00Z",
  "adresseDepart": "789 Rue de Rivoli, 75001 Paris",
  "adresseArrivee": "321 Boulevard Saint-Germain, 75007 Paris",
  "idCategorie": 3,
  "poidsEstime": 0.5,
  "valeurDeclaree": 1000.00,
  "instructionsSpeciales": "Remise en main propre uniquement"
}
```

#### Réponse (201)

```json
{
  "idAnnonce": 789,
  "titre": "Livraison documents urgents",
  "description": "Livraison de documents administratifs importants",
  "prix": 35.00,
  "dateCreation": "2024-12-01T15:45:00Z",
  "dateExpiration": "2024-12-03T17:00:00Z",
  "adresseDepart": "789 Rue de Rivoli, 75001 Paris",
  "adresseArrivee": "321 Boulevard Saint-Germain, 75007 Paris",
  "estActive": true,
  "statut": "PUBLIEE",
  "codeAnnonce": "ECO-789-2024"
}
```

### GET /api/annonces/{id}

Détails d'une annonce spécifique.

#### Réponse (200)

```json
{
  "idAnnonce": 456,
  "titre": "Livraison courses urgente",
  "description": "Besoin de livrer des courses alimentaires avant 19h",
  "prix": 25.50,
  "dateCreation": "2024-12-01T14:30:00Z",
  "dateExpiration": "2024-12-02T18:00:00Z",
  "adresseDepart": "123 Rue du Commerce, 75015 Paris",
  "adresseArrivee": "456 Avenue Victor Hugo, 75016 Paris",
  "coordonneesDepart": {
    "latitude": 48.8534,
    "longitude": 2.2945
  },
  "coordonneesArrivee": {
    "latitude": 48.8698,
    "longitude": 2.2885
  },
  "distance": 3.2,
  "tempsEstime": 18,
  "estActive": true,
  "poidsEstime": 2.5,
  "valeurDeclaree": 45.00,
  "instructionsSpeciales": "Sonnez 2 fois, code 1234A",
  "client": {
    "idUtilisateur": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "telephone": "01****56789",
    "noteGlobale": 4.8,
    "nombreEvaluations": 156
  },
  "categorie": {
    "idCategorie": 1,
    "nom": "Alimentaire",
    "icone": "🍎",
    "description": "Produits alimentaires et boissons"
  },
  "candidatures": [
    {
      "idCandidature": 789,
      "prix": 22.00,
      "delaiCollecte": 30,
      "delaiLivraison": 60,
      "message": "Je peux m'en occuper immédiatement",
      "dateCreation": "2024-12-01T14:45:00Z",
      "livreur": {
        "idUtilisateur": 234,
        "nom": "Martin",
        "prenom": "Sophie",
        "typeVehicule": "VELO",
        "noteGlobale": 4.9,
        "nombreLivraisons": 234
      }
    }
  ]
}
```

### PUT /api/annonces/{id}

Modification d'une annonce existante.

#### Requête

```json
{
  "titre": "Livraison courses urgente - MISE À JOUR",
  "prix": 30.00,
  "dateExpiration": "2024-12-02T20:00:00Z",
  "instructionsSpeciales": "Sonnez 3 fois, nouveau code 5678B"
}
```

#### Réponse (200)

```json
{
  "message": "Annonce mise à jour avec succès",
  "annonce": {
    "idAnnonce": 456,
    "titre": "Livraison courses urgente - MISE À JOUR",
    "prix": 30.00,
    "dateModification": "2024-12-01T16:00:00Z"
  }
}
```

### DELETE /api/annonces/{id}

Suppression d'une annonce.

#### Réponse (200)

```json
{
  "message": "Annonce supprimée avec succès"
}
```

---

## Gestion des Candidatures

### GET /api/candidatures

Liste des candidatures selon le profil utilisateur.

#### Paramètres de Requête

| Paramètre | Type | Description |
|-----------|------|-------------|
| statut | string | Statut de la candidature |
| dateDebut | date | Date de début de période |
| dateFin | date | Date de fin de période |

#### Réponse (200)

```json
{
  "content": [
    {
      "idCandidature": 789,
      "prix": 22.00,
      "delaiCollecte": 30,
      "delaiLivraison": 60,
      "message": "Je peux m'en occuper immédiatement",
      "statut": "EN_ATTENTE",
      "dateCreation": "2024-12-01T14:45:00Z",
      "annonce": {
        "idAnnonce": 456,
        "titre": "Livraison courses urgente",
        "adresseDepart": "123 Rue du Commerce, 75015 Paris",
        "adresseArrivee": "456 Avenue Victor Hugo, 75016 Paris"
      },
      "client": {
        "nom": "Dupont",
        "prenom": "Jean",
        "noteGlobale": 4.8
      }
    }
  ],
  "totalElements": 12,
  "totalPages": 2
}
```

### POST /api/candidatures

Création d'une candidature pour une annonce.

#### Requête

```json
{
  "idAnnonce": 456,
  "prix": 22.00,
  "delaiCollecte": 30,
  "delaiLivraison": 60,
  "message": "Je peux me charger de cette livraison dès maintenant. J'ai l'habitude de ce secteur."
}
```

#### Réponse (201)

```json
{
  "idCandidature": 890,
  "prix": 22.00,
  "delaiCollecte": 30,
  "delaiLivraison": 60,
  "message": "Je peux me charger de cette livraison dès maintenant.",
  "statut": "EN_ATTENTE",
  "dateCreation": "2024-12-01T15:30:00Z",
  "codeCandidature": "CAND-890-2024"
}
```

### PUT /api/candidatures/{id}/accepter

Acceptation d'une candidature par le client.

#### Réponse (200)

```json
{
  "message": "Candidature acceptée avec succès",
  "livraison": {
    "idLivraison": 345,
    "statut": "CONFIRMEE",
    "dateCreation": "2024-12-01T15:45:00Z",
    "codeLivraison": "LIV-345-2024"
  }
}
```

### PUT /api/candidatures/{id}/refuser

Refus d'une candidature par le client.

#### Requête

```json
{
  "motifRefus": "Prix trop élevé"
}
```

#### Réponse (200)

```json
{
  "message": "Candidature refusée"
}
```

---

## Gestion des Livraisons

### GET /api/livraisons

Liste des livraisons selon le profil utilisateur.

#### Paramètres de Requête

| Paramètre | Type | Description |
|-----------|------|-------------|
| statut | string | Statut de la livraison |
| dateDebut | date | Date de début de période |
| dateFin | date | Date de fin de période |

#### Réponse (200)

```json
{
  "content": [
    {
      "idLivraison": 345,
      "statut": "EN_COURS",
      "dateCreation": "2024-12-01T15:45:00Z",
      "dateDebut": "2024-12-01T16:00:00Z",
      "dateFinEstimee": "2024-12-01T17:00:00Z",
      "codeLivraison": "LIV-345-2024",
      "codeValidation": "2468",
      "annonce": {
        "idAnnonce": 456,
        "titre": "Livraison courses urgente",
        "adresseDepart": "123 Rue du Commerce, 75015 Paris",
        "adresseArrivee": "456 Avenue Victor Hugo, 75016 Paris",
        "prix": 25.50
      },
      "client": {
        "nom": "Dupont",
        "prenom": "Jean",
        "telephone": "01****56789"
      },
      "livreur": {
        "nom": "Martin",
        "prenom": "Sophie",
        "telephone": "06****12345",
        "typeVehicule": "VELO",
        "noteGlobale": 4.9
      },
      "tracking": {
        "positionActuelle": {
          "latitude": 48.8566,
          "longitude": 2.2920
        },
        "distanceRestante": 1.8,
        "tempsEstimeArrivee": "2024-12-01T16:45:00Z"
      }
    }
  ],
  "totalElements": 8,
  "totalPages": 1
}
```

### GET /api/livraisons/{id}

Détails d'une livraison spécifique.

#### Réponse (200)

```json
{
  "idLivraison": 345,
  "statut": "EN_COURS",
  "dateCreation": "2024-12-01T15:45:00Z",
  "dateDebut": "2024-12-01T16:00:00Z",
  "dateFinEstimee": "2024-12-01T17:00:00Z",
  "dateFin": null,
  "codeLivraison": "LIV-345-2024",
  "codeValidation": "2468",
  "commentaires": "Livraison en cours, tout se passe bien",
  "photosCollecte": [
    "https://storage.ecodeli.fr/photos/liv-345-collecte-1.jpg"
  ],
  "photosLivraison": [],
  "annonce": {
    "idAnnonce": 456,
    "titre": "Livraison courses urgente",
    "description": "Besoin de livrer des courses alimentaires",
    "adresseDepart": "123 Rue du Commerce, 75015 Paris",
    "adresseArrivee": "456 Avenue Victor Hugo, 75016 Paris",
    "coordonneesDepart": {
      "latitude": 48.8534,
      "longitude": 2.2945
    },
    "coordonneesArrivee": {
      "latitude": 48.8698,
      "longitude": 2.2885
    },
    "instructionsSpeciales": "Sonnez 2 fois, code 1234A",
    "prix": 25.50
  },
  "client": {
    "idUtilisateur": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "telephone": "01****56789",
    "email": "j***n@example.com"
  },
  "livreur": {
    "idUtilisateur": 234,
    "nom": "Martin",
    "prenom": "Sophie",
    "telephone": "06****12345",
    "typeVehicule": "VELO",
    "noteGlobale": 4.9,
    "nombreLivraisons": 234
  },
  "tracking": {
    "positionActuelle": {
      "latitude": 48.8566,
      "longitude": 2.2920,
      "datePosition": "2024-12-01T16:30:00Z"
    },
    "itineraire": [
      {
        "latitude": 48.8534,
        "longitude": 2.2945,
        "timestamp": "2024-12-01T16:00:00Z"
      },
      {
        "latitude": 48.8550,
        "longitude": 2.2930,
        "timestamp": "2024-12-01T16:15:00Z"
      }
    ],
    "distanceRestante": 1.8,
    "tempsEstimeArrivee": "2024-12-01T16:45:00Z",
    "vitesseMoyenne": 12.5
  },
  "historique": [
    {
      "statut": "CONFIRMEE",
      "date": "2024-12-01T15:45:00Z",
      "description": "Livraison confirmée"
    },
    {
      "statut": "EN_COURS_COLLECTE",
      "date": "2024-12-01T16:00:00Z",
      "description": "Livreur en route vers le point de collecte"
    },
    {
      "statut": "COLLECTEE",
      "date": "2024-12-01T16:15:00Z",
      "description": "Colis collecté avec succès"
    },
    {
      "statut": "EN_COURS",
      "date": "2024-12-01T16:20:00Z",
      "description": "En route vers la destination"
    }
  ]
}
```

### PUT /api/livraisons/{id}/statut

Mise à jour du statut d'une livraison.

#### Requête

```json
{
  "statut": "COLLECTEE",
  "commentaires": "Colis collecté sans problème",
  "photos": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD..."
  ],
  "position": {
    "latitude": 48.8534,
    "longitude": 2.2945
  }
}
```

#### Réponse (200)

```json
{
  "message": "Statut mis à jour avec succès",
  "livraison": {
    "idLivraison": 345,
    "statut": "COLLECTEE",
    "dateModification": "2024-12-01T16:15:00Z"
  }
}
```

### POST /api/livraisons/{id}/validation

Validation de la livraison par le client.

#### Requête

```json
{
  "codeValidation": "2468",
  "evaluation": {
    "note": 5,
    "commentaire": "Livraison parfaite, très rapide et soigné",
    "criteres": {
      "ponctualite": 5,
      "communication": 5,
      "etatColis": 5,
      "professionnalisme": 5
    }
  }
}
```

#### Réponse (200)

```json
{
  "message": "Livraison validée avec succès",
  "livraison": {
    "idLivraison": 345,
    "statut": "LIVREE",
    "dateFin": "2024-12-01T16:45:00Z"
  },
  "evaluation": {
    "idEvaluation": 456,
    "note": 5,
    "commentaire": "Livraison parfaite, très rapide et soigné"
  }
}
```

---

## Gestion des Paiements

### POST /api/paiements/create-payment-intent

Création d'une intention de paiement Stripe.

#### Requête

```json
{
  "montant": 2550,
  "devise": "eur",
  "idLivraison": 345,
  "methodePaiement": "card",
  "confirmationAutomatique": true
}
```

#### Réponse (200)

```json
{
  "clientSecret": "pi_1234567890_secret_abcdef",
  "idPaiement": 789,
  "montant": 2550,
  "devise": "eur",
  "statut": "requires_payment_method"
}
```

### POST /api/paiements/confirm-payment

Confirmation du paiement après saisie des données bancaires.

#### Requête

```json
{
  "paymentIntentId": "pi_1234567890",
  "paymentMethodId": "pm_card_visa"
}
```

#### Réponse (200)

```json
{
  "statut": "succeeded",
  "montant": 2550,
  "devise": "eur",
  "numeroTransaction": "TXN-789-2024",
  "datePaiement": "2024-12-01T16:45:00Z",
  "recu": {
    "url": "https://ecodeli.fr/receipts/789",
    "numero": "REC-789-2024"
  }
}
```

### GET /api/paiements/history

Historique des paiements.

#### Paramètres de Requête

| Paramètre | Type | Description |
|-----------|------|-------------|
| dateDebut | date | Date de début |
| dateFin | date | Date de fin |
| statut | string | Statut du paiement |

#### Réponse (200)

```json
{
  "content": [
    {
      "idPaiement": 789,
      "montant": 25.50,
      "devise": "EUR",
      "statut": "REUSSI",
      "datePaiement": "2024-12-01T16:45:00Z",
      "numeroTransaction": "TXN-789-2024",
      "methodePaiement": "CARTE_BANCAIRE",
      "livraison": {
        "idLivraison": 345,
        "codeLivraison": "LIV-345-2024",
        "titre": "Livraison courses urgente"
      },
      "recu": {
        "numero": "REC-789-2024",
        "url": "https://ecodeli.fr/receipts/789"
      }
    }
  ],
  "totalElements": 15,
  "totalPages": 2
}
```

---

## Administration

### GET /api/admin/dashboard

Métriques principales pour le tableau de bord admin.

#### Réponse (200)

```json
{
  "utilisateurs": {
    "total": 15840,
    "clients": 12350,
    "livreurs": 2100,
    "commercants": 980,
    "prestataires": 410,
    "nouveauxCeMois": 284
  },
  "livraisons": {
    "totalAujourdHui": 45,
    "totalCeMois": 1250,
    "enCours": 12,
    "terminees": 33
  },
  "revenus": {
    "aujourdHui": 1125.50,
    "ceMois": 28450.75,
    "commissions": 3567.60
  },
  "satisfaction": {
    "noteGlobale": 4.7,
    "nombreEvaluations": 8950,
    "repartitionNotes": {
      "5": 6520,
      "4": 1890,
      "3": 420,
      "2": 85,
      "1": 35
    }
  }
}
```

### GET /api/admin/users

Liste des utilisateurs avec filtres avancés.

#### Paramètres de Requête

| Paramètre | Type | Description |
|-----------|------|-------------|
| type | string | Type d'utilisateur |
| statut | string | Statut du compte |
| dateInscriptionDebut | date | Date début inscription |
| dateInscriptionFin | date | Date fin inscription |
| recherche | string | Recherche textuelle |

#### Réponse (200)

```json
{
  "content": [
    {
      "idUtilisateur": 123,
      "nom": "Dupont",
      "prenom": "Jean",
      "email": "jean.dupont@example.com",
      "type": "CLIENT",
      "statut": "ACTIVE",
      "dateInscription": "2024-01-15T10:30:00Z",
      "dernierConnexion": "2024-12-01T14:20:00Z",
      "noteGlobale": 4.8,
      "nombreEvaluations": 156,
      "nombreCommandes": 45,
      "chiffreAffaires": 1250.75
    }
  ],
  "totalElements": 15840,
  "totalPages": 792
}
```

### PUT /api/admin/users/{id}/statut

Modification du statut d'un utilisateur.

#### Requête

```json
{
  "statut": "SUSPENDU",
  "motif": "Non-respect des conditions d'utilisation",
  "duree": 30
}
```

#### Réponse (200)

```json
{
  "message": "Statut utilisateur mis à jour",
  "utilisateur": {
    "idUtilisateur": 123,
    "statut": "SUSPENDU",
    "dateModification": "2024-12-01T17:00:00Z"
  }
}
```

---

## Gestion des Erreurs

### Format des Erreurs

Toutes les erreurs suivent le format standardisé suivant :

```json
{
  "timestamp": "2024-12-01T17:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Les données fournies ne sont pas valides",
  "details": [
    {
      "field": "email",
      "message": "Format d'email invalide"
    },
    {
      "field": "motDePasse",
      "message": "Le mot de passe doit contenir au moins 8 caractères"
    }
  ],
  "path": "/api/auth/register"
}
```

### Codes d'Erreur Spécifiques

| Code | Description |
|------|-------------|
| EMAIL_ALREADY_EXISTS | Email déjà utilisé |
| USER_NOT_FOUND | Utilisateur non trouvé |
| INVALID_CREDENTIALS | Identifiants incorrects |
| TOKEN_EXPIRED | Token JWT expiré |
| INSUFFICIENT_PERMISSIONS | Permissions insuffisantes |
| ANNONCE_NOT_FOUND | Annonce non trouvée |
| CANDIDATURE_ALREADY_EXISTS | Candidature déjà soumise |
| LIVRAISON_NOT_FOUND | Livraison non trouvée |
| PAYMENT_FAILED | Échec du paiement |
| VALIDATION_ERROR | Erreur de validation |

---

## Exemples d'Utilisation

### Workflow Complet Client

```javascript
// 1. Inscription
const registerResponse = await fetch('/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    nom: 'Dupont',
    prenom: 'Jean',
    email: 'jean.dupont@example.com',
    motDePasse: 'motdepasse123',
    type: 'CLIENT'
  })
});

// 2. Connexion
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'jean.dupont@example.com',
    motDePasse: 'motdepasse123'
  })
});
const { token } = await loginResponse.json();

// 3. Créer une annonce
const annonceResponse = await fetch('/api/annonces', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    titre: 'Livraison urgente',
    description: 'Besoin de livrer des documents',
    prix: 25.00,
    adresseDepart: 'Paris 15ème',
    adresseArrivee: 'Paris 16ème',
    idCategorie: 3
  })
});

// 4. Suivre les candidatures
const candidaturesResponse = await fetch('/api/candidatures', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

### Workflow Complet Livreur

```javascript
// 1. Rechercher des annonces
const annoncesResponse = await fetch('/api/annonces?ville=Paris&prixMin=20', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 2. Candidater
const candidatureResponse = await fetch('/api/candidatures', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    idAnnonce: 456,
    prix: 22.00,
    delaiCollecte: 30,
    delaiLivraison: 60,
    message: 'Je peux m\'en occuper rapidement'
  })
});

// 3. Mettre à jour le statut de livraison
const statusResponse = await fetch('/api/livraisons/345/statut', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    statut: 'COLLECTEE',
    commentaires: 'Colis récupéré',
    position: { latitude: 48.8534, longitude: 2.2945 }
  })
});
```

---

## Limites et Quotas

### Limitation de Débit

| Endpoint | Limite | Période |
|----------|--------|---------|
| /api/auth/login | 5 tentatives | 15 minutes |
| /api/auth/register | 3 créations | 1 heure |
| /api/annonces | 100 requêtes | 1 heure |
| /api/candidatures | 50 créations | 1 heure |
| Autres endpoints | 1000 requêtes | 1 heure |

### Taille des Données

- **Photos** : Maximum 5MB par image
- **Descriptions** : Maximum 2000 caractères
- **Messages** : Maximum 500 caractères
- **Payload JSON** : Maximum 10MB

---

## Versioning de l'API

L'API EcoDeli suit la spécification de versioning sémantique :

- **Version actuelle** : v1.0.0
- **URL avec version** : `/api/v1/...` (optionnel)
- **Headers de version** : `Accept: application/vnd.ecodeli.v1+json`

### Rétrocompatibilité

- Les versions mineures maintiennent la compatibilité
- Les changements majeurs introduisent une nouvelle version
- Support de 2 versions majeures simultanément

---

*Documentation API EcoDeli - Version 1.0 - Décembre 2024*

Pour plus d'informations, consultez notre [documentation Swagger](https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/swagger-ui.html) en ligne.