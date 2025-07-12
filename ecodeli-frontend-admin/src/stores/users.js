import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useUsersStore = defineStore('users', () => {
  const users = ref([])
  const loading = ref(false)
  const error = ref(null)

  const totalUsers = computed(() => users.value.length)

  const usersByType = computed(() => {
    const types = {
      CLIENT: 0,
      LIVREUR: 0,
      PRESTATAIRE: 0,
      COMMERCANT: 0,
      ADMIN: 0
    }

    users.value.forEach(user => {
      if (types.hasOwnProperty(user.userType)) {
        types[user.userType]++
      }
    })

    return types
  })

  const stats = computed(() => ({
    totalUsers: totalUsers.value,
    clients: usersByType.value.CLIENT,
    livreurs: usersByType.value.LIVREUR,
    prestataires: usersByType.value.PRESTATAIRE,
    commercants: usersByType.value.COMMERCANT,
    admins: usersByType.value.ADMIN
  }))

  const fetchUsers = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get('http://localhost:8080/api/admin/users')
      users.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement des utilisateurs'
      console.error('Erreur fetchUsers:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchUserById = async (id) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`http://localhost:8080/api/admin/users/${id}`)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors du chargement de l\'utilisateur'
      console.error('Erreur fetchUserById:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const createUser = async (userData) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.post('http://localhost:8080/api/auth/register', userData)

      await fetchUsers()

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la création de l\'utilisateur'
      console.error('Erreur createUser:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const updateUser = async (id, userData) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.put(`http://localhost:8080/api/admin/users/${id}`, userData)

      const index = users.value.findIndex(user => user.idUtilisateur === id)
      if (index !== -1) {
        users.value[index] = response.data
      }

      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la modification de l\'utilisateur'
      console.error('Erreur updateUser:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const deleteUser = async (id) => {
    loading.value = true
    error.value = null

    try {
      await axios.delete(`http://localhost:8080/api/admin/users/${id}`)

      users.value = users.value.filter(user => user.idUtilisateur !== id)

      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.message || 'Erreur lors de la suppression de l\'utilisateur'
      console.error('Erreur deleteUser:', err)
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchStats = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/admin/users/stats')
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Erreur fetchStats:', err)
      return { success: true, data: stats.value }
    }
  }

  return {
    users,
    loading,
    error,

    totalUsers,
    usersByType,
    stats,

    fetchUsers,
    fetchUserById,
    createUser,
    updateUser,
    deleteUser,
    fetchStats
  }
})
