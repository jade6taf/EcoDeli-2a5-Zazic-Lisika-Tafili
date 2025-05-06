<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-info">
        <h1>{{ prestataire.nomEntreprise }}</h1>
        <div class="rating-container">
          <div class="rating">
            <i v-for="n in 5" :key="n"
               class="fas fa-star"
               :class="{ 'filled': n <= Math.round(averageRating) }">
            </i>
          </div>
          <span class="rating-value">{{ averageRating }}/5</span>
          <span class="review-count">({{ prestataire.evaluations.length }} avis)</span>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <div class="main-content">
        <!-- Informations principales -->
        <section class="info-section">
          <div class="info-grid">
            <div class="info-item">
              <i class="fas fa-briefcase"></i>
              <h3>Domaine d'expertise</h3>
              <p>{{ getServiceLibelle(prestataire.domaineExpertise) }}</p>
            </div>
            <div class="info-item">
              <i class="fas fa-map-marker-alt"></i>
              <h3>Zone d'intervention</h3>
              <p>{{ prestataire.zoneIntervention }}</p>
            </div>
            <div class="info-item">
              <i class="fas fa-euro-sign"></i>
              <h3>Tarif horaire</h3>
              <p>{{ prestataire.tarifHoraire }}€/h</p>
            </div>
            <div class="info-item">
              <i class="fas fa-clock"></i>
              <h3>Statut</h3>
              <p :class="{ 'status-available': prestataire.disponible, 'status-unavailable': !prestataire.disponible }">
                {{ prestataire.disponible ? 'Disponible' : 'Non disponible' }}
              </p>
            </div>
          </div>
        </section>

        <!-- Description -->
        <section class="description-section">
          <h2>À propos</h2>
          <p>{{ prestataire.description || 'Aucune description disponible.' }}</p>
        </section>

        <!-- Avis -->
        <section class="reviews-section">
          <h2>Avis clients</h2>
          <div v-if="prestataire.evaluations.length" class="reviews-list">
            <div v-for="evaluation in evaluations" :key="evaluation.idEvaluation" class="review-card">
              <div class="review-header">
                <div class="review-user">
                  <i class="fas fa-user-circle"></i>
                  <span class="user-name">{{ evaluation.utilisateur?.nom || 'Anonyme' }}</span>
                </div>
                <div class="review-rating">
                  <div class="stars">
                    <i v-for="n in 5" :key="n"
                       class="fas fa-star"
                       :class="{ 'filled': n <= evaluation.note }">
                    </i>
                  </div>
                  <span class="review-date">{{ formatDate(evaluation.date) }}</span>
                </div>
              </div>
              <p class="review-text">{{ evaluation.commentaire }}</p>
            </div>
            <div v-if="hasMoreReviews" class="load-more">
              <button @click="loadMoreReviews" class="btn-secondary">
                Voir plus d'avis
              </button>
            </div>
          </div>
          <p v-else class="no-reviews">Aucun avis pour le moment</p>
        </section>
      </div>

      <div class="sidebar">
        <div class="contact-card">
          <h3>Contacter ce prestataire</h3>
          <form @submit.prevent="sendMessage" class="contact-form">
            <div class="form-group">
              <label for="message">Message</label>
              <textarea
                id="message"
                v-model="messageForm.contenu"
                rows="4"
                placeholder="Décrivez votre besoin..."
                required
              ></textarea>
            </div>
            <button type="submit" class="btn-primary" :disabled="sending">
              {{ sending ? 'Envoi...' : 'Envoyer un message' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'VoirProfilPublicView',
  data() {
    return {
      servicesTypes: [],
      prestataire: {
        nomEntreprise: '',
        domaineExpertise: '',
        zoneIntervention: '',
        tarifHoraire: 0,
        disponible: false,
        description: '',
        evaluations: []
      },
      page: 1,
      messageForm: {
        contenu: ''
      },
      sending: false
    }
  },
  computed: {
    averageRating() {
      if (!this.prestataire.evaluations.length) return 0;
      const sum = this.prestataire.evaluations.reduce((acc, eval) => acc + eval.note, 0);
      return (sum / this.prestataire.evaluations.length).toFixed(1);
    },
    evaluations() {
      return this.prestataire.evaluations
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .slice(0, this.page * 5);
    },
    hasMoreReviews() {
      return this.prestataire.evaluations.length > this.page * 5;
    }
  },
  methods: {
    async loadPrestataireData() {
      try {
        const response = await fetch(`/api/prestataires/${this.$route.params.id}`);
        const data = await response.json();
        this.prestataire = data;
      } catch (error) {
        console.error('Erreur lors du chargement du profil:', error);
      }
    },
    async loadServicesTypes() {
      try {
        const response = await fetch('/api/prestataires/services-types');
        const data = await response.json();
        this.servicesTypes = data;
      } catch (error) {
        console.error('Erreur lors du chargement des types de services:', error);
      }
    },
    getServiceLibelle(code) {
      const service = this.servicesTypes.find(s => s.code === code);
      return service ? service.libelle : code;
    },
    formatDate(date) {
      return new Date(date).toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      });
    },
    loadMoreReviews() {
      this.page++;
    },
    async sendMessage() {
      if (!this.messageForm.contenu.trim()) return;
      
      this.sending = true;
      try {
        const response = await fetch(`/api/messages/prestataire/${this.$route.params.id}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.messageForm)
        });

        if (response.ok) {
          this.messageForm.contenu = '';
          alert('Message envoyé avec succès !');
        } else {
          throw new Error('Erreur lors de l\'envoi du message');
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Une erreur est survenue lors de l\'envoi du message');
      } finally {
        this.sending = false;
      }
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

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.profile-header {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.profile-header h1 {
  margin: 0 0 1rem 0;
  color: #333;
}

.rating-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.rating {
  color: #ffc107;
}

.rating i.filled {
  color: #ffc107;
}

.rating i:not(.filled) {
  color: #e0e0e0;
}

.rating-value {
  font-weight: bold;
  color: #333;
}

.review-count {
  color: #666;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 2rem;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

section {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.info-item {
  text-align: center;
  padding: 1rem;
}

.info-item i {
  font-size: 2rem;
  color: #4CAF50;
  margin-bottom: 1rem;
}

.info-item h3 {
  margin: 0.5rem 0;
  color: #333;
  font-size: 1rem;
}

.info-item p {
  margin: 0;
  color: #666;
}

.status-available {
  color: #2e7d32;
}

.status-unavailable {
  color: #c62828;
}

.description-section h2,
.reviews-section h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.review-card {
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 1rem;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.review-date {
  font-size: 0.875rem;
  color: #666;
}

.review-text {
  margin: 0;
  color: #333;
  line-height: 1.5;
}

.load-more {
  text-align: center;
  margin-top: 1rem;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}

.sidebar {
  position: sticky;
  top: 2rem;
}

.contact-card {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.contact-card h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  min-height: 100px;
  font-size: 1rem;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 4px;
  width: 100%;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #43a047;
}

.btn-primary:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

.no-reviews {
  text-align: center;
  color: #666;
  font-style: italic;
}

@media (max-width: 1024px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }

  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
