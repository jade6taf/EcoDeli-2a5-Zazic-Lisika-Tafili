<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loadStripe } from '@stripe/stripe-js'
import { useToast } from 'primevue/usetoast'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const loading = ref(false)
const paymentProcessing = ref(false)
const annonce = ref(null)
const ibanClient = ref('')
const stripe = ref(null)
const elements = ref(null)
const cardElement = ref(null)
const clientSecret = ref('')
const paymentIntentId = ref('')

const isFormValid = computed(() => {
  return ibanClient.value.length >= 15 && cardElement.value
})

onMounted(async () => {
  try {
    await initStripe()
    await loadAnnonce()
    await createPaymentIntent()
  } catch (error) {
    console.error('Erreur lors de l\'initialisation:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Erreur lors du chargement du paiement',
      life: 5000
    })
  }
})

const initStripe = async () => {
  try {
    const configResponse = await axios.get('http://localhost:8080/api/payment/config')
    const publishableKey = configResponse.data.publishableKey

    stripe.value = await loadStripe(publishableKey)

    if (!stripe.value) {
      throw new Error('Impossible de charger Stripe')
    }

    elements.value = stripe.value.elements()

    cardElement.value = elements.value.create('card', {
      style: {
        base: {
          fontSize: '16px',
          color: '#424770',
          '::placeholder': {
            color: '#aab7c4',
          },
        },
      },
    })

    setTimeout(() => {
      const cardContainer = document.getElementById('card-element')
      if (cardContainer) {
        cardElement.value.mount('#card-element')
      }
    }, 100)

  } catch (error) {
    console.error('Erreur Stripe:', error)
    throw error
  }
}

const loadAnnonce = async () => {
  loading.value = true
  try {
    const annonceId = route.params.annonceId
    const response = await axios.get(`http://localhost:8080/api/annonces/${annonceId}`)
    annonce.value = response.data
  } catch (error) {
    console.error('Erreur lors du chargement de l\'annonce:', error)
    throw error
  } finally {
    loading.value = false
  }
}

const createPaymentIntent = async () => {
  try {
    const annonceId = route.params.annonceId
    const response = await axios.post(`http://localhost:8080/api/payment/create-intent/${annonceId}`)

    if (response.data.success) {
      clientSecret.value = response.data.clientSecret
      paymentIntentId.value = response.data.paymentIntentId
    } else {
      throw new Error(response.data.message)
    }
  } catch (error) {
    console.error('Erreur PaymentIntent:', error)
    throw error
  }
}

const handlePayment = async () => {
  if (!isFormValid.value) {
    toast.add({
      severity: 'warn',
      summary: 'Formulaire incomplet',
      detail: 'Veuillez remplir tous les champs',
      life: 3000
    })
    return
  }

  paymentProcessing.value = true

  try {
    const { error, paymentIntent } = await stripe.value.confirmCardPayment(clientSecret.value, {
      payment_method: {
        card: cardElement.value,
        billing_details: {
          name: 'Client EcoDeli',
        },
      }
    })

    if (error) {
      throw new Error(error.message)
    }

    if (paymentIntent.status === 'succeeded') {
      const confirmResponse = await axios.post(`http://localhost:8080/api/payment/confirm/${route.params.annonceId}`, {
        paymentIntentId: paymentIntentId.value,
        ibanClient: ibanClient.value
      })

      if (confirmResponse.data.success) {
        toast.add({
          severity: 'success',
          summary: 'Paiement réussi !',
          detail: 'Votre annonce a été payée avec succès',
          life: 4000
        })

        setTimeout(() => {
          router.push('/client')
        }, 2000)
      } else {
        throw new Error(confirmResponse.data.message)
      }
    }
  } catch (error) {
    console.error('Erreur paiement:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur de paiement',
      detail: error.message || 'Une erreur est survenue lors du paiement',
      life: 5000
    })
  } finally {
    paymentProcessing.value = false
  }
}

const goBack = () => {
  router.push('/client')
}
</script>

<template>
  <div class="ecodeli-layout">
    <!-- Header simplifié -->
    <header class="ecodeli-header">
      <div class="flex align-items-center justify-content-between p-4">
        <div class="flex align-items-center">
          <Button
            icon="pi pi-arrow-left"
            text
            rounded
            @click="goBack"
            class="mr-3"
          />
          <i class="pi pi-leaf text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
          <span class="text-xl font-semibold ecodeli-title">EcoDeli</span>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="ecodeli-content">
      <div class="max-w-4xl mx-auto">
        <!-- Loading -->
        <div v-if="loading" class="text-center p-6">
          <ProgressSpinner />
          <p class="mt-3">Chargement...</p>
        </div>

        <!-- Contenu principal -->
        <div v-else-if="annonce">
          <!-- En-tête -->
          <Card class="mb-4">
            <template #content>
              <div class="text-center">
                <h1 class="text-3xl font-bold ecodeli-title m-0">
                  <i class="pi pi-credit-card mr-3" style="color: var(--ecodeli-green)"></i>
                  Paiement de votre annonce
                </h1>
                <p class="text-600 mt-2">Finalisez le paiement pour publier votre demande de livraison</p>
              </div>
            </template>
          </Card>

          <!-- Récapitulatif de l'annonce -->
          <Card class="mb-4">
            <template #header>
              <div class="p-4 pb-0">
                <h2 class="text-xl font-semibold ecodeli-title m-0">
                  <i class="pi pi-file-edit mr-2"></i>
                  Récapitulatif de votre annonce
                </h2>
              </div>
            </template>
            <template #content>
              <div class="grid gap-4">
                <div class="col-12">
                  <strong>{{ annonce.titre }}</strong>
                </div>
                <div class="col-12 md:col-6">
                  <div class="flex align-items-center gap-2">
                    <i class="pi pi-map-marker text-green-600"></i>
                    <span class="font-medium">Départ:</span>
                    <span>{{ annonce.adresseDepart }}</span>
                  </div>
                </div>
                <div class="col-12 md:col-6">
                  <div class="flex align-items-center gap-2">
                    <i class="pi pi-flag text-red-600"></i>
                    <span class="font-medium">Arrivée:</span>
                    <span>{{ annonce.adresseFin }}</span>
                  </div>
                </div>
                <div class="col-12">
                  <div class="flex align-items-center gap-2">
                    <i class="pi pi-clone text-blue-600" v-if="annonce.livraisonPartielleAutorisee"></i>
                    <i class="pi pi-truck text-blue-600" v-else></i>
                    <span class="font-medium">Type:</span>
                    <span>{{ annonce.livraisonPartielleAutorisee ? 'Livraison partielle autorisée' : 'Livraison directe' }}</span>
                  </div>
                </div>
              </div>
            </template>
          </Card>

          <!-- Formulaire de paiement -->
          <Card>
            <template #header>
              <div class="p-4 pb-0">
                <h2 class="text-xl font-semibold ecodeli-title m-0">
                  <i class="pi pi-credit-card mr-2"></i>
                  Informations de paiement
                </h2>
              </div>
            </template>
            <template #content>
              <div class="grid gap-4">

                <!-- IBAN Client (fictif) -->
                <div class="col-12">
                  <label for="iban" class="font-medium text-900 mb-2 block">
                    <i class="pi pi-building mr-2"></i>
                    Votre IBAN (fictif)
                  </label>
                  <InputText
                    id="iban"
                    v-model="ibanClient"
                    placeholder="FR76 1234 5678 9012 3456 7890 123"
                    class="w-full"
                  />
                  <small class="text-600">
                    <i class="pi pi-info-circle mr-1"></i>
                    Saisissez un IBAN fictif pour la démonstration
                  </small>
                </div>

                <!-- Montant -->
                <div class="col-12">
                  <Card class="bg-green-50 border-green-200">
                    <template #content>
                      <div class="flex align-items-center justify-content-between">
                        <div>
                          <div class="font-medium text-green-800">Montant à payer</div>
                          <div class="text-sm text-green-600">Frais de livraison</div>
                        </div>
                        <div class="text-2xl font-bold text-green-800">
                          {{ annonce.prixUnitaire }}€
                        </div>
                      </div>
                    </template>
                  </Card>
                </div>

                <!-- Élément carte Stripe -->
                <div class="col-12">
                  <label class="font-medium text-900 mb-2 block">
                    <i class="pi pi-credit-card mr-2"></i>
                    Informations de carte
                  </label>
                  <div
                    id="card-element"
                    class="p-3 border-1 border-300 border-round"
                    style="min-height: 40px;"
                  ></div>
                  <small class="text-600">
                    <i class="pi pi-shield mr-1"></i>
                    Utilisez 4242 4242 4242 4242 pour tester
                  </small>
                </div>

                <!-- Boutons -->
                <div class="col-12">
                  <div class="flex justify-content-end gap-3 mt-4">
                    <Button
                      label="Annuler"
                      icon="pi pi-times"
                      severity="secondary"
                      outlined
                      @click="goBack"
                      :disabled="paymentProcessing"
                    />
                    <Button
                      :label="paymentProcessing ? 'Paiement en cours...' : 'Payer ' + annonce.prixUnitaire + '€'"
                      icon="pi pi-credit-card"
                      :loading="paymentProcessing"
                      :disabled="!isFormValid"
                      @click="handlePayment"
                    />
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </main>

    <!-- Toast -->
    <Toast />
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1rem;
}

.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
}

.max-w-4xl {
  max-width: 56rem;
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

.bg-green-50 {
  background-color: #f0fdf4;
}

.border-green-200 {
  border-color: #bbf7d0;
}

.text-green-800 {
  color: #166534;
}

.text-green-600 {
  color: #16a34a;
}

#card-element {
  background: white;
}
</style>
