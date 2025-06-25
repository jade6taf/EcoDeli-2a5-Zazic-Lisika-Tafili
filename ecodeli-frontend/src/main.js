import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import i18n from './i18n/index.js'
import VueHcaptcha from '@hcaptcha/vue3-hcaptcha'
import 'leaflet/dist/leaflet.css';
import '@/assets/css/design-system.css';

createApp(App)
  .use(router)
  .use(i18n)
  .component('VueHcaptcha', VueHcaptcha)
  .mount('#app')
