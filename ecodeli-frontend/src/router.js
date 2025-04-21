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
  },
  {
    path: '/client/annonces/:id/edit',
    name: 'client-annonce-edit',
    component: () => import('./views/client/AnnonceEditView.vue'),
    meta: { requiresAuth: true, role: 'CLIENT' }
  },
  {
    path: '/livreur',
    name: 'livreur-dashboard',
    component: () => import('./views/livreur/DashboardView.vue'),
    meta: { requiresAuth: true, role: 'LIVREUR' }
  },
  {
    path: '/livreur/annonces',
    name: 'livreur-annonces',
    component: () => import('./views/livreur/AnnoncesDisponiblesView.vue'),
    meta: { requiresAuth: true, role: 'LIVREUR' }
  },
  {
    path: '/livreur/mes-livraisons',
    name: 'livreur-livraisons',
    component: () => import('./views/livreur/MesLivraisonsView.vue'),
    meta: { requiresAuth: true, role: 'LIVREUR' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

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
            next('/');
        } else {
            next();
        }
    } else {
        next();
    }
});

export default router