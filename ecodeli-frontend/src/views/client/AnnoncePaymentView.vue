<script>
import StripePaymentForm from '@/components/payment/StripePaymentForm.vue'

export default {
  name: 'AnnoncePaymentView',
  components: {
    StripePaymentForm
  },
  data() {
    return {
      annonce: null,
      user: null,
      isLoading: true,
      error: null
    }
  },
  methods: {
    async fetchAnnonce() {
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

        const annonceId = this.$route.params.id;
        const response = await fetch(`/api/annonces/${annonceId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Erreur lors de la récupération de l\'annonce');
        }

        this.annonce = await response.json();

        if (this.annonce.expediteur.idUtilisateur !== this.user.idUtilisateur) {
          throw new Error('Vous n\'êtes pas autorisé à payer cette annonce');
        }

      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isLoading = false;
      }
    },

    getColisDetails(annonce) {
      if (!annonce.colis) return 'Non spécifié';

      const dimensions = `${annonce.colis.longueur || 0}×${annonce.colis.largeur || 0}×${annonce.colis.hauteur || 0} cm`;
      const poids = `${annonce.colis.poids || 0} kg`;

      return `${dimensions}, ${poids}${annonce.colis.fragile ? ', Fragile' : ''}`;
    },

    onPaymentSuccess(result) {
      console.log('Paiement réussi:', result);

      this.$router.push({
        path: '/client/annonces',
        query: { paymentSuccess: 'true' }
      });
    },

    onPaymentError(error) {
      console.error('Erreur de paiement:', error);
      this.error = `Erreur de paiement: ${error.message || 'Une erreur est survenue'}`;
    }
  },

  mounted() {
    this.fetchAnnonce();
  }
}
</script>

<template>
  <div class="payment-container">
    <div class="payment-header">
      <h1>Paiement de l'annonce</h1>
      <router-link to="/client/annonces" class="back-link">
        <i class="fas fa-arrow-left"></i> Retour aux annonces
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      Chargement de l'annonce...
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="annonce" class="payment-content">
      <!-- Récapitulatif de l'annonce -->
      <div class="annonce-summary">
        <h2>Récapitulatif de votre commande</h2>
        <div class="summary-card">
          <h3>{{ annonce.titre }}</h3>
          <p>{{ annonce.description }}</p>

          <div class="summary-details">
            <div class="detail-item">
              <i class="fas fa-map-marker-alt"></i>
              <span>De: {{ annonce.adresseDepart }}</span>
            </div>
            <div class="detail-item">
              <i class="fas fa-map-marker"></i>
              <span>À: {{ annonce.adresseFin }}</span>
            </div>
            <div class="detail-item" v-if="annonce.colis">
              <i class="fas fa-box"></i>
              <span>{{ getColisDetails(annonce) }}</span>
            </div>
          </div>

          <div class="price-breakdown">
            <div class="price-line">
              <span>Prix de la livraison :</span>
              <span class="price">{{ annonce.prixUnitaire }} €</span>
            </div>
            <div class="price-line total">
              <span><strong>Total à payer :</strong></span>
              <span class="price"><strong>{{ annonce.prixUnitaire }} €</strong></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Formulaire de paiement Stripe -->
      <div class="payment-form-section">
        <h2>Informations de paiement</h2>
        <StripePaymentForm
          :annonce-id="annonce.idAnnonce"
          :client-id="user.idUtilisateur"
          :montant="annonce.prixUnitaire"
          @payment-success="onPaymentSuccess"
          @payment-error="onPaymentError"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.payment-header h1 {
  color: var(--text-primary);
  margin: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.back-link:hover {
  color: var(--primary-hover);
}

.back-link i {
  margin-right: 0.5rem;
}

.payment-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.annonce-summary h2,
.payment-form-section h2 {
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-size: 1.4rem;
}

.summary-card {
  background-color: var(--card-bg);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
}

.summary-card h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.summary-card p {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.summary-details {
  margin: 1rem 0;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.detail-item i {
  margin-right: 0.5rem;
  color: var(--text-secondary);
  width: 18px;
  text-align: center;
}

.price-breakdown {
  border-top: 1px solid var(--border-color);
  padding-top: 1rem;
  margin-top: 1rem;
}

.price-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.price-line.total {
  border-top: 1px solid var(--border-color);
  padding-top: 0.5rem;
  margin-top: 0.5rem;
  font-size: 1.1rem;
}

.price {
  color: var(--primary-color);
  font-weight: 600;
}

.payment-form-section {
  background-color: var(--card-bg);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: var(--shadow);
  height: fit-content;
}

.loading, .error-message {
  text-align: center;
  padding: 2rem;
  color: var(--text-primary);
}

.error-message {
  color: var(--error-color);
  background-color: var(--card-bg);
  border-radius: 8px;
  border: 1px solid var(--error-color);
}

@media (max-width: 768px) {
  .payment-content {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .payment-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
}
</style>
