# Optimisation Mémoire Railway - EcoDeli Backend

## 🚨 Problème rencontré
**Date :** 22/07/2025  
**Erreur :** `Deploy Ran Out of Memory` sur Railway  
**Plan Railway :** Hobby (512MB RAM)  

## ⚙️ Optimisations appliquées

### 1. Configuration JVM optimisée
```bash
JAVA_TOOL_OPTIONS = "-Xmx400m -Xms200m -XX:MaxMetaspaceSize=100m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"
```

**Détails :**
- **Heap max :** 400MB (laisse 112MB pour système + metaspace)
- **Heap initial :** 200MB (démarrage plus rapide)
- **Metaspace :** 100MB max (classes Java)
- **G1GC :** Garbage Collector optimisé faible latence
- **String deduplication :** Économie mémoire sur chaînes dupliquées

### 2. Configuration base de données allégée
```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
```

### 3. Hibernate optimisé
```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=false
spring.jpa.properties.hibernate.cache.use_query_cache=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

### 4. Logs réduits
```properties
logging.level.org.springframework=WARN
logging.level.org.hibernate.SQL=WARN
logging.level.com.zaxxer.hikari=WARN
```

## 📊 Utilisation mémoire cible

| Composant | Mémoire estimée | Description |
|-----------|----------------|-------------|
| JVM Heap | 400MB max | Application Spring Boot |
| Metaspace | 100MB max | Classes Java chargées |
| Stack + Direct | ~50MB | Threads et buffers |
| Système OS | ~50MB | Processus système |
| **TOTAL** | **~500MB** | ✅ Compatible 512MB |

## 🔍 Monitoring

### Commandes utiles Railway
```bash
railway logs
railway status
railway metrics
```

### Indicateurs à surveiller
- ✅ **Démarrage sans OOM**
- ✅ **Health check UP** : `/actuator/health`
- ✅ **CORS résolu** : Frontend peut se connecter
- ✅ **Temps de réponse < 2s**

## 🚀 Tests de validation

1. **Backend démarre :** `https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/actuator/health`
2. **Frontend connecté :** Inscription/connexion depuis localhost:5173
3. **Pas d'erreur mémoire :** Vérifier logs Railway
4. **Performance acceptable :** API répond rapidement

## 💰 Options d'upgrade si nécessaire

Si les optimisations ne suffisent pas :

| Plan | RAM | Prix | Recommandation |
|------|-----|------|----------------|
| Hobby | 512MB | Gratuit | ✅ Actuel avec optimisations |
| Starter | 1GB | $5/mois | 🔄 Si OOM persiste |
| Pro | 8GB | $20/mois | ❌ Overkill pour ce projet |

## 📝 Notes importantes

- **Mode développement :** Optimisations peuvent être relâchées en local
- **Production :** Ces paramètres sont critiques pour Railway
- **Monitoring :** Surveiller régulièrement l'utilisation mémoire
- **Évolutivité :** Prévoir upgrade si charge utilisateur augmente

## 🔧 Rollback si problème

Pour revenir en arrière :
```bash
git revert 32dbd90  # Commit d'optimisation mémoire
```

---
**Statut :** 🟡 En cours de validation  
**Prochaine étape :** Tester le redéploiement et valider que l'application démarre sans OOM
