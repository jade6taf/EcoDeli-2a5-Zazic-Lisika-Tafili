import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))
  const isLoading = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.userType)
  const userName = computed(() => {
    if (!user.value) return ''
    return `${user.value.prenom} ${user.value.nom}`
  })

  const login = async (email, motDePasse) => {
    isLoading.value = true
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        email,
        motDePasse
      })

      const { token: authToken, ...userData } = response.data

      token.value = authToken
      user.value = userData
      localStorage.setItem('token', authToken)
      localStorage.setItem('user', JSON.stringify(userData))

      setAxiosToken(authToken)

      return { success: true, user: userData }
    } catch (error) {
      console.error('Erreur de connexion:', error)
      return {
        success: false,
        message: error.response?.data?.message || 'Erreur de connexion'
      }
    } finally {
      isLoading.value = false
    }
  }

  const register = async (registerData) => {
    isLoading.value = true
    try {
      const response = await axios.post('http://localhost:8080/api/auth/register', registerData)

      const { token: authToken, ...userData } = response.data

      token.value = authToken
      user.value = userData
      localStorage.setItem('token', authToken)
      localStorage.setItem('user', JSON.stringify(userData))

      setAxiosToken(authToken)

      return { success: true, user: userData }
    } catch (error) {
      console.error('Erreur d\'inscription:', error)
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
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    delete axios.defaults.headers.common['Authorization']
  }

  const loadUserFromStorage = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')

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

  const updateUserData = (userData) => {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
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

    login,
    register,
    logout,
    loadUserFromStorage,
    updateUserData,
    hasRole
  }
})
