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
        disponible: false
      }
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
    
    <div class="preview-card">
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

      <div class="preview-description">
        <h4>Description</h4>
        <p>{{ prestataire.description || 'Aucune description' }}</p>
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

.preview-description h4 {
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.preview-description p {
  color: #666;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .preview-card {
    padding: 1rem;
  }
}
</style>
