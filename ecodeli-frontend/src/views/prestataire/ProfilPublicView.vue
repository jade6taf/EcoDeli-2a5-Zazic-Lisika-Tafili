<script>
import apiServices from '@/services/apiServices';

export default {
  name: 'ProfilPublicView',
  data() {
    return {
      servicesTypes: [
        { code: 'TRANSPORT_PERSONNES', libelle: 'Transport de personnes' },
        { code: 'ACHAT_ETRANGER', libelle: 'Achat à l\'étranger' },
        { code: 'GARDE_ANIMAUX', libelle: 'Garde d\'animaux' },
        { code: 'LIVRAISON_COURSES', libelle: 'Livraison de courses' },
        { code: 'PETITS_TRAVAUX', libelle: 'Petits travaux' }
      ],
      prestataire: {
        nomEntreprise: '',
        domaineExpertise: '',
        zoneIntervention: '',
        tarifHoraire: 0,
        description: '',
        disponible: false,
        imageUrl: ''
      },
      saving: false,
      message: '',
      messageType: '',
      editing: false
    }
  },
  methods: {
    async loadServicesTypes() {
      try {
        const response = await apiServices.get('prestataires/services-types');
        const data = await response.json();
        this.servicesTypes = data;
      } catch (error) {
        console.error('Erreur lors du chargement des types de services:', error);
      }
    },
    async loadPrestataireData() {
      try {
        const response = await apiServices.get('prestataires/profile');
        const data = await response.json();
        this.prestataire = data;
      } catch (error) {
        console.error('Erreur lors du chargement du profil:', error);
      }
    },
    getServiceLibelle(code) {
      const service = this.servicesTypes.find(s => s.code === code);
      return service ? service.libelle : code;
    },
    toggleEdit() {
      this.editing = !this.editing;
    },
    async saveProfile() {
      this.saving = true;
      this.message = '';
      try {
        const response = await apiServices.put('prestataires/profile', this.prestataire);
        if (response.ok) {
          const updatedPrestataire = await response.json();
          this.prestataire = updatedPrestataire;
          this.message = 'Profil mis à jour avec succès';
          this.messageType = 'success';
          this.editing = false;
        } else {
          const error = await response.json();
          this.message = error.message || 'Une erreur est survenue lors de la mise à jour';
          this.messageType = 'error';
        }
      } catch (error) {
        this.message = 'Une erreur est survenue lors de la communication avec le serveur';
        this.messageType = 'error';
        console.error('Erreur lors de la sauvegarde du profil:', error);
      } finally {
        this.saving = false;
      }
    },
    cancelEdit() {
      this.loadPrestataireData();
      this.editing = false;
    }
  },
  async created() {
    await Promise.all([
      this.loadPrestataireData(),
      this.loadServicesTypes()
    ]);
  }
}
</script>

<template>
  <div class="profile-container">
    <h1>Profil public</h1>

    <div v-if="message" :class="['alert', messageType === 'success' ? 'alert-success' : 'alert-error']">
      {{ message }}
    </div>

    <div class="action-buttons">
      <button v-if="!editing" class="btn-edit" @click="toggleEdit">Modifier mon profil</button>
      <template v-else>
        <button class="btn-save" @click="saveProfile" :disabled="saving">
          {{ saving ? 'Enregistrement...' : 'Enregistrer' }}
        </button>
        <button class="btn-cancel" @click="cancelEdit">Annuler</button>
      </template>
    </div>

    <div v-if="!editing" class="preview-card">
      <div class="preview-header">
        <h3>{{ prestataire.nomEntreprise }}</h3>
        <span :class="{ 'status-available': prestataire.disponible, 'status-unavailable': !prestataire.disponible }">
          {{ prestataire.disponible ? 'Disponible' : 'Non disponible' }}
        </span>
      </div>

      <div class="preview-info">
        <p><i class="fas fa-briefcase"></i> {{ getServiceLibelle(prestataire.domaineExpertise) || 'Non spécifié' }}</p>
        <p><i class="fas fa-map-marker-alt"></i> {{ prestataire.zoneIntervention || 'Non spécifiée' }}</p>
        <p><i class="fas fa-euro-sign"></i> {{ prestataire.tarifHoraire || '0' }}€/h</p>
      </div>

      <div v-if="prestataire.imageUrl" class="preview-image">
        <img :src="prestataire.imageUrl" alt="Image de profil" />
      </div>

      <div class="preview-description">
        <h4>Description</h4>
        <p>{{ prestataire.description || 'Aucune description' }}</p>
      </div>
    </div>

    <div v-else class="edit-form">
      <div class="form-group">
        <label for="domaine">Domaine d'expertise</label>
        <select id="domaine" v-model="prestataire.domaineExpertise">
          <option v-for="service in servicesTypes" :key="service.code" :value="service.code">
            {{ service.libelle }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="zone">Zone d'intervention</label>
        <input type="text" id="zone" v-model="prestataire.zoneIntervention" placeholder="Ex: Paris, Île-de-France">
      </div>

      <div class="form-group">
        <label for="tarif">Tarif horaire (€)</label>
        <input type="number" id="tarif" v-model="prestataire.tarifHoraire" min="0" step="0.01">
      </div>

      <div class="form-group">
        <label for="imageUrl">URL de votre image de profil</label>
        <input type="text" id="imageUrl" v-model="prestataire.imageUrl" placeholder="https://...">
      </div>

      <div class="form-group">
        <label for="description">Description de vos services</label>
        <textarea id="description" v-model="prestataire.description" rows="5" placeholder="Décrivez vos services et votre expertise..."></textarea>
      </div>

      <div class="form-group checkbox-group">
        <label>
          <input type="checkbox" v-model="prestataire.disponible">
          Je suis disponible pour de nouvelles missions
        </label>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

h1 {
  margin-bottom: 2rem;
  color: #333;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn-edit, .btn-save, .btn-cancel {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  border: none;
}

.btn-edit, .btn-save {
  background-color: #4CAF50;
  color: white;
}

.btn-edit:hover, .btn-save:hover {
  background-color: #3e8e41;
}

.btn-cancel {
  background-color: #f44336;
  color: white;
}

.btn-cancel:hover {
  background-color: #d32f2f;
}

.alert {
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.alert-success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.alert-error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.preview-card {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.preview-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.25rem;
}

.status-available {
  color: #2e7d32;
  background-color: #e8f5e9;
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.status-unavailable {
  color: #c62828;
  background-color: #ffebee;
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.preview-info {
  margin-bottom: 1.5rem;
}

.preview-info p {
  margin: 0.5rem 0;
  color: #666;
}

.preview-info i {
  width: 20px;
  margin-right: 0.5rem;
  color: #4CAF50;
}

.preview-image {
  margin-bottom: 1.5rem;
}

.preview-image img {
  max-width: 100%;
  border-radius: 6px;
}

.preview-description h4 {
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.preview-description p {
  color: #666;
  line-height: 1.5;
}

.edit-form {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

input[type="text"],
input[type="number"],
select,
textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.checkbox-group {
  display: flex;
  align-items: center;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-group input[type="checkbox"] {
  margin-right: 0.5rem;
}

@media (max-width: 768px) {
  .preview-card, .edit-form {
    padding: 1rem;
  }
}
</style>
