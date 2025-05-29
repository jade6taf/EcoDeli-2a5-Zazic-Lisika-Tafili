<script>
export default {
  name: 'MesSegmentsView',
  data() {
    return {
      segments: [],
      user: null,
      isLoading: true,
      error: null,
      showConfirmModal: false,
      actionData: null
    }
  },
  computed: {
    segmentsPretsADemarrer() {
      return this.segments.filter(segment => {
        if (segment.livreurSegment1?.idUtilisateur === this.user?.idUtilisateur) {
          return segment.statut === 'SEGMENTS_COMPLETS' && segment.statutSegment1 === 'PRIS';
        }
        if (segment.livreurSegment2?.idUtilisateur === this.user?.idUtilisateur) {
          return segment.statut === 'ATTENTE_ENTREPOT' && segment.statutSegment2 === 'PRIS';
        }
        return false;
      });
    },
    segmentsEnCours() {
      return this.segments.filter(segment => {
        if (segment.livreurSegment1?.idUtilisateur === this.user?.idUtilisateur) {
          return segment.statut === 'EN_COURS_SEGMENT_1' && segment.statutSegment1 === 'EN_COURS';
        }
        if (segment.livreurSegment2?.idUtilisateur === this.user?.idUtilisateur) {
          return segment.statut === 'EN_COURS_SEGMENT_2' && segment.statutSegment2 === 'EN_COURS';
        }
        return false;
      });
    },
    segmentsTermines() {
      return this.segments.filter(segment => {
        if (segment.livreurSegment1?.idUtilisateur === this.user?.idUtilisateur) {
          return segment.statutSegment1 === 'TERMINE';
        }
        if (segment.livreurSegment2?.idUtilisateur === this.user?.idUtilisateur) {
          return segment.statutSegment2 === 'TERMINE';
        }
        return false;
      });
    },
    statutLabels() {
      return {
        'SEGMENTS_COMPLETS': { text: 'Prêt à démarrer', class: 'status-ready' },
        'EN_COURS_SEGMENT_1': { text: 'Segment 1 en cours', class: 'status-in-progress' },
        'ATTENTE_ENTREPOT': { text: 'En attente entrepôt', class: 'status-waiting' },
        'EN_COURS_SEGMENT_2': { text: 'Segment 2 en cours', class: 'status-in-progress' },
        'TERMINEE': { text: 'Terminé', class: 'status-completed' }
      }
    }
  },
  methods: {
    async fetchMesSegments() {
      this.isLoading = true;
      this.error = null;
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) {
          this.$router.push('/login');
          return;
        }
        this.user = JSON.parse(userStr);

        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }

        const response = await fetch(`/api/annonces/mes-segments?idLivreur=${this.user.idUtilisateur}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des segments');
        }

        this.segments = await response.json();
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isLoading = false;
      }
    },

    getSegmentNumber(segment) {
      if (segment.livreurSegment1?.idUtilisateur === this.user?.idUtilisateur) {
        return 1;
      }
      if (segment.livreurSegment2?.idUtilisateur === this.user?.idUtilisateur) {
        return 2;
      }
      return null;
    },

    getSegmentAddresses(segment, segmentNumber) {
      if (segmentNumber === 1) {
        return {
          depart: segment.adresseDepart,
          arrivee: `Entrepôt ${segment.entrepotIntermediaire}`
        };
      } else {
        return {
          depart: `Entrepôt ${segment.entrepotIntermediaire}`,
          arrivee: segment.adresseFin
        };
      }
    },

    openConfirmModal(action, segment, segmentNumber) {
      this.actionData = {
        action,
        segment,
        segmentNumber
      };
      this.showConfirmModal = true;
    },

    closeConfirmModal() {
      this.showConfirmModal = false;
      this.actionData = null;
    },

    async confirmerAction() {
      if (!this.actionData) return;

      const { action, segment, segmentNumber } = this.actionData;

      try {
        const token = localStorage.getItem('token');
        const endpoint = `/api/annonces/${segment.idAnnonce}/${action}-segment${segmentNumber}?idLivreur=${this.user.idUtilisateur}`;

        const response = await fetch(endpoint, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || `Erreur lors de l'action ${action}`);
        }

        let message = '';
        if (action === 'commencer') {
          message = `Segment ${segmentNumber} commencé avec succès !`;
        } else if (action === 'terminer') {
          if (segmentNumber === 1) {
            message = 'Colis livré à l\'entrepôt avec succès !';
          } else {
            message = 'Livraison terminée avec succès !';
          }
        }

        alert(message);
        this.closeConfirmModal();
        await this.fetchMesSegments();
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
      }
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date);
    },

    getActionLabel(segment, segmentNumber) {
      if (this.segmentsPretsADemarrer.includes(segment)) {
        return 'Commencer la livraison';
      } else if (this.segmentsEnCours.includes(segment)) {
        if (segmentNumber === 1) {
          return 'Livré à l\'entrepôt';
        } else {
          return 'Livré au destinataire';
        }
      }
      return '';
    },

    getActionIcon(segment, segmentNumber) {
      if (this.segmentsPretsADemarrer.includes(segment)) {
        return 'fas fa-play';
      } else if (this.segmentsEnCours.includes(segment)) {
        return 'fas fa-check';
      }
      return '';
    }
  },

  mounted() {
    this.fetchMesSegments();
  }
}
</script>

<template>
  <div class="segments-container">
    <div class="segments-header">
      <h1>Mes Segments</h1>
      <router-link to="/livreur" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour au dashboard
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement des segments...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else class="segments-content">
      <!-- Segments prêts à démarrer -->
      <div class="segments-section">
        <h2 class="section-title ready">
          <i class="fas fa-play-circle"></i>
          Prêts à démarrer ({{ segmentsPretsADemarrer.length }})
        </h2>

        <div v-if="segmentsPretsADemarrer.length === 0" class="empty-section">
          <i class="fas fa-clock empty-icon"></i>
          <p>Aucun segment prêt à démarrer</p>
        </div>

        <div v-else class="segments-grid">
          <div v-for="segment in segmentsPretsADemarrer" :key="`ready-${segment.idAnnonce}`" class="segment-card ready">
            <div class="segment-header">
              <div class="segment-info">
                <h3>{{ segment.titre }}</h3>
                <span class="segment-badge">Segment {{ getSegmentNumber(segment) }}</span>
              </div>
              <span class="status-badge" :class="statutLabels[segment.statut]?.class">
                {{ statutLabels[segment.statut]?.text }}
              </span>
            </div>

            <div class="segment-details">
              <div class="addresses">
                <div class="address-item">
                  <i class="fas fa-map-marker-alt start"></i>
                  <span>{{ getSegmentAddresses(segment, getSegmentNumber(segment)).depart }}</span>
                </div>
                <div class="route-arrow">
                  <i class="fas fa-arrow-down"></i>
                </div>
                <div class="address-item">
                  <i class="fas fa-map-marker-alt end"></i>
                  <span>{{ getSegmentAddresses(segment, getSegmentNumber(segment)).arrivee }}</span>
                </div>
              </div>

              <div class="segment-meta">
                <div class="meta-item">
                  <i class="fas fa-euro-sign"></i>
                  <span>{{ Math.round(segment.prixUnitaire / 2) }} €</span>
                </div>
                <div class="meta-item" v-if="segment.colis">
                  <i class="fas fa-box"></i>
                  <span>{{ segment.colis.poids }} kg</span>
                </div>
                <div class="meta-item">
                  <i class="fas fa-calendar"></i>
                  <span>{{ formatDate(segment.dateDebut) }}</span>
                </div>
              </div>
            </div>

            <div class="segment-actions">
              <button
                @click="openConfirmModal('commencer', segment, getSegmentNumber(segment))"
                class="btn-action btn-start">
                <i class="fas fa-play"></i>
                {{ getActionLabel(segment, getSegmentNumber(segment)) }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Segments en cours -->
      <div class="segments-section">
        <h2 class="section-title in-progress">
          <i class="fas fa-truck"></i>
          En cours ({{ segmentsEnCours.length }})
        </h2>

        <div v-if="segmentsEnCours.length === 0" class="empty-section">
          <i class="fas fa-truck empty-icon"></i>
          <p>Aucun segment en cours</p>
        </div>

        <div v-else class="segments-grid">
          <div v-for="segment in segmentsEnCours" :key="`progress-${segment.idAnnonce}`" class="segment-card in-progress">
            <div class="segment-header">
              <div class="segment-info">
                <h3>{{ segment.titre }}</h3>
                <span class="segment-badge">Segment {{ getSegmentNumber(segment) }}</span>
              </div>
              <span class="status-badge" :class="statutLabels[segment.statut]?.class">
                {{ statutLabels[segment.statut]?.text }}
              </span>
            </div>

            <div class="segment-details">
              <div class="addresses">
                <div class="address-item">
                  <i class="fas fa-map-marker-alt start"></i>
                  <span>{{ getSegmentAddresses(segment, getSegmentNumber(segment)).depart }}</span>
                </div>
                <div class="route-arrow">
                  <i class="fas fa-arrow-down"></i>
                </div>
                <div class="address-item">
                  <i class="fas fa-map-marker-alt end"></i>
                  <span>{{ getSegmentAddresses(segment, getSegmentNumber(segment)).arrivee }}</span>
                </div>
              </div>

              <div class="segment-meta">
                <div class="meta-item">
                  <i class="fas fa-euro-sign"></i>
                  <span>{{ Math.round(segment.prixUnitaire / 2) }} €</span>
                </div>
                <div class="meta-item" v-if="segment.colis">
                  <i class="fas fa-box"></i>
                  <span>{{ segment.colis.poids }} kg</span>
                </div>
                <div class="meta-item">
                  <i class="fas fa-calendar"></i>
                  <span>{{ formatDate(segment.dateDebut) }}</span>
                </div>
              </div>
            </div>

            <div class="segment-actions">
              <button
                @click="openConfirmModal('terminer', segment, getSegmentNumber(segment))"
                class="btn-action btn-complete">
                <i class="fas fa-check"></i>
                {{ getActionLabel(segment, getSegmentNumber(segment)) }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Segments terminés -->
      <div class="segments-section">
        <h2 class="section-title completed">
          <i class="fas fa-check-circle"></i>
          Terminés ({{ segmentsTermines.length }})
        </h2>

        <div v-if="segmentsTermines.length === 0" class="empty-section">
          <i class="fas fa-check-circle empty-icon"></i>
          <p>Aucun segment terminé</p>
        </div>

        <div v-else class="segments-grid">
          <div v-for="segment in segmentsTermines" :key="`completed-${segment.idAnnonce}`" class="segment-card completed">
            <div class="segment-header">
              <div class="segment-info">
                <h3>{{ segment.titre }}</h3>
                <span class="segment-badge">Segment {{ getSegmentNumber(segment) }}</span>
              </div>
              <span class="status-badge status-completed">
                Terminé
              </span>
            </div>

            <div class="segment-details">
              <div class="addresses">
                <div class="address-item">
                  <i class="fas fa-map-marker-alt start"></i>
                  <span>{{ getSegmentAddresses(segment, getSegmentNumber(segment)).depart }}</span>
                </div>
                <div class="route-arrow">
                  <i class="fas fa-arrow-down"></i>
                </div>
                <div class="address-item">
                  <i class="fas fa-map-marker-alt end"></i>
                  <span>{{ getSegmentAddresses(segment, getSegmentNumber(segment)).arrivee }}</span>
                </div>
              </div>

              <div class="segment-meta">
                <div class="meta-item">
                  <i class="fas fa-euro-sign"></i>
                  <span>{{ Math.round(segment.prixUnitaire / 2) }} €</span>
                </div>
                <div class="meta-item" v-if="segment.colis">
                  <i class="fas fa-box"></i>
                  <span>{{ segment.colis.poids }} kg</span>
                </div>
                <div class="meta-item">
                  <i class="fas fa-calendar"></i>
                  <span>{{ formatDate(segment.dateDebut) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmation -->
    <div v-if="showConfirmModal" class="modal-overlay" @click="closeConfirmModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Confirmation</h3>
          <button @click="closeConfirmModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body" v-if="actionData">
          <div class="confirmation-content">
            <i class="fas fa-question-circle confirmation-icon"></i>
            <p>
              <strong>{{ actionData.segment.titre }}</strong><br>
              Segment {{ actionData.segmentNumber }}
            </p>

            <div class="action-description">
              <template v-if="actionData.action === 'commencer'">
                <p>Êtes-vous prêt à commencer la livraison de ce segment ?</p>
                <p class="action-note">Une fois commencé, vous devrez terminer la livraison.</p>
              </template>
              <template v-else-if="actionData.action === 'terminer'">
                <p v-if="actionData.segmentNumber === 1">
                  Confirmez-vous avoir livré le colis à l'entrepôt ?
                </p>
                <p v-else>
                  Confirmez-vous avoir livré le colis au destinataire final ?
                </p>
                <p class="action-note">Cette action ne peut pas être annulée.</p>
              </template>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeConfirmModal" class="btn-cancel">
            Annuler
          </button>
          <button @click="confirmerAction" class="btn-confirm">
            <i class="fas fa-check"></i>
            Confirmer
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.segments-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.segments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.segments-header h1 {
  color: #333;
  margin: 0;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  background-color: #f5f5f5;
  color: #333;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #e0e0e0;
}

.btn-back i {
  margin-right: 0.5rem;
}

.segments-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.segments-section {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  border-bottom: 1px solid #eee;
}

.section-title.ready {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  color: #1565c0;
}

.section-title.in-progress {
  background: linear-gradient(135deg, #fff3e0, #ffcc02);
  color: #e65100;
}

.section-title.completed {
  background: linear-gradient(135deg, #e8f5e8, #c8e6c9);
  color: #2e7d32;
}

.empty-section {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.segments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
  padding: 1.5rem;
}

.segment-card {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid;
}

.segment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.segment-card.ready {
  border-left-color: #2196f3;
  background: #fafafa;
}

.segment-card.in-progress {
  border-left-color: #ff9800;
  background: #fafafa;
}

.segment-card.completed {
  border-left-color: #4caf50;
  background: #f9f9f9;
}

.segment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: white;
  border-bottom: 1px solid #eee;
}

.segment-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.segment-info h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.segment-badge {
  font-size: 0.8rem;
  font-weight: 600;
  color: #666;
  background: #e0e0e0;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  align-self: flex-start;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-ready {
  background-color: #e3f2fd;
  color: #1976d2;
}

.status-in-progress {
  background-color: #fff3e0;
  color: #f57c00;
}

.status-waiting {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.status-completed {
  background-color: #e8f5e8;
  color: #388e3c;
}

.segment-details {
  padding: 1.5rem;
}

.addresses {
  margin-bottom: 1rem;
}

.address-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.5rem 0;
}

.address-item i.start {
  color: #4caf50;
}

.address-item i.end {
  color: #f44336;
}

.route-arrow {
  text-align: center;
  color: #666;
  margin: 0.5rem 0;
}

.segment-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

.meta-item i {
  width: 16px;
  text-align: center;
}

.segment-actions {
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.btn-action {
  width: 100%;
  padding: 0.8rem 1rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s;
}

.btn-start {
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
}

.btn-start:hover {
  background: linear-gradient(135deg, #1976d2, #1565c0);
  transform: translateY(-1px);
}

.btn-complete {
  background: linear-gradient(135deg, #4caf50, #388e3c);
  color: white;
}

.btn-complete:hover {
  background: linear-gradient(135deg, #388e3c, #2e7d32);
  transform: translateY(-1px);
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
  border-radius: 12px 12px 0 0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e9ecef;
  color: #333;
}

.modal-body {
  padding: 2rem;
}

.confirmation-content {
  text-align: center;
}

.confirmation-icon {
  font-size: 3rem;
  color: #ff9800;
  margin-bottom: 1rem;
}

.confirmation-content p {
  margin: 1rem 0;
  color: #333;
}

.action-description {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
}

.action-note {
  font-size: 0.9rem;
  color: #666;
  font-style: italic;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #eee;
  background: #f8f9fa;
  border-radius: 0 0 12px 12px;
}

.btn-cancel {
  padding: 0.8rem 1.5rem;
  border: 1px solid #ddd;
  background: white;
  color: #666;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.btn-confirm {
  padding: 0.8rem 1.5rem;
  border: none;
  background: #ff9800;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.btn-confirm:hover {
  background: #f57c00;
}

.loading, .error-message {
  text-align: center;
  padding: 3rem;
}

.loading i {
  font-size: 2rem;
  color: #3F51B5;
  margin-bottom: 1rem;
}

.error-message {
  color: #e53935;
  background-color: #ffebee;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .segments-container {
    padding: 1rem;
  }
  
  .segments-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .segments-grid {
    grid-template-columns: 1fr;
    padding: 1rem;
  }
  
  .segment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
