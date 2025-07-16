<template>
  <div class="demande-service">
    <!-- Header avec navigation -->
    <Card class="mb-4">
      <template #content>
        <div class="flex justify-content-between align-items-center">
          <div>
            <h1 class="text-3xl font-bold ecodeli-title m-0">
              {{ etapeActuelle === 1 ? 'Choisir les catégories' : 
                 etapeActuelle === 2 ? 'Décrire votre demande' : 
                 'Vérifier et publier' }}
            </h1>
            <p class="text-600 mt-2 mb-0">Étape {{ etapeActuelle }}/3</p>
          </div>
          <Button 
            icon="pi pi-arrow-left" 
            label="Retour" 
            outlined 
            @click="retourEtapePrecedente"
            :disabled="etapeActuelle === 1"
          />
        </div>
        
        <!-- Barre de progression -->
        <div class="mt-3">
          <ProgressBar :value="(etapeActuelle / 3) * 100" class="mb-2" />
          <div class="flex justify-content-between text-sm text-600">
            <span :class="{ 'text-primary font-medium': etapeActuelle >= 1 }">1. Catégories</span>
            <span :class="{ 'text-primary font-medium': etapeActuelle >= 2 }">2. Formulaire</span>
            <span :class="{ 'text-primary font-medium': etapeActuelle >= 3 }">3. Récapitulatif</span>
          </div>
        </div>
      </template>
    </Card>

    <!-- Étape 1: Sélection des catégories -->
    <div v-if="etapeActuelle === 1">
      <Card>
        <template #content>
          <h2 class="text-xl font-semibold mb-3">Sélectionnez vos catégories de service</h2>
          <p class="text-600 mb-4">Choisissez une ou plusieurs catégories correspondant à votre besoin.</p>
          
          <div v-if="loadingCategories" class="text-center p-4">
            <ProgressSpinner style="width: 30px; height: 30px" />
            <p class="mt-2">Chargement des catégories...</p>
          </div>
          
          <div v-else class="grid">
            <div 
              v-for="categorie in categories" 
              :key="categorie.id"
              class="col-12 md:col-6 lg:col-4"
            >
              <div 
                class="categorie-card p-4 border-round cursor-pointer transition-all duration-200"
                :class="{ 'categorie-selected': isCategorieSelectionnee(categorie.id) }"
                @click="toggleCategorie(categorie)"
              >
                <div class="text-center">
                  <span class="text-4xl block mb-2">{{ categorie.icon }}</span>
                  <h3 class="text-lg font-semibold mb-2" :style="{ color: categorie.couleur }">
                    {{ categorie.nom }}
                  </h3>
                  <p class="text-sm text-600 m-0">{{ categorie.description }}</p>
                </div>
                <div class="mt-3 text-center">
                  <Checkbox 
                    :modelValue="isCategorieSelectionnee(categorie.id)"
                    @click.stop
                    readonly
                  />
                </div>
              </div>
            </div>
          </div>

          <div class="mt-4 text-center">
            <Message v-if="categoriesSelectionnees.length === 0" severity="info" :closable="false">
              💡 Sélectionnez au moins une catégorie pour continuer
            </Message>
            <div v-else class="mb-3">
              <Tag 
                v-for="categorie in categoriesSelectionnees" 
                :key="categorie.id"
                :value="categorie.nom"
                class="mr-2 mb-2"
                :style="{ backgroundColor: categorie.couleur }"
              />
            </div>
            
            <Button 
              label="Continuer" 
              icon="pi pi-arrow-right" 
              iconPos="right"
              :disabled="categoriesSelectionnees.length === 0"
              @click="etapeSuivante"
              size="large"
            />
          </div>
        </template>
      </Card>
    </div>

    <!-- Étape 2: Formulaire dynamique -->
    <div v-if="etapeActuelle === 2">
      <Card>
        <template #content>
          <h2 class="text-xl font-semibold mb-3">Décrivez votre demande</h2>
          <div class="mb-3">
            <span class="text-sm text-600">Catégories sélectionnées : </span>
            <Tag 
              v-for="categorie in categoriesSelectionnees" 
              :key="categorie.id"
              :value="`${categorie.icon} ${categorie.nom}`"
              class="mr-2"
              size="small"
            />
          </div>

          <!-- Champs communs -->
          <div class="champs-communs mb-4">
            <h3 class="text-lg font-medium mb-3">📋 Informations générales</h3>
            
            <div class="field mb-3">
              <label for="titre" class="field-label mb-2">
                Titre de votre demande <span class="text-red-500">*</span>
              </label>
              <InputText 
                id="titre"
                v-model="formulaire.titre" 
                placeholder="ex: Ménage hebdomadaire + courses"
                class="w-full"
                :class="{ 'p-invalid': errors.titre }"
              />
              <small v-if="errors.titre" class="p-error">{{ errors.titre }}</small>
            </div>

            <div class="field mb-3">
              <label for="description" class="field-label mb-2">
                Description détaillée <span class="text-red-500">*</span>
              </label>
              <Textarea 
                id="description"
                v-model="formulaire.description" 
                placeholder="Décrivez précisément votre besoin..."
                rows="4"
                class="w-full"
                :class="{ 'p-invalid': errors.description }"
              />
              <small v-if="errors.description" class="p-error">{{ errors.description }}</small>
            </div>

            <div class="grid">
              <div class="col-12 md:col-6">
                <div class="field mb-3">
                  <label for="adresseDepart" class="field-label mb-2">
                    Adresse de départ <span class="text-red-500">*</span>
                  </label>
                  <InputText 
                    id="adresseDepart"
                    v-model="formulaire.adresseDepart" 
                    placeholder="ex: 25 rue de la République, 75011 Paris"
                    class="w-full"
                    :class="{ 'p-invalid': errors.adresseDepart }"
                  />
                  <small v-if="errors.adresseDepart" class="p-error">{{ errors.adresseDepart }}</small>
                </div>
              </div>
              
              <div class="col-12 md:col-6">
                <div class="field mb-3">
                  <label for="adresseArrivee" class="field-label mb-2">
                    Adresse d'arrivée (optionnel)
                  </label>
                  <InputText 
                    id="adresseArrivee"
                    v-model="formulaire.adresseArrivee" 
                    placeholder="ex: Même adresse ou lieu spécifique"
                    class="w-full"
                  />
                </div>
              </div>
            </div>

            <div class="grid">
              <div class="col-12 md:col-6">
                <div class="field mb-3">
                  <label for="dateSouhaitee" class="field-label mb-2">
                    Date souhaitée <span class="text-red-500">*</span>
                  </label>
                  <Calendar 
                    id="dateSouhaitee"
                    v-model="formulaire.dateSouhaitee" 
                    placeholder="Sélectionnez une date"
                    class="w-full"
                    :class="{ 'p-invalid': errors.dateSouhaitee }"
                    :minDate="new Date()"
                    dateFormat="dd/mm/yy"
                  />
                  <small v-if="errors.dateSouhaitee" class="p-error">{{ errors.dateSouhaitee }}</small>
                </div>
              </div>
              
              <div class="col-12 md:col-6">
                <div class="field mb-3">
                  <label for="creneauHoraire" class="field-label mb-2">
                    Créneaux horaires souhaités
                  </label>
                  <InputText 
                    id="creneauHoraire"
                    v-model="formulaire.creneauHoraire" 
                    placeholder="ex: Matin (9h-12h), flexible"
                    class="w-full"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Champs spécifiques dynamiques -->
          <div v-if="categoriesSelectionnees.length > 0" class="champs-specifiques mb-4">
            <h3 class="text-lg font-medium mb-3">🔧 Détails spécifiques</h3>
            <DynamicServiceForm 
              :categories="categoriesSelectionnees"
              v-model="formulaire.detailsSpecifiques"
            />
          </div>

          <Divider />
          
          <div class="flex justify-content-between">
            <Button 
              label="Retour" 
              icon="pi pi-arrow-left" 
              outlined
              @click="retourEtapePrecedente"
            />
            <Button 
              label="Continuer" 
              icon="pi pi-arrow-right" 
              iconPos="right"
              @click="etapeSuivante"
            />
          </div>
        </template>
      </Card>
    </div>

    <!-- Étape 3: Récapitulatif -->
    <div v-if="etapeActuelle === 3">
      <Card>
        <template #content>
          <h2 class="text-xl font-semibold mb-3">Vérifiez votre demande</h2>
          <p class="text-600 mb-4">Relisez attentivement avant de publier votre demande.</p>

          <div class="recapitulatif p-4 surface-100 border-round mb-4">
            <h3 class="text-lg font-medium mb-3">📋 {{ formulaire.titre }}</h3>
            
            <div class="mb-3">
              <strong>🎯 Catégories :</strong>
              <Tag 
                v-for="categorie in categoriesSelectionnees" 
                :key="categorie.id"
                :value="`${categorie.icon} ${categorie.nom}`"
                class="ml-2 mr-1"
                size="small"
              />
            </div>

            <div class="mb-3">
              <strong>📝 Description :</strong>
              <p class="mt-1 mb-0">{{ formulaire.description }}</p>
            </div>

            <div class="grid mb-3">
              <div class="col-12 md:col-6">
                <strong>📍 Adresse de départ :</strong>
                <p class="mt-1 mb-0">{{ formulaire.adresseDepart }}</p>
              </div>
              <div v-if="formulaire.adresseArrivee" class="col-12 md:col-6">
                <strong>📍 Adresse d'arrivée :</strong>
                <p class="mt-1 mb-0">{{ formulaire.adresseArrivee }}</p>
              </div>
            </div>

            <div class="grid mb-3">
              <div class="col-12 md:col-6">
                <strong>📅 Date souhaitée :</strong>
                <p class="mt-1 mb-0">{{ formatDate(formulaire.dateSouhaitee) }}</p>
              </div>
              <div v-if="formulaire.creneauHoraire" class="col-12 md:col-6">
                <strong>⏰ Créneaux :</strong>
                <p class="mt-1 mb-0">{{ formulaire.creneauHoraire }}</p>
              </div>
            </div>

            <div v-if="Object.keys(formulaire.detailsSpecifiques).length > 0" class="mb-3">
              <strong>🔧 Détails spécifiques :</strong>
              <div class="mt-2">
                <div v-for="(value, key) in formulaire.detailsSpecifiques" :key="key" class="mb-1">
                  <span class="font-medium">{{ getFieldLabel(key) }} :</span>
                  <span class="ml-2">{{ formatFieldValue(value) }}</span>
                </div>
              </div>
            </div>
          </div>

          <Card class="mb-4" style="background: #f0f9ff;">
            <template #content>
              <h4 class="text-lg font-medium mb-3">✅ Que va-t-il se passer ?</h4>
              <ul class="list-none p-0 m-0">
                <li class="flex align-items-center mb-2">
                  <i class="pi pi-check-circle text-green-500 mr-2"></i>
                  Votre demande sera visible par nos prestataires validés
                </li>
                <li class="flex align-items-center mb-2">
                  <i class="pi pi-check-circle text-green-500 mr-2"></i>
                  Vous recevrez les candidatures avec tarifs par email
                </li>
                <li class="flex align-items-center mb-2">
                  <i class="pi pi-check-circle text-green-500 mr-2"></i>
                  Vous pourrez choisir le prestataire qui vous convient
                </li>
                <li class="flex align-items-center mb-0">
                  <i class="pi pi-check-circle text-green-500 mr-2"></i>
                  Le paiement se fera après réalisation du service
                </li>
              </ul>
            </template>
          </Card>

          <div class="flex justify-content-between">
            <Button 
              label="Modifier" 
              icon="pi pi-pencil" 
              outlined
              @click="retourEtapePrecedente"
            />
            <Button 
              label="🚀 Publier ma demande" 
              :loading="loading"
              @click="publierDemande"
              size="large"
            />
          </div>
        </template>
      </Card>
    </div>

    <Toast />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useServicesStore } from '@/stores/services'
import { useToast } from 'primevue/usetoast'
import DynamicServiceForm from '@/components/DynamicServiceForm.vue'

const router = useRouter()
const authStore = useAuthStore()
const servicesStore = useServicesStore()
const toast = useToast()

const etapeActuelle = ref(1)
const categories = ref([])
const categoriesSelectionnees = ref([])
const loadingCategories = ref(false)
const loading = ref(false)
const errors = ref({})

const formulaire = ref({
  titre: '',
  description: '',
  adresseDepart: '',
  adresseArrivee: '',
  dateSouhaitee: null,
  creneauHoraire: '',
  detailsSpecifiques: {}
})

onMounted(async () => {
  loadingCategories.value = true
  const result = await servicesStore.chargerCategories()
  
  if (result.success) {
    categories.value = result.data
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les catégories',
      life: 3000
    })
  }
  loadingCategories.value = false
})

const isCategorieSelectionnee = (categorieId) => {
  return categoriesSelectionnees.value.some(cat => cat.id === categorieId)
}

const toggleCategorie = (categorie) => {
  const index = categoriesSelectionnees.value.findIndex(cat => cat.id === categorie.id)
  if (index > -1) {
    categoriesSelectionnees.value.splice(index, 1)
  } else {
    categoriesSelectionnees.value.push(categorie)
  }
}

const etapeSuivante = () => {
  if (etapeActuelle.value === 2 && !validerFormulaire()) {
    return
  }
  
  etapeActuelle.value++
}

const retourEtapePrecedente = () => {
  if (etapeActuelle.value > 1) {
    etapeActuelle.value--
  } else {
    router.push('/client')
  }
}

const validerFormulaire = () => {
  errors.value = {}
  
  if (!formulaire.value.titre.trim()) {
    errors.value.titre = 'Le titre est obligatoire'
  }
  
  if (!formulaire.value.description.trim()) {
    errors.value.description = 'La description est obligatoire'
  }
  
  if (!formulaire.value.adresseDepart.trim()) {
    errors.value.adresseDepart = 'L\'adresse de départ est obligatoire'
  }
  
  if (!formulaire.value.dateSouhaitee) {
    errors.value.dateSouhaitee = 'La date souhaitée est obligatoire'
  }
  
  return Object.keys(errors.value).length === 0
}

const publierDemande = async () => {
  if (!validerFormulaire()) {
    toast.add({
      severity: 'warn',
      summary: 'Formulaire incomplet',
      detail: 'Veuillez corriger les erreurs',
      life: 3000
    })
    return
  }

  loading.value = true
  
  console.log('AuthStore user:', authStore.user)
  console.log('User ID:', authStore.user?.id)
  
  if (!authStore.user || !authStore.user.id) {
    toast.add({
      severity: 'error',
      summary: 'Erreur d\'authentification',
      detail: 'Utilisateur non connecté ou ID manquant',
      life: 3000
    })
    loading.value = false
    return
  }
  
  const demandeData = {
    titre: formulaire.value.titre,
    description: formulaire.value.description,
    adresseDepart: formulaire.value.adresseDepart,
    adresseArrivee: formulaire.value.adresseArrivee || null,
    dateSouhaitee: formulaire.value.dateSouhaitee ? formatDateForBackend(formulaire.value.dateSouhaitee) : null,
    creneauHoraire: formulaire.value.creneauHoraire || null,
    detailsSpecifiques: formulaire.value.detailsSpecifiques || {},
    categoriesService: categoriesSelectionnees.value.map(cat => cat.backendId || cat.id),
    client: { idUtilisateur: authStore.user.id }
  }

  console.log('Données envoyées au backend:', demandeData)
  console.log('Client object envoyé:', demandeData.client)

  const result = await servicesStore.creerDemandeService(demandeData)
  
  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Demande publiée !',
      detail: 'Votre demande a été publiée avec succès',
      life: 4000
    })
    
    router.push('/client')
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.error || 'Erreur lors de la publication',
      life: 3000
    })
  }
  
  loading.value = false
}

const formatDateForBackend = (date) => {
  if (!date) return null
  if (typeof date === 'string') return date
  
  return date.toISOString().split('T')[0]
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('fr-FR')
}

const getFieldLabel = (key) => {
  return key.replace(/([A-Z])/g, ' $1').toLowerCase()
}

const formatFieldValue = (value) => {
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  return value
}
</script>

<style scoped>
.categorie-card {
  border: 2px solid #e5e7eb;
  background: white;
  transition: all 0.3s ease;
}

.categorie-card:hover {
  border-color: var(--ecodeli-green);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.categorie-selected {
  border-color: var(--ecodeli-green) !important;
  background: #f0f9ff !important;
}

.field-label {
  display: block;
  font-weight: 500;
  color: #374151;
}

.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1rem;
}

.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }
.col-4 { grid-column: span 4; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-4 { grid-column: span 4; }
}

@media (min-width: 1024px) {
  .lg\:col-4 { grid-column: span 4; }
}
</style>