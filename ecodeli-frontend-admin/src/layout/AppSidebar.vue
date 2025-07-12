<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  },
})

const router = useRouter()
const authStore = useAuthStore()

const menuItems = [
  {
    label: 'Dashboard',
    icon: 'pi pi-home',
    route: '/admin/dashboard'
  },
  {
    label: 'Utilisateurs',
    icon: 'pi pi-users',
    route: '/admin/users'
  },
  {
    label: 'Livraisons',
    icon: 'pi pi-truck',
    route: '/admin/livraisons'
  },
  {
    label: 'Statistiques',
    icon: 'pi pi-chart-bar',
    route: '/admin/stats'
  },
  {
    label: 'Paramètres',
    icon: 'pi pi-cog',
    route: '/admin/settings'
  }
]

const handleLogout = () => {
  authStore.logout()
  router.push('/admin/login')
}
</script>

<template>
  <aside
    :class="[
      'admin-sidebar',
      { 'collapsed': collapsed },
    ]"
    @click="handleSidebarClick">

    <div class="sidebar-logo">
      <div class="flex align-items-center justify-content-center">
        <i class="pi pi-leaf text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
        <h2 v-show="!collapsed">EcoDeli</h2>
      </div>
      <div v-show="!collapsed" class="admin-badge mt-2">Administration</div>
    </div>

    <nav class="sidebar-menu">
      <router-link
        v-for="item in menuItems"
        :key="item.route"
        :to="item.route"
        class="menu-item"
        :class="{ 'active': $route.path === item.route }"
      >
        <i :class="item.icon"></i>
        <span v-show="!collapsed">{{ item.label }}</span>
      </router-link>

      <div class="menu-divider" v-show="!collapsed"></div>

      <button
        class="menu-item logout-btn"
        @click="handleLogout"
      >
        <i class="pi pi-sign-out"></i>
        <span v-show="!collapsed">Déconnexion</span>
      </button>
    </nav>
  </aside>
</template>

<style scoped>
.admin-badge {
  background: var(--ecodeli-green);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-block;
}

.menu-divider {
  height: 1px;
  background: rgba(255,255,255,0.1);
  margin: 1rem 1.5rem;
}

.logout-btn {
  margin-top: auto;
  color: #ef4444 !important;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.1) !important;
  color: #ef4444 !important;
}
</style>
