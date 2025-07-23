# FAQ EcoDeli - Foire Aux Questions

Cette FAQ répond aux questions les plus fréquemment posées concernant l'utilisation, l'installation et le développement de la plateforme EcoDeli.

## Table des Matières

1. [Questions Générales](#questions-générales)
2. [Installation et Configuration](#installation-et-configuration)
3. [Utilisation de l'Application](#utilisation-de-lapplication)
4. [Problèmes Techniques](#problèmes-techniques)
5. [Développement](#développement)
6. [Sécurité](#sécurité)
7. [Support](#support)

---

## Questions Générales

### Qu'est-ce qu'EcoDeli ?

EcoDeli est une plateforme de livraison durable qui connecte commerçants, prestataires de services, livreurs et clients dans un écosystème respectueux de l'environnement. Elle vise à promouvoir une logistique responsable et à soutenir le commerce local.

### Quels sont les types d'utilisateurs sur EcoDeli ?

- **Clients** : Passent des commandes et demandent des livraisons
- **Commerçants** : Proposent leurs produits et services
- **Livreurs** : Effectuent les livraisons avec différents types de véhicules
- **Prestataires** : Offrent des services spécialisés (montage, réparation, etc.)
- **Administrateurs** : Gèrent la plateforme et modèrent les contenus

### EcoDeli est-il gratuit ?

L'inscription et l'utilisation de base sont gratuites. EcoDeli prélève une commission sur les transactions réalisées via la plateforme :
- **Clients** : Aucun frais d'inscription
- **Livreurs** : Commission de 15-20% sur les livraisons
- **Commerçants** : Commission de 8-12% sur les ventes
- **Prestataires** : Commission de 10-15% sur les services

### Dans quelles villes EcoDeli est-il disponible ?

Actuellement, EcoDeli est déployé en France métropolitaine. L'expansion vers d'autres pays européens est prévue pour 2025.

---

## Installation et Configuration

### Quels sont les prérequis pour installer EcoDeli ?

**Pour le développement :**
- Java JDK 21 ou supérieur
- Node.js 16.x ou supérieur
- PostgreSQL 12 ou supérieur
- Git pour le contrôle de version

**Pour la production :**
- Docker et Docker Compose
- Serveur avec 2 CPU et 4GB de RAM minimum

### Comment installer EcoDeli en local ?

Suivez le [Guide d'Installation](GUIDE-INSTALLATION.md) complet. En résumé :

1. Cloner le repository
2. Configurer PostgreSQL
3. Installer et démarrer le backend
4. Installer et démarrer les frontends
5. Configurer les variables d'environnement

### L'application ne démarre pas, que faire ?

**Vérifications communes :**

1. **Backend ne démarre pas :**
   ```bash
   # Vérifier Java
   java -version
   
   # Vérifier la base de données
   psql -h localhost -U ecodeli_user -d ecodeli
   
   # Voir les logs
   ./mvnw spring-boot:run --debug
   ```

2. **Frontend ne démarre pas :**
   ```bash
   # Vérifier Node.js
   node --version
   npm --version
   
   # Nettoyer et réinstaller
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Problème de base de données :**
   ```bash
   # Recréer la base
   dropdb ecodeli
   createdb ecodeli
   ```

### Comment configurer les variables d'environnement ?

Créez un fichier `.env` dans le dossier backend :

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/ecodeli
DATABASE_USERNAME=ecodeli_user
DATABASE_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_key_64_chars_minimum
GOOGLE_MAPS_API_KEY=your_google_maps_key
STRIPE_SECRET_KEY=sk_test_your_stripe_secret
```

### Comment obtenir les clés API nécessaires ?

**Google Maps API :**
1. Aller sur [Google Cloud Console](https://console.cloud.google.com)
2. Créer un projet ou sélectionner un projet existant
3. Activer l'API Maps JavaScript et Geocoding
4. Créer une clé API dans "Identifiants"

**Stripe :**
1. Créer un compte sur [Stripe](https://stripe.com)
2. Récupérer les clés dans le tableau de bord
3. Utiliser les clés de test pour le développement

---

## Utilisation de l'Application

### Comment créer un compte ?

1. Aller sur la page d'inscription
2. Choisir votre type de profil (Client, Livreur, Commerçant, Prestataire)
3. Remplir le formulaire avec vos informations
4. Valider votre email
5. Compléter votre profil selon votre type d'utilisateur

### J'ai oublié mon mot de passe, comment le récupérer ?

1. Cliquer sur "Mot de passe oublié ?" sur la page de connexion
2. Saisir votre adresse email
3. Vérifier votre boîte email
4. Cliquer sur le lien de réinitialisation
5. Créer un nouveau mot de passe

### Comment créer une annonce de livraison ?

1. Se connecter en tant que client
2. Aller dans "Mes annonces" → "Créer une annonce"
3. Remplir les informations :
   - Titre et description
   - Adresses de départ et d'arrivée
   - Prix proposé
   - Date limite
4. Publier l'annonce
5. Recevoir et gérer les candidatures

### Comment candidater à une livraison en tant que livreur ?

1. Se connecter en tant que livreur
2. Parcourir les annonces disponibles
3. Utiliser les filtres (zone, prix, type de véhicule)
4. Cliquer sur une annonce qui vous intéresse
5. Proposer votre prix et délai
6. Ajouter un message personnalisé
7. Envoyer votre candidature

### Comment suivre une livraison en temps réel ?

1. Aller dans "Mes livraisons"
2. Cliquer sur la livraison en cours
3. Voir la position du livreur sur la carte
4. Utiliser le chat intégré pour communiquer
5. Recevoir des notifications automatiques

### Comment évaluer un livreur ou un client ?

Après chaque livraison terminée :
1. Une notification vous invite à évaluer
2. Noter de 1 à 5 étoiles selon les critères :
   - Ponctualité
   - Communication
   - Qualité du service
   - Professionnalisme
3. Laisser un commentaire constructif
4. Valider l'évaluation

---

## Problèmes Techniques

### L'application est lente, que faire ?

**Côté client :**
- Vider le cache du navigateur
- Fermer les autres onglets
- Vérifier la connexion internet
- Utiliser un navigateur récent

**Côté serveur :**
- Vérifier les métriques dans `/actuator/health`
- Consulter les logs d'application
- Vérifier l'utilisation des ressources (CPU, RAM)

### J'ai des erreurs 500, comment les diagnostiquer ?

1. **Consulter les logs :**
   ```bash
   # Backend
   tail -f logs/ecodeli-backend.log
   
   # Nginx
   tail -f /var/log/nginx/error.log
   ```

2. **Vérifier la base de données :**
   ```sql
   SELECT version();
   SELECT * FROM pg_stat_activity;
   ```

3. **Tester les endpoints :**
   ```bash
   curl -X GET http://localhost:8080/actuator/health
   ```

### Les notifications push ne fonctionnent pas

1. **Vérifier les permissions du navigateur**
2. **Contrôler la configuration WebSocket :**
   ```javascript
   // Dans la console du navigateur
   console.log('WebSocket support:', 'WebSocket' in window)
   ```

3. **Vérifier les logs serveur pour les connexions WebSocket**

### L'authentification ne fonctionne pas

**Problèmes courants :**

1. **Token expiré :**
   - Se déconnecter et se reconnecter
   - Vérifier la durée de validité du JWT

2. **Problème CORS :**
   ```bash
   # Vérifier la configuration CORS dans les logs
   grep -i cors logs/ecodeli-backend.log
   ```

3. **Cookies bloqués :**
   - Vérifier les paramètres du navigateur
   - Autoriser les cookies pour le site

### Les images ne s'affichent pas

1. **Vérifier les permissions de fichier**
2. **Contrôler la configuration du stockage**
3. **Vérifier les URLs générées :**
   ```bash
   # Tester l'accès direct
   curl -I http://localhost:8080/uploads/image.jpg
   ```

---

## Développement

### Comment contribuer au projet ?

1. **Forker le repository**
2. **Créer une branche feature :**
   ```bash
   git checkout -b feature/ma-nouvelle-fonctionnalite
   ```
3. **Développer et tester vos modifications**
4. **Suivre les conventions de code** (voir [Guide de Développement](GUIDE-DEVELOPPEMENT.md))
5. **Créer une Pull Request**

### Quelles sont les conventions de code ?

**Backend Java :**
- PascalCase pour les classes
- camelCase pour les méthodes et variables
- UPPER_SNAKE_CASE pour les constantes

**Frontend Vue.js :**
- PascalCase pour les composants
- camelCase pour les props et data
- kebab-case pour les events et fichiers

### Comment exécuter les tests ?

```bash
# Tests backend
cd ecodeli-backend
./mvnw test

# Tests frontend utilisateur
cd ecodeli-frontend-user
npm test

# Tests frontend admin
cd ecodeli-frontend-admin
npm test

# Tests E2E
npm run test:e2e
```

### Comment débugger l'application ?

**Backend :**
1. **IntelliJ IDEA :** Utiliser le debugger intégré
2. **Remote debugging :**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
   ```

**Frontend :**
1. **Vue DevTools :** Extension navigateur
2. **Console navigateur :** F12 → Console
3. **Breakpoints :** Sources → Ajouter des breakpoints

### Comment ajouter une nouvelle fonctionnalité ?

1. **Analyser le besoin** et créer une issue
2. **Concevoir l'architecture** (modèles, API, UI)
3. **Créer les tests** avant le code (TDD)
4. **Implémenter le backend :**
   - Modèle JPA
   - Repository
   - Service
   - Controller
5. **Implémenter le frontend :**
   - Store Pinia
   - Service API
   - Composants Vue
6. **Tester l'intégration**
7. **Documenter** la fonctionnalité

---

## Sécurité

### Comment sont protégées les données personnelles ?

- **Chiffrement** des mots de passe avec BCrypt
- **Tokens JWT** pour l'authentification
- **HTTPS** obligatoire en production
- **Validation** stricte des entrées utilisateur
- **Protection CSRF** et XSS
- **Logs d'audit** pour traçabilité

### Comment signaler une vulnérabilité de sécurité ?

**Ne pas créer d'issue publique.** Envoyer un email à security@ecodeli.fr avec :
- Description détaillée de la vulnérabilité
- Étapes pour reproduire
- Impact potentiel
- Version affectée

### Les paiements sont-ils sécurisés ?

Oui, EcoDeli utilise **Stripe** pour traiter les paiements :
- **PCI DSS** compliant
- **Chiffrement** des données bancaires
- **3D Secure** pour les cartes européennes
- Aucune donnée bancaire stockée sur nos serveurs

---

## Support

### Comment contacter le support ?

**Selon la nature de votre problème :**

1. **Questions techniques :** [GitHub Issues](https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili/issues)
2. **Problèmes de compte :** support@ecodeli.fr
3. **Signalement de bug :** Via l'interface ou GitHub Issues
4. **Suggestions d'amélioration :** GitHub Discussions

### Quels sont les horaires de support ?

- **Support technique :** 24h/24 via GitHub Issues
- **Support utilisateur :** Lundi-Vendredi 9h-18h
- **Urgences production :** 24h/24 pour les clients premium

### Comment signaler un bug ?

1. **Vérifier** que le bug n'est pas déjà signalé
2. **Créer une issue GitHub** avec :
   - Titre descriptif
   - Étapes pour reproduire
   - Comportement attendu vs observé
   - Captures d'écran si pertinentes
   - Environnement (navigateur, OS, version)

### Format type d'issue GitHub :

```markdown
## Description
Brève description du problème

## Étapes pour reproduire
1. Aller sur...
2. Cliquer sur...
3. Voir l'erreur

## Comportement attendu
Ce qui devrait se passer

## Comportement observé
Ce qui se passe réellement

## Environnement
- OS: [ex. macOS 14.1]
- Navigateur: [ex. Chrome 119]
- Version EcoDeli: [ex. 1.0.0]

## Captures d'écran
Si applicable

## Informations supplémentaires
Logs, messages d'erreur, etc.
```

### Comment mettre à jour EcoDeli ?

**En développement :**
```bash
git pull origin main
./mvnw clean install  # Backend
npm install            # Frontend
```

**En production :**
- Suivre le guide de déploiement
- Sauvegarder la base de données avant la mise à jour
- Tester sur un environnement de staging

### Y a-t-il une roadmap publique ?

Oui, consultez :
- **GitHub Projects** : Roadmap détaillée
- **Issues avec label "enhancement"** : Nouvelles fonctionnalités
- **Milestones** : Versions planifiées

---

## Questions Spécifiques par Rôle

### Pour les Clients

**Q : Puis-je modifier une annonce après publication ?**
R : Oui, tant qu'aucune candidature n'a été acceptée. Aller dans "Mes annonces" → "Modifier".

**Q : Comment annuler une livraison ?**
R : Contactez le livreur via le chat. Si pas de réponse, contactez le support. Des frais peuvent s'appliquer selon le timing.

**Q : Puis-je programmer une livraison pour plus tard ?**
R : Oui, spécifiez la date et l'heure souhaitées lors de la création de l'annonce.

### Pour les Livreurs

**Q : Comment optimiser mes revenus ?**
R : 
- Maintenir une note élevée (>4.5)
- Être réactif aux nouvelles annonces
- Proposer des retours à vide
- Travailler aux heures de pointe

**Q : Puis-je refuser une livraison après acceptation ?**
R : Possible mais fortement déconseillé. Cela impacte votre note et peut entraîner une suspension temporaire.

**Q : Comment mettre à jour ma position ?**
R : L'application suit automatiquement votre position pendant les livraisons (avec votre autorisation).

### Pour les Commerçants

**Q : Comment gérer mon catalogue ?**
R : Aller dans "Mon commerce" → "Catalogue" pour ajouter, modifier ou supprimer des produits.

**Q : Quand suis-je payé ?**
R : Paiement sous 48h après confirmation de livraison pour les contrats standard, immédiat pour les contrats premium.

**Q : Comment gérer les retours clients ?**
R : Contactez le support avec le numéro de commande. Une médiation peut être proposée.

---

## Glossaire

**Annonce** : Demande de livraison publiée par un client
**Candidature** : Proposition de prix et délai d'un livreur pour une annonce
**Colis** : Objet à transporter lors d'une livraison
**Géocodage** : Conversion d'une adresse en coordonnées GPS
**JWT** : JSON Web Token, système d'authentification
**Livraison** : Mission acceptée par un livreur
**Prestataire** : Professionnel proposant des services (montage, réparation, etc.)
**WebSocket** : Technologie pour la communication temps réel

---

## Liens Utiles

- **Documentation complète** : [DOCUMENTATION.md](DOCUMENTATION.md)
- **Guide d'installation** : [GUIDE-INSTALLATION.md](GUIDE-INSTALLATION.md)
- **Guide d'utilisation** : [GUIDE-UTILISATION.md](GUIDE-UTILISATION.md)
- **API Documentation** : [API-DOCUMENTATION.md](API-DOCUMENTATION.md)
- **Repository GitHub** : [EcoDeli GitHub](https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili)
- **Issues** : [GitHub Issues](https://github.com/jade6taf/EcoDeli-2a5-Zazic-Lisika-Tafili/issues)

---

*FAQ EcoDeli - Version 1.0 - Décembre 2024*

**Cette FAQ est mise à jour régulièrement. Si votre question n'y figure pas, n'hésitez pas à créer une issue GitHub ou contacter le support.**