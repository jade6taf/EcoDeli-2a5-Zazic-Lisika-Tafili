<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePrestataireStore } from '@/stores/prestataire'

const router = useRouter()
const authStore = useAuthStore()
const prestataireStore = usePrestataireStore()

const loading = ref(false)
const demandes = ref([])
const totalRecords = ref(0)
const currentPage = ref(0)
const rowsPerPage = ref(10)

const filtres = ref({
  search: '',
  localisation: '',
  dateMin: null,
  dateMax: null
})

const candidatureDialog = ref(false)
const selectedDemande = ref(null)
const candidatureForm = ref({
  disponibilites: [],
  creneaux: [],
  delaiPropose: 2,
  messagePersonnalise: ''
})

const menuItems = [
  {
    label: 'Dashboard',
    icon: 'pi pi-home',
    command: () => router.push('/prestataire')
  },
  {
    label: 'Demandes disponibles',
    icon: 'pi pi-inbox',
    command: () => router.push('/prestataire/demandes')
  },
  {
    label: 'Mes candidatures',
    icon: 'pi pi-send',
    command: () => router.push('/prestataire/candidatures')
  },
  {
    label: 'Mes missions',
    icon: 'pi pi-briefcase',
    command: () => router.push('/prestataire/missions')
  },
  {
    label: 'Mes évaluations',
    icon: 'pi pi-star',
    command: () => router.push('/prestataire/evaluations')
  },
  {
    label: 'Mon Portefeuille',
    icon: 'pi pi-wallet',
    command: () => router.push('/prestataire/portefeuille')
  },
  {
    label: 'Mon Profil',
    icon: 'pi pi-user',
    command: () => router.push('/prestataire/profil')
  }
]

const creneauxOptions = [
  { label: '8h-12h', value: '8h-12h' },
  { label: '14h-18h', value: '14h-18h' },
  { label: '18h-22h', value: '18h-22h' },
  { label: 'Week-end', value: 'weekend' }
]

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const messageComplet = computed(() => {
  if (!selectedDemande.value) return ''
  
  const demande = selectedDemande.value
  const prestataire = authStore.user
  const tarif = prestataireStore.statutValidation?.tarifHoraire || 25
  
  let message = `Bonjour ${demande.client?.prenom || 'Monsieur/Madame'},\n\n`
  message += `Je suis disponible pour votre demande "${demande.titre}".\n`
  message += `Mon tarif est de ${tarif}€/h pour les services de transport & livraison.\n\n`
  
  if (candidatureForm.value.messagePersonnalise) {
    message += candidatureForm.value.messagePersonnalise + '\n\n'
  }
  
  message += `Cordialement,\n${prestataire?.prenom} ${prestataire?.nom}`
  
  return message
})

const getCategorieLabel = (categorie) => {
  const labels = {
    'TRANSPORT_LIVRAISON': 'Transport & Livraison',
    'SERVICES_DOMICILE': 'Services à Domicile',
    'TRAVAUX_REPARATIONS': 'Travaux & Réparations',
    'COURSES_ACHATS': 'Courses & Achats',
    'SERVICES_PERSONNELS': 'Services Personnels',
    'EDUCATION_FORMATION': 'Éducation & Formation'
  }
  return labels[categorie] || categorie
}

const formatDate = (dateString) => {
  if (!dateString) return 'Date non spécifiée'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    })
  } catch {
    return 'Date invalide'
  }
}

const loadDemandes = async () => {
  loading.value = true
  
  try {
    const filters = {}
    if (filtres.value.search) filters.search = filtres.value.search
    if (filtres.value.localisation) filters.localisation = filtres.value.localisation
    if (filtres.value.dateMin) filters.dateMin = filtres.value.dateMin
    if (filtres.value.dateMax) filters.dateMax = filtres.value.dateMax
    
    const result = await prestataireStore.getDemandesPaginated(
      currentPage.value,
      rowsPerPage.value,
      filters
    )
    
    if (result.success) {
      demandes.value = result.data.content || []
      totalRecords.value = result.data.totalElements || 0
    }
  } catch (error) {
    console.error('Erreur lors du chargement des demandes:', error)
  } finally {
    loading.value = false
  }
}

const onPageChange = (event) => {
  currentPage.value = event.page
  loadDemandes()
}

const appliquerFiltres = () => {
  currentPage.value = 0
  loadDemandes()
}

const reinitialiserFiltres = () => {
  filtres.value = {
    search: '',
    localisation: '',
    dateMin: null,
    dateMax: null
  }
  currentPage.value = 0
  loadDemandes()
}

const ouvrirCandidature = (demande) => {
  selectedDemande.value = demande
  candidatureForm.value = {
    disponibilites: [],
    creneaux: [],
    delaiPropose: 2,
    messagePersonnalise: ''
  }
  candidatureDialog.value = true
}

const envoyerCandidature = async () => {
  if (!selectedDemande.value) return
  
  try {
    const candidatureData = {
      demandeId: selectedDemande.value.idDemande,
      messagePersonnalise: candidatureForm.value.messagePersonnalise,
      delaiPropose: candidatureForm.value.delaiPropose,
      prestataireId: authStore.user?.id || 1
    }
    
    console.log('=== FRONTEND: Envoi candidature avec prestataireId ===')
    console.log('User connecté:', authStore.user)
    console.log('PrestataireId envoyé:', candidatureData.prestataireId)
    
    const result = await prestataireStore.creerCandidature(candidatureData)
    
    if (result.success) {
      candidatureDialog.value = false
      await loadDemandes()
    }
  } catch (error) {
    console.error('Erreur lors de l\'envoi de la candidature:', error)
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadDemandes()
})
</script>

<template>
  <div class="ecodeli-layout">
    <header class="ecodeli-header">
      <Menubar :model="menuItems" class="border-0">
        <template #start>
          <div class="flex align-items-center">
            <i class="pi pi-wrench text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-semibold ecodeli-title">EcoDeli - Demandes Disponibles</span>
          </div>
        </template>

        <template #end>
          <div class="flex align-items-center gap-2">
            <span class="text-sm text-600">{{ authStore.userName }}</span>
            <Avatar :label="userInitials" class="bg-primary text-white" shape="circle" />
            <Button icon="pi pi-sign-out" text rounded @click="handleLogout" />
          </div>
        </template>
      </Menubar>
    </header>

    <main class="ecodeli-content">
      <div class="grid">
        
        <!-- Filtres -->
        <div class="col-12">
          <Card class="mb-4">
            <template #title>
              <div class="flex align-items-center">
                <i class="pi pi-filter mr-2"></i>
                Filtres
              </div>
            </template>
            <template #content>
              <div class="grid">
                <div class="col-12 md:col-3">
                  <label for="search" class="font-semibold">Recherche</label>
                  <InputText 
                    id="search"
                    v-model="filtres.search" 
                    placeholder="Titre, description..."
                    class="w-full mt-1"
                  />
                </div>
                <div class="col-12 md:col-3">
                  <label for="localisation" class="font-semibold">Localisation</label>
                  <InputText 
                    id="localisation"
                    v-model="filtres.localisation" 
                    placeholder="Ville, quartier..."
                    class="w-full mt-1"
                  />
                </div>
                <div class="col-12 md:col-2">
                  <label for="dateMin" class="font-semibold">Date minimum</label>
                  <Calendar 
                    id="dateMin"
                    v-model="filtres.dateMin" 
                    class="w-full mt-1"
                    placeholder="Date min"
                  />
                </div>
                <div class="col-12 md:col-2">
                  <label for="dateMax" class="font-semibold">Date maximum</label>
                  <Calendar 
                    id="dateMax"
                    v-model="filtres.dateMax" 
                    class="w-full mt-1"
                    placeholder="Date max"
                  />
                </div>
                <div class="col-12 md:col-2 flex align-items-end">
                  <div class="w-full flex gap-2">
                    <Button 
                      label="Appliquer" 
                      icon="pi pi-search"
                      @click="appliquerFiltres"
                      class="flex-1"
                    />
                    <Button 
                      label="Reset" 
                      icon="pi pi-refresh"
                      severity="secondary"
                      @click="reinitialiserFiltres"
                      class="flex-1"
                    />
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Liste des demandes -->
        <div class="col-12">
          <Card>
            <template #title>
              <div class="flex justify-content-between align-items-center">
                <div class="flex align-items-center">
                  <i class="pi pi-inbox mr-2"></i>
                  Demandes Disponibles ({{ totalRecords }})
                </div>
                <Tag 
                  :value="getCategorieLabel(prestataireStore.statutValidation?.domaineExpertise)"
                  severity="info"
                />
              </div>
            </template>
            <template #content>
              <div v-if="loading" class="text-center p-4">
                <ProgressSpinner />
                <p class="mt-2">Chargement des demandes...</p>
              </div>
              
              <div v-else-if="demandes.length === 0" class="text-center p-4 text-600">
                <i class="pi pi-inbox text-4xl mb-3"></i>
                <p>Aucune demande disponible pour le moment</p>
                <small>Revenez plus tard ou modifiez vos filtres</small>
              </div>
              
              <div v-else class="space-y-3">
                <div
                  v-for="demande in demandes"
                  :key="demande.idDemande"
                  class="p-4 border-round surface-100 hover:surface-200 transition-colors"
                >
                  <div class="flex justify-content-between align-items-start">
                    <div class="flex-1">
                      <div class="flex align-items-center justify-content-between mb-2">
                        <h4 class="m-0 font-semibold text-primary">{{ demande.titre }}</h4>
                        <Tag 
                          :value="getCategorieLabel(demande.categorieService)"
                          severity="info"
                        />
                      </div>
                      
                      <p class="text-sm text-600 mb-3">{{ demande.description }}</p>
                      
                      <div class="grid">
                        <div class="col-12 md:col-6">
                          <div class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-user mr-2 text-blue-500"></i>
                            <span>{{ demande.client?.prenom }} {{ demande.client?.nom }}</span>
                          </div>
                          <div class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-calendar mr-2 text-green-500"></i>
                            <span>{{ formatDate(demande.dateSouhaitee) }}</span>
                          </div>
                        </div>
                        <div class="col-12 md:col-6">
                          <div class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-clock mr-2 text-orange-500"></i>
                            <span>{{ demande.creneauHoraire || 'Horaire flexible' }}</span>
                          </div>
                          <div class="flex align-items-center mb-2 text-sm">
                            <i class="pi pi-map-marker mr-2 text-red-500"></i>
                            <span>{{ demande.adresseDepart || 'Adresse non spécifiée' }}</span>
                          </div>
                        </div>
                      </div>
                      
                      <div class="flex justify-content-between align-items-center mt-3">
                        <div class="text-sm text-500">
                          Publié le {{ formatDate(demande.dateCreation) }}
                        </div>
                        <Button 
                          label="Candidater" 
                          icon="pi pi-send"
                          @click="ouvrirCandidature(demande)"
                          :disabled="!prestataireStore.peutCandidater"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Pagination -->
              <div v-if="totalRecords > rowsPerPage" class="mt-4">
                <Paginator 
                  :rows="rowsPerPage" 
                  :totalRecords="totalRecords"
                  :first="currentPage * rowsPerPage"
                  @page="onPageChange"
                />
              </div>
            </template>
          </Card>
        </div>

      </div>
    </main>

    <!-- Dialog candidature -->
    <Dialog 
      v-model:visible="candidatureDialog" 
      header="Candidater pour cette demande" 
      modal
      :style="{ width: '50rem' }"
      :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
    >
      <div v-if="selectedDemande" class="candidature-form">
        <!-- Résumé de la demande -->
        <Card class="mb-4">
          <template #title>Résumé de la demande</template>
          <template #content>
            <h4>{{ selectedDemande.titre }}</h4>
            <p>{{ selectedDemande.description }}</p>
            <div class="grid">
              <div class="col-6">
                <div><strong>Client :</strong> {{ selectedDemande.client?.prenom }} {{ selectedDemande.client?.nom }}</div>
                <div><strong>Date :</strong> {{ formatDate(selectedDemande.dateSouhaitee) }}</div>
              </div>
              <div class="col-6">
                <div><strong>Créneau :</strong> {{ selectedDemande.creneauHoraire || 'Flexible' }}</div>
                <div><strong>Lieu :</strong> {{ selectedDemande.adresseDepart }}</div>
              </div>
            </div>
          </template>
        </Card>

        <!-- Formulaire candidature -->
        <Card class="mb-4">
          <template #title>Votre candidature</template>
          <template #content>
            <div class="grid">
              <div class="col-12 md:col-6">
                <label for="delai" class="font-semibold">Délai proposé (jours)</label>
                <InputNumber 
                  id="delai"
                  v-model="candidatureForm.delaiPropose"
                  :min="1" 
                  :max="30"
                  class="w-full mt-1"
                />
              </div>
              <div class="col-12 md:col-6">
                <label for="creneaux" class="font-semibold">Créneaux préférés</label>
                <MultiSelect 
                  id="creneaux"
                  v-model="candidatureForm.creneaux"
                  :options="creneauxOptions"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="Sélectionnez vos créneaux"
                  class="w-full mt-1"
                />
              </div>
              <div class="col-12">
                <label for="message" class="font-semibold">Message personnalisé (optionnel)</label>
                <Textarea 
                  id="message"
                  v-model="candidatureForm.messagePersonnalise"
                  rows="4"
                  placeholder="Ajoutez des informations spécifiques sur votre expérience, équipement, etc."
                  class="w-full mt-1"
                />
              </div>
            </div>
          </template>
        </Card>

        <!-- Preview du message -->
        <Card>
          <template #title>Aperçu du message envoyé</template>
          <template #content>
            <div class="message-preview p-3 surface-50 border-round">
              <pre class="m-0 text-sm line-height-3">{{ messageComplet }}</pre>
            </div>
          </template>
        </Card>
      </div>

      <template #footer>
        <Button label="Annuler" @click="candidatureDialog = false" class="p-button-text" />
        <Button 
          label="Envoyer la candidature" 
          icon="pi pi-send"
          @click="envoyerCandidature"
          :loading="prestataireStore.loading"
        />
      </template>
    </Dialog>

    <Toast />
  </div>
</template>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(12, 1fr); gap: 1rem; }
.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }
.col-3 { grid-column: span 3; }
.col-2 { grid-column: span 2; }
.bg-primary { background: var(--ecodeli-green) !important; }
.space-y-3 > * + * { margin-top: 0.75rem; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-3 { grid-column: span 3; }
  .md\:col-2 { grid-column: span 2; }
}

.message-preview {
  white-space: pre-wrap;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.flex-1 { flex: 1; }
</style>