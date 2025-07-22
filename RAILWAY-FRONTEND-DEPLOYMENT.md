# Déploiement Frontend User sur Railway - EcoDeli

## 🎯 **Configuration déployée :**

- **Type :** Build statique Vite/Vue.js
- **Serveur :** `npm run preview` (Vite preview server)
- **URL :** Railway générée automatiquement
- **Backend :** Connecté au backend Railway existant

## ⚙️ **Fichiers configurés :**

### 1. **railway.toml**
```toml
[build]
builder = "nixpacks"

[deploy]
startCommand = "npm run preview"
restartPolicyType = "on_failure"

[variables]
VITE_API_BASE_URL = "https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app"
NODE_ENV = "production"
VITE_PORT = "8080"
HOST = "0.0.0.0"
PORT = "8080"
```

### 2. **vite.config.js - Optimisé pour production**
- DevTools désactivés en production
- Configuration serveur/preview pour Railway
- Build optimisé avec chunks vendor
- Minification Terser

### 3. **src/config/api.js - Variables d'environnement**
```javascript
BASE_URL: import.meta.env.VITE_API_BASE_URL || 'https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app'
```

### 4. **Backend CORS mis à jour**
- Patterns Railway génériques ajoutés
- Support des URLs frontend Railway

## 🚀 **Processus de déploiement Railway :**

1. **Créer nouveau service Railway :**
   ```bash
   railway login
   railway create ecodeli-frontend-user
   ```

2. **Connecter le dépôt :**
   - Connecter ce repository GitHub
   - Choisir le dossier `ecodeli-frontend-user`

3. **Variables d'environnement automatiques :**
   - `VITE_API_BASE_URL` déjà configuré dans railway.toml
   - `NODE_ENV=production`
   - `PORT=8080`

4. **Déploiement automatique :**
   - Railway détecte `railway.toml`
   - Build avec `npm run build`
   - Démarrage avec `npm run preview`

## 📊 **Ressources estimées :**

| Ressource | Usage | Limite Railway |
|-----------|-------|----------------|
| **RAM** | ~100MB | ✅ 512MB (Hobby) |
| **CPU** | Faible | ✅ Partagé |
| **Stockage** | ~50MB | ✅ 1GB |

## 🔗 **URLs finales attendues :**

- **Backend :** `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app` ✅
- **Frontend User :** `https://ecodeli-frontend-user-production-[hash].up.railway.app` 🔄
- **Database :** PostgreSQL Railway interne ✅

## 🧪 **Tests à effectuer :**

1. **Accès à l'application :** URL Railway générée
2. **Inscription/Connexion :** Doit fonctionner avec backend
3. **Navigation :** Router Vue.js SPA
4. **API calls :** Vérifier que toutes les requêtes passent
5. **Performance :** Temps de chargement < 3s

## 📝 **Commandes utiles Railway :**

```bash
# Voir les services
railway status

# Logs du frontend
railway logs -s ecodeli-frontend-user

# Variables d'environnement
railway variables

# Redéployer manuellement
railway redeploy
```

## 🔧 **Dépannage :**

### **Build échoue :**
- Vérifier les dépendances dans package.json
- Logs : `railway logs -s ecodeli-frontend-user`

### **404 sur routes Vue :**
- Vérifier configuration SPA dans vite.config.js
- S'assurer que le serveur sert index.html pour toutes les routes

### **CORS errors :**
- Backend déjà configuré avec patterns Railway
- Variables FRONTEND_USER_URL si URL personnalisée

## 💰 **Coûts Railway :**

- **Frontend User :** Inclus dans plan Hobby (gratuit)
- **Consommation :** Très faible (site statique)
- **Bande passante :** Limitée par plan Hobby

---

**Status :** 🟡 Prêt pour déploiement  
**Prochaine étape :** Créer service Railway et connecter GitHub
