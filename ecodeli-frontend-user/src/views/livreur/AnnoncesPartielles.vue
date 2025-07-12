<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnnoncesStore } from '@/stores/annonces'
import { useCandidaturesStore } from '@/stores/candidatures'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const authStore = useAuthStore()
const annoncesStore = useAnnoncesStore()
const candidaturesStore = useCandidaturesStore()
const toast = useToast()

const annonces = ref([])
const entrepots = ref([])
const loading = ref(false)
const showCandidatureDialog = ref(false)
const selectedAnnonce = ref(null)
const candidatureForm = ref({
  segment: null,
  entrepotChoisi: null,
  message: ''
})

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const segmentOptions = [
  { label: 'Segment 1 - Départ vers entrepôt', value: 'SEGMENT_1' },
  { label: 'Segment 2 - Entrepôt vers destination', value: 'SEGMENT_2' }
]

const candidatureFormValid = computed(() => {
  if (!candidatureForm.value.segment) return false
  if (candidatureForm.value.segment === 'SEGMENT_1' && !candidatureForm.value.entrepotChoisi) return false
  return true
})

const getStatutLabel = (statut) => {
  const labels = {
    'PUBLIEE': 'Disponible',
    'SEGMENT_1_PRIS': 'Segment 1 pris',
    'SEGMENT_2_PRIS': 'Segment 2 pris',
    'SEGMENTS_COMPLETS': 'Segments complets'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'PUBLIEE': 'success',
    'SEGMENT_1_PRIS': 'warning',
    'SEGMENT_2_PRIS': 'warning',
    'SEGMENTS_COMPLETS': 'info'
  }
  return severities[statut] || 'secondary'
}

const getSegmentsDisponibles = (annonce) => {
  const segments = []

  if (annonce.statut === 'PUBLIEE') {
    segments.push(...segmentOptions)
  } else if (annonce.statut === 'SEGMENT_1_PRIS') {
    segments.push(segmentOptions[1]) // Segment 2 seulement
  } else if (annonce.statut === 'SEGMENT_2_PRIS') {
    segments.push(segmentOptions[0]) // Segment 1 seulement
  }

  return segments
}

const loadAnnonces = async () => {
  loading.value = true
  try {
    const result = await annoncesStore.fetchAvailableAnnonces()
    if (result.success) {
      annonces.value = result.data.filter(annonce =>
        annonce.livraisonPartielleAutorisee &&
        annonce.statut === 'PUBLIEE'
      )
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.message,
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur lors du chargement:', error)
  } finally {
    loading.value = false
  }
}

const loadEntrepots = async () => {
  try {
    const result = await candidaturesStore.getEntrepotsDisponibles()
    if (result.success) {
      entrepots.value = result.data
    }
  } catch (error) {
    console.error('Erreur lors du chargement des entrepôts:', error)
  }
}

const openCandidatureDialog = (annonce) => {
  selectedAnnonce.value = annonce
  candidatureForm.value = {
    segment: null,
    entrepotChoisi: null,
    message: ''
  }

  const segmentsDisponibles = getSegmentsDisponibles(annonce)
  if (segmentsDisponibles.length === 1) {
    candidatureForm.value.segment = segmentsDisponibles[0].value
  }

  showCandidatureDialog.value = true
}

const submitCandidature = async () => {
  if (!candidatureFormValid.value || !selectedAnnonce.value) return

  try {
    const userId = authStore.user.id || authStore.user.idUtilisateur

    const result = await candidaturesStore.candidaterPartielle(
      selectedAnnonce.value.idAnnonce,
      userId,
      candidatureForm.value.segment,
      candidatureForm.value.entrepotChoisi,
      candidatureForm.value.message
    )

    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Candidature envoyée',
        detail: `Votre candidature pour le ${candidatureForm.value.segment === 'SEGMENT_1' ? 'segment 1' : 'segment 2'} a été envoyée`,
        life: 4000
      })

      showCandidatureDialog.value = false
      loadAnnonces()
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.message,
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur candidature:', error)
  }
}

const closeCandidatureDialog = () => {
  showCandidatureDialog.value = false
  selectedAnnonce.value = null
}

const onSegmentChange = () => {
  if (candidatureForm.value.segment === 'SEGMENT_2') {
    candidatureForm.value.entrepotChoisi = null
  }
}

const goBack = () => {
  router.push('/livreur')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadAnnonces()
  loadEntrepots()
})
</script>

<template>
  <div class="ecodeli-layout">
    <!-- Header -->
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
          <i class="pi pi-clone text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
          <span class="text-xl font-semibold ecodeli-title">Livraisons Partielles</span>
        </div>

        <div class="flex align-items-center gap-2">
          <span class="text-sm text-600">{{ authStore.userName }}</span>
          <Avatar
            :label="userInitials"
            class="bg-primary text-white"
            shape="circle"
          />
          <Button
            icon="pi pi-sign-out"
            text
            rounded
            @click="handleLogout"
          />
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="ecodeli-content">
      <div class="max-w-6xl mx-auto">
        <!-- En-tête -->
        <Card class="mb-4">
          <template #content>
            <div class="flex justify-content-between align-items-center">
              <div>
                <h1 class="text-3xl font-bold ecodeli-title m-0">
                  <i class="pi pi-clone mr-3" style="color: var(--ecodeli-green)"></i>
                  Livraisons Partielles
                </h1>
                <p class="text-600 mt-2">Candidatez pour des segments de livraison via entrepôts</p>
              </div>
              <Button
                icon="pi pi-refresh"
                class="p-button-outlined"
                @click="loadAnnonces"
                :loading="loading"
                label="Actualiser"
              />
            </div>
          </template>
        </Card>

        <!-- Explication -->
        <Card class="mb-4">
          <template #content>
            <div class="bg-blue-50 border-round p-3">
              <div class="flex align-items-start gap-2">
                <i class="pi pi-info-circle text-blue-600 mt-1"></i>
                <div class="text-sm text-blue-800">
                  <strong>Livraison partielle :</strong> Les colis sont livrés en 2 étapes par 2 livreurs différents.
                  <br />
                  • <strong>Segment 1 :</strong> Du point de départ vers un entrepôt EcoDeli (vous choisissez l'entrepôt)
                  <br />
                  • <strong>Segment 2 :</strong> De l'entrepôt vers la destination finale
                </div>
              </div>
            </div>
          </template>
        </Card>

        <!-- Liste des annonces -->
        <div v-if="loading" class="text-center p-6">
          <ProgressSpinner style="width: 50px; height: 50px" />
          <p class="mt-3">Chargement des annonces...</p>
        </div>

        <div v-else-if="annonces.length === 0" class="text-center p-6">
          <Card>
            <template #content>
              <i class="pi pi-clone text-6xl text-400 mb-4"></i>
              <h3 class="text-xl font-semibold">Aucune annonce partielle</h3>
              <p class="text-600 mb-4">Il n'y a pas d'annonces partielles disponibles pour le moment</p>
              <Button
                label="Voir toutes les annonces"
                icon="pi pi-search"
                @click="goBack"
              />
            </template>
          </Card>
        </div>

        <div v-else class="grid gap-4">
          <div
            v-for="annonce in annonces"
            :key="annonce.idAnnonce"
            class="col-12"
          >
            <Card>
              <template #content>
                <div class="flex align-items-start gap-4">
                  <!-- Icône livraison partielle -->
                  <div class="flex flex-column align-items-center">
                    <div
                      class="flex align-items-center justify-content-center border-circle bg-blue-100"
                      style="width: 50px; height: 50px;"
                    >
                      <i class="pi pi-clone text-xl text-blue-600"></i>
                    </div>
                    <div class="mt-2 text-center">
                      <Tag
                        :value="getStatutLabel(annonce.statut)"
                        :severity="getStatutSeverity(annonce.statut)"
                      />
                    </div>
                  </div>

                  <!-- Contenu -->
                  <div class="flex-1">
                    <div class="flex justify-content-between align-items-start mb-3">
                      <div>
                        <h3 class="text-xl font-semibold ecodeli-title m-0">{{ annonce.titre }}</h3>
                        <p class="text-600 mt-1">{{ annonce.description }}</p>
                      </div>
                      <div class="text-right">
                        <div class="text-2xl font-bold text-primary">{{ annonce.prixUnitaire }}€</div>
                        <div class="text-sm text-600">Rémunération totale</div>
                      </div>
                    </div>

                    <!-- Adresses -->
                    <div class="grid mb-4">
                      <div class="col-12 md:col-6">
                        <div class="flex align-items-center gap-2 mb-2">
                          <i class="pi pi-map-marker text-orange-500"></i>
                          <span class="font-medium text-600">Départ</span>
                        </div>
                        <p class="text-900 m-0">{{ annonce.adresseDepart }}</p>
                      </div>
                      <div class="col-12 md:col-6">
                        <div class="flex align-items-center gap-2 mb-2">
                          <i class="pi pi-flag text-green-500"></i>
                          <span class="font-medium text-600">Destination finale</span>
                        </div>
                        <p class="text-900 m-0">{{ annonce.adresseFin }}</p>
                      </div>
                    </div>

                    <!-- Segments disponibles -->
                    <div class="mb-4">
                      <h4 class="font-medium text-600 m-0 mb-2">Segments disponibles :</h4>
                      <div class="flex gap-2">
                        <Tag
                          v-for="segment in getSegmentsDisponibles(annonce)"
                          :key="segment.value"
                          :value="segment.label"
                          severity="info"
                        />
                      </div>
                    </div>

                    <!-- Informations colis -->
                    <div v-if="annonce.colis" class="surface-100 border-round p-3 mb-4">
                      <h4 class="font-medium text-600 m-0 mb-2">Informations colis</h4>
                      <div class="grid">
                        <div class="col-6 md:col-3">
                          <span class="text-sm text-600">Poids:</span>
                          <div class="font-medium">{{ annonce.colis.poids }}kg</div>
                        </div>
                        <div v-if="annonce.colis.fragile" class="col-6 md:col-3">
                          <Tag value="Fragile" severity="danger" />
                        </div>
                      </div>
                      <div v-if="annonce.colis.description" class="mt-2">
                        <span class="text-sm text-600">Description:</span>
                        <p class="m-0 mt-1">{{ annonce.colis.description }}</p>
                      </div>
                    </div>

                    <!-- Action -->
                    <div class="text-right">
                      <Button
                        label="Candidater"
                        icon="pi pi-send"
                        @click="openCandidatureDialog(annonce)"
                        :disabled="getSegmentsDisponibles(annonce).length === 0"
                      />
                    </div>
                  </div>
                </div>
              </template>
            </Card>
          </div>
        </div>
      </div>
    </main>

    <!-- Toast -->
    <Toast />

    <!-- Dialog de candidature -->
    <Dialog
      v-model:visible="showCandidatureDialog"
      :header="`Candidature partielle - ${selectedAnnonce?.titre}`"
      :modal="true"
      :closable="true"
      :style="{ width: '500px' }"
      @hide="closeCandidatureDialog"
    >
      <div class="p-4">
        <!-- Segment -->
        <div class="field">
          <label for="segment" class="font-medium text-900 block mb-2">Segment souhaité *</label>
          <Dropdown
            id="segment"
            v-model="candidatureForm.segment"
            :options="getSegmentsDisponibles(selectedAnnonce || {})"
            optionLabel="label"
            optionValue="value"
            placeholder="Choisissez un segment"
            class="w-full"
            @change="onSegmentChange"
          />
        </div>

        <!-- Entrepôt (seulement pour segment 1) -->
        <div v-if="candidatureForm.segment === 'SEGMENT_1'" class="field">
          <label for="entrepot" class="font-medium text-900 block mb-2">Entrepôt de dépôt *</label>
          <Dropdown
            id="entrepot"
            v-model="candidatureForm.entrepotChoisi"
            :options="entrepots"
            optionLabel="ville"
            optionValue="ville"
            placeholder="Choisissez un entrepôt"
            class="w-full"
          />
          <small class="text-600">Vous déposerez le colis à cet entrepôt EcoDeli</small>
        </div>

        <!-- Entrepôt info (pour segment 2) -->
        <div v-else-if="candidatureForm.segment === 'SEGMENT_2'" class="field">
          <label class="font-medium text-900 block mb-2">Point de départ</label>
          <div class="p-3 surface-100 border-round">
            <i class="pi pi-info-circle text-blue-500 mr-2"></i>
            <span class="text-600">Vous récupérerez le colis à l'entrepôt choisi par le livreur du segment 1</span>
          </div>
        </div>

        <!-- Message -->
        <div class="field">
          <label for="message" class="font-medium text-900 block mb-2">Message (optionnel)</label>
          <Textarea
            id="message"
            v-model="candidatureForm.message"
            placeholder="Présentez-vous ou ajoutez des informations..."
            rows="3"
            class="w-full"
          />
        </div>

        <!-- Boutons -->
        <div class="flex justify-content-end gap-2 mt-4">
          <Button
            label="Annuler"
            severity="secondary"
            outlined
            @click="closeCandidatureDialog"
          />
          <Button
            label="Envoyer candidature"
            icon="pi pi-send"
            @click="submitCandidature"
            :disabled="!candidatureFormValid"
            :loading="candidaturesStore.loading"
          />
        </div>
      </div>
    </Dialog>
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
  .md\:col-3 { grid-column: span 3; }
}

.max-w-6xl {
  max-width: 72rem;
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

.field {
  margin-bottom: 1rem;
}
</style>
