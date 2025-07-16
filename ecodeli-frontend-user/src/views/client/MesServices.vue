<template>
  <div class="mes-services">
    <!-- Header avec navigation -->
    <Card class="mb-4">
      <template #content>
        <div class="flex justify-content-between align-items-center">
          <div>
            <h1 class="text-3xl font-bold ecodeli-title m-0">Mes Services</h1>
            <p class="text-600 mt-2 mb-0">
              Suivez vos demandes et validez les services terminés
            </p>
          </div>
          <div class="flex gap-2">
            <Button 
              icon="pi pi-arrow-left" 
              label="Retour" 
              outlined 
              @click="retourDashboard"
            />
            <Button 
              icon="pi pi-refresh" 
              label="Actualiser" 
              outlined
              @click="chargerServices"
              :loading="loading"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Statistiques services -->
    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-blue-500">{{ statistiques.total || 0 }}</div>
            <div class="text-600">Total services</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-orange-500">{{ statistiques.enCours || 0 }}</div>
            <div class="text-600">En cours</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-red-500">{{ statistiques.aValider || 0 }}</div>
            <div class="text-600">À valider</div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-green-500">{{ statistiques.termines || 0 }}</div>
            <div class="text-600">Terminés</div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Alerte missions à valider -->
    <Card v-if="missionsAValider.length > 0" class="mb-4" style="background: #fef3c7;">
      <template #content>
        <div class="flex align-items-center">
          <i class="pi pi-exclamation-triangle text-orange-600 text-2xl mr-3"></i>
          <div class="flex-1">
            <h4 class="text-lg font-medium mb-1 text-orange-800">
              {{ missionsAValider.length }} mission(s) à valider
            </h4>
            <p class="text-orange-700 mb-0">
              Des prestataires ont terminé leurs missions et attendent votre validation.
            </p>
          </div>
          <Button 
            label="Valider maintenant"
            icon="pi pi-check"
            severity="warning"
            @click="filtreStatut = 'A_VALIDER'"
          />
        </div>
      </template>
    </Card>

    <!-- Filtres par statut -->
    <Card class="mb-4">
      <template #title>
        <div class="flex align-items-center">
          <i class="pi pi-filter mr-2"></i>
          Filtrer par statut
        </div>
      </template>
      <template #content>
        <div class="flex gap-2 flex-wrap">
          <Button
            v-for="option in statutOptions"
            :key="option.value"
            :label="option.label"
            :icon="option.icon"
            :severity="filtreStatut === option.value ? 'primary' : option.severity"
            :outlined="filtreStatut !== option.value"
            @click="filtreStatut = option.value"
          />
        </div>
      </template>
    </Card>

    <!-- Liste des services -->
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <div class="flex align-items-center">
            <i class="pi pi-list mr-2"></i>
            Mes Services ({{ servicesFiltres.length }})
          </div>
        </div>
      </template>
      <template #content>
        <div v-if="loading" class="text-center p-4">
          <ProgressSpinner />
          <p class="mt-2">Chargement des services...</p>
        </div>
        
        <div v-else-if="servicesFiltres.length === 0" class="text-center p-4 text-600">
          <i class="pi pi-inbox text-4xl mb-3"></i>
          <p v-if="filtreStatut === 'TOUS'">Aucun service commandé pour le moment</p>
          <p v-else>Aucun service {{ getStatutLabel(filtreStatut).toLowerCase() }}</p>
          <small>
            <span v-if="filtreStatut === 'TOUS'">Créez votre première demande de service</span>
            <span v-else>Modifiez le filtre pour voir d'autres services</span>
          </small>
        </div>
        
        <div v-else class="space-y-3">
          <div
            v-for="service in servicesFiltres"
            :key="service.id"
            class="service-card p-4 border-round surface-100"
            :class="{ 'border-2 border-orange-300': service.statut === 'A_VALIDER' }"
          >
            <div class="flex justify-content-between align-items-start mb-3">
              <div class="flex-1">
                <div class="flex align-items-center justify-content-between mb-2">
                  <h4 class="m-0 font-semibold text-primary">
                    {{ service.demandeService?.titre || 'Service sans titre' }}
                  </h4>
                  <Tag 
                    :value="getStatutLabel(service.statut)"
                    :severity="getStatutSeverity(service.statut)"
                    :icon="getStatutIcon(service.statut)"
                  />
                </div>
                
                <p class="text-sm text-600 mb-3">{{ service.demandeService?.description }}</p>
                
                <div class="grid mb-3">
                  <div class="col-12 md:col-6">
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-user mr-2 text-blue-500"></i>
                      <span>
                        {{ service.prestataire?.prenom }} 
                        {{ service.prestataire?.nom }}
                      </span>
                    </div>
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-calendar mr-2 text-green-500"></i>
                      <span>{{ formatDate(service.demandeService?.dateSouhaitee) }}</span>
                    </div>
                  </div>
                  <div class="col-12 md:col-6">
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-euro mr-2 text-orange-500"></i>
                      <span>Prix convenu : {{ service.prixAccepte || 'Non défini' }}€</span>
                    </div>
                    <div class="flex align-items-center mb-2 text-sm">
                      <i class="pi pi-map-marker mr-2 text-red-500"></i>
                      <span>{{ service.demandeService?.adresseDepart }}</span>
                    </div>
                  </div>
                </div>

                <!-- Timeline service -->
                <div class="mb-3">
                  <h5 class="text-sm font-medium text-600 mb-2">Progression :</h5>
                  <div class="flex align-items-center gap-2">
                    <div class="flex align-items-center">
                      <i class="pi pi-check-circle text-green-500 mr-1"></i>
                      <span class="text-xs">Prestataire sélectionné</span>
                    </div>
                    <div class="border-top border-300" style="width: 30px;"></div>
                    <div class="flex align-items-center">
                      <i :class="['EN_COURS', 'A_VALIDER', 'TERMINE'].includes(service.statut) ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-300'" class="mr-1"></i>
                      <span class="text-xs" :class="['EN_COURS', 'A_VALIDER', 'TERMINE'].includes(service.statut) ? '' : 'text-300'">En cours</span>
                    </div>
                    <div class="border-top border-300" style="width: 30px;"></div>
                    <div class="flex align-items-center">
                      <i :class="['A_VALIDER', 'TERMINE'].includes(service.statut) ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-300'" class="mr-1"></i>
                      <span class="text-xs" :class="['A_VALIDER', 'TERMINE'].includes(service.statut) ? '' : 'text-300'">Terminé</span>
                    </div>
                    <div class="border-top border-300" style="width: 30px;"></div>
                    <div class="flex align-items-center">
                      <i :class="service.statut === 'TERMINE' ? 'pi pi-check-circle text-green-500' : 'pi pi-circle text-300'" class="mr-1"></i>
                      <span class="text-xs" :class="service.statut === 'TERMINE' ? '' : 'text-300'">Validé</span>
                    </div>
                  </div>
                </div>

                <!-- Actions selon le statut -->
                <div class="flex justify-content-between align-items-center">
                  <div class="text-sm text-500">
                    <div v-if="service.statut === 'A_VALIDER'" class="text-orange-600">
                      <i class="pi pi-exclamation-triangle mr-1"></i>
                      Service terminé - Validation requise
                    </div>
                    <div v-else-if="service.statut === 'TERMINE'" class="text-green-600">
                      <i class="pi pi-check-circle mr-1"></i>
                      Service validé et terminé
                    </div>
                    <div v-else-if="service.statut === 'EN_COURS'" class="text-blue-600">
                      <i class="pi pi-clock mr-1"></i>
                      Service en cours de réalisation
                    </div>
                    <div v-else class="text-600">
                      <i class="pi pi-info-circle mr-1"></i>
                      Prestataire sélectionné
                    </div>
                  </div>
                  
                  <div class="flex gap-2">
                    <Button
                      icon="pi pi-eye"
                      severity="secondary"
                      size="small"
                      text
                      @click="voirDetailsService(service)"
                    />
                    
                    <!-- Actions selon statut -->
                    <Button 
                      v-if="service.statut === 'A_VALIDER'"
                      icon="pi pi-star" 
                      label="Valider & Évaluer"
                      severity="warning"
                      size="small"
                      @click="allerValidation(service)"
                    />
                    
                    <Button 
                      v-if="service.statut === 'TERMINE'"
                      icon="pi pi-eye" 
                      label="Voir évaluation"
                      severity="info"
                      size="small"
                      text
                      @click="voirEvaluation(service)"
                    />
                    
                    <Button
                      v-if="['EN_COURS', 'A_VALIDER'].includes(service.statut)"
                      icon="pi pi-comment"
                      severity="info"
                      size="small"
                      text
                      @click="contacterPrestataire(service)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Dialog détails service -->
    <Dialog 
      v-model:visible="showDetailsDialog" 
      header="Détails du service" 
      modal
      :style="{ width: '60rem' }"
      :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
    >
      <div v-if="serviceSelectionne" class="service-details">
        <Card class="mb-4">
          <template #title>{{ serviceSelectionne.demandeService?.titre }}</template>
          <template #content>
            <div class="grid">
              <div class="col-12 md:col-6">
                <h5>Informations prestataire</h5>
                <p><strong>Prestataire :</strong> {{ serviceSelectionne.prestataire?.prenom }} {{ serviceSelectionne.prestataire?.nom }}</p>
                <p><strong>Email :</strong> {{ serviceSelectionne.prestataire?.email }}</p>
                <p><strong>Téléphone :</strong> {{ serviceSelectionne.prestataire?.telephone || 'Non renseigné' }}</p>
              </div>
              <div class="col-12 md:col-6">
                <h5>Détails service</h5>
                <p><strong>Prix :</strong> {{ serviceSelectionne.prixAccepte }}€</p>
                <p><strong>Date souhaitée :</strong> {{ formatDate(serviceSelectionne.demandeService?.dateSouhaitee) }}</p>
                <p><strong>Créneau :</strong> {{ serviceSelectionne.demandeService?.creneauHoraire || 'Flexible' }}</p>
              </div>
            </div>
            
            <Divider />
            
            <h5>Description détaillée</h5>
            <p>{{ serviceSelectionne.demandeService?.description }}</p>
            
            <h5>Adresses</h5>
            <p><strong>Lieu d'intervention :</strong> {{ serviceSelectionne.demandeService?.adresseDepart }}</p>
            <p v-if="serviceSelectionne.demandeService?.adresseArrivee">
              <strong>Destination :</strong> {{ serviceSelectionne.demandeService?.adresseArrivee }}
            </p>
          </template>
        </Card>
      </div>
    </Dialog>

    <Toast />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const services = ref([])
const statistiques = ref({})
const loading = ref(false)
const filtreStatut = ref('TOUS')

const showDetailsDialog = ref(false)
const serviceSelectionne = ref(null)

const statutOptions = [
  { label: 'Tous', value: 'TOUS', icon: 'pi pi-list', severity: 'secondary' },
  { label: 'À valider', value: 'A_VALIDER', icon: 'pi pi-exclamation-triangle', severity: 'warning' },
  { label: 'En cours', value: 'EN_COURS', icon: 'pi pi-clock', severity: 'info' },
  { label: 'Terminés', value: 'TERMINE', icon: 'pi pi-check', severity: 'success' }
]

const servicesFiltres = computed(() => {
  if (filtreStatut.value === 'TOUS') {
    return services.value
  }
  return services.value.filter(service => service.statut === filtreStatut.value)
})

const missionsAValider = computed(() => {
  return services.value.filter(service => service.statut === 'A_VALIDER')
})

const getStatutLabel = (statut) => {
  const labels = {
    'PRESTATAIRE_SELECTIONNE': 'Prestataire sélectionné',
    'EN_COURS': 'En cours',
    'A_VALIDER': 'À valider',
    'TERMINE': 'Terminé'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'PRESTATAIRE_SELECTIONNE': 'info',
    'EN_COURS': 'warning',
    'A_VALIDER': 'danger',
    'TERMINE': 'success'
  }
  return severities[statut] || 'secondary'
}

const getStatutIcon = (statut) => {
  const icons = {
    'PRESTATAIRE_SELECTIONNE': 'pi pi-user',
    'EN_COURS': 'pi pi-clock',
    'A_VALIDER': 'pi pi-exclamation-triangle',
    'TERMINE': 'pi pi-check'
  }
  return icons[statut] || 'pi pi-question'
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

const chargerServices = async () => {
  loading.value = true
  
  try {
    const clientId = authStore.user?.id || authStore.user?.idUtilisateur
    
    if (!clientId) {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Session utilisateur expirée, veuillez vous reconnecter',
        life: 3000
      })
      router.push('/login')
      return
    }
    
    const response = await axios.get(`http://localhost:8080/api/missions/client/${clientId}`)
    
    if (response.data) {
      services.value = response.data.map(mission => ({
        ...mission,
        statut: mission.statut === 'TERMINEE' ? 'A_VALIDER' : 
                mission.statut === 'EN_COURS' ? 'EN_COURS' : 
                'PRESTATAIRE_SELECTIONNE'
      }))
      
      calculerStatistiques()
    }
    
  } catch (error) {
    console.error('Erreur chargement services:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les services',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

const calculerStatistiques = () => {
  statistiques.value = {
    total: services.value.length,
    enCours: services.value.filter(s => s.statut === 'EN_COURS').length,
    aValider: services.value.filter(s => s.statut === 'A_VALIDER').length,
    termines: services.value.filter(s => s.statut === 'TERMINE').length
  }
}

const allerValidation = (service) => {
  sessionStorage.setItem('fromMesServices', 'true')
  router.push(`/client/mission/${service.id}/validation`)
}

const voirDetailsService = (service) => {
  serviceSelectionne.value = service
  showDetailsDialog.value = true
}

const voirEvaluation = (service) => {
  toast.add({
    severity: 'info',
    summary: 'Fonctionnalité à venir',
    detail: 'La consultation des évaluations sera disponible prochainement',
    life: 3000
  })
}

const contacterPrestataire = (service) => {
  toast.add({
    severity: 'info',
    summary: 'Fonctionnalité à venir',
    detail: 'Le système de messagerie sera disponible prochainement',
    life: 3000
  })
}

const retourDashboard = () => {
  router.push('/client')
}

onMounted(() => {
  chargerServices()
})
</script>

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
  .md\:col-3 { grid-column: span 3; }
}

.space-y-3 > * + * {
  margin-top: 0.75rem;
}

.service-card {
  transition: all 0.3s ease;
}

.service-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.flex-1 { flex: 1; }
</style>