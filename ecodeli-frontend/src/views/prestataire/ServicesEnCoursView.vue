<script>
export default {
  name: 'ServicesEnCoursView',
  data() {
    return {
      servicesEnCours: [],
      isLoading: true,
      error: null,
      showProgressModal: false,
      selectedService: null,
      isUpdatingProgress: false,
      progressForm: {
        progression: 0,
        commentaire: ''
      },
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
    servicesUrgents() {
      return this.servicesEnCours.filter(service => this.isUrgent(service));
    },
    chiffresAffairesEnCours() {
      return this.servicesEnCours.reduce((total, service) => total + (service.prixAccorde || 0), 0);
    }
  },
  async mounted() {
    await this.loadServices();
  },
  methods: {
    async loadServices() {
      this.isLoading = true;
      this.error = null;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/services/en-cours', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const services = await response.json();
          this.servicesEnCours = services.map(service => ({
            id: service.idService,
            titre: service.demandeService?.titre || 'Service sans titre',
            description: service.demandeService?.description || '',
            categorieService: service.demandeService?.categorieService || '',
            prixAccorde: service.prixFinal,
            dateDebut: service.dateDebut,
            dateLimiteSouhaitee: service.dateRealisationPrevue,
            progression: service.progression || 0,
            adresse: service.adresseService || service.demandeService?.adresseDepart,
            client: {
              nom: service.client?.nom || '',
              prenom: service.client?.prenom || '',
              telephone: service.client?.telephone || '',
              email: service.client?.email || ''
            },
            commentaires: [
              service.notesClient && {
                id: 1,
                auteur: `${service.client?.prenom} ${service.client?.nom}`,
                date: service.dateModification || service.dateCreation,
                texte: service.notesClient
              },
              service.notesPrestataire && {
                id: 2,
                auteur: 'Moi',
                date: service.dateModification || service.dateCreation,
                texte: service.notesPrestataire
              }
            ].filter(Boolean)
          }));
        } else {
          const error = await response.json();
          this.error = error.error || 'Erreur lors du chargement des services';
        }

      } catch (error) {
        console.error('Erreur:', error);
        this.error = 'Erreur lors du chargement des services';
      } finally {
        this.isLoading = false;
      }
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : code;
    },

    isUrgent(service) {
      if (!service.dateLimiteSouhaitee) return false;
      const dateLimit = new Date(service.dateLimiteSouhaitee);
      const maintenant = new Date();
      const diffJours = Math.ceil((dateLimit - maintenant) / (1000 * 60 * 60 * 24));
      return diffJours <= 1;
    },

    isOverdue(service) {
      if (!service.dateLimiteSouhaitee) return false;
      const dateLimit = new Date(service.dateLimiteSouhaitee);
      const maintenant = new Date();
      return dateLimit < maintenant;
    },

    getTimeUntilDeadline(service) {
      if (!service.dateLimiteSouhaitee) return '';
      const dateLimit = new Date(service.dateLimiteSouhaitee);
      const maintenant = new Date();
      const diffHeures = Math.abs(Math.floor((dateLimit - maintenant) / (1000 * 60 * 60)));

      if (diffHeures < 24) {
        return `${diffHeures}h`;
      } else {
        const diffJours = Math.floor(diffHeures / 24);
        return `${diffJours} jour${diffJours > 1 ? 's' : ''}`;
      }
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    formatDateRelative(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const maintenant = new Date();
      const diffJours = Math.floor((maintenant - date) / (1000 * 60 * 60 * 24));

      if (diffJours === 0) return 'aujourd\'hui';
      if (diffJours === 1) return 'hier';
      if (diffJours < 7) return `il y a ${diffJours} jours`;

      return date.toLocaleDateString('fr-FR');
    },

    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    mettreAJourProgression(service) {
      this.selectedService = service;
      this.progressForm = {
        progression: service.progression || 0,
        commentaire: ''
      };
      this.showProgressModal = true;
    },

    closeProgressModal() {
      this.showProgressModal = false;
      this.selectedService = null;
    },

    async sauvegarderProgression() {
      if (!this.selectedService) return;

      this.isUpdatingProgress = true;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/services/${this.selectedService.id}/progression`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            progression: parseInt(this.progressForm.progression),
            notes: this.progressForm.commentaire
          })
        });

        if (response.ok) {
          this.selectedService.progression = this.progressForm.progression;
          if (this.progressForm.commentaire) {
            if (!this.selectedService.commentaires) {
              this.selectedService.commentaires = [];
            }
            this.selectedService.commentaires.unshift({
              id: Date.now(),
              auteur: 'Moi',
              date: new Date().toISOString(),
              texte: this.progressForm.commentaire
            });
          }

          this.closeProgressModal();
          alert('Progression mise à jour avec succès !');
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de la mise à jour de la progression');
      } finally {
        this.isUpdatingProgress = false;
      }
    },

    ajouterCommentaire(service) {
      const commentaire = prompt('Ajouter un commentaire:');
      if (commentaire) {
        if (!service.commentaires) {
          service.commentaires = [];
        }
        service.commentaires.unshift({
          id: Date.now(),
          auteur: 'Moi',
          date: new Date().toISOString(),
          texte: commentaire
        });
      }
    },
    async terminerService(service) {
      if (!confirm(`Êtes-vous sûr de vouloir marquer le service "${service.titre}" comme terminé ?`)) {
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/services/${service.id}/terminer`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            notes: `Service terminé le ${new Date().toLocaleDateString('fr-FR')}`
          })
        });

        if (response.ok) {
          const index = this.servicesEnCours.findIndex(s => s.id === service.id);
          if (index !== -1) {
            this.servicesEnCours.splice(index, 1);
          }

          alert('Service marqué comme terminé !');
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }
      } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur lors de la finalisation du service');
      }
    },

    contacterClient(service) {
      if (service.client?.telephone) {
        window.open(`tel:${service.client.telephone}`);
      }
    },

    envoyerMessage(service) {
      if (service.client?.email) {
        window.open(`mailto:${service.client.email}?subject=Service: ${service.titre}`);
      }
    },

    voirDetails(service) {
      // TODO: Implémenter la vue détail
      this.$router.push(`/prestataire/services/${service.id}`);
    }
  }
}
</script>

<template>
  <div class="services-en-cours-container">
    <div class="page-header">
      <h1>Services en cours</h1>
      <div class="header-actions">
        <button @click="loadServices" class="btn btn-primary">
          <i class="fas fa-sync-alt"></i> Actualiser
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Chargement...
    </div>

    <div v-else-if="error" class="error-message">
      <i class="fas fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div v-else>
      <div class="summary-section">
        <div class="summary-card">
          <div class="summary-icon">
            <i class="fas fa-tools"></i>
          </div>
          <div class="summary-content">
            <div class="summary-number">{{ servicesEnCours.length }}</div>
            <div class="summary-label">Services actifs</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon urgent">
            <i class="fas fa-exclamation-triangle"></i>
          </div>
          <div class="summary-content">
            <div class="summary-number">{{ servicesUrgents.length }}</div>
            <div class="summary-label">Services urgents</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon revenue">
            <i class="fas fa-euro-sign"></i>
          </div>
          <div class="summary-content">
            <div class="summary-number">{{ chiffresAffairesEnCours }}€</div>
            <div class="summary-label">CA en cours</div>
          </div>
        </div>
      </div>

      <div v-if="servicesEnCours.length === 0" class="empty-state">
        <i class="fas fa-calendar-check"></i>
        <h3>Aucun service en cours</h3>
        <p>Vous n'avez pas de service actuellement en cours. Consultez vos candidatures acceptées pour commencer de nouveaux services.</p>
        <router-link to="/prestataire/mes-candidatures" class="btn btn-primary">
          Voir mes candidatures
        </router-link>
      </div>

      <div v-else class="services-list">
        <div
          v-for="service in servicesEnCours"
          :key="service.id"
          class="service-card"
          :class="{ 'urgent': isUrgent(service) }"
        >
          <div class="service-header">
            <div class="service-title">
              <h3>{{ service.titre }}</h3>
              <div class="service-badges">
                <span class="category-badge">{{ getCategoryLabel(service.categorieService) }}</span>
                <span v-if="isUrgent(service)" class="urgent-badge">Urgent</span>
                <span class="status-badge">En cours</span>
              </div>
            </div>
            <div class="service-amount">
              {{ service.prixAccorde }}€
            </div>
          </div>

          <div class="service-meta">
            <div class="meta-item">
              <i class="fas fa-calendar-alt"></i>
              <span>Commencé {{ formatDateRelative(service.dateDebut) }}</span>
            </div>
            <div v-if="service.dateLimiteSouhaitee" class="meta-item">
              <i class="fas fa-clock"></i>
              <span>À terminer {{ isOverdue(service) ? 'depuis' : 'dans' }} {{ getTimeUntilDeadline(service) }}</span>
            </div>
            <div class="meta-item">
              <i class="fas fa-map-marker-alt"></i>
              <span>{{ service.adresse || 'Adresse non précisée' }}</span>
            </div>
          </div>

          <div class="service-description">
            {{ truncateText(service.description, 120) }}
          </div>

          <div class="service-progress">
            <div class="progress-header">
              <span>Progression</span>
              <span>{{ service.progression || 0 }}%</span>
            </div>
            <div class="progress-bar">
              <div 
                class="progress-fill"
                :style="{ width: `${service.progression || 0}%` }"
              ></div>
            </div>
            <div class="progress-milestones">
              <div class="milestone" :class="{ 'completed': (service.progression || 0) >= 25 }">
                <div class="milestone-dot"></div>
                <div class="milestone-label">25%</div>
              </div>
              <div class="milestone" :class="{ 'completed': (service.progression || 0) >= 50 }">
                <div class="milestone-dot"></div>
                <div class="milestone-label">50%</div>
              </div>
              <div class="milestone" :class="{ 'completed': (service.progression || 0) >= 75 }">
                <div class="milestone-dot"></div>
                <div class="milestone-label">75%</div>
              </div>
              <div class="milestone" :class="{ 'completed': (service.progression || 0) >= 100 }">
                <div class="milestone-dot"></div>
                <div class="milestone-label">100%</div>
              </div>
            </div>
          </div>

          <div class="service-client">
            <div class="client-info">
              <div class="client-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="client-details">
                <div class="client-name">{{ service.client?.nom }} {{ service.client?.prenom }}</div>
                <div class="client-contact">{{ service.client?.telephone || service.client?.email }}</div>
              </div>
            </div>
            <div class="contact-actions">
              <button @click="contacterClient(service)" class="btn btn-sm btn-secondary">
                <i class="fas fa-phone"></i>
              </button>
              <button @click="envoyerMessage(service)" class="btn btn-sm btn-secondary">
                <i class="fas fa-envelope"></i>
              </button>
            </div>
          </div>

          <div class="service-actions">
            <button @click="mettreAJourProgression(service)" class="btn btn-primary btn-sm">
              <i class="fas fa-edit"></i> Mettre à jour
            </button>
            <button @click="ajouterCommentaire(service)" class="btn btn-secondary btn-sm">
              <i class="fas fa-comment"></i> Commentaire
            </button>
            <button 
              v-if="(service.progression || 0) >= 100"
              @click="terminerService(service)"
              class="btn btn-success btn-sm"
            >
              <i class="fas fa-check"></i> Terminer
            </button>
            <button @click="voirDetails(service)" class="btn btn-info btn-sm">
              <i class="fas fa-eye"></i> Détails
            </button>
          </div>

          <div v-if="service.commentaires && service.commentaires.length > 0" class="service-comments">
            <h4>Derniers commentaires</h4>
            <div class="comments-list">
              <div
                v-for="commentaire in service.commentaires.slice(0, 2)"
                :key="commentaire.id"
                class="comment-item"
              >
                <div class="comment-meta">
                  <strong>{{ commentaire.auteur }}</strong>
                  <span class="comment-date">{{ formatDate(commentaire.date) }}</span>
                </div>
                <div class="comment-text">{{ commentaire.texte }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showProgressModal" class="modal-overlay" @click="closeProgressModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Mettre à jour la progression</h3>
          <button @click="closeProgressModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label for="progression">Progression (%)</label>
            <input
              type="range"
              id="progression"
              v-model="progressForm.progression"
              min="0"
              max="100"
              step="5"
              class="progress-slider"
            >
            <div class="progress-value">{{ progressForm.progression }}%</div>
          </div>

          <div class="form-group">
            <label for="commentaire">Commentaire (optionnel)</label>
            <textarea
              id="commentaire"
              v-model="progressForm.commentaire"
              placeholder="Décrivez l'avancement des travaux..."
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeProgressModal" class="btn btn-secondary">
            Annuler
          </button>
          <button
            @click="sauvegarderProgression"
            class="btn btn-primary"
            :disabled="isUpdatingProgress"
          >
            {{ isUpdatingProgress ? 'Mise à jour...' : 'Sauvegarder' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.services-en-cours-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: var(--text-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.summary-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.summary-card {
  background-color: var(--card-bg);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.summary-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  font-size: 1.2rem;
}

.summary-icon.urgent {
  background-color: var(--warning-color);
}

.summary-icon.revenue {
  background-color: var(--success-color);
}

.summary-content {
  flex: 1;
}

.summary-number {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1;
}

.summary-label {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 0.25rem;
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

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--text-secondary);
}

.empty-state i {
  font-size: 4rem;
  margin-bottom: 1rem;
  color: var(--border-color);
}

.empty-state h3 {
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.services-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.service-card {
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: var(--shadow);
  padding: 1.5rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-left: 4px solid var(--success-color);
}

.service-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.service-card.urgent {
  border-left-color: var(--warning-color);
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.service-title h3 {
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
}

.service-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.category-badge, .urgent-badge, .status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.category-badge {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
}

.urgent-badge {
  background-color: var(--warning-color-light);
  color: var(--warning-color);
}

.status-badge {
  background-color: var(--success-color-light);
  color: var(--success-color);
}

.service-amount {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--success-color);
}

.service-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.meta-item i {
  color: var(--primary-color);
  width: 14px;
}

.service-description {
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1.5rem;
}

.service-progress {
  margin-bottom: 1.5rem;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.progress-header span:first-child {
  color: var(--text-primary);
  font-weight: 500;
}

.progress-header span:last-child {
  color: var(--primary-color);
  font-weight: bold;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: var(--border-color);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 1rem;
}

.progress-fill {
  height: 100%;
  background-color: var(--primary-color);
  transition: width 0.3s ease;
}

.progress-milestones {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.milestone {
  text-align: center;
}

.milestone-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: var(--border-color);
  margin: 0 auto 0.25rem;
}

.milestone.completed .milestone-dot {
  background-color: var(--primary-color);
}

.milestone-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.service-client {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: var(--background-color);
  border-radius: 6px;
  margin-bottom: 1rem;
}

.client-info {
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
  font-size: 0.9rem;
}

.client-contact {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.contact-actions {
  display: flex;
  gap: 0.5rem;
}

.service-actions {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.service-comments {
  border-top: 1px solid var(--border-color);
  padding-top: 1rem;
}

.service-comments h4 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  font-size: 1rem;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.comment-item {
  padding: 0.75rem;
  background-color: var(--background-color);
  border-radius: 4px;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.comment-meta strong {
  color: var(--text-primary);
  font-size: 0.85rem;
}

.comment-date {
  color: var(--text-secondary);
  font-size: 0.75rem;
}

.comment-text {
  color: var(--text-secondary);
  font-size: 0.85rem;
  line-height: 1.4;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.8rem;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-color-dark);
}

.btn-secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background-color: var(--text-secondary);
}

.btn-success {
  background-color: var(--success-color);
  color: white;
}

.btn-success:hover {
  background-color: var(--success-color-dark);
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background-color: var(--card-bg);
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.5rem;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
}

.progress-slider {
  width: 100%;
  margin-bottom: 0.5rem;
}

.progress-value {
  text-align: center;
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--primary-color);
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
  resize: vertical;
}

.form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-color);
}
</style>
