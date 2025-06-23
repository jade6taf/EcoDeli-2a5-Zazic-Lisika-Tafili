<script>
export default {
  name: 'MesLivraisonsView',
  data() {
    return {
      livraisons: [],
      user: null,
      isLoading: true,
      error: null,
      activeTab: 'en-cours',
      otpInputs: {}
    }
  },
    computed: {
        statutLabels() {
        return {
            'EN_ATTENTE_VALIDATION': { text: 'En attente', class: 'status-pending' },
            'VALIDEE': { text: 'Validée', class: 'status-validated' },
            'EN_COURS': { text: 'En cours', class: 'status-in-progress' },
            'ARRIVED': { text: 'Arrivé (attente OTP)', class: 'status-arrived' },
            'TERMINEE': { text: 'Terminée', class: 'status-completed' },
            'ANNULEE': { text: 'Annulée', class: 'status-cancelled' }
        }
        },
        livraisonsFiltrees() {
        if (this.activeTab === 'en-cours') {
            return this.livraisons.filter(livraison =>
            livraison.statut === 'EN_ATTENTE_VALIDATION' ||
            livraison.statut === 'VALIDEE' ||
            livraison.statut === 'EN_COURS' ||
            livraison.statut === 'ARRIVED'
            );
        } else if (this.activeTab === 'terminées') {
            return this.livraisons.filter(livraison =>
            livraison.statut === 'TERMINEE'
            );
        }
        return this.livraisons;
        }
    },
  methods: {
    async fetchLivraisons() {
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
        const response = await fetch(`/api/livraisons/livreur/${this.user.idUtilisateur}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération des livraisons');
        }
        this.livraisons = await response.json();
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isLoading = false;
      }
    },
    changeTab(tab) {
      this.activeTab = tab;
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

    async demarrerLivraison(idLivraison) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/livraisons/${idLivraison}/demarrer`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors du démarrage de la livraison');
        }
        this.fetchLivraisons();
        alert('La livraison a été démarrée avec succès!');
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
      }
    },
    async arriverALivraison(idLivraison) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/livraisons/${idLivraison}/arriver`, {
          method: 'PUT',
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => ({ message: 'Erreur lors de la mise à jour du statut "Arrivé".' }));
          throw new Error(errorData.message || 'Erreur lors de la mise à jour du statut "Arrivé".');
        }
        await this.fetchLivraisons();
        alert('Statut mis à jour à "Arrivé". Un OTP a été envoyé au destinataire.');
      } catch (err) {
        alert(err.message || 'Une erreur est survenue.');
      }
    },
    async confirmerParOtp(idLivraison) {
      const otp = this.otpInputs[idLivraison];
      if (!otp || otp.length !== 6) {
        alert('Veuillez saisir un code de validation à 6 chiffres.');
        return;
      }

      const livraison = this.livraisons.find(l => l.idLivraison === idLivraison);
      if (!livraison) return;

      try {
        const token = localStorage.getItem('token');
        let endpoint, successMessage;

        if (livraison.typeLivraison === 'PARTIELLE' && this.isLivreurSegment1(livraison)) {
          // Segment 1 : validation du dépôt entrepôt par le livreur
          endpoint = `/api/livraisons/${idLivraison}/confirmer-depot-segment-1`;
          successMessage = 'Dépôt à l\'entrepôt confirmé ! Le livreur segment 2 a été notifié.';
        } else {
          // Livraison simple ou segment 2 : validation par le client destinataire
          endpoint = `/api/livraisons/${idLivraison}/confirmer-otp`;
          successMessage = 'Livraison confirmée avec succès !';
        }

        const response = await fetch(endpoint, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ otp })
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({ message: 'Code incorrect ou expiré.' }));
          throw new Error(errorData.message || 'Code incorrect ou expiré.');
        }

        this.otpInputs[idLivraison] = '';
        await this.fetchLivraisons();
        alert(successMessage);
      } catch (err) {
        alert(err.message || 'Une erreur est survenue.');
      }
    },

    isLivreurSegment1(livraison) {
      return livraison.annonce &&
             livraison.annonce.livreur &&
             livraison.annonce.livreur.idUtilisateur === this.user.idUtilisateur;
    },

    getOtpMessage(livraison) {
      if (livraison.typeLivraison === 'PARTIELLE' && this.isLivreurSegment1(livraison)) {
        return 'Un code de validation a été envoyé à votre adresse email. Vérifiez votre boîte de réception.';
      } else {
        return 'Un code de validation a été envoyé au destinataire. Demandez-lui le code et saisissez-le ci-dessous.';
      }
    },

    getOtpPlaceholder(livraison) {
      if (livraison.typeLivraison === 'PARTIELLE' && this.isLivreurSegment1(livraison)) {
        return 'Code reçu par email (6 chiffres)';
      } else {
        return 'Code du destinataire (6 chiffres)';
      }
    },

    getOtpButtonText(livraison) {
      if (livraison.typeLivraison === 'PARTIELLE' && this.isLivreurSegment1(livraison)) {
        return 'Confirmer dépôt entrepôt';
      } else {
        return 'Confirmer livraison';
      }
    },
    async terminerLivraison(idLivraison) {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/livraisons/${idLivraison}/terminer`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la finalisation de la livraison');
        }
        this.fetchLivraisons();
        alert('La livraison a été marquée comme terminée (action manuelle)!');
      } catch (err) {
        alert(err.message || 'Une erreur est survenue');
      }
    }
  },
  mounted() {
    this.fetchLivraisons();
  }
}
</script>

<template>
  <div class="livraisons-container">
    <div class="livraisons-header">
      <h1>Mes Livraisons</h1>
      <router-link to="/livreur" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour au dashboard
      </router-link>
    </div>
    <div class="tabs">
      <button
        @click="changeTab('en-cours')"
        class="tab"
        :class="{ active: activeTab === 'en-cours' }">
        En cours
      </button>
      <button
        @click="changeTab('terminées')"
        class="tab"
        :class="{ active: activeTab === 'terminées' }">
        Terminées
      </button>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement des livraisons...</p>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="livraisonsFiltrees.length === 0" class="empty-state">
      <i class="fas fa-truck empty-icon"></i>
      <h3>Aucune livraison {{ activeTab === 'en-cours' ? 'en cours' : 'terminée' }}</h3>
      <p v-if="activeTab === 'en-cours'">
        Vous n'avez pas encore de livraisons en cours. Consultez les annonces disponibles pour en trouver.
      </p>
      <p v-else>
        Vous n'avez pas encore de livraisons terminées.
      </p>
      <router-link to="/livreur/annonces" class="btn-primary" v-if="activeTab === 'en-cours'">
        Voir les annonces disponibles
      </router-link>
    </div>

    <div v-else class="livraisons-list">
      <div v-for="livraison in livraisonsFiltrees" :key="livraison.idLivraison" class="livraison-card">
        <div class="livraison-header">
          <h3>Livraison #{{ livraison.idLivraison }}</h3>
          <span class="status-badge" :class="statutLabels[livraison.statut]?.class">
            {{ statutLabels[livraison.statut]?.text || livraison.statut }}
          </span>
        </div>
        <div class="livraison-details">
          <div class="addresses">
            <div class="address-item">
              <i class="fas fa-map-marker-alt"></i>
              <div>
                <h4>Adresse de départ</h4>
                <p>{{ livraison.adresseEnvoi }}</p>
                <p>{{ livraison.codePostalEnvoi }}</p>
              </div>
            </div>
            <div class="divider"><i class="fas fa-arrow-right"></i></div>
            <div class="address-item">
              <i class="fas fa-map-marker"></i>
              <div>
                <h4>Adresse d'arrivée</h4>
                <p>{{ livraison.adresseDeLivraison }}</p>
                <p>{{ livraison.codePostalLivraison }}</p>
              </div>
            </div>
          </div>
          <div class="livraison-info">
            <div class="info-item">
              <i class="fas fa-calendar"></i>
              <span>Du {{ formatDate(livraison.dateDebut) }} au {{ formatDate(livraison.dateFin) }}</span>
            </div>
            <div class="info-item">
              <i class="fas fa-euro-sign"></i>
              <span>{{ livraison.prix }} €</span>
            </div>
            <div class="info-item" v-if="livraison.colis">
              <i class="fas fa-box"></i>
              <span>Colis: {{ livraison.colis.description || 'Non spécifié' }}</span>
            </div>
          </div>
          <div class="contact-info">
            <div class="contact-item">
              <i class="fas fa-user"></i>
              <div>
                <h4>Expéditeur</h4>
                <p>{{ livraison.expediteur.prenom }} {{ livraison.expediteur.nom }}</p>
                <p v-if="livraison.expediteur.telephone">
                  <i class="fas fa-phone"></i> {{ livraison.expediteur.telephone }}
                </p>
              </div>
            </div>
            <div class="contact-item">
              <i class="fas fa-user-friends"></i>
              <div>
                <h4>Destinataire</h4>
                <p>{{ livraison.destinataire.prenom }} {{ livraison.destinataire.nom }}</p>
                <p v-if="livraison.destinataire.telephone">
                  <i class="fas fa-phone"></i> {{ livraison.destinataire.telephone }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="livraison-actions" v-if="activeTab === 'en-cours'">
            <button
                v-if="livraison.statut === 'VALIDEE'"
                @click="demarrerLivraison(livraison.idLivraison)"
                class="btn-action btn-start">
                <i class="fas fa-play"></i> Démarrer la livraison
            </button>
            <button
                v-if="livraison.statut === 'EN_COURS'"
                @click="arriverALivraison(livraison.idLivraison)"
                class="btn-action btn-arrived">
                <i class="fas fa-map-marker-alt"></i> Je suis arrivé
            </button>

            <div v-if="livraison.statut === 'ARRIVED'" class="validation-section">
              <p class="validation-message">
                {{ getOtpMessage(livraison) }}
              </p>
              <div class="otp-section">
                <input
                  type="text"
                  v-model="otpInputs[livraison.idLivraison]"
                  :placeholder="getOtpPlaceholder(livraison)"
                  maxlength="6"
                  class="otp-input"
                />
                <button
                  @click="confirmerParOtp(livraison.idLivraison)"
                  class="btn-action btn-confirm-otp">
                  <i class="fas fa-key"></i> {{ getOtpButtonText(livraison) }}
                </button>
              </div>
            </div>

            <button v-if="livraison.statut === 'EN_ATTENTE_VALIDATION'" class="btn-action btn-disabled" disabled>
                En attente de validation
            </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.livraisons-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.livraisons-header {
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

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid #ddd;
}

.tab {
  padding: 1rem 1.5rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-weight: 500;
  font-size: 1rem;
  transition: all 0.2s;
  color: #666;
}

.tab:hover {
  color: #3F51B5;
  background-color: #f8f9fa;
}

.tab.active {
  color: #3F51B5;
  border-bottom-color: #3F51B5;
}

.livraisons-list {
  display: grid;
  gap: 1.5rem;
}

.livraison-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  overflow: hidden;
}

.livraison-header {
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

.status-pending {
  background-color: #fff3e0;
  color: #e65100;
}

.status-validated {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-in-progress {
  background-color: #e8eaf6;
  color: #3f51b5;
}

.status-arrived {
  background-color: #fff9c4;
  color: #f57f17;
}

.status-completed {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.livraison-details {
  padding: 1.5rem;
}

.addresses {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
}

.address-item {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.address-item i {
  font-size: 1.5rem;
  color: #3F51B5;
}

.address-item h4 {
  margin-bottom: 0.5rem;
  color: #666;
  font-weight: 500;
}

.divider {
  padding: 0 1.5rem;
  color: #bdbdbd;
}

.livraison-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
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

.contact-item p {
  margin-bottom: 0.3rem;
}

.livraison-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background-color: #f8f8f8;
  border-top: 1px solid #eee;
}

.btn-action {
  padding: 0.8rem 1.2rem;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  flex: 1;
}

.btn-start {
  background-color: #3F51B5;
  color: white;
}

.btn-start:hover {
  background-color: #303f9f;
}

.btn-complete {
  background-color: #4CAF50;
  color: white;
}

.btn-complete:hover {
  background-color: #388e3c;
}

.btn-arrived {
  background-color: #FF9800;
  color: white;
}
.btn-arrived:hover {
  background-color: #F57C00;
}

.validation-section {
  width: 100%;
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 0.5rem;
  border-left: 4px solid #009688;
}

.validation-message {
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  color: #555;
  text-align: center;
  font-style: italic;
}

.otp-section {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}
.otp-input {
  padding: 0.7rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  flex-grow: 1;
}
.btn-confirm-otp {
  background-color: #009688;
  color: white;
}
.btn-confirm-otp:hover {
  background-color: #00796B;
}
.btn-complete-manual {
  background-color: #757575;
  color: white;
  font-size: 0.8rem;
}
.btn-complete-manual:hover {
  background-color: #616161;
}


.btn-disabled {
  background-color: #e0e0e0;
  color: #9e9e9e;
  cursor: not-allowed;
}

.btn-action i {
  margin-right: 0.5rem;
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
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.btn-primary {
  display: inline-block;
  background-color: #3F51B5;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: #303f9f;
}

@media (max-width: 768px) {
  .livraisons-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .addresses {
    flex-direction: column;
    gap: 1.5rem;
  }

  .divider {
    transform: rotate(90deg);
    padding: 0.5rem 0;
  }
}
</style>
