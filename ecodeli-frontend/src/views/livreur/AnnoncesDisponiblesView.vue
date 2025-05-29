<script>
export default {
  name: 'AnnoncesDisponiblesView',
  data() {
    return {
      annonces: [],
      user: null,
      isLoading: true,
      error: null,
      entrepots: [],
      showPartialModal: false,
      selectedAnnonce: null,
      selectedEntrepot: '',
      selectedSegment: 1,
      filtres: {
        adresse: '',
        minPrix: '',
        maxPrix: '',
        livraisonPartielle: false
      }
    }
  },
  computed: {
    annoncesFiltrees() {
      return this.annonces;
    },
    statutLabels() {
      return {
        'PUBLIEE': { text: 'Disponible', class: 'status-published' }
      }
    }
  },
  methods: {
    async fetchAnnonces() {
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
        const response = await fetch('/api/annonces/disponibles-livreurs', {
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

    async fetchEntrepots() {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/entrepots/disponibles', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (response.ok) {
          this.entrepots = await response.json();
        }
      } catch (err) {
        console.error('Erreur lors de la récupération des entrepôts:', err);
      }
    },

    async demanderValidation(idAnnonce) {
      if (!confirm('Êtes-vous sûr de vouloir prendre en charge cette livraison complète?')) {
        return;
      }
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/annonces/${idAnnonce}/demande-validation?idLivreur=${this.user.idUtilisateur}`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la prise en charge de l\'annonce');
        }
        alert('Vous avez pris en charge cette livraison avec succès!');
        this.fetchAnnonces();
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
      }
    },

    openPartialModal(annonce) {
      this.selectedAnnonce = annonce;
      this.selectedEntrepot = '';
      this.showPartialModal = true;
    },

    closePartialModal() {
      this.showPartialModal = false;
      this.selectedAnnonce = null;
      this.selectedEntrepot = '';
    },

    async confirmerLivraisonPartielle() {
      if (!this.selectedEntrepot) {
        alert('Veuillez sélectionner un entrepôt');
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(
          `/api/annonces/${this.selectedAnnonce.idAnnonce}/demande-validation-partielle?idLivreur=${this.user.idUtilisateur}&entrepotVille=${this.selectedEntrepot}`,
          {
            method: 'PUT',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          }
        );

        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la prise en charge partielle');
        }

        alert('Vous avez pris en charge le segment 1 de cette livraison avec succès!');
        this.closePartialModal();
        this.fetchAnnonces();
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
      }
    },
    appliquerFiltres() {
      this.fetchAnnonces();
    },
    resetFiltres() {
      this.filtres = {
        adresse: '',
        minPrix: '',
        maxPrix: ''
      };
      this.fetchAnnonces();
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
    getSegmentStatusClass(livreur) {
      return livreur ? 'segment-taken' : 'segment-available';
    },
    canTakeSegment1(annonce) {
      return annonce.livraisonPartielleAutorisee && !annonce.livreurSegment1;
    },
    canTakeSegment2(annonce) {
      return annonce.livraisonPartielleAutorisee && !annonce.livreurSegment2;
    },
    openSegmentModal(annonce, segment) {
      this.selectedAnnonce = annonce;
      this.selectedSegment = segment;
      this.selectedEntrepot = annonce.entrepotIntermediaire || '';
      this.showPartialModal = true;
    },
    async confirmerSegment() {
      if (this.selectedSegment === 2 && this.selectedAnnonce.entrepotIntermediaire) {
        this.selectedEntrepot = this.selectedAnnonce.entrepotIntermediaire;
      } else if (!this.selectedEntrepot) {
        alert('Veuillez sélectionner un entrepôt');
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const endpoint = this.selectedSegment === 1
          ? `/api/annonces/${this.selectedAnnonce.idAnnonce}/demande-validation-segment1`
          : `/api/annonces/${this.selectedAnnonce.idAnnonce}/demande-validation-segment2`;

        const params = new URLSearchParams({
          idLivreur: this.user.idUtilisateur
        });

        if (this.selectedSegment === 1 || !this.selectedAnnonce.entrepotIntermediaire) {
          params.append('entrepotVille', this.selectedEntrepot);
        }

        const response = await fetch(`${endpoint}?${params}`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la prise en charge du segment');
        }

        alert(`Vous avez pris en charge le segment ${this.selectedSegment} avec succès!`);
        this.closePartialModal();
        this.fetchAnnonces();
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
      }
    }
  },
  mounted() {
    this.fetchAnnonces();
    this.fetchEntrepots();
  }
}
</script>

<template>
  <div class="annonces-container">
    <div class="annonces-header">
      <h1>Annonces disponibles</h1>
      <router-link to="/livreur" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour au dashboard
      </router-link>
    </div>

    <div class="filters-container">
      <div class="filters-form">
        <div class="form-group">
          <label for="adresse">Adresse</label>
          <input
            id="adresse"
            v-model="filtres.adresse"
            type="text"
            placeholder="Ville ou code postal"
          >
        </div>
        <div class="form-group price-range">
          <label>Prix (€)</label>
          <div class="price-inputs">
            <input
              v-model="filtres.minPrix"
              type="number"
              min="0"
              step="0.01"
              placeholder="Min"
            >
            <span class="separator">-</span>
            <input
              v-model="filtres.maxPrix"
              type="number"
              min="0"
              step="0.01"
              placeholder="Max"
            >
          </div>
        </div>
        <div class="filters-actions">
          <button @click="appliquerFiltres" class="btn-filter">
            <i class="fas fa-search"></i> Filtrer
          </button>
          <button @click="resetFiltres" class="btn-reset">
            <i class="fas fa-times"></i> Réinitialiser
          </button>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement des annonces...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="annoncesFiltrees.length === 0" class="empty-state">
      <i class="fas fa-search empty-icon"></i>
      <h3>Aucune annonce disponible</h3>
      <p>Il n'y a actuellement aucune annonce correspondant à vos critères.</p>
    </div>

    <div v-else class="annonces-list">
      <div v-for="annonce in annoncesFiltrees" :key="annonce.idAnnonce" class="annonce-card">
        <div class="annonce-header">
          <h3>{{ annonce.titre }}</h3>
          <span class="status-badge" :class="statutLabels[annonce.statut]?.class">
            {{ statutLabels[annonce.statut]?.text || annonce.statut }}
          </span>
        </div>

        <div class="annonce-details">
          <p>{{ annonce.description }}</p>

          <div class="annonce-info">
            <div class="info-item">
              <i class="fas fa-map-marker-alt"></i>
              <span>De: {{ annonce.adresseDepart }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-map-marker"></i>
              <span>À: {{ annonce.adresseFin }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-calendar"></i>
              <span>Du {{ formatDate(annonce.dateDebut) }} au {{ formatDate(annonce.dateFin) }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-euro-sign"></i>
              <span>{{ annonce.prixUnitaire }} €</span>
            </div>
            <div class="info-item" v-if="annonce.colis">
              <i class="fas fa-box"></i>
              <span>{{ annonce.colis.poids }} kg - {{ annonce.colis.longueur }}×{{ annonce.colis.largeur }}×{{ annonce.colis.hauteur }} cm</span>
            </div>
            <div class="info-item" v-if="annonce.colis && annonce.colis.fragile">
              <i class="fas fa-glass-whiskey"></i>
              <span>Colis fragile</span>
            </div>
            <div class="info-item" v-if="annonce.livraisonPartielleAutorisee">
              <i class="fas fa-route"></i>
              <span class="partial-delivery-badge">Livraison partielle autorisée</span>
            </div>
            <!-- Affichage de l'état des segments -->
            <div v-if="annonce.livraisonPartielleAutorisee" class="segments-status">
              <div class="segment-status">
                <i class="fas fa-play"></i>
                <span>Segment 1:</span>
                <span :class="getSegmentStatusClass(annonce.livreurSegment1)">
                  {{ annonce.livreurSegment1 ? 'Pris' : 'Disponible' }}
                </span>
              </div>
              <div class="segment-status">
                <i class="fas fa-stop"></i>
                <span>Segment 2:</span>
                <span :class="getSegmentStatusClass(annonce.livreurSegment2)">
                  {{ annonce.livreurSegment2 ? 'Pris' : 'Disponible' }}
                </span>
              </div>
              <div v-if="annonce.entrepotIntermediaire" class="entrepot-info">
                <i class="fas fa-warehouse"></i>
                <span>Entrepôt: {{ annonce.entrepotIntermediaire }}</span>
              </div>
            </div>
          </div>

          <div class="contact-info">
            <div class="contact-item">
              <i class="fas fa-user"></i>
              <div>
                <h4>Expéditeur</h4>
                <p>{{ annonce.expediteur.prenom }} {{ annonce.expediteur.nom }}</p>
              </div>
            </div>
            <div class="contact-item">
              <i class="fas fa-user-friends"></i>
              <div>
                <h4>Destinataire</h4>
                <p>{{ annonce.destinataire.prenom }} {{ annonce.destinataire.nom }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="annonce-actions">
          <!-- Livraison complète (uniquement si aucun segment n'est pris) -->
          <button
            v-if="!annonce.livraisonPartielleAutorisee || (!annonce.livreurSegment1 && !annonce.livreurSegment2)"
            @click="demanderValidation(annonce.idAnnonce)"
            class="btn-action btn-primary">
            <i class="fas fa-check"></i> Livraison complète
          </button>

          <!-- Boutons pour les segments -->
          <template v-if="annonce.livraisonPartielleAutorisee">
            <button
              v-if="canTakeSegment1(annonce)"
              @click="openSegmentModal(annonce, 1)"
              class="btn-action btn-segment-1">
              <i class="fas fa-play"></i> Prendre segment 1
            </button>

            <button
              v-if="canTakeSegment2(annonce)"
              @click="openSegmentModal(annonce, 2)"
              class="btn-action btn-segment-2">
              <i class="fas fa-stop"></i> Prendre segment 2
            </button>
          </template>
        </div>
      </div>
    </div>

    <!-- Modal pour livraison partielle -->
    <div v-if="showPartialModal" class="modal-overlay" @click="closePartialModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Livraison partielle - Segment {{ selectedSegment }}</h3>
          <button @click="closePartialModal" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="modal-body">
          <div v-if="selectedAnnonce" class="annonce-summary">
            <h4>{{ selectedAnnonce.titre }}</h4>
            <p><strong>De :</strong> {{ selectedAnnonce.adresseDepart }}</p>
            <p><strong>À :</strong> {{ selectedAnnonce.adresseFin }}</p>
            <p><strong>Prix total :</strong> {{ selectedAnnonce.prixUnitaire }}€</p>
          </div>

          <div class="segment-info">
            <div class="info-box" :class="selectedSegment === 1 ? 'segment-1' : 'segment-2'">
              <h5>
                <i :class="selectedSegment === 1 ? 'fas fa-play' : 'fas fa-stop'"></i>
                Segment {{ selectedSegment }} (Votre mission)
              </h5>
              <p v-if="selectedSegment === 1">De l'adresse de départ vers un entrepôt intermédiaire</p>
              <p v-else>De l'entrepôt vers l'adresse de destination finale</p>
              <p><strong>Rémunération :</strong> {{ Math.round((selectedAnnonce?.prixUnitaire || 0) / 2) }}€</p>
            </div>

            <div class="info-box" :class="selectedSegment === 1 ? 'segment-2' : 'segment-1'">
              <h5>
                <i :class="selectedSegment === 1 ? 'fas fa-stop' : 'fas fa-play'"></i>
                Segment {{ selectedSegment === 1 ? 2 : 1 }} (Autre livreur)
              </h5>
              <p v-if="selectedSegment === 1">De l'entrepôt vers l'adresse de destination finale</p>
              <p v-else>De l'adresse de départ vers un entrepôt intermédiaire</p>
              <p><strong>Rémunération :</strong> {{ Math.round((selectedAnnonce?.prixUnitaire || 0) / 2) }}€</p>
            </div>
          </div>

          <div class="entrepot-selection" v-if="selectedSegment === 1 || !selectedAnnonce?.entrepotIntermediaire">
            <label for="entrepot-select">
              {{ selectedSegment === 1 ? 'Sélectionnez l\'entrepôt de dépôt :' : 'Sélectionnez l\'entrepôt de récupération :' }}
            </label>
            <select id="entrepot-select" v-model="selectedEntrepot" required>
              <option value="">-- Choisir un entrepôt --</option>
              <option v-for="entrepot in entrepots" :key="entrepot" :value="entrepot">
                {{ entrepot }}
              </option>
            </select>
          </div>

          <div v-else class="entrepot-info-fixed">
            <p><strong>Entrepôt de récupération :</strong> {{ selectedAnnonce.entrepotIntermediaire }}</p>
            <p class="info-text">L'entrepôt est déjà défini par le livreur du segment 1.</p>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closePartialModal" class="btn-cancel">
            Annuler
          </button>
          <button
            @click="confirmerSegment"
            class="btn-confirm"
            :disabled="(selectedSegment === 1 || !selectedAnnonce?.entrepotIntermediaire) && !selectedEntrepot">
            <i class="fas fa-check"></i> Confirmer le segment {{ selectedSegment }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.annonces-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.annonces-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
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

.filters-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.filters-form {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  align-items: flex-end;
}

.form-group {
  flex: 1;
  min-width: 200px;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.price-range {
  flex: 1;
  min-width: 200px;
}

.price-inputs {
  display: flex;
  align-items: center;
}

.price-inputs input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.separator {
  margin: 0 0.5rem;
  color: #666;
}

.filters-actions {
  display: flex;
  gap: 1rem;
}

.btn-filter, .btn-reset {
  padding: 0.8rem 1.2rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-filter {
  background-color: #4CAF50;
  color: white;
}

.btn-filter:hover {
  background-color: #45a049;
}

.btn-reset {
  background-color: #f1f1f1;
  color: #666;
}

.btn-reset:hover {
  background-color: #e0e0e0;
}

.annonces-list {
  display: grid;
  gap: 1.5rem;
}

.annonce-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  overflow: hidden;
}

.annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-published {
  background-color: #e3f2fd;
  color: #1976d2;
}

.annonce-details {
  padding: 1.5rem;
}

.annonce-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item i {
  margin-right: 0.5rem;
  color: #666;
  width: 18px;
  text-align: center;
}

.contact-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.contact-item i {
  font-size: 1.2rem;
  color: #3F51B5;
}

.contact-item h4 {
  margin-bottom: 0.5rem;
  color: #666;
  font-weight: 500;
}

.annonce-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background-color: #f8f8f8;
  border-top: 1px solid #eee;
}

.btn-action {
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  border: none;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #3F51B5;
  color: white;
}

.btn-primary:hover {
  background-color: #303f9f;
}

.btn-primary i {
  margin-right: 0.5rem;
}

.btn-secondary {
  background-color: #ff9800;
  color: white;
}

.btn-secondary:hover {
  background-color: #f57c00;
}

.btn-secondary i {
  margin-right: 0.5rem;
}

.partial-delivery-badge {
  color: #ff9800;
  font-weight: 600;
}

.segments-status {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-top: 1rem;
}

.segment-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.segment-available {
  color: #28a745;
  font-weight: 600;
}

.segment-taken {
  color: #dc3545;
  font-weight: 600;
}

.entrepot-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6c757d;
  font-weight: 500;
}

.btn-segment-1 {
  background-color: #28a745;
  color: white;
}

.btn-segment-1:hover {
  background-color: #218838;
}

.btn-segment-1 i {
  margin-right: 0.5rem;
}

.btn-segment-2 {
  background-color: #6c757d;
  color: white;
}

.btn-segment-2:hover {
  background-color: #5a6268;
}

.btn-segment-2 i {
  margin-right: 0.5rem;
}

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
  max-width: 600px;
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

.annonce-summary {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.annonce-summary h4 {
  margin: 0 0 1rem 0;
  color: #333;
}

.annonce-summary p {
  margin: 0.5rem 0;
  color: #666;
}

.segment-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 2rem;
}

.info-box {
  padding: 1.5rem;
  border-radius: 8px;
  border: 2px solid;
}

.segment-1 {
  border-color: #28a745;
  background: #f8fff9;
}

.segment-2 {
  border-color: #6c757d;
  background: #f8f9fa;
}

.info-box h5 {
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.segment-1 h5 {
  color: #28a745;
}

.segment-2 h5 {
  color: #6c757d;
}

.info-box p {
  margin: 0.5rem 0;
  color: #666;
}

.entrepot-selection {
  margin-top: 2rem;
}

.entrepot-selection label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.entrepot-selection select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  background: white;
}

.entrepot-selection select:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.entrepot-info-fixed {
  margin-top: 2rem;
  padding: 1rem;
  background-color: #e3f2fd;
  border-radius: 8px;
  border-left: 4px solid #2196f3;
}

.entrepot-info-fixed p {
  margin: 0.5rem 0;
  color: #333;
}

.entrepot-info-fixed .info-text {
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
  background: #28a745;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.btn-confirm:hover:not(:disabled) {
  background: #218838;
}

.btn-confirm:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
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
  .annonces-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .filters-form {
    flex-direction: column;
  }

  .contact-info {
    grid-template-columns: 1fr;
  }
}
</style>
