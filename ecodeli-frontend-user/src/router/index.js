import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/client/create-annonce',
      name: 'CreateAnnonce',
      component: () => import('@/views/client/CreateAnnonce.vue'),
      meta: { requiresAuth: true, allowedRoles: ['CLIENT'] }
    },
    {
      path: '/paiement/:annonceId',
      name: 'PaiementAnnonce',
      component: () => import('@/views/client/PaiementAnnonce.vue'),
      meta: { requiresAuth: true, allowedRoles: ['CLIENT'] }
    },
    {
      path: '/client/annonces',
      name: 'ClientAnnonces',
      component: () => import('@/views/client/AnnoncesList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
  {
    path: '/livreur/livraisons',
    name: 'livreur-livraisons',
    component: () => import('../views/livreur/LivraisonsList.vue'),
    meta: { requiresAuth: true, role: 'LIVREUR' }
  },
  {
    path: '/livreur/partielles',
    name: 'livreur-partielles',
    component: () => import('../views/livreur/AnnoncesPartielles.vue'),
    meta: { requiresAuth: true, role: 'LIVREUR' }
  },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/auth/LoginView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/auth/RegisterView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/client',
      name: 'client',
      component: () => import('../views/client/DashboardView.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/client/create-annonce',
      name: 'client-create-annonce',
      component: () => import('../views/client/CreateAnnonce.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/client/candidatures-partielles/:id',
      name: 'client-candidatures-partielles',
      component: () => import('../views/client/CandidaturesPartielles.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/client/demande-service',
      name: 'client-demande-service',
      component: () => import('../views/client/DemandeService.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/client/demandes-services',
      name: 'client-demandes-services',
      component: () => import('../views/client/DemandesServicesList.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/client/demandes-services/:id/candidatures',
      name: 'client-candidatures-recues',
      component: () => import('../views/client/CandidaturesRecues.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/client/services',
      name: 'client-mes-services',
      component: () => import('../views/client/MesServices.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/client/mission/:id/validation',
      name: 'client-validation-mission',
      component: () => import('../views/client/ValidationMission.vue'),
      meta: { requiresAuth: true, role: 'CLIENT' }
    },
    {
      path: '/livreur',
      name: 'livreur',
      component: () => import('../views/livreur/DashboardView.vue'),
      meta: { requiresAuth: true, role: 'LIVREUR' }
    },
    {
      path: '/commercant',
      name: 'commercant',
      component: () => import('../views/commercant/DashboardView.vue'),
      meta: { requiresAuth: true, role: 'COMMERCANT' }
    },
    {
      path: '/prestataire',
      name: 'prestataire',
      component: () => import('../views/prestataire/DashboardView.vue'),
      meta: { requiresAuth: true, role: 'PRESTATAIRE' }
    },
    {
      path: '/prestataire/profil',
      name: 'prestataire-profil',
      component: () => import('../views/prestataire/ConfigurationProfil.vue'),
      meta: { requiresAuth: true, role: 'PRESTATAIRE' }
    },
    {
      path: '/prestataire/demandes',
      name: 'prestataire-demandes',
      component: () => import('../views/prestataire/DemandesDisponibles.vue'),
      meta: { requiresAuth: true, role: 'PRESTATAIRE' }
    },
    {
      path: '/prestataire/candidatures',
      name: 'prestataire-candidatures',
      component: () => import('../views/prestataire/MesCandidatures.vue'),
      meta: { requiresAuth: true, role: 'PRESTATAIRE' }
    },
    {
      path: '/prestataire/missions',
      name: 'prestataire-missions',
      component: () => import('../views/prestataire/MesMissions.vue'),
      meta: { requiresAuth: true, role: 'PRESTATAIRE' }
    },
    {
      path: '/prestataire/evaluations',
      name: 'prestataire-evaluations',
      component: () => import('../views/prestataire/MesEvaluations.vue'),
      meta: { requiresAuth: true, role: 'PRESTATAIRE' }
    },
    {
      path: '/prestataire/portefeuille',
      name: 'prestataire-portefeuille',
      component: () => import('../views/prestataire/PortefeuillePrestataire.vue'),
      meta: { requiresAuth: true, role: 'PRESTATAIRE' }
    },
        {
          path: '/livreur/partielles',
          name: 'AnnoncesPartielles',
          component: () => import('../views/livreur/AnnoncesPartielles.vue'),
          meta: { requiresAuth: true, role: 'LIVREUR' }
        },
        {
          path: '/livreur/livraisons',
          name: 'LivraisonsList',
          component: () => import('../views/livreur/LivraisonsList.vue'),
          meta: { requiresAuth: true, role: 'LIVREUR' }
        },
        {
          path: '/livreur/portefeuille',
          name: 'PortefeuilleLivreur',
          component: () => import('../views/livreur/PortefeuilleLivreur.vue'),
          meta: { requiresAuth: true, role: 'LIVREUR' }
        }
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    const role = authStore.userRole
    next(`/${role.toLowerCase()}`)
  } else if (to.meta.role && authStore.userRole !== to.meta.role) {
    const role = authStore.userRole
    next(`/${role.toLowerCase()}`)
  } else {
    next()
  }
})

export default router
