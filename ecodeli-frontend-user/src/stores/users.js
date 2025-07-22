import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useUsersStore = defineStore('users', () => {
  const loading = ref(false)
  const error = ref(null)

  const updateUserProfile = async (userId, profileData) => {
    loading.value = true
    error.value = null

    try {
      const payload = {
        ...profileData,
        userId: userId
      }

      const response = await axios.put('https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/users/profile', payload)

      return {
        success: response.data.success,
        data: response.data.user,
        message: response.data.message
      }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors de la mise à jour du profil'
      console.error('Erreur updateUserProfile:', err)
      return {
        success: false,
        message: error.value
      }
    } finally {
      loading.value = false
    }
  }


  const getUserProfile = async (userId) => {
    loading.value = true
    error.value = null

    try {
      const response = await axios.get(`https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app/api/users/profile/${userId}`)

      return {
        success: response.data.success,
        data: response.data.user
      }
    } catch (err) {
      error.value = err.response?.data?.error || 'Erreur lors du chargement du profil'
      console.error('Erreur getUserProfile:', err)
      return {
        success: false,
        message: error.value
      }
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,

    updateUserProfile,
    getUserProfile
  }
})
