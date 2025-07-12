<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAnnoncesStore } from '@/stores/annonces'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const annoncesStore = useAnnoncesStore()
const authStore = useAuthStore()
const toast = useToast()

const form = reactive({
  titre: '',
  description: '',
  adresseDepart: '',
  adresseFin: '',
  emailDestinataire: '',
  livraisonPartielleAutorisee: false,
  colis: {
    poids: '',
    longueur: '',
    largeur: '',
    hauteur: '',
    fragile: false,
    description: ''
  }
})

const errors = ref({})
const submitError = ref('')
const submitSuccess = ref('')
const calculatedPrice = ref(null)
const calculatedDistance = ref(null)
const isCalculatingPrice = ref(false)

const canCalculatePrice = computed(() => {
  return form.adresseDepart.trim() && form.adresseFin.trim()
})

const isFormValid = computed(() => {
  return form.titre.trim() &&
         form.description.trim() &&
         form.adresseDepart.trim() &&
         form.adresseFin.trim() &&
         form.colis.poids &&
         calculatedPrice.value !== null
})

const calculatePrice = async () => {
  if (!canCalculatePrice.value) return

  isCalculatingPrice.value = true
  try {
    const result = await annoncesStore.calculateDistanceAndPrice(
      form.adresseDepart,
      form.adresseFin
    )

    if (result.success) {
      calculatedDistance.value = result.distance
      calculatedPrice.value = result.prix

      toast.add({
        severity: 'success',
        summary: 'Prix calculé',
        detail: `Distance: ${result.distance} km - Prix: ${result.prix}€`,
        life: 3000
      })
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.message,
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur lors du calcul:', error)
  } finally {
    isCalculatingPrice.value = false
  }
}

const onAddressChange = () => {
  calculatedPrice.value = null
  calculatedDistance.value = null

  if (canCalculatePrice.value) {
    setTimeout(() => {
      if (canCalculatePrice.value) {
        calculatePrice()
      }
    }, 1000)
  }
}

const validateForm = () => {
  errors.value = {}

  if (!form.titre.trim()) {
    errors.value.titre = 'Titre requis'
  }

  if (!form.description.trim()) {
    errors.value.description = 'Description requise'
  }

  if (!form.adresseDepart.trim()) {
    errors.value.adresseDepart = 'Adresse de départ requise'
  }

  if (!form.adresseFin.trim()) {
    errors.value.adresseFin = 'Adresse d\'arrivée requise'
  }

  if (!form.colis.poids) {
    errors.value.poids = 'Poids requis'
  }

  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  submitError.value = ''
  submitSuccess.value = ''

  if (!validateForm()) {
    return
  }

  if (calculatedPrice.value === null) {
    toast.add({
      severity: 'warn',
      summary: 'Prix non calculé',
      detail: 'Veuillez attendre le calcul du prix',
      life: 3000
    })
    return
  }

  try {
    const expediteurId = authStore.user.id || authStore.user.idUtilisateur

    const annonceData = {
      titre: form.titre,
      description: form.description,
      adresseDepart: form.adresseDepart,
      adresseFin: form.adresseFin,
      emailDestinataire: form.emailDestinataire,
      livraisonPartielleAutorisee: form.livraisonPartielleAutorisee,
      expediteurId: expediteurId,
      colis: {
        poids: parseFloat(form.colis.poids),
        longueur: form.colis.longueur ? parseFloat(form.colis.longueur) : null,
        largeur: form.colis.largeur ? parseFloat(form.colis.largeur) : null,
        hauteur: form.colis.hauteur ? parseFloat(form.colis.hauteur) : null,
        fragile: form.colis.fragile,
        description: form.colis.description
      }
    }

    const result = await annoncesStore.createAnnonce(annonceData)

    if (result.success) {
      submitSuccess.value = 'Annonce créée avec succès ! Redirection vers le paiement...'

      toast.add({
        severity: 'success',
        summary: 'Annonce créée',
        detail: `Redirection vers le paiement pour "${form.titre}"`,
        life: 3000
      })

      setTimeout(() => {
        router.push(`/paiement/${result.data.idAnnonce}`)
      }, 1000)
    } else {
      submitError.value = result.message || 'Erreur lors de la création'
    }

  } catch (error) {
    console.error('Erreur lors de la création:', error)
    submitError.value = 'Une erreur inattendue s\'est produite'
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
        <div class="flex align-items-center gap-2">
          <span class="text-sm text-600">{{ authStore.userName }}</span>
          <Avatar
            :label="authStore.user?.prenom?.charAt(0) + authStore.user?.nom?.charAt(0)" 
            class="bg-primary text-white"
            shape="circle"
          />
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="ecodeli-content">
      <div class="max-w-4xl mx-auto">
        <!-- En-tête -->
        <Card class="mb-4">
          <template #content>
            <div class="text-center">
              <h1 class="text-3xl font-bold ecodeli-title m-0">
                <i class="pi pi-plus mr-3" style="color: var(--ecodeli-green)"></i>
                Nouvelle demande de livraison
              </h1>
              <p class="text-600 mt-2">Créez votre annonce de livraison de colis</p>
            </div>
          </template>
        </Card>

        <!-- Formulaire -->
        <Card>
          <template #content>
            <form @submit.prevent="handleSubmit" class="p-4">
              <div class="grid gap-4">

                <!-- Titre -->
                <div class="col-12">
                  <label for="titre" class="font-medium text-900 mb-2 block">Titre de l'annonce *</label>
                  <InputText
                    id="titre"
                    v-model="form.titre"
                    placeholder="Ex: Livraison urgent colis fragile"
                    class="w-full"
                    :class="{ 'p-invalid': errors.titre }"
                  />
                  <small v-if="errors.titre" class="p-error">{{ errors.titre }}</small>
                </div>

                <!-- Description -->
                <div class="col-12">
                  <label for="description" class="font-medium text-900 mb-2 block">Description *</label>
                  <Textarea
                    id="description"
                    v-model="form.description"
                    placeholder="Décrivez votre demande de livraison..."
                    rows="3"
                    class="w-full"
                    :class="{ 'p-invalid': errors.description }"
                  />
                  <small v-if="errors.description" class="p-error">{{ errors.description }}</small>
                </div>

                <!-- Adresses -->
                <div class="col-12 md:col-6">
                  <label for="adresseDepart" class="font-medium text-900 mb-2 block">Adresse de départ *</label>
                  <InputText
                    id="adresseDepart"
                    v-model="form.adresseDepart"
                    placeholder="Adresse de récupération"
                    class="w-full"
                    :class="{ 'p-invalid': errors.adresseDepart }"
                    @blur="onAddressChange"
                  />
                  <small v-if="errors.adresseDepart" class="p-error">{{ errors.adresseDepart }}</small>
                </div>

                <div class="col-12 md:col-6">
                  <label for="adresseFin" class="font-medium text-900 mb-2 block">Adresse d'arrivée *</label>
                  <InputText
                    id="adresseFin"
                    v-model="form.adresseFin"
                    placeholder="Adresse de livraison"
                    class="w-full"
                    :class="{ 'p-invalid': errors.adresseFin }"
                    @blur="onAddressChange"
                  />
                  <small v-if="errors.adresseFin" class="p-error">{{ errors.adresseFin }}</small>
                </div>

                <!-- Email destinataire -->
                <div class="col-12">
                  <label for="emailDestinataire" class="font-medium text-900 mb-2 block">
                    <i class="pi pi-envelope mr-2"></i>
                    Email du destinataire
                  </label>
                  <InputText
                    id="emailDestinataire"
                    v-model="form.emailDestinataire"
                    placeholder="email@exemple.fr"
                    type="email"
                    class="w-full"
                    :class="{ 'p-invalid': errors.emailDestinataire }"
                  />
                  <small class="text-600">
                    <i class="pi pi-info-circle mr-1"></i>
                    Un code de validation sera envoyé à cette adresse pour confirmer la livraison
                  </small>
                  <small v-if="errors.emailDestinataire" class="p-error block">{{ errors.emailDestinataire }}</small>
                </div>

                <!-- Options de livraison -->
                <div class="col-12">
                  <h3 class="text-xl font-semibold ecodeli-title mt-4 mb-3">Options de livraison</h3>

                  <div class="field-checkbox">
                    <Checkbox
                      id="livraisonPartielle"
                      v-model="form.livraisonPartielleAutorisee"
                      :binary="true"
                    />
                    <label for="livraisonPartielle" class="ml-2 font-medium text-900">
                      <i class="pi pi-clone mr-2"></i>
                      Autoriser la livraison partielle (2 livreurs + entrepôt)
                    </label>
                  </div>

                  <div v-if="form.livraisonPartielleAutorisee" class="mt-3 p-3 bg-blue-50 border-round border-1 border-blue-200">
                    <div class="flex align-items-start gap-2">
                      <i class="pi pi-info-circle text-blue-600 mt-1"></i>
                      <div class="text-sm text-blue-800">
                        <strong>Livraison partielle :</strong> Votre colis sera livré en 2 étapes par 2 livreurs différents.
                        <br />
                        • <strong>Segment 1 :</strong> Du point de départ vers un entrepôt EcoDeli
                        <br />
                        • <strong>Segment 2 :</strong> De l'entrepôt vers la destination finale
                        <br />
                        <em>Cette option peut permettre une livraison plus rapide et économique.</em>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Prix calculé -->
                <div class="col-12" v-if="calculatedPrice !== null || isCalculatingPrice">
                  <Card class="bg-green-50 border-green-200">
                    <template #content>
                      <div class="flex align-items-center justify-content-between">
                        <div>
                          <div class="font-medium text-green-800">Prix de la livraison</div>
                          <div class="text-sm text-green-600">Distance: {{ calculatedDistance }} km • Tarif: 0,80€/km</div>
                        </div>
                        <div class="text-2xl font-bold text-green-800">
                          <ProgressSpinner v-if="isCalculatingPrice" style="width: 30px; height: 30px" />
                          <span v-else>{{ calculatedPrice }}€</span>
                        </div>
                      </div>
                    </template>
                  </Card>
                </div>

                <!-- Informations colis -->
                <div class="col-12">
                  <h3 class="text-xl font-semibold ecodeli-title mt-4 mb-3">Informations du colis</h3>
                </div>

                <!-- Poids -->
                <div class="col-12 md:col-6 lg:col-3">
                  <label for="poids" class="font-medium text-900 mb-2 block">Poids (kg) *</label>
                  <InputNumber
                    id="poids"
                    v-model="form.colis.poids"
                    placeholder="0.0"
                    :min="0"
                    :max="1000"
                    :maxFractionDigits="2"
                    class="w-full"
                    :class="{ 'p-invalid': errors.poids }"
                  />
                  <small v-if="errors.poids" class="p-error">{{ errors.poids }}</small>
                </div>

                <!-- Dimensions -->
                <div class="col-12 md:col-6 lg:col-3">
                  <label for="longueur" class="font-medium text-900 mb-2 block">Longueur (cm)</label>
                  <InputNumber
                    id="longueur"
                    v-model="form.colis.longueur"
                    placeholder="0"
                    :min="0"
                    :max="500"
                    class="w-full"
                  />
                </div>

                <div class="col-12 md:col-6 lg:col-3">
                  <label for="largeur" class="font-medium text-900 mb-2 block">Largeur (cm)</label>
                  <InputNumber
                    id="largeur"
                    v-model="form.colis.largeur"
                    placeholder="0"
                    :min="0"
                    :max="500"
                    class="w-full"
                  />
                </div>

                <div class="col-12 md:col-6 lg:col-3">
                  <label for="hauteur" class="font-medium text-900 mb-2 block">Hauteur (cm)</label>
                  <InputNumber
                    id="hauteur"
                    v-model="form.colis.hauteur"
                    placeholder="0"
                    :min="0"
                    :max="500"
                    class="w-full"
                  />
                </div>

                <!-- Fragile -->
                <div class="col-12">
                  <div class="flex align-items-center">
                    <Checkbox
                      id="fragile"
                      v-model="form.colis.fragile"
                      :binary="true"
                    />
                    <label for="fragile" class="ml-2 font-medium text-900">Colis fragile</label>
                  </div>
                </div>

                <!-- Description colis -->
                <div class="col-12">
                  <label for="colisDescription" class="font-medium text-900 mb-2 block">Description du colis</label>
                  <Textarea
                    id="colisDescription"
                    v-model="form.colis.description"
                    placeholder="Décrivez le contenu du colis..."
                    rows="2"
                    class="w-full"
                  />
                </div>

              </div>

              <!-- Messages d'erreur/succès -->
              <div v-if="submitError" class="mt-4">
                <Message severity="error" :closable="false">
                  <i class="pi pi-exclamation-triangle mr-2"></i>
                  {{ submitError }}
                </Message>
              </div>

              <div v-if="submitSuccess" class="mt-4">
                <Message severity="success" :closable="false">
                  <i class="pi pi-check mr-2"></i>
                  {{ submitSuccess }}
                </Message>
              </div>

              <!-- Boutons -->
              <div class="flex justify-content-end gap-3 mt-6">
                <Button
                  label="Annuler"
                  icon="pi pi-times"
                  severity="secondary"
                  outlined
                  @click="goBack"
                  type="button"
                />
                <Button
                  type="submit"
                  :label="annoncesStore.loading ? 'Création en cours...' : 'Publier l\'annonce'"
                  icon="pi pi-check"
                  :loading="annoncesStore.loading"
                  :disabled="!isFormValid"
                />
              </div>
            </form>
          </template>
        </Card>
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
.col-3 { grid-column: span 3; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
}

@media (min-width: 1024px) {
  .lg\:col-3 { grid-column: span 3; }
}

.max-w-4xl {
  max-width: 56rem;
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

.bg-primary {
  background: var(--ecodeli-green) !important;
}

.text-primary {
  color: var(--ecodeli-green) !important;
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
</style>
