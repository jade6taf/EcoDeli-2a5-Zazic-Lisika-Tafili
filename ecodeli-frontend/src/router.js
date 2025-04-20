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
  },
  // Nouvelles routes pour le dashboard client
  {
    path: '/client',
    name: 'client-dashboard',
    component: () => import('./views/client/DashboardView.vue'),
    meta: { requiresAuth: true, role: 'CLIENT' }
  },
  {
    path: '/client/annonces',
    name: 'client-annonces',
    component: () => import('./views/client/AnnoncesListView.vue'),
    meta: { requiresAuth: true, role: 'CLIENT' }
  },
  {
    path: '/client/annonces/new',
    name: 'client-annonce-create',
    component: () => import('./views/client/AnnonceCreateView.vue'),
    meta: { requiresAuth: true, role: 'CLIENT' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation Guard pour protéger les routes authentifiées
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiredRole = to.meta.role;
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  
  if (requiresAuth && (!token || !userStr)) {
    next('/login');
  } else if (requiresAuth && requiredRole) {
    const user = JSON.parse(userStr);
    if (user.type !== requiredRole) {
      next('/'); // Rediriger vers la page d'accueil si le rôle ne correspond pas
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router