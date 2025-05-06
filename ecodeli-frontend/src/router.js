import { createRouter, createWebHistory } from 'vue-router'
import AdminDashboard from './views/admin/DashboardView.vue'
import AdminUserList from './views/admin/UserListView.vue'
import AdminUserCreate from './views/admin/UserCreateView.vue'
import AdminUserEdit from './views/admin/UserEditView.vue'
import AdminAnnonceList from './views/admin/AnnonceListView.vue'
import AdminLivraisonsList from './views/admin/LivraisonsListView.vue'

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
    path: '/client/annonces/:id',
    name: 'client-annonce-detail',
    component: () => import('./views/client/AnnonceDetailView.vue'),
    meta: { requiresAuth: true, role: 'CLIENT' }
  },
  {
    path: '/client/services-types',
    name: 'services-types',
    component: () => import('./views/client/ServicesTypesView.vue'),
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
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/utilisateurs',
    name: 'admin-users',
    component: AdminUserList,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/utilisateurs/new',
    name: 'admin-user-create',
    component: AdminUserCreate,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/utilisateurs/:id/edit',
    name: 'admin-user-edit',
    component: AdminUserEdit,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/annonces',
    name: 'admin-annonces',
    component: AdminAnnonceList,
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/livraisons',
    name: 'admin-livraisons',
    component: AdminLivraisonsList,
    meta: { requiresAuth: true, role: 'ADMIN' }
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