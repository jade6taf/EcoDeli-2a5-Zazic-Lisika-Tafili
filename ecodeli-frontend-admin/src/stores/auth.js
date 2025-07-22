import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import { API_URLS } from '../config/api.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('admin_token'))
  const isLoading = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.userType)
  const userName = computed(() => {
    if (!user.value)
      return ''
    return `${user.value.prenom} ${user.value.nom}`
  })
  const isAdmin = computed(() => user.value?.userType === 'ADMIN')

  const login = async (email, motDePasse) => {
    isLoading.value = true
    try {
      const response = await axios.post(API_URLS.AUTH_LOGIN, {
        email,
        motDePasse
      })

      const { token: authToken, ...userData } = response.data

      if (userData.userType !== 'ADMIN') {
        throw new Error('Accès non autorisé. Seuls les administrateurs peuvent se connecter.')
      }

      token.value = authToken
      user.value = userData
      localStorage.setItem('admin_token', authToken)
      localStorage.setItem('admin_user', JSON.stringify(userData))

      setAxiosToken(authToken)

      return { success: true, user: userData }
    } catch (error) {
      console.error('Erreur de connexion admin:', error)
      return {
        success: false,
        message: error.message || 'Erreur de connexion'
      }
    } finally {
      isLoading.value = false
    }
  }

  const register = async (registerData) => {
    isLoading.value = true
    try {
      const adminData = {
        ...registerData,
        userType: 'ADMIN'
      }

      const response = await axios.post(API_URLS.AUTH_REGISTER, adminData)

      const { token: authToken, ...userData } = response.data

      token.value = authToken
      user.value = userData
      localStorage.setItem('admin_token', authToken)
      localStorage.setItem('admin_user', JSON.stringify(userData))

      setAxiosToken(authToken)

      return { success: true, user: userData }
    } catch (error) {
      console.error('Erreur d\'inscription admin:', error)
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur d\'inscription'
      }
    } finally {
      isLoading.value = false
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    delete axios.defaults.headers.common['Authorization']
  }

  const loadUserFromStorage = () => {
    const storedToken = localStorage.getItem('admin_token')
    const storedUser = localStorage.getItem('admin_user')

    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
      setAxiosToken(storedToken)
    }
  }

  const setAxiosToken = (authToken) => {
    axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`
  }

  const hasRole = (role) => {
    return userRole.value === role
  }

  if (token.value) {
    loadUserFromStorage()
  }

  return {
    user,
    token,
    isLoading,

    isAuthenticated,
    userRole,
    userName,
    isAdmin,

    login,
    register,
    logout,
    loadUserFromStorage,
    hasRole
  }
})
