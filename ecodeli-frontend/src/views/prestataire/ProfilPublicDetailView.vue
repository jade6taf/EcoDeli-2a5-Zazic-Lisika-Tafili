<script>
export default {
  name: 'ProfilPublicDetailView',
  data() {
    return {
      prestataire: null,
      evaluations: [],
      portfolio: [],
      servicesPropposes: [],
      isLoading: true,
      error: null,
      categories: [
        { code: 'TRANSPORT_LIVRAISON', libelle: 'TRANSPORT & LIVRAISON' },
        { code: 'SERVICES_DOMICILE', libelle: 'SERVICES À DOMICILE' },
        { code: 'TRAVAUX_REPARATIONS', libelle: 'TRAVAUX & RÉPARATIONS' },
        { code: 'COURSES_ACHATS', libelle: 'COURSES & ACHATS' },
        { code: 'SERVICES_PERSONNELS', libelle: 'SERVICES PERSONNELS' },
        { code: 'EDUCATION_FORMATION', libelle: 'ÉDUCATION & FORMATION' }
      ]
    }
  },
  computed: {
    tauxReussite() {
      if (!this.prestataire?.nombreServices || this.prestataire.nombreServices === 0) return 0;
      const servicesReussis = this.prestataire.servicesReussis || this.prestataire.nombreServices;
      return Math.round((servicesReussis / this.prestataire.nombreServices) * 100);
    }
  },
  async mounted() {
    const prestataireId = this.$route.params.id;
    if (prestataireId) {
      await this.loadPrestataire(prestataireId);
    } else {
      this.error = 'ID prestataire manquant';
      this.isLoading = false;
    }
  },
  methods: {
    async loadPrestataire(prestataireId) {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/prestataires/${prestataireId}/profil-public`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const data = await response.json();
          this.prestataire = data.prestataire;
          this.evaluations = data.evaluations || [];
          this.portfolio = data.portfolio || [];
          this.servicesPropposes = data.services || this.generateDefaultServices();
        } else {
          await this.loadPrestataireBasic(prestataireId);
        }
      } catch (error) {
        console.error('Erreur:', error);
        this.error = 'Erreur lors du chargement du profil';
      } finally {
        this.isLoading = false;
      }
    },

    async loadPrestataireBasic(prestataireId) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/utilisateurs/${prestataireId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          this.prestataire = await response.json();
          // Générer des données d'exemple pour la démo
          this.generateMockData();
        } else {
          this.error = 'Prestataire non trouvé';
        }
      } catch (error) {
        console.error('Erreur:', error);
        this.error = 'Erreur de connexion';
      }
    },

    generateMockData() {
      this.evaluations = [
        {
          id: 1,
          clientNom: 'Marie D.',
          note: 5,
          commentaire: 'Excellent travail, très professionnel et ponctuel. Je recommande vivement !',
          typeService: 'Nettoyage à domicile',
          dateEvaluation: '2024-01-15'
        },
        {
          id: 2,
          clientNom: 'Pierre M.',
          note: 4,
          commentaire: 'Bonne prestation, délais respectés. Quelques petits détails à améliorer.',
          typeService: 'Réparation plomberie',
          dateEvaluation: '2024-01-10'
        },
        {
          id: 3,
          clientNom: 'Sophie L.',
          note: 5,
          commentaire: 'Parfait ! Travail soigné et tarif très correct.',
          typeService: 'Jardinage',
          dateEvaluation: '2024-01-05'
        }
      ];
      this.servicesPropposes = this.generateDefaultServices();
    },

    generateDefaultServices() {
      return [
        {
          id: 1,
          nom: 'Nettoyage domicile',
          categorie: 'SERVICES_DOMICILE',
          prixMoyen: 25
        },
        {
          id: 2,
          nom: 'Petites réparations',
          categorie: 'TRAVAUX_REPARATIONS',
          prixMoyen: 40
        },
        {
          id: 3,
          nom: 'Jardinage',
          categorie: 'SERVICES_DOMICILE',
          prixMoyen: 30
        }
      ];
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
    },

    getServiceIcon(categorie) {
      const icons = {
        'TRANSPORT_LIVRAISON': 'fas fa-truck',
        'SERVICES_DOMICILE': 'fas fa-home',
        'TRAVAUX_REPARATIONS': 'fas fa-tools',
        'COURSES_ACHATS': 'fas fa-shopping-cart',
        'SERVICES_PERSONNELS': 'fas fa-user-tie',
        'EDUCATION_FORMATION': 'fas fa-graduation-cap'
      };
      return icons[categorie] || 'fas fa-concierge-bell';
    },

    getDisponibiliteClass(disponibilite) {
      const classes = {
        'DISPONIBLE': 'available',
        'OCCUPE': 'busy',
        'INDISPONIBLE': 'unavailable'
      };
      return classes[disponibilite] || 'unknown';
    },

    getDisponibiliteLabel(disponibilite) {
      const labels = {
        'DISPONIBLE': 'Disponible',
        'OCCUPE': 'Occupé',
        'INDISPONIBLE': 'Indisponible'
      };
      return labels[disponibilite] || 'Non spécifiée';
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    },

    goBack() {
      this.$router.go(-1);
    },

    contacterPrestataire() {
      if (this.prestataire?.telephone) {
        window.open(`tel:${this.prestataire.telephone}`);
      } else {
        alert('Numéro de téléphone non disponible');
      }
    },

    envoyerMessage() {
      if (this.prestataire?.email) {
        window.open(`mailto:${this.prestataire.email}?subject=Demande de contact via EcoDeli`);
      } else {
        alert('Email non disponible');
      }
    },

    demanderDevis() {
      this.$router.push({
        path: '/client/demandes-services/new',
        query: { prestataire: this.prestataire.idUtilisateur }
      });
    },

    ouvrirGalerie(realisation) {
      // Ouvrir une galerie d'images (à implémenter)
      alert(`Ouverture de la galerie pour : ${realisation.titre}`);
    }
  }
}
</script>

<template>
  <div class="profil-public-container">
    <div class="page-header">
      <button @click="goBack" class="btn btn-secondary">
        <i class="fas fa-arrow-left"></i> Retour
      </button>
      <h1>Profil prestataire</h1>
      <div class="header-actions">
        <button @click="contacterPrestataire" class="btn btn-primary">
          <i class="fas fa-phone"></i> Contacter
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else-if="prestataire" class="profil-content">
      <div class="prestataire-header-card">
        <div class="prestataire-avatar-section">
          <div class="prestataire-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="prestataire-verified" v-if="prestataire.estVerifie">
            <i class="fas fa-check-circle"></i>
            <span>Prestataire vérifié</span>
          </div>
        </div>

        <div class="prestataire-info">
          <h2>{{ prestataire.prenom }} {{ prestataire.nom }}</h2>
          <div class="prestataire-meta">
            <div v-if="prestataire.specialite" class="meta-item">
              <i class="fas fa-tools"></i>
              <span>{{ prestataire.specialite }}</span>
            </div>
            <div v-if="prestataire.ville" class="meta-item">
              <i class="fas fa-map-marker-alt"></i>
              <span>{{ prestataire.ville }}</span>
            </div>
            <div v-if="prestataire.anneesExperience" class="meta-item">
              <i class="fas fa-calendar"></i>
              <span>{{ prestataire.anneesExperience }} années d'expérience</span>
            </div>
          </div>

          <div class="prestataire-description" v-if="prestataire.description">
            <p>{{ prestataire.description }}</p>
          </div>
        </div>

        <div class="prestataire-stats">
          <div class="stat-card">
            <div class="stat-number">{{ prestataire.noteMoyenne || 'N/A' }}</div>
            <div class="stat-label">Note moyenne</div>
            <div class="stat-stars" v-if="prestataire.noteMoyenne">
              <i v-for="n in 5" :key="n" class="fas fa-star" :class="{ 'filled': n <= Math.round(prestataire.noteMoyenne) }"></i>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-number">{{ prestataire.nombreServices || 0 }}</div>
            <div class="stat-label">Services réalisés</div>
          </div>

          <div class="stat-card">
            <div class="stat-number">{{ tauxReussite }}%</div>
            <div class="stat-label">Taux de réussite</div>
          </div>
        </div>
      </div>

      <div class="services-section">
        <h3><i class="fas fa-concierge-bell"></i> Services proposés</h3>
        <div class="services-grid">
          <div
            v-for="service in servicesPropposes"
            :key="service.id"
            class="service-card"
          >
            <div class="service-icon">
              <i :class="getServiceIcon(service.categorie)"></i>
            </div>
            <div class="service-info">
              <div class="service-name">{{ service.nom }}</div>
              <div class="service-category">{{ getCategoryLabel(service.categorie) }}</div>
              <div class="service-price" v-if="service.prixMoyen">
                À partir de {{ service.prixMoyen }}€
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="availability-section">
        <h3><i class="fas fa-calendar-check"></i> Disponibilité</h3>
        <div class="availability-info">
          <div class="availability-item">
            <div class="availability-label">Délai moyen de réponse :</div>
            <div class="availability-value">{{ prestataire.delaiReponse || 'Non spécifié' }}</div>
          </div>
          <div class="availability-item">
            <div class="availability-label">Zone d'intervention :</div>
            <div class="availability-value">{{ prestataire.zoneIntervention || prestataire.ville || 'Non spécifiée' }}</div>
          </div>
          <div class="availability-item">
            <div class="availability-label">Disponibilité actuelle :</div>
            <div class="availability-value">
              <span class="availability-status" :class="getDisponibiliteClass(prestataire.disponibilite)">
                {{ getDisponibiliteLabel(prestataire.disponibilite) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="reviews-section" v-if="evaluations.length > 0">
        <h3><i class="fas fa-star"></i> Avis clients ({{ evaluations.length }})</h3>
        <div class="reviews-list">
          <div
            v-for="evaluation in evaluations"
            :key="evaluation.id"
            class="review-card"
          >
            <div class="review-header">
              <div class="review-client">
                <div class="client-avatar">
                  <i class="fas fa-user"></i>
                </div>
                <div class="client-info">
                  <div class="client-name">{{ evaluation.clientNom }}</div>
                  <div class="review-date">{{ formatDate(evaluation.dateEvaluation) }}</div>
                </div>
              </div>
              <div class="review-rating">
                <div class="stars">
                  <i v-for="n in 5" :key="n" class="fas fa-star" :class="{ 'filled': n <= evaluation.note }"></i>
                </div>
                <span class="rating-text">{{ evaluation.note }}/5</span>
              </div>
            </div>

            <div class="review-content">
              <div class="service-type">{{ evaluation.typeService }}</div>
              <p class="review-text">{{ evaluation.commentaire }}</p>
            </div>
          </div>
        </div>
      </div>


      <div class="certifications-section" v-if="prestataire.certifications && prestataire.certifications.length > 0">
        <h3><i class="fas fa-certificate"></i> Certifications et compétences</h3>
        <div class="certifications-grid">
          <div
            v-for="certification in prestataire.certifications"
            :key="certification.id"
            class="certification-card"
          >
            <div class="certification-icon">
              <i class="fas fa-award"></i>
            </div>
            <div class="certification-info">
              <div class="certification-name">{{ certification.nom }}</div>
              <div class="certification-organisme">{{ certification.organisme }}</div>
              <div class="certification-date" v-if="certification.dateObtention">
                Obtenue le {{ formatDate(certification.dateObtention) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="portfolio-section" v-if="portfolio.length > 0">
        <h3><i class="fas fa-images"></i> Réalisations</h3>
        <div class="portfolio-grid">
          <div
            v-for="realisation in portfolio"
            :key="realisation.id"
            class="portfolio-item"
            @click="ouvrirGalerie(realisation)"
          >
            <div class="portfolio-image">
              <img :src="realisation.image" :alt="realisation.titre" />
              <div class="portfolio-overlay">
                <i class="fas fa-eye"></i>
              </div>
            </div>
            <div class="portfolio-info">
              <div class="portfolio-title">{{ realisation.titre }}</div>
              <div class="portfolio-description">{{ realisation.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="contact-section">
        <h3><i class="fas fa-handshake"></i> Collaborer avec {{ prestataire.prenom }}</h3>
        <div class="contact-actions">
          <button @click="contacterPrestataire" class="contact-btn primary">
            <i class="fas fa-phone"></i>
            <div class="btn-content">
              <div class="btn-title">Appeler directement</div>
              <div class="btn-description">Discutez de vos besoins</div>
            </div>
          </button>

          <button @click="envoyerMessage" class="contact-btn secondary">
            <i class="fas fa-envelope"></i>
            <div class="btn-content">
              <div class="btn-title">Envoyer un message</div>
              <div class="btn-description">Posez vos questions</div>
            </div>
          </button>

          <button @click="demanderDevis" class="contact-btn success">
            <i class="fas fa-file-invoice"></i>
            <div class="btn-content">
              <div class="btn-title">Demander un devis</div>
              <div class="btn-description">Obtenez une estimation</div>
            </div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profil-public-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.page-header h1 {
  color: var(--text-primary);
  margin: 0;
  flex: 1;
  text-align: center;
}

.loading, .error-message {
  text-align: center;
  padding: 3rem;
  color: var(--text-primary);
}

.error-message {
  color: var(--error-color);
  background-color: var(--error-bg);
  border-radius: 8px;
}

.profil-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.prestataire-header-card {
  background-color: var(--card-bg);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 2rem;
  align-items: start;
}

.prestataire-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.prestataire-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: var(--primary-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 3rem;
}

.prestataire-verified {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--success-color);
  font-size: 0.9rem;
  font-weight: 500;
}

.prestataire-info h2 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  font-size: 2rem;
}

.prestataire-meta {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
}

.meta-item i {
  color: var(--primary-color);
  width: 16px;
}

.prestataire-description p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.prestataire-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stat-card {
  text-align: center;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 8px;
  min-width: 120px;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: 0.25rem;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.stat-stars {
  display: flex;
  justify-content: center;
  gap: 0.25rem;
}

.stat-stars i {
  color: #ddd;
  font-size: 0.9rem;
}

.stat-stars i.filled {
  color: var(--warning-color);
}

.services-section, .availability-section, .reviews-section, .certifications-section, .portfolio-section, .contact-section {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.services-section h3, .availability-section h3, .reviews-section h3, .certifications-section h3, .portfolio-section h3, .contact-section h3 {
  color: var(--text-primary);
  margin: 0 0 1.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.services-section h3 i, .availability-section h3 i, .reviews-section h3 i, .certifications-section h3 i, .portfolio-section h3 i, .contact-section h3 i {
  color: var(--primary-color);
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.service-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.service-card:hover {
  transform: translateY(-2px);
}

.service-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: var(--primary-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 1.2rem;
}

.service-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.service-category {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.service-price {
  color: var(--success-color);
  font-weight: 500;
  font-size: 0.9rem;
}

.availability-info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.availability-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background-color: var(--background-color);
  border-radius: 6px;
}

.availability-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.availability-value {
  color: var(--text-primary);
}

.availability-status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.availability-status.available {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.availability-status.busy {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

.availability-status.unavailable {
  background-color: var(--error-color-light);
  color: var(--error-color);
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.review-card {
  padding: 1.5rem;
  background-color: var(--background-color);
  border-radius: 8px;
  border-left: 4px solid var(--primary-color);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.review-client {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.client-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--primary-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.client-name {
  font-weight: 500;
  color: var(--text-primary);
}

.review-date {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stars {
  display: flex;
  gap: 0.25rem;
}

.stars i {
  color: #ddd;
}

.stars i.filled {
  color: var(--warning-color);
}

.rating-text {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

.review-content {
  margin-top: 1rem;
}

.service-type {
  color: var(--primary-color);
  font-weight: 500;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.review-text {
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
}

.certifications-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.certification-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 8px;
}

.certification-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: var(--warning-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--warning-color);
  font-size: 1.2rem;
}

.certification-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.certification-organisme {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.certification-date {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.portfolio-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--background-color);
  transition: transform 0.3s ease;
}

.portfolio-item:hover {
  transform: translateY(-2px);
}

.portfolio-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.portfolio-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.portfolio-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.portfolio-item:hover .portfolio-overlay {
  opacity: 1;
}

.portfolio-overlay i {
  color: white;
  font-size: 2rem;
}

.portfolio-info {
  padding: 1rem;
}

.portfolio-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.portfolio-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.4;
}

.contact-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.contact-btn {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: left;
}

.contact-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.contact-btn.primary {
  background-color: var(--primary-color);
  color: white;
}

.contact-btn.secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.contact-btn.success {
  background-color: var(--success-color);
  color: white;
}

.contact-btn i {
  font-size: 1.5rem;
}

.btn-title {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.btn-description {
  font-size: 0.9rem;
  opacity: 0.9;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background-color: var(--primary-color-dark);
}

.btn-secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background-color: var(--text-secondary);
}

@media (max-width: 768px) {
  .profil-public-container {
    padding: 1rem;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .page-header h1 {
    text-align: left;
  }
  
  .prestataire-header-card {
    grid-template-columns: 1fr;
    text-align: center;
    gap: 1.5rem;
  }
  
  .prestataire-stats {
    flex-direction: row;
    justify-content: space-around;
  }
  
  .services-grid {
    grid-template-columns: 1fr;
  }
  
  .availability-item {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }
  
  .review-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .certifications-grid {
    grid-template-columns: 1fr;
  }
  
  .portfolio-grid {
    grid-template-columns: 1fr;
  }
  
  .contact-actions {
    grid-template-columns: 1fr;
  }
}
</style>
