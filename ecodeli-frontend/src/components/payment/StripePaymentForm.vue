<script>
import paymentService from '@/services/paymentService.js'

export default {
  name: 'StripePaymentForm',
  props: {
    annonceId: {
      type: Number,
      required: true
    },
    clientId: {
      type: Number,
      required: true
    },
    montant: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      stripeLoaded: false,
      stripe: null,
      elements: null,
      cardElement: null,
      cardValid: false,
      processing: false,
      paymentIntentCreated: false,
      paymentIntent: null,
      commission: 0,
      montantLivreur: 0,
      errorMessage: '',
      successMessage: ''
    }
  },
  async mounted() {
    await this.loadStripe()
    await this.createPaymentIntent()
  },
  methods: {
    async loadStripe() {
      try {
        if (!window.Stripe) {
          const script = document.createElement('script')
          script.src = 'https://js.stripe.com/v3/'
          script.onload = () => {
            this.initializeStripe()
          }
          document.head.appendChild(script)
        } else {
          this.initializeStripe()
        }
      } catch (error) {
        console.error('Erreur lors du chargement de Stripe:', error)
        this.errorMessage = 'Erreur lors du chargement du système de paiement'
      }
    },

    initializeStripe() {
      const stripePublicKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY || 'pk_test_...'
      this.stripe = window.Stripe(stripePublicKey)
      this.stripeLoaded = true
      this.setupElements()
    },

    setupElements() {
      this.$nextTick(() => {
        setTimeout(() => {
          const cardElementContainer = document.getElementById('card-element')
          if (!cardElementContainer) {
            console.error('Element #card-element not found')
            return
          }
          console.log('Initialisation des éléments Stripe...', cardElementContainer)
          this.elements = this.stripe.elements()

          const style = {
            base: {
              fontSize: '16px',
              color: '#424770',
              fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
              fontSmoothing: 'antialiased',
              '::placeholder': {
                color: '#aab7c4',
              },
            },
            invalid: {
              color: '#9e2146',
            },
          }

          this.cardElement = this.elements.create('card', {
            style,
            hidePostalCode: true
          })

          this.cardElement.mount('#card-element')

          console.log('Stripe Elements monté avec succès')

          this.cardElement.on('ready', () => {
            console.log('Stripe Element ready - peut maintenant recevoir le focus')
            setTimeout(() => {
              this.cardElement.focus()
            }, 100)
          })

          this.cardElement.on('focus', () => {
            console.log('Stripe Element a reçu le focus')
          })

          this.cardElement.on('blur', () => {
            console.log('Stripe Element a perdu le focus')
          })

          this.cardElement.on('change', (event) => {
            console.log('Stripe Element change:', event)
            const displayError = document.getElementById('card-errors')
            if (event.error) {
              displayError.textContent = event.error.message
              this.cardValid = false
            } else {
              displayError.textContent = ''
              this.cardValid = event.complete
            }
          })

        }, 200)
      })
    },

    async createPaymentIntent() {
      try {
        const response = await paymentService.createPaymentIntent(
          this.annonceId,
          this.clientId,
          this.montant
        )

        this.paymentIntent = response
        this.commission = response.commission
        this.montantLivreur = response.montantLivreur
        this.paymentIntentCreated = true

      } catch (error) {
        console.error('Erreur lors de la création du Payment Intent:', error)
        this.errorMessage = error.message || 'Erreur lors de la préparation du paiement'
      }
    },

    async processPayment() {
      if (!this.cardValid || this.processing) return

      this.processing = true
      this.errorMessage = ''
      this.successMessage = ''

      try {
        const { error, paymentIntent } = await this.stripe.confirmCardPayment(
          this.paymentIntent.clientSecret,
          {
            payment_method: {
              card: this.cardElement,
              billing_details: {
                name: 'Client EcoDeli',
              },
            }
          }
        )

        if (error) {
          this.errorMessage = error.message
        } else if (paymentIntent.status === 'succeeded') {
          this.successMessage = 'Paiement effectué avec succès ! Votre annonce est maintenant active.'

          try {
            await paymentService.confirmPayment(paymentIntent.id)
          } catch (serverError) {
            console.warn('Erreur confirmation serveur (webhook peut gérer):', serverError)
          }

          this.$emit('payment-success', {
            paymentIntentId: paymentIntent.id,
            annonceId: this.annonceId,
            montant: this.montant
          })

          setTimeout(() => {
            this.$router.push('/client/annonces')
          }, 3000)
        }

      } catch (error) {
        console.error('Erreur lors du paiement:', error)
        this.errorMessage = 'Erreur technique lors du paiement. Veuillez réessayer.'
      } finally {
        this.processing = false
      }
    },

    cancelPayment() {
      this.$emit('payment-cancel')
      this.$router.go(-1)
    },

    formatAmount(amount) {
      return paymentService.formatAmount(amount)
    },

    focusCardElement() {
      console.log('Tentative de focus sur le champ Stripe...')
      if (this.cardElement) {
        try {
          this.cardElement.focus()
          console.log('Focus appliqué avec succès')
        } catch (error) {
          console.error('Erreur lors du focus:', error)
        }
      } else {
        console.warn('cardElement non initialisé')
      }
    },

    debugStripeElement() {
      console.log('=== DEBUG STRIPE ELEMENT ===')
      console.log('Stripe loaded:', this.stripeLoaded)
      console.log('Stripe instance:', this.stripe)
      console.log('Elements instance:', this.elements)
      console.log('Card element:', this.cardElement)

      const container = document.getElementById('card-element')
      console.log('Container element:', container)
      console.log('Container innerHTML:', container?.innerHTML)

      if (this.cardElement) {
        console.log('Tentative de focus forcé...')
        this.cardElement.focus()

        setTimeout(() => {
          const stripeFrame = container?.querySelector('iframe')
          if (stripeFrame) {
            console.log('Frame Stripe trouvée, tentative d\'activation...')
            stripeFrame.focus()
          }
        }, 100)
      } else {
        console.log('Réinitialisation complète de Stripe Elements...')
        this.setupElements()
      }
    }
  }
}
</script>

<template>
  <div class="stripe-payment-form">
    <div class="payment-header">
      <h3>💳 Paiement sécurisé</h3>
      <p class="payment-info">
        Montant à payer : <strong>{{ formatAmount(montant) }}</strong>
        <br>
        <small>Commission EcoDeli (10%) : {{ formatAmount(commission) }}</small>
        <br>
        <small>Montant livreur : {{ formatAmount(montantLivreur) }}</small>
      </p>
    </div>

    <div v-if="!stripeLoaded" class="loading">
      <p>🔄 Chargement du système de paiement...</p>
    </div>

    <div v-else-if="!paymentIntentCreated" class="loading">
      <p>🔄 Préparation du paiement...</p>
    </div>

    <div v-else class="payment-form">
      <!-- Formulaire Stripe Elements -->
      <div class="stripe-elements-container">
        <label for="card-element" class="card-label">
          Informations de carte bancaire
        </label>
        <div id="card-element" class="stripe-element" @click="focusCardElement">
          <!-- Stripe Elements sera injecté ici -->
        </div>
        <div id="card-errors" role="alert" class="error-message"></div>

        <!-- Bouton de debug si le champ ne fonctionne pas -->
        <button
          v-if="!cardValid"
          @click="debugStripeElement"
          type="button"
          class="btn-debug"
        >
          🔧 Activer le champ de carte
        </button>
      </div>

      <!-- Boutons d'action -->
      <div class="payment-actions">
        <button
          @click="processPayment"
          :disabled="processing || !cardValid"
          class="btn-pay"
          :class="{ 'processing': processing }"
        >
          <span v-if="processing">🔄 Traitement...</span>
          <span v-else>🔒 Payer {{ formatAmount(montant) }}</span>
        </button>

        <button @click="cancelPayment" class="btn-cancel" :disabled="processing">
          Annuler
        </button>
      </div>

      <!-- Informations de sécurité -->
      <div class="security-info">
        <p>
          🔒 Paiement sécurisé par Stripe<br>
          <small>Vos données bancaires ne sont jamais stockées sur nos serveurs</small>
        </p>
      </div>
    </div>

    <!-- Messages d'erreur ou de succès -->
    <div v-if="errorMessage" class="error-alert">
      ❌ {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="success-alert">
      ✅ {{ successMessage }}
    </div>
  </div>
</template>

<style scoped>
.stripe-payment-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.payment-header {
  text-align: center;
  margin-bottom: 2rem;
}

.payment-header h3 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.payment-info {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #007bff;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}

.stripe-elements-container {
  margin-bottom: 2rem;
}

.card-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #495057;
  font-size: 0.9rem;
}

.stripe-element {
  border: 1px solid #ced4da;
  border-radius: 8px;
  padding: 1rem;
  background: white;
  min-height: 50px;
  cursor: text;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.stripe-element:hover {
  border-color: #80bdff;
}

.stripe-element:focus-within {
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.error-message {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.btn-debug {
  background: #ffc107;
  color: #212529;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: background 0.3s ease;
}

.btn-debug:hover {
  background: #e0a800;
}

.payment-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn-pay {
  flex: 1;
  background: #28a745;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-pay:hover:not(:disabled) {
  background: #218838;
  transform: translateY(-1px);
}

.btn-pay:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
}

.btn-pay.processing {
  background: #007bff;
}

.btn-cancel {
  background: #6c757d;
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.3s ease;
}

.btn-cancel:hover:not(:disabled) {
  background: #5a6268;
}

.security-info {
  text-align: center;
  color: #6c757d;
  font-size: 0.875rem;
  border-top: 1px solid #dee2e6;
  padding-top: 1rem;
}

.error-alert {
  background: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #dc3545;
  margin-top: 1rem;
}

.success-alert {
  background: #d4edda;
  color: #155724;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #28a745;
  margin-top: 1rem;
}

/* Responsive */
@media (max-width: 576px) {
  .stripe-payment-form {
    padding: 1rem;
    margin: 1rem;
  }
  
  .payment-actions {
    flex-direction: column;
  }
}
</style>
