<script>
  export default {
    name: 'ServicesTypesView',
    data() {
      return {
        typesServices: [],
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
        this.$router.push({
          name: 'prestataires-service',
          params: { typeService: typeServiceCode }
        });
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

      <div class="services-grid" v-if="typesServices.length > 0">
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

      <div v-else-if="!error" class="loading">
        <p>Chargement des services disponibles...</p>
      </div>
    </div>
  </template>

<style scoped>
  .services-types-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }

  h1 {
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
</style>