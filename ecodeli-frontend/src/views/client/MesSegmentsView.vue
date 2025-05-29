<script>
export default {
  name: 'ClientMesSegmentsView',
  data() {
    return {
      annonces: [],
      user: null,
      isLoading: true,
      error: null,
      selectedAnnonce: null,
      showDetailModal: false
    }
  },
  computed: {
    annoncesAvecSegments() {
      return this.annonces.filter(annonce => annonce.livraisonPartielleAutorisee);
    },
    annoncesEnCours() {
      return this.annoncesAvecSegments.filter(annonce =>
        ['SEGMENTS_COMPLETS', 'EN_COURS_SEGMENT_1', 'ATTENTE_ENTREPOT', 'EN_COURS_SEGMENT_2'].includes(annonce.statut)
      );
    },
    annoncesTerminees() {
      return this.annoncesAvecSegments.filter(annonce => annonce.statut === 'TERMINEE');
    },
    statutLabels() {
      return {
        'SEGMENT_1_PRIS': { text: 'Segment 1 pris', class: 'status-partial', icon: 'fas fa-play' },
        'SEGMENT_2_PRIS': { text: 'Segment 2 pris', class: 'status-partial', icon: 'fas fa-stop' },
        'SEGMENTS_COMPLETS': { text: 'Segments complets', class: 'status-ready', icon: 'fas fa-check-double' },
        'EN_COURS_SEGMENT_1': { text: 'Segment 1 en cours', class: 'status-in-progress', icon: 'fas fa-truck' },
        'ATTENTE_ENTREPOT': { text: 'En attente entrepôt', class: 'status-waiting', icon: 'fas fa-warehouse' },
        'EN_COURS_SEGMENT_2': { text: 'Segment 2 en cours', class: 'status-in-progress', icon: 'fas fa-truck' },
        'TERMINEE': { text: 'Livraison terminée', class: 'status-completed', icon: 'fas fa-check-circle' }
      }
    }
  },
  methods: {
    async fetchMesAnnonces() {
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

        const response = await fetch(`/api/annonces/expediteur/${this.user.idUtilisateur}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des annonces');
        }

        this.annonces = await response.json();
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isLoading = false;
      }
    },

    async getStatutDetaille(annonceId) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/annonces/${annonceId}/statut-detaille`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Erreur lors de la récupération du statut détaillé');
        }

        return await response.json();
      } catch (err) {
        return null;
      }
    },

    async openDetailModal(annonce) {
      this.selectedAnnonce = annonce;
      if (annonce.livraisonPartielleAutorisee) {
        const statutDetaille = await this.getStatutDetaille(annonce.idAnnonce);
        if (statutDetaille) {
          this.selectedAnnonce.statutDetaille = statutDetaille;
        }
      }
      this.showDetailModal = true;
    },

    closeDetailModal() {
      this.showDetailModal = false;
      this.selectedAnnonce = null;
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

    getSegmentStatus(annonce, segmentNumber) {
      if (segmentNumber === 1) {
        return annonce.statutSegment1 || 'DISPONIBLE';
      } else {
        return annonce.statutSegment2 || 'DISPONIBLE';
      }
    },

    getSegmentStatusText(status) {
      const statusMap = {
        'DISPONIBLE': 'Disponible',
        'PRIS': 'Pris',
        'EN_COURS': 'En cours',
        'TERMINE': 'Terminé'
      };
      return statusMap[status] || status;
    },

    getSegmentStatusClass(status) {
      const classMap = {
        'DISPONIBLE': 'segment-disponible',
        'PRIS': 'segment-pris',
        'EN_COURS': 'segment-en-cours',
        'TERMINE': 'segment-termine'
      };
      return classMap[status] || '';
    },

    getProgressPercentage(annonce) {
      const steps = ['SEGMENTS_COMPLETS', 'EN_COURS_SEGMENT_1', 'ATTENTE_ENTREPOT', 'EN_COURS_SEGMENT_2', 'TERMINEE'];
      const currentIndex = steps.indexOf(annonce.statut);
      if (currentIndex === -1) return 0;
      return ((currentIndex + 1) / steps.length) * 100;
    },

    getTimelineSteps(annonce) {
      return [
        {
          key: 'SEGMENTS_COMPLETS',
          title: 'Segments complets',
          description: 'Les deux livreurs sont assignés',
          icon: 'fas fa-users',
          completed: ['SEGMENTS_COMPLETS', 'EN_COURS_SEGMENT_1', 'ATTENTE_ENTREPOT', 'EN_COURS_SEGMENT_2', 'TERMINEE'].includes(annonce.statut),
          active: annonce.statut === 'SEGMENTS_COMPLETS'
        },
        {
          key: 'EN_COURS_SEGMENT_1',
          title: 'Segment 1 en cours',
          description: 'Collecte du colis en cours',
          icon: 'fas fa-truck',
          completed: ['ATTENTE_ENTREPOT', 'EN_COURS_SEGMENT_2', 'TERMINEE'].includes(annonce.statut),
          active: annonce.statut === 'EN_COURS_SEGMENT_1'
        },
        {
          key: 'ATTENTE_ENTREPOT',
          title: 'À l\'entrepôt',
          description: 'Colis livré à l\'entrepôt',
          icon: 'fas fa-warehouse',
          completed: ['EN_COURS_SEGMENT_2', 'TERMINEE'].includes(annonce.statut),
          active: annonce.statut === 'ATTENTE_ENTREPOT'
        },
        {
          key: 'EN_COURS_SEGMENT_2',
          title: 'Segment 2 en cours',
          description: 'Livraison finale en cours',
          icon: 'fas fa-truck',
          completed: ['TERMINEE'].includes(annonce.statut),
          active: annonce.statut === 'EN_COURS_SEGMENT_2'
        },
        {
          key: 'TERMINEE',
          title: 'Livraison terminée',
          description: 'Colis livré au destinataire',
          icon: 'fas fa-check-circle',
          completed: annonce.statut === 'TERMINEE',
          active: annonce.statut === 'TERMINEE'
        }
      ];
    }
  },

  mounted() {
    this.fetchMesAnnonces();
  }
}
</script>

<template>
  <div class="segments-container">
    <div class="segments-header">
      <h1>Suivi de mes livraisons partielles</h1>
      <router-link to="/client" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour au dashboard
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement des livraisons...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="annoncesAvecSegments.length === 0" class="empty-state">
      <i class="fas fa-route empty-icon"></i>
      <h3>Aucune livraison partielle</h3>
      <p>Vous n'avez actuellement aucune annonce avec livraison partielle.</p>
      <router-link to="/client/annonces/new" class="btn-create">
        <i class="fas fa-plus"></i> Créer une annonce
      </router-link>
    </div>

    <div v-else class="segments-content">
      <!-- Livraisons en cours -->
      <div class="segments-section" v-if="annoncesEnCours.length > 0">
        <h2 class="section-title in-progress">
          <i class="fas fa-truck"></i>
          Livraisons en cours ({{ annoncesEnCours.length }})
        </h2>

        <div class="annonces-grid">
          <div v-for="annonce in annoncesEnCours" :key="`progress-${annonce.idAnnonce}`" class="annonce-card in-progress">
            <div class="annonce-header">
              <div class="annonce-info">
                <h3>{{ annonce.titre }}</h3>
                <span class="delivery-type-badge">Livraison partielle</span>
              </div>
              <span class="status-badge" :class="statutLabels[annonce.statut]?.class">
                <i :class="statutLabels[annonce.statut]?.icon"></i>
                {{ statutLabels[annonce.statut]?.text }}
              </span>
            </div>

            <div class="annonce-details">
              <div class="route-info">
                <div class="route-item">
                  <i class="fas fa-map-marker-alt start"></i>
                  <span>{{ annonce.adresseDepart }}</span>
                </div>
                <div class="route-connector">
                  <div class="connector-line"></div>
                  <div class="warehouse-stop">
                    <i class="fas fa-warehouse"></i>
                    <span>{{ annonce.entrepotIntermediaire }}</span>
                  </div>
                  <div class="connector-line"></div>
                </div>
                <div class="route-item">
                  <i class="fas fa-map-marker-alt end"></i>
                  <span>{{ annonce.adresseFin }}</span>
                </div>
              </div>

              <div class="progress-info">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: getProgressPercentage(annonce) + '%' }"></div>
                </div>
                <div class="progress-text">
                  {{ Math.round(getProgressPercentage(annonce)) }}% terminé
                </div>
              </div>

              <div class="segments-status">
                <div class="segment-item">
                  <div class="segment-header">
                    <i class="fas fa-play"></i>
                    <span>Segment 1</span>
                    <span class="segment-status" :class="getSegmentStatusClass(getSegmentStatus(annonce, 1))">
                      {{ getSegmentStatusText(getSegmentStatus(annonce, 1)) }}
                    </span>
                  </div>
                  <div class="segment-details" v-if="annonce.livreurSegment1">
                    <i class="fas fa-user"></i>
                    <span>{{ annonce.livreurSegment1.prenom }} {{ annonce.livreurSegment1.nom }}</span>
                  </div>
                </div>

                <div class="segment-item">
                  <div class="segment-header">
                    <i class="fas fa-stop"></i>
                    <span>Segment 2</span>
                    <span class="segment-status" :class="getSegmentStatusClass(getSegmentStatus(annonce, 2))">
                      {{ getSegmentStatusText(getSegmentStatus(annonce, 2)) }}
                    </span>
                  </div>
                  <div class="segment-details" v-if="annonce.livreurSegment2">
                    <i class="fas fa-user"></i>
                    <span>{{ annonce.livreurSegment2.prenom }} {{ annonce.livreurSegment2.nom }}</span>
                  </div>
                </div>
              </div>

              <div class="annonce-meta">
                <div class="meta-item">
                  <i class="fas fa-euro-sign"></i>
                  <span>{{ annonce.prixUnitaire }} €</span>
                </div>
                <div class="meta-item">
                  <i class="fas fa-calendar"></i>
                  <span>{{ formatDate(annonce.dateDebut) }}</span>
                </div>
                <div class="meta-item" v-if="annonce.colis">
                  <i class="fas fa-box"></i>
                  <span>{{ annonce.colis.poids }} kg</span>
                </div>
              </div>
            </div>

            <div class="annonce-actions">
              <button @click="openDetailModal(annonce)" class="btn-detail">
                <i class="fas fa-eye"></i>
                Voir le détail
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Livraisons terminées -->
      <div class="segments-section" v-if="annoncesTerminees.length > 0">
        <h2 class="section-title completed">
          <i class="fas fa-check-circle"></i>
          Livraisons terminées ({{ annoncesTerminees.length }})
        </h2>

        <div class="annonces-grid">
          <div v-for="annonce in annoncesTerminees" :key="`completed-${annonce.idAnnonce}`" class="annonce-card completed">
            <div class="annonce-header">
              <div class="annonce-info">
                <h3>{{ annonce.titre }}</h3>
                <span class="delivery-type-badge">Livraison partielle</span>
              </div>
              <span class="status-badge status-completed">
                <i class="fas fa-check-circle"></i>
                Terminée
              </span>
            </div>

            <div class="annonce-details">
              <div class="route-info">
                <div class="route-item">
                  <i class="fas fa-map-marker-alt start"></i>
                  <span>{{ annonce.adresseDepart }}</span>
                </div>
                <div class="route-connector">
                  <div class="connector-line completed"></div>
                  <div class="warehouse-stop completed">
                    <i class="fas fa-warehouse"></i>
                    <span>{{ annonce.entrepotIntermediaire }}</span>
                  </div>
                  <div class="connector-line completed"></div>
                </div>
                <div class="route-item">
                  <i class="fas fa-map-marker-alt end"></i>
                  <span>{{ annonce.adresseFin }}</span>
                </div>
              </div>

              <div class="segments-status">
                <div class="segment-item">
                  <div class="segment-header">
                    <i class="fas fa-play"></i>
                    <span>Segment 1</span>
                    <span class="segment-status segment-termine">Terminé</span>
                  </div>
                  <div class="segment-details" v-if="annonce.livreurSegment1">
                    <i class="fas fa-user"></i>
                    <span>{{ annonce.livreurSegment1.prenom }} {{ annonce.livreurSegment1.nom }}</span>
                  </div>
                </div>

                <div class="segment-item">
                  <div class="segment-header">
                    <i class="fas fa-stop"></i>
                    <span>Segment 2</span>
                    <span class="segment-status segment-termine">Terminé</span>
                  </div>
                  <div class="segment-details" v-if="annonce.livreurSegment2">
                    <i class="fas fa-user"></i>
                    <span>{{ annonce.livreurSegment2.prenom }} {{ annonce.livreurSegment2.nom }}</span>
                  </div>
                </div>
              </div>

              <div class="annonce-meta">
                <div class="meta-item">
                  <i class="fas fa-euro-sign"></i>
                  <span>{{ annonce.prixUnitaire }} €</span>
                </div>
                <div class="meta-item">
                  <i class="fas fa-calendar"></i>
                  <span>{{ formatDate(annonce.dateDebut) }}</span>
                </div>
                <div class="meta-item" v-if="annonce.colis">
                  <i class="fas fa-box"></i>
                  <span>{{ annonce.colis.poids }} kg</span>
                </div>
              </div>
            </div>

            <div class="annonce-actions">
              <button @click="openDetailModal(annonce)" class="btn-detail">
                <i class="fas fa-eye"></i>
                Voir le détail
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de détail -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedAnnonce?.titre }}</h3>
          <button @click="closeDetailModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body" v-if="selectedAnnonce">
          <div class="detail-section">
            <h4>Informations générales</h4>
            <div class="detail-info">
              <div class="info-item">
                <strong>Description :</strong>
                <span>{{ selectedAnnonce.description || 'Aucune description' }}</span>
              </div>
              <div class="info-item">
                <strong>Prix :</strong>
                <span>{{ selectedAnnonce.prixUnitaire }} €</span>
              </div>
              <div class="info-item">
                <strong>Date de début :</strong>
                <span>{{ formatDate(selectedAnnonce.dateDebut) }}</span>
              </div>
              <div class="info-item" v-if="selectedAnnonce.colis">
                <strong>Colis :</strong>
                <span>{{ selectedAnnonce.colis.poids }} kg - {{ selectedAnnonce.colis.longueur }}×{{ selectedAnnonce.colis.largeur }}×{{ selectedAnnonce.colis.hauteur }} cm</span>
              </div>
            </div>
          </div>

          <div class="detail-section" v-if="selectedAnnonce.livraisonPartielleAutorisee">
            <h4>Timeline de livraison</h4>
            <div class="timeline">
              <div v-for="(step, index) in getTimelineSteps(selectedAnnonce)"
                   :key="step.key"
                   class="timeline-item"
                   :class="{
                     'completed': step.completed,
                     'active': step.active,
                     'pending': !step.completed && !step.active
                   }">
                <div class="timeline-marker">
                  <i :class="step.icon"></i>
                </div>
                <div class="timeline-content">
                  <h5>{{ step.title }}</h5>
                  <p>{{ step.description }}</p>
                </div>
                <div v-if="index < getTimelineSteps(selectedAnnonce).length - 1" class="timeline-connector"></div>
              </div>
            </div>
          </div>

          <div class="detail-section" v-if="selectedAnnonce.livraisonPartielleAutorisee">
            <h4>Livreurs assignés</h4>
            <div class="livreurs-info">
              <div class="livreur-card" v-if="selectedAnnonce.livreurSegment1">
                <div class="livreur-header">
                  <i class="fas fa-user"></i>
                  <strong>Segment 1</strong>
                </div>
                <div class="livreur-details">
                  <p>{{ selectedAnnonce.livreurSegment1.prenom }} {{ selectedAnnonce.livreurSegment1.nom }}</p>
                  <p class="livreur-route">{{ selectedAnnonce.adresseDepart }} → Entrepôt {{ selectedAnnonce.entrepotIntermediaire }}</p>
                </div>
              </div>

              <div class="livreur-card" v-if="selectedAnnonce.livreurSegment2">
                <div class="livreur-header">
                  <i class="fas fa-user"></i>
                  <strong>Segment 2</strong>
                </div>
                <div class="livreur-details">
                  <p>{{ selectedAnnonce.livreurSegment2.prenom }} {{ selectedAnnonce.livreurSegment2.nom }}</p>
                  <p class="livreur-route">Entrepôt {{ selectedAnnonce.entrepotIntermediaire }} → {{ selectedAnnonce.adresseFin }}</p>
                </div>
              </div>
            </div>
          </div>
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

.btn-create {
  display: inline-flex;
  align-items: center;
  background-color: #2196f3;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
  margin-top: 1rem;
}

.btn-create:hover {
  background-color: #1976d2;
}

.btn-create i {
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

.section-title.in-progress {
  background: linear-gradient(135deg, #fff3e0, #ffcc02);
  color: #e65100;
}

.section-title.completed {
  background: linear-gradient(135deg, #e8f5e8, #c8e6c9);
  color: #2e7d32;
}

.annonces-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 1.5rem;
  padding: 1.5rem;
}

.annonce-card {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid;
  background: white;
}

.annonce-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.annonce-card.in-progress {
  border-left-color: #ff9800;
}

.annonce-card.completed {
  border-left-color: #4caf50;
}

.annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.annonce-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.annonce-info h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.delivery-type-badge {
  font-size: 0.8rem;
  font-weight: 600;
  color: #ff9800;
  background: #fff3e0;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  align-self: flex-start;
}

.status-badge {
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.status-partial {
  background-color: #fff3e0;
  color: #f57c00;
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

.annonce-details {
  padding: 1.5rem;
}

.route-info {
  margin-bottom: 1.5rem;
}

.route-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.5rem 0;
}

.route-item i.start {
  color: #4caf50;
}

.route-item i.end {
  color: #f44336;
}

.route-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0.5rem 0;
}

.connector-line {
  width: 3px;
  height: 20px;
  background: #ddd;
  border-radius: 2px;
}

.connector-line.completed {
  background: #4caf50;
}

.warehouse-stop {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
  font-size: 0.9rem;
  margin: 0.5rem 0;
  padding: 0.3rem 0.8rem;
  background: #f0f0f0;
  border-radius: 15px;
}

.warehouse-stop.completed {
  background: #e8f5e8;
  color: #2e7d32;
}

.warehouse-stop i {
  color: #ff9800;
}

.progress-info {
  margin-bottom: 1.5rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4caf50, #8bc34a);
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.segments-status {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.segment-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1rem;
}

.segment-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.segment-header i {
  color: #666;
}

.segment-status {
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-left: auto;
}

.segment-disponible {
  background: #f5f5f5;
  color: #666;
}

.segment-pris {
  background: #e3f2fd;
  color: #1976d2;
}

.segment-en-cours {
  background: #fff3e0;
  color: #f57c00;
}

.segment-termine {
  background: #e8f5e8;
  color: #388e3c;
}

.segment-details {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

.annonce-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
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

.annonce-actions {
  padding: 1rem 1.5rem;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.btn-detail {
  width: 100%;
  padding: 0.8rem 1rem;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s;
}

.btn-detail:hover {
  background: linear-gradient(135deg, #1976d2, #1565c0);
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
  max-width: 700px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
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

.detail-section {
  margin-bottom: 2rem;
}

.detail-section h4 {
  margin: 0 0 1rem 0;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0.5rem;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
}

.info-item strong {
  color: #666;
  min-width: 120px;
}

.timeline {
  position: relative;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  position: relative;
  padding-bottom: 2rem;
}

.timeline-marker {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

.timeline-item.completed .timeline-marker {
  background: #4caf50;
  color: white;
}

.timeline-item.active .timeline-marker {
  background: #ff9800;
  color: white;
}

.timeline-item.pending .timeline-marker {
  background: #e0e0e0;
  color: #666;
}

.timeline-content {
  flex: 1;
  padding-top: 0.5rem;
}

.timeline-content h5 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.timeline-content p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.timeline-connector {
  position: absolute;
  left: 19px;
  top: 40px;
  width: 2px;
  height: calc(100% - 40px);
  background: #e0e0e0;
  z-index: 1;
}

.timeline-item.completed .timeline-connector {
  background: #4caf50;
}

.livreurs-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.livreur-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1rem;
  background: #fafafa;
}

.livreur-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.8rem;
  color: #333;
}

.livreur-details p {
  margin: 0.3rem 0;
  color: #666;
}

.livreur-route {
  font-size: 0.9rem;
  font-style: italic;
}

.loading, .error-message, .empty-state {
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

.empty-state {
  background-color: #f5f5f5;
  border-radius: 8px;
}

.empty-icon {
  font-size: 3rem;
  color: #bdbdbd;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 1rem;
  color: #616161;
}

.empty-state p {
  margin-bottom: 1.5rem;
  color: #757575;
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
  
  .annonces-grid {
    grid-template-columns: 1fr;
    padding: 1rem;
  }
  
  .segments-status {
    grid-template-columns: 1fr;
  }
  
  .livreurs-info {
    grid-template-columns: 1fr;
  }
  
  .timeline-item {
    gap: 0.5rem;
  }
  
  .timeline-connector {
    left: 14px;
  }
}
</style>
