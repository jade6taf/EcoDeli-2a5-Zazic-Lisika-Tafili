<script>
import PrestatairesList from '@/views/client/PrestatairesList.vue';

export default {
  name: 'ServicesTypesView',
  components: {
    PrestatairesList
  },
  data() {
    return {
      typesServices: [],
      selectedService: null,
      selectedPrestataire: null,
      error: null
    };
  },
  async created() {
    try {
      this.typesServices = [
        { code: 'TRANSPORT_PERSONNES', libelle: 'Transport de personnes' },
        { code: 'ACHAT_ETRANGER', libelle: 'Achat à l\'étranger' },
        { code: 'GARDE_ANIMAUX', libelle: 'Garde d\'animaux' },
        { code: 'LIVRAISON_COURSES', libelle: 'Livraison de courses' },
        { code: 'PETITS_TRAVAUX', libelle: 'Petits travaux' }
      ];
    } catch (error) {
      this.error = 'Impossible de charger les services disponibles.';
    }
  },
  methods: {
    selectService(typeServiceCode) {
      this.selectedService = typeServiceCode;
      this.selectedPrestataire = null;
    },
    getServiceIcon(typeServiceCode) {
      const iconMap = {
        'TRANSPORT_PERSONNES': 'fas fa-car',
        'ACHAT_ETRANGER': 'fas fa-globe',
        'GARDE_ANIMAUX': 'fas fa-paw',
        'LIVRAISON_COURSES': 'fas fa-shopping-basket',
        'PETITS_TRAVAUX': 'fas fa-tools'
      };
      return iconMap[typeServiceCode] || 'fas fa-question';
    },
    handlePrestataireSelected(prestataire) {
      this.selectedPrestataire = prestataire;
      // Ici vous pouvez ajouter la logique pour gérer la sélection du prestataire
      // Par exemple, rediriger vers une page de réservation
    }
  }
};
</script>

<template>
  <div class="services-types-container">
    <h1>Nos services disponibles</h1>
    <p class="description">
      Choisissez le service qui vous intéresse parmi notre sélection de services proposés par nos prestataires
    </p>

    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
    </div>

    <div class="services-grid" v-if="typesServices.length > 0 && !selectedService">
      <div
        v-for="(type, index) in typesServices"
        :key="index"
        class="service-card"
        @click="selectService(type.code)"
      >
        <div class="service-icon">
          <i :class="getServiceIcon(type.code)" class="service-icon-fa"></i>
        </div>
        <h3>{{ type.libelle }}</h3>
        <p>Cliquez pour découvrir nos prestataires</p>
      </div>
    </div>

    <div v-else-if="!error && !typesServices.length" class="loading">
      <p>Chargement des services disponibles...</p>
    </div>

    <div v-if="selectedService" class="selected-service">
      <button class="back-button" @click="selectedService = null">
        <i class="fas fa-arrow-left"></i> Retour aux services
      </button>
      <h2>{{ typesServices.find(t => t.code === selectedService)?.libelle }}</h2>
      <PrestatairesList 
        :serviceType="selectedService"
        @prestataire-selected="handlePrestataireSelected"
      />
    </div>
  </div>
</template>

<style scoped>
.services-types-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

h1, h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.description {
  text-align: center;
  color: #666;
  margin-bottom: 2.5rem;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 2rem;
}

.service-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.service-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.service-icon {
  margin-bottom: 1rem;
  color: #4CAF50;
}

.service-card h3 {
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.service-card p {
  color: #666;
  font-size: 0.9rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error-message {
  text-align: center;
  padding: 2rem;
  color: #e53935;
  background-color: #ffebee;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.service-icon-fa {
  font-size: 2em;
}

.back-button {
  background: none;
  border: none;
  color: #4CAF50;
  cursor: pointer;
  padding: 0.5rem 1rem;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.back-button:hover {
  color: #388E3C;
}

.selected-service {
  margin-top: 2rem;
}
</style>