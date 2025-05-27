import { reactive } from 'vue'

export const themeStore = reactive({
  isDark: false,
  initialize() {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      this.isDark = savedTheme === 'dark'
    } else {
      this.isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    this.applyTheme()
  },

  toggleTheme() {
    this.isDark = !this.isDark
    this.applyTheme()
    localStorage.setItem('theme', this.isDark ? 'dark' : 'light')
  },

  applyTheme() {
    if (this.isDark) {
      document.documentElement.setAttribute('data-theme', 'dark')
    } else {
      document.documentElement.removeAttribute('data-theme')
    }
  }
})

themeStore.initialize()
