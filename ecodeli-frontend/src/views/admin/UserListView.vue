<script>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
    name: 'AdminUserList',
    setup() {
        const users = ref([])
        const isLoading = ref(true)
        const error = ref(null)
        const router = useRouter()

        const fetchUsers = async () => {
            isLoading.value = true
            error.value = null
            try {
              const token = localStorage.getItem('token')
              const res = await fetch('http://localhost:8080/api/admin/utilisateurs', {
                headers: {
                  'Authorization': 'Bearer ' + token
                }
              })
              if (!res.ok) throw new Error('Erreur lors du chargement des utilisateurs')
              users.value = await res.json()
            } catch (e) {
              error.value = e.message
            } finally {
              isLoading.value = false
            }
        }

        const goToEdit = (id) => {
          router.push(`/admin/utilisateurs/${id}/edit`)
        }

        const deleteUser = async (id) => {
          if (!confirm('Supprimer cet utilisateur ?')) return
          try {
            const token = localStorage.getItem('token')
            const res = await fetch(`http://localhost:8080/api/admin/utilisateurs/${id}`, {
              method: 'DELETE',
              headers: {
                'Authorization': 'Bearer ' + token
              }
            })
            if (!res.ok) throw new Error('Erreur lors de la suppression')
            users.value = users.value.filter(u => u.idUtilisateur !== id)
          } catch (e) {
            alert(e.message)
          }
        }
        onMounted(fetchUsers)
        return { users, isLoading, error, goToEdit, deleteUser }
    }
}
</script>

<template>
  <div class="dashboard-container">
    <h1>Utilisateurs</h1>
    <div v-if="isLoading" class="loading">Chargement...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>
    <div v-else>
      <table class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nom</th>
            <th>Prénom</th>
            <th>Email</th>
            <th>Type</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.idUtilisateur">
            <td>{{ u.idUtilisateur }}</td>
            <td>{{ u.nom }}</td>
            <td>{{ u.prenom }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.type }}</td>
            <td>
              <button @click="goToEdit(u.idUtilisateur)">
                Éditer
              </button>
              <button @click="deleteUser(u.idUtilisateur)" class="danger">
                Supprimer
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <router-link to="/admin/utilisateurs/new" class="add-user-btn">
        <i class="fas fa-user-plus"></i> Ajouter un utilisateur
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 2rem;
}

.user-table th, .user-table td {
  border: 1px solid #ddd;
  padding: 0.75rem;
  text-align: left;
}

.user-table th {
  background: #f5f5f5;
}

.add-user-btn {
  display: inline-block;
  margin-top: 1rem;
  padding: 0.75rem 1.5rem;
  background: #4CAF50;
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
  font-weight: bold;
  transition: background 0.2s;
}

.add-user-btn:hover {
  background: #388e3c;
}

button {
  margin-right: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  background: #1976d2;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

button.danger {
  background: #e53935;
}

button:hover {
  background: #1565c0;
}

button.danger:hover {
  background: #b71c1c;
}

.loading, .error-message {
  text-align: center;
  padding: 2rem;
}

.error-message {
  color: #e53935;
}
</style>
