<script>
import { onMounted, ref } from 'vue'

export default {
    name: 'AdminAnnonceList',
    setup() {
      const annonces = ref([])
      const isLoading = ref(true)
      const error = ref(null)

      const fetchAnnonces = async () => {
        isLoading.value = true
        error.value = null
        try {
          const token = localStorage.getItem('token')
          const res = await fetch('/api/admin/utilisateurs/annonces', {
            headers: {
              'Authorization': 'Bearer ' + token
            }
          })
          if (!res.ok)
            throw new Error('Erreur lors du chargement des annonces')
          annonces.value = await res.json()
        } catch (e) {
          error.value = e.message
        } finally {
          isLoading.value = false
        }
      }
      onMounted(fetchAnnonces)
      return { annonces, isLoading, error }
    }
}
</script>

<template>
  <div class="dashboard-container">
    <h1>Annonces</h1>
    <div v-if="isLoading" class="loading">
      Chargement...
    </div>
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    <div v-else>
      <table class="annonce-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Titre</th>
            <th>Description</th>
            <th>Statut</th>
            <th>Client</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in annonces" :key="a.idAnnonce">
            <td>{{ a.idAnnonce }}</td>
            <td>{{ a.titre }}</td>
            <td>{{ a.description }}</td>
            <td>{{ a.statut }}</td>
            <td>{{ a.client ? a.client.nom + ' ' + a.client.prenom : '' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.annonce-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 2rem;
}

.annonce-table th, .annonce-table td {
  border: 1px solid #ddd;
  padding: 0.75rem;
  text-align: left;
}

.annonce-table th {
  background: #f5f5f5;
}

.loading, .error-message {
  text-align: center;
  padding: 2rem;
}

.error-message {
  color: #e53935;
}
</style>
