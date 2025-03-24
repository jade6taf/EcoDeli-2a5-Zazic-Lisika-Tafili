import { createApp } from 'vue' // fct de Vue -> créer l'instance de l'application
import App from './App.vue'
import router from './router'

createApp(App)
  .use(router) // Enregistre le plugin router dans l'application (pour utiliser <router-view>, <router-link>)
  .mount('#app') // Monte l'application dans l'élément HTML avec l'id "app" (index.html)
