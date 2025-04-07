import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('./views/HomeView.vue')
  },
  {
    path: '/services',
    name: 'services',
    component: () => import('./views/ServicesView.vue')
  },
  {
    path: '/profiles',
    name: 'profiles',
    component: () => import('./views/ProfilView.vue')
  },
  {
    path: '/contact',
    name: 'contact',
    component: () => import('./views/ContactView.vue')
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('./views/AboutView.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('./views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('./views/RegisterView.vue')
  }
]

const router = createRouter({
    history: createWebHistory(), // Crée des URLs propres comme /about au lieu de /#/about
    routes
})

export default router
