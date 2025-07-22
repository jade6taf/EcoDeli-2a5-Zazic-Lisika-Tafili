# 🚂 Déploiement EcoDeli sur Railway

## Configuration terminée ✅

Le backend a été adapté pour PostgreSQL et Railway avec les modifications suivantes :

### Modifications effectuées :

1. **Driver de base de données**
   - ✅ MariaDB → PostgreSQL dans `pom.xml`
   - ✅ Configuration PostgreSQL dans `application.properties`
   - ✅ Dialect Hibernate adapté

2. **Configuration Railway**
   - ✅ Fichier `railway.toml` créé
   - ✅ Health check configuré sur `/actuator/health`
   - ✅ Variables d'environnement définies

3. **CORS adapté**
   - ✅ Support des URLs Railway dynamiques
   - ✅ Variables d'environnement pour frontend URLs

## 🚀 Étapes de déploiement sur Railway :

### 1. Créer les services sur Railway

1. **Connectez-vous à Railway** : https://railway.app
2. **New Project** → **Deploy from GitHub repo**
3. **Sélectionnez votre repository EcoDeli**

### 2. Ajouter PostgreSQL

1. Dans votre projet Railway, cliquez **+ Add Service**
2. Sélectionnez **Database** → **PostgreSQL**
3. Railway générera automatiquement `DATABASE_URL`

### 3. Configurer les variables d'environnement

Dans l'interface Railway, ajoutez ces variables pour le service backend :

```
JWT_SECRET=votre_jwt_secret_securise
MAIL_USERNAME=votre_email@gmail.com
MAIL_PASSWORD=votre_mot_de_passe_application
GOOGLE_MAPS_API_KEY=votre_cle_google_maps
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

### 4. Déploiement automatique

1. **Push vos changements** sur GitHub
2. Railway déploiera automatiquement
3. Surveillez les logs de déploiement
4. Testez l'endpoint : `https://votre-backend.railway.app/actuator/health`

## 🔧 URL finale du backend

Une fois déployé, votre backend sera disponible à :
`https://[nom-du-service].railway.app`

## ⚡ Avantages Railway vs Fly.io

- ✅ **Déploiement en 1-click** depuis GitHub
- ✅ **PostgreSQL gratuit** (500MB)
- ✅ **Interface simple** pour variables d'environnement
- ✅ **Logs en temps réel** faciles à lire
- ✅ **SSL automatique**
- ✅ **Pas de configuration complexe**

## 🔄 Migration des données (optionnel)

Si vous avez des données existantes dans MariaDB :

1. **Export depuis MariaDB** : `mysqldump -u user -p database > backup.sql`
2. **Conversion** : Adapter la syntaxe SQL si nécessaire
3. **Import dans PostgreSQL** : Via l'interface Railway ou psql

## 📱 Mise à jour des frontends

Une fois le backend déployé, mettez à jour l'URL API dans vos frontends :

```javascript
// Dans votre fichier de configuration frontend
const API_BASE_URL = 'https://votre-backend.railway.app'
```

## 🎯 Test final

1. Backend : `https://votre-backend.railway.app/actuator/health`
2. API Auth : `https://votre-backend.railway.app/api/auth/login`
3. Swagger UI : `https://votre-backend.railway.app/swagger-ui.html`

---

**Railway est prêt ! Plus simple et plus stable que Fly.io pour votre cas d'usage.** 🎉
