<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppTopbar from './AppTopbar.vue'

const sidebarCollapsed = ref(false)
const sidebarMobileOpen = ref(false)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const toggleMobileSidebar = () => {
  sidebarMobileOpen.value = !sidebarMobileOpen.value
}

const closeMobileSidebar = () => {
  sidebarMobileOpen.value = false
}

const handleResize = () => {
  if (window.innerWidth <= 768) {
    sidebarCollapsed.value = false
    sidebarMobileOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  handleResize()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="admin-layout">
    <AppSidebar
      :collapsed="sidebarCollapsed"
      :mobileOpen="sidebarMobileOpen"
      @toggle="toggleSidebar"
      @close-mobile="closeMobileSidebar"
    />

    <div class="admin-main">
      <AppTopbar
        @toggle-sidebar="toggleSidebar"
        @toggle-mobile-sidebar="toggleMobileSidebar"
      />

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
</style>
