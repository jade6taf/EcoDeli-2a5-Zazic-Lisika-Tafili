<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineEmits(['toggle-sidebar', 'toggle-mobile-sidebar'])

const route = useRoute()
const authStore = useAuthStore()

const userInitials = computed(() => {
  const name = authStore.userName || 'Admin User'
  return name.split(' ').map(n => n[0]).join('').toUpperCase()
})

const toggleUserMenu = () => {
  // TODO: Implémenter le menu utilisateur
}
</script>

<template>
  <header class="admin-topbar">
    <div class="topbar-left">
      <Button
        icon="pi pi-bars"
        class="p-button-text p-button-plain hidden md:inline-flex"
        @click="$emit('toggle-sidebar')"
      />
    </div>

    <div class="topbar-right">
      <div class="user-profile">
        <Button
          class="p-button-text p-button-plain"
          @click="toggleUserMenu">
          <div class="flex align-items-center gap-2">
            <Avatar
              :label="userInitials"
              class="bg-green-500 text-white"
              size="normal"
            />
            <div class="hidden md:block">
              <div class="font-semibold text-sm">{{ authStore.userName || 'Admin' }}</div>
              <div class="text-xs text-500">Administrateur</div>
            </div>
            <i class="pi pi-chevron-down text-xs"></i>
          </div>
        </Button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.user-profile {
  position: relative;
}

.hidden {
  display: none;
}

@media (min-width: 768px) {
  .md\:hidden {
    display: none;
  }

  .md\:inline-flex {
    display: inline-flex;
  }

  .md\:block {
    display: block;
  }
}

.text-500 {
  color: var(--surface-500);
}

.ml-2 {
  margin-left: 0.5rem;
}

.mt-2 {
  margin-top: 0.5rem;
}
</style>
