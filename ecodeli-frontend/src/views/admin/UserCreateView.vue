<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
    name: 'AdminUserCreate',
    setup() {
      const router = useRouter()
      const user = ref({
        nom: '',
        prenom: '',
        email: '',
        motDePasse: '',
        type: 'CLIENT',
        telephone: '',
        adresse: '',
        ville: '',
        codePostal: '',
        pays: ''
      })
      const isLoading = ref(false)
      const error = ref(null)

      const createUser = async () => {
          isLoading.value = true
          error.value = null
          try {
            const token = localStorage.getItem('token')
            const res = await fetch('/api/admin/utilisateurs', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
              },
              body: JSON.stringify({
                ...user.value,
                type: user.value.type
              })
            })
            if (!res.ok) {
              const data = await res.json()
              throw new Error(data.message || 'Erreur lors de la création')
            }
            router.push('/admin/utilisateurs')
          } catch (e) {
            error.value = e.message
          } finally {
            isLoading.value = false
          }
      }
      return { user, isLoading, error, createUser }
    }
}
</script>

<template>
  <div class="dashboard-container">
    <h1>Créer un utilisateur</h1>
    <form @submit.prevent="createUser" class="user-form">

      <div class="form-row">
        <label>Type</label>
        <select v-model="user.type">
          <option value="CLIENT">Client</option>
          <option value="LIVREUR">Livreur</option>
          <option value="COMMERCANT">Commerçant</option>
          <option value="PRESTATAIRE">Prestataire</option>
          <option value="ADMIN">Admin</option>
        </select>
      </div>

      <div class="form-row">
        <label>Nom</label>
        <input v-model="user.nom" required />
      </div>
      <div class="form-row">
        <label>Prénom</label>
        <input v-model="user.prenom" required />
      </div>
      <div class="form-row">
        <label>Email</label>
        <input v-model="user.email" type="email" required />
      </div>
      <div class="form-row">
        <label>Mot de passe</label>
        <input v-model="user.motDePasse" type="password" required minlength="8" />
      </div>
      <div class="form-row">
        <label>Téléphone</label>
        <input v-model="user.telephone" />
      </div>
      <div class="form-row">
        <label>Adresse</label>
        <input v-model="user.adresse" />
      </div>
      <div class="form-row">
        <label>Ville</label>
        <input v-model="user.ville" />
      </div>
      <div class="form-row">
        <label>Code postal</label>
        <input v-model="user.codePostal" />
      </div>
      <div class="form-row">
        <label>Pays</label>
        <input v-model="user.pays" />
      </div>

      <div class="form-actions">
        <button type="submit" :disabled="isLoading">
          Créer
        </button>
        <router-link to="/admin/utilisateurs" class="cancel-btn">
          Annuler
        </router-link>
      </div>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </form>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
}

.user-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: flex;
  flex-direction: column;
}

.form-row label {
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

button[type="submit"] {
  background: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.75rem 1.5rem;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
}

button[type="submit"]:hover {
  background: #388e3c;
}

.cancel-btn {
  color: #1976d2;
  text-decoration: none;
  font-weight: bold;
}

.cancel-btn:hover {
  text-decoration: underline;
}

.error-message {
  color: #e53935;
  margin-top: 1rem;
  text-align: center;
}
</style>
