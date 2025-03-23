# Guide du Projet Ecodeli Frontend

## Structure générale du projet

```
ecodeli-frontend/
├── public/             # Fichiers statiques qui ne seront pas traités par Webpack
│   ├── favicon.ico     # Icône du site
│   └── index.html      # Template HTML principal
├── src/                # Code source de l'application
│   ├── assets/         # Ressources (images, fonts, etc.)
│   ├── components/     # Composants Vue réutilisables
│   ├── router/         # Configuration du routeur Vue
│   ├── store/          # État global (Vuex)
│   ├── views/          # Pages/vues de l'application
│   ├── App.vue         # Composant racine
│   └── main.js         # Point d'entrée JavaScript
├── .eslintrc.js        # Configuration ESLint
├── .gitignore          # Fichiers ignorés par Git
├── babel.config.js     # Configuration Babel
├── package.json        # Dépendances et scripts npm
└── vue.config.js       # Configuration Vue CLI
```

## Explication détaillée des dossiers et fichiers

### Dossier `public/`
Ce dossier contient tous les fichiers statiques qui ne seront pas traités par le bundler (Webpack). Ils seront copiés tels quels dans le dossier de build.

- **index.html** : Le template HTML principal de l'application. C'est ici que votre application Vue sera montée.
- **favicon.ico** : L'icône de votre site web.
- **Autres fichiers statiques** : Vous pouvez y placer d'autres fichiers comme robots.txt, images statiques, etc.

### Dossier `src/`
C'est le cœur de votre application. Tout votre code source s'y trouve.

#### `src/assets/`
Contient les ressources utilisées par votre application, comme les images, les fichiers CSS globaux, les polices d'écriture, etc. Contrairement aux fichiers dans `public/`, ces ressources sont traitées par Webpack.

#### `src/components/`
Contient les composants Vue réutilisables. Organisez vos composants de la façon suivante :

```
components/
├── common/           # Composants génériques (boutons, inputs, modals...)
├── layout/           # Composants de mise en page (header, footer, sidebar...)
└── feature/          # Composants spécifiques à certaines fonctionnalités
```

#### `src/views/`
Contient les pages complètes de votre application. Chaque vue correspond généralement à une route.

```
views/
├── Home.vue         # Page d'accueil
├── About.vue        # Page À propos
├── ProductList.vue  # Liste des produits
└── ProductDetail.vue # Détail d'un produit
```

#### `src/router/`
Configuration du routeur Vue (Vue Router). Définit les routes de votre application.

- **index.js** : Configuration principale des routes.

#### `src/store/`
État global de l'application utilisant Vuex. Organisez votre store comme suit :

```
store/
├── index.js        # Configuration principale du store
├── modules/        # Modules du store pour différentes fonctionnalités
│   ├── cart.js     # Module pour le panier
│   └── user.js     # Module pour les utilisateurs
└── plugins/        # Plugins Vuex
```

#### `src/services/`
Contient le code pour les appels API et autres services externes.

```
services/
├── api.js          # Configuration de base de l'API (axios)
├── productService.js # Service pour les produits
└── authService.js  # Service pour l'authentification
```

#### `src/utils/`
Fonctions utilitaires réutilisables dans toute l'application.

#### `src/App.vue`
Composant racine de l'application. Contient généralement la structure globale de la page.

#### `src/main.js`
Point d'entrée JavaScript de l'application. C'est ici que l'application Vue est initialisée et montée dans le DOM.

### Fichiers de configuration

- **.eslintrc.js** : Configuration du linter ESLint pour maintenir la qualité du code.
- **.gitignore** : Liste des fichiers à ignorer par Git.
- **babel.config.js** : Configuration de Babel pour la compatibilité JavaScript.
- **package.json** : Liste des dépendances et scripts npm.
- **vue.config.js** : Configuration supplémentaire pour Vue CLI.

## Bonnes pratiques d'organisation

### Nommage des fichiers
- Composants : Utilisez la notation PascalCase (ex: `HeaderNavigation.vue`)
- Autres fichiers JS : Utilisez la notation camelCase (ex: `userService.js`)

### Structure des composants
Chaque composant Vue (.vue) est structuré en trois parties :
1. **template** : La structure HTML du composant
2. **script** : La logique JavaScript du composant
3. **style** : Les styles CSS/SCSS du composant

```vue
<template>
  <!-- Structure HTML ici -->
</template>

<script>
export default {
  name: 'MonComposant',
  props: {
    // Props ici
  },
  data() {
    return {
      // État local ici
    }
  },
  methods: {
    // Méthodes ici
  }
}
</script>

<style scoped>
/* Styles ici */
</style>
```

### Communication entre composants
- Props : Pour passer des données parent → enfant
- Events : Pour passer des informations enfant → parent
- Vuex : Pour l'état global partagé entre plusieurs composants

## Workflow de développement

### Commandes npm utiles
- `npm run serve` : Démarre le serveur de développement
- `npm run build` : Compile l'application pour la production
- `npm run lint` : Vérifie le style du code
- `npm run test` : Lance les tests unitaires

### Cycle de développement
1. Créer/modifier des composants dans `src/components/`
2. Assembler ces composants dans des vues dans `src/views/`
3. Configurer les routes pour ces vues dans `src/router/`
4. Gérer l'état global dans `src/store/` si nécessaire
5. Tester l'application avec `npm run serve`

## Ressources d'apprentissage

- [Documentation officielle de Vue.js](https://vuejs.org/guide/introduction.html)
- [Vue Router](https://router.vuejs.org/)
- [Vuex](https://vuex.vuejs.org/)
- [Vue CLI](https://cli.vuejs.org/)

---
