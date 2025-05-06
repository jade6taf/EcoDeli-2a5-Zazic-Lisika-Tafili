<script>
import { onMounted, ref } from 'vue'

export default {
    name: 'AdminLivraisonsList',
    setup() {
        const livraisons = ref([])
        const isLoading = ref(true)
        const error = ref(null)

        const fetchLivraisons = async () => {
          isLoading.value = true
          error.value = null
          try {
            const token = localStorage.getItem('token')
            const res = await fetch('http://localhost:8080/api/admin/utilisateurs/livraisons', {
              headers: {
                'Authorization': 'Bearer ' + token
              }
            })
            if (!res.ok)
              throw new Error('Erreur lors du chargement des livraisons')
            livraisons.value = await res.json()
          } catch (e) {
            error.value = e.message
          } finally {
            isLoading.value = false
          }
        }
        onMounted(fetchLivraisons)
        return { livraisons, isLoading, error }
    }
}
</script>

<template>
  <div class="dashboard-container">
    <h1>Livraisons</h1>
    <div v-if="isLoading" class="loading">
      Chargement...
    </div>
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    <div v-else>
      <table class="livraison-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Statut</th>
            <th>Annonce</th>
            <th>Livreur</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in livraisons" :key="l.idLivraison">
            <td>{{ l.idLivraison }}</td>
            <td>{{ l.statut }}</td>
            <td>{{ l.annonce ? l.annonce.titre : '' }}</td>
            <td>{{ l.annonce && l.annonce.livreur ? l.annonce.livreur.nom + ' ' + l.annonce.livreur.prenom : '' }}</td>
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

.livraison-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 2rem;
}

.livraison-table th, .livraison-table td {
  border: 1px solid #ddd;
  padding: 0.75rem;
  text-align: left;
}

.livraison-table th {
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
