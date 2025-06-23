<script>
import apiServices from '@/services/apiServices';
import { authStore } from '@/store/auth';

export default {
  name: 'InformationsPersonnelles',
  data() {
    return {
      userData: {
        email: '',
        nom: '',
        prenom: '',
        telephone: '',
        nomEntreprise: '',
        siret: ''
      }
    }
  },
  methods: {
    async loadUserData() {
      try {
        if (!authStore.isAuthenticated) {
          this.$router.push('/login');
          return;
        }
        const token = localStorage.getItem('token');
        if (!token) {
          throw new Error('Token non trouvé');
        }
        const response = await fetch('/api/prestataires/profile', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Erreur ${response.status}: ${errorText || response.statusText}`);
        }
        const data = await response.json();
        this.userData = {
          email: data.email || '',
          nom: data.nom || '',
          prenom: data.prenom || '',
          telephone: data.telephone || '',
          nomEntreprise: data.nomEntreprise || '',
          siret: data.siret || ''
        };
      } catch (error) {
        if (error.message.includes('session') || error.message.includes('reconnecter')) {
          authStore.clearAuth();
          this.$router.push('/login');
        }
      }
    }
  },
  created() {
    if (!authStore.isAuthenticated) {
      this.$router.push('/login');
      return;
    }
    this.loadUserData();
  }
}
</script>

<template>
  <div class="informations-container">
    <h1>Informations personnelles</h1>

    <div class="informations-card">
      <!-- Informations personnelles -->
      <div class="section">
        <h2>Informations utilisateur</h2>
        <div class="info-group">
          <label>Email</label>
          <p>{{ userData.email }}</p>
        </div>
        <div class="info-group">
          <label>Nom</label>
          <p>{{ userData.nom }}</p>
        </div>
        <div class="info-group">
          <label>Prénom</label>
          <p>{{ userData.prenom }}</p>
        </div>
        <div class="info-group">
          <label>Téléphone</label>
          <p>{{ userData.telephone }}</p>
        </div>
      </div>

      <!-- Informations entreprise -->
      <div class="section">
        <h2>Informations entreprise</h2>
        <div class="info-group">
          <label>Nom de l'entreprise</label>
          <p>{{ userData.nomEntreprise }}</p>
        </div>
        <div class="info-group">
          <label>SIRET</label>
          <p>{{ userData.siret }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.informations-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.informations-card {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

h1 {
  margin-bottom: 2rem;
  color: #333;
  font-size: 1.75rem;
}

h2 {
  color: #4CAF50;
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
}

.info-group {
  margin-bottom: 1.25rem;
  display: flex;
  flex-direction: column;
}

.info-group:last-child {
  margin-bottom: 0;
}

label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

p {
  color: #333;
  font-size: 1rem;
  margin: 0;
  padding: 0.5rem;
  background-color: #f8f9fa;
  border-radius: 4px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .informations-card {
    padding: 1rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  h2 {
    font-size: 1.1rem;
  }
}
</style>
