import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/admin/login'
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../views/auth/AdminLogin.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/admin/register',
      name: 'admin-register',
      component: () => import('../views/auth/AdminRegister.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/admin',
      component: () => import('../layout/AppLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/dashboard/DashboardView.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/users/UserList.vue')
        },
        {
          path: 'users/create',
          name: 'admin-users-create',
          component: () => import('../views/users/UserCreate.vue')
        },
        {
          path: 'users/:id/edit',
          name: 'admin-users-edit',
          component: () => import('../views/users/UserEdit.vue'),
          props: true
        },
        {
          path: 'livraisons',
          name: 'admin-livraisons',
          component: () => import('../views/livraisons/LivraisonList.vue')
        },
        {
          path: 'prestataires',
          name: 'admin-prestataires',
          component: () => import('../views/prestataires/PrestatairesList.vue')
        },
        {
          path: 'stats',
          name: 'admin-stats',
          component: () => import('../views/dashboard/StatsView.vue')
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: () => import('../views/dashboard/SettingsView.vue')
        },
        {
          path: 'contrats',
          name: 'admin-contrats',
          component: () => import('../views/contrats/ContratsList.vue')
        }
      ]
    }
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/admin/login')
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next('/admin/login')
  } else if (to.meta.requiresGuest && authStore.isAuthenticated && authStore.isAdmin) {
    next('/admin/dashboard')
  } else {
    next()
  }
})

export default router
