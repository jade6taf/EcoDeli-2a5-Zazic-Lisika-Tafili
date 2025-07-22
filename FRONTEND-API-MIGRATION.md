# Migration des URLs API vers Railway

## ✅ Modifications effectuées

### Backend
- **Déployé sur Railway** : `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app`
- **PostgreSQL** connecté et fonctionnel
- **Variables d'environnement** configurées
- **Health check** : `/actuator/health` → `{"status":"UP"}`

### Frontend Utilisateur (`ecodeli-frontend-user`)
- ✅ **Configuration API centralisée** : `/src/config/api.js`
- ✅ **Store auth.js** mis à jour avec API_URLS
- ✅ **Tous les autres stores** mis à jour automatiquement
- ✅ **URLs localhost remplacées** par l'URL Railway

### Frontend Admin (`ecodeli-frontend-admin`)
- ✅ **Configuration API centralisée** : `/src/config/api.js`
- ✅ **Store auth.js** mis à jour avec API_URLS
- ✅ **Tous les autres stores** mis à jour automatiquement
- ✅ **URLs localhost remplacées** par l'URL Railway

## 🔗 URLs de production

- **Backend API** : `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app`
- **Health Check** : `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/actuator/health`
- **Auth Login** : `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/auth/login`
- **Auth Register** : `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/auth/register`

## 🚀 Tests à effectuer

1. **Démarrer les frontends** :
   ```bash
   cd ecodeli-frontend-user && npm run dev
   cd ecodeli-frontend-admin && npm run dev
   ```

2. **Tester l'inscription/connexion** depuis les interfaces Vue.js

3. **Vérifier que les requêtes** vont bien vers Railway (F12 → Network)

## 📝 Notes importantes

- **CORS configuré** dans le backend pour accepter les requêtes des frontends
- **Configuration centralisée** : pour changer l'URL, modifier uniquement `/src/config/api.js`
- **Domaine personnalisé** : `ecodeli-solutions.fr` configuré mais nécessite DNS OVH

## 🔧 Pour revenir au développement local

Modifier dans `/src/config/api.js` :
```javascript
BASE_URL: 'http://localhost:8080'
