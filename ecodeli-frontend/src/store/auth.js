import { reactive } from 'vue'

export const authStore = reactive({
  isAuthenticated: false,
  user: null,
  token: null,

  initialize() {
    const token = localStorage.getItem('token')
    const user = localStorage.getItem('user')
    if (token && user) {
      this.token = token
      this.user = JSON.parse(user)
      this.isAuthenticated = true
    }
  },

  setAuth(token, user) {
    this.token = token
    this.user = user
    this.isAuthenticated = true
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(user))
  },

  clearAuth() {
    this.token = null
    this.user = null
    this.isAuthenticated = false
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  updateUser(user) {
    this.user = user
    localStorage.setItem('user', JSON.stringify(user))
  }
})

authStore.initialize()
