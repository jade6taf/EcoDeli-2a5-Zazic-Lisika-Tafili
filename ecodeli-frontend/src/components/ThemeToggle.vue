<script>
import { themeStore } from '@/store/theme.js'

export default {
  name: 'ThemeToggle',
  computed: {
    isDark() {
      return themeStore.isDark
    }
  },
  methods: {
    toggleTheme() {
      themeStore.toggleTheme()
    }
  }
}
</script>

<template>
    <button
        @click="toggleTheme"
        class="theme-toggle"
        :title="isDark ? 'Activer le mode clair' : 'Activer le mode sombre'">
        <i :class="isDark ? 'fas fa-sun' : 'fas fa-moon'"></i>
        <span class="sr-only">
        {{ isDark ? 'Activer le mode clair' : 'Activer le mode sombre' }}
        </span>
    </button>
</template>

<style scoped>
.theme-toggle {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-color);
}

.theme-toggle:hover {
  background-color: var(--hover-bg);
  transform: scale(1.05);
}

.theme-toggle i {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.theme-toggle:hover i {
  transform: rotate(360deg);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
