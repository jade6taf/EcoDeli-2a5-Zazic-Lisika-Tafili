<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnnoncesStore } from '@/stores/annonces'
import { useCandidaturesStore } from '@/stores/candidatures'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const annoncesStore = useAnnoncesStore()
const candidaturesStore = useCandidaturesStore()
const toast = useToast()
const confirm = useConfirm()

const annonce = ref(null)
const candidaturesData = ref(null)
const loading = ref(false)
const showCommentDialog = ref(false)
const actionCandidature = ref(null)
const commentaire = ref('')

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const annonceId = computed(() => parseInt(route.params.id))

const peutValiderLivraison = computed(() => {
  return candidaturesData.value?.segment1Accepte && candidaturesData.value?.segment2Accepte
})

const getStatutLabel = (statut) => {
  const labels = {
    'EN_ATTENTE': 'En attente',
    'ACCEPTEE': 'Acceptée',
    'REFUSEE': 'Refusée'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'EN_ATTENTE': 'warning',
    'ACCEPTEE': 'success',
    'REFUSEE': 'danger'
  }
  return severities[statut] || 'secondary'
}

const getSegmentLabel = (segment) => {
  return segment === 'SEGMENT_1' ? 'Segment 1 - Départ vers entrepôt' : 'Segment 2 - Entrepôt vers destination'
}

const loadAnnonce = async () => {
  try {
    const result = await annoncesStore.fetchAnnonces()
    if (result.success) {
      annonce.value = result.data.find(a => a.idAnnonce === annonceId.value)
      if (!annonce.value) {
        throw new Error('Annonce non trouvée')
      }
    }
  } catch (error) {
    console.error('Erreur lors du chargement de l\'annonce:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger l\'annonce',
      life: 3000
    })
  }
}

const loadCandidatures = async () => {
  loading.value = true
  try {
    const result = await candidaturesStore.getCandidaturesParSegment(annonceId.value)
    if (result.success) {
      candidaturesData.value = result.data
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.message,
        life: 3000
      })
    }
  } catch (error) {
    console.error('Erreur lors du chargement des candidatures:', error)
  } finally {
    loading.value = false
  }
}

const openCommentDialog = (candidature, action) => {
  actionCandidature.value = { candidature, action }
  commentaire.value = ''
  showCommentDialog.value = true
}

const submitAction = async () => {
  if (!actionCandidature.value)
    return

  const { candidature, action } = actionCandidature.value

  try {
    let result
    if (action === 'accepter') {
      result = await candidaturesStore.accepterCandidaturePartielle(
        candidature.idCandidatureLivraison,
        commentaire.value
      )
    } else {
      result = await candidaturesStore.refuserCandidature(
        candidature.idCandidatureLivraison,
        commentaire.value
      )
    }

    if (result.success) {
      toast.add({
        severity: 'success',
        summary: action === 'accepter' ? 'Candidature acceptée' : 'Candidature refusée',
        detail: `La candidature de ${candidature.livreur.prenom} ${candidature.livreur.nom} a été ${action === 'accepter' ? 'acceptée' : 'refusée'}`,
        life: 4000
      })

      showCommentDialog.value = false
      await loadCandidatures()
      await loadAnnonce()
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur lors de l\'acceptation',
        detail: result.message || 'Une erreur s\'est produite',
        life: 5000
      })
    }
  } catch (error) {
    console.error('Erreur lors de l\'action:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur technique',
      detail: 'Une erreur technique s\'est produite. Veuillez réessayer.',
      life: 5000
    })
  }
}

const validerLivraison = async () => {
  confirm.require({
    message: 'Confirmer la validation des deux livreurs ? La livraison partielle sera créée.',
    header: 'Valider la livraison partielle',
    icon: 'pi pi-check-circle',
    acceptClass: 'p-button-success',
    acceptLabel: 'Oui, valider',
    rejectLabel: 'Annuler',
    accept: async () => {
      try {
        const result = await annoncesStore.validerLivreursPartielle(annonceId.value)
        if (result.success) {
          toast.add({
            severity: 'success',
            summary: 'Livraison validée !',
            detail: 'La livraison partielle a été créée avec succès',
            life: 4000
          })
          router.push('/client')
        } else {
          toast.add({
            severity: 'error',
            summary: 'Erreur',
            detail: result.message,
            life: 3000
          })
        }
      } catch (error) {
        console.error('Erreur lors de la validation:', error)
      }
    }
  })
}

const closeCommentDialog = () => {
  showCommentDialog.value = false
  actionCandidature.value = null
  commentaire.value = ''
}

const goBack = () => {
  router.push('/client')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadAnnonce()
  loadCandidatures()
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
          <span class="text-xl font-semibold ecodeli-title">Candidatures Partielles</span>
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
        <!-- En-tête avec info annonce -->
        <Card class="mb-4" v-if="annonce">
          <template #content>
            <div class="flex justify-content-between align-items-center">
              <div>
                <h1 class="text-3xl font-bold ecodeli-title m-0">
                  <i class="pi pi-clone mr-3" style="color: var(--ecodeli-green)"></i>
                  {{ annonce.titre }}
                </h1>
                <p class="text-600 mt-2">Gestion des candidatures pour livraison partielle</p>
                <div class="mt-3">
                  <div class="grid">
                    <div class="col-12 md:col-6">
                      <div class="flex align-items-center gap-2 mb-2">
                        <i class="pi pi-map-marker text-orange-500"></i>
                        <span class="font-medium text-600">Départ:</span>
                        <span>{{ annonce.adresseDepart }}</span>
                      </div>
                    </div>
                    <div class="col-12 md:col-6">
                      <div class="flex align-items-center gap-2 mb-2">
                        <i class="pi pi-flag text-green-500"></i>
                        <span class="font-medium text-600">Destination:</span>
                        <span>{{ annonce.adresseFin }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="text-right">
                <div class="text-2xl font-bold text-primary">{{ annonce.prixUnitaire }}€</div>
                <div class="text-sm text-600">Prix total</div>
              </div>
            </div>
          </template>
        </Card>

        <!-- Statut et validation -->
        <Card class="mb-4" v-if="candidaturesData">
          <template #content>
            <div class="flex justify-content-between align-items-center">
              <div>
                <h3 class="font-semibold m-0 mb-2">État de la livraison partielle</h3>
                <div class="flex gap-4">
                  <div class="flex align-items-center gap-2">
                    <i class="pi pi-circle-fill" :class="candidaturesData.segment1Accepte ? 'text-green-500' : 'text-orange-500'"></i>
                    <span>Segment 1: {{ candidaturesData.segment1Accepte ? 'Accepté' : 'En attente' }}</span>
                  </div>
                  <div class="flex align-items-center gap-2">
                    <i class="pi pi-circle-fill" :class="candidaturesData.segment2Accepte ? 'text-green-500' : 'text-orange-500'"></i>
                    <span>Segment 2: {{ candidaturesData.segment2Accepte ? 'Accepté' : 'En attente' }}</span>
                  </div>
                </div>
              </div>
              <div v-if="peutValiderLivraison">
                <Button
                  label="Valider la livraison"
                  icon="pi pi-check"
                  severity="success"
                  @click="validerLivraison"
                />
              </div>
            </div>
          </template>
        </Card>

        <!-- Candidatures par segment -->
        <div v-if="loading" class="text-center p-6">
          <ProgressSpinner style="width: 50px; height: 50px" />
          <p class="mt-3">Chargement des candidatures...</p>
        </div>

        <div v-else-if="candidaturesData" class="grid gap-4">
          <!-- Segment 1 -->
          <div class="col-12 lg:col-6">
            <Card>
              <template #header>
                <div class="px-4 pt-4">
                  <h3 class="text-xl font-semibold ecodeli-title m-0">
                    <i class="pi pi-truck mr-2"></i>
                    Segment 1 - Départ vers entrepôt
                  </h3>
                  <p class="text-600 mt-1">{{ annonce?.adresseDepart }} → Entrepôt EcoDeli</p>
                </div>
              </template>
              <template #content>
                <div v-if="candidaturesData.segment1.length === 0" class="text-center p-4">
                  <i class="pi pi-inbox text-4xl text-400 mb-3"></i>
                  <p class="text-600">Aucune candidature pour ce segment</p>
                </div>

                <div v-else class="flex flex-column gap-3">
                  <div
                    v-for="candidature in candidaturesData.segment1"
                    :key="candidature.idCandidatureLivraison"
                    class="border-round border-1 surface-border p-3"
                  >
                    <div class="flex justify-content-between align-items-start mb-3">
                      <div>
                        <div class="font-medium text-lg">
                          {{ candidature.livreur.prenom }} {{ candidature.livreur.nom }}
                        </div>
                        <div class="text-sm text-600 mb-2">{{ candidature.livreur.email }}</div>
                        <Tag
                          :value="getStatutLabel(candidature.statut)"
                          :severity="getStatutSeverity(candidature.statut)"
                        />
                      </div>
                    </div>

                    <div v-if="candidature.entrepotChoisi" class="mb-3">
                      <div class="font-medium text-600 mb-1">Entrepôt choisi:</div>
                      <div class="flex align-items-center gap-2">
                        <i class="pi pi-building text-blue-500"></i>
                        <span>{{ candidature.entrepotChoisi }}</span>
                      </div>
                    </div>

                    <div v-if="candidature.messageLivreur" class="mb-3">
                      <div class="font-medium text-600 mb-1">Message du livreur:</div>
                      <p class="text-sm m-0">{{ candidature.messageLivreur }}</p>
                    </div>

                    <div v-if="candidature.statut === 'EN_ATTENTE'" class="flex gap-2">
                      <Button
                        label="Accepter"
                        icon="pi pi-check"
                        severity="success"
                        size="small"
                        @click="openCommentDialog(candidature, 'accepter')"
                      />
                      <Button
                        label="Refuser"
                        icon="pi pi-times"
                        severity="danger"
                        size="small"
                        outlined
                        @click="openCommentDialog(candidature, 'refuser')"
                      />
                    </div>

                    <div v-if="candidature.commentaireClient" class="mt-3 p-3 surface-100 border-round">
                      <div class="font-medium text-600 mb-1">Votre commentaire:</div>
                      <p class="text-sm m-0">{{ candidature.commentaireClient }}</p>
                    </div>
                  </div>
                </div>
              </template>
            </Card>
          </div>

          <!-- Segment 2 -->
          <div class="col-12 lg:col-6">
            <Card>
              <template #header>
                <div class="px-4 pt-4">
                  <h3 class="text-xl font-semibold ecodeli-title m-0">
                    <i class="pi pi-truck mr-2"></i>
                    Segment 2 - Entrepôt vers destination
                  </h3>
                  <p class="text-600 mt-1">Entrepôt EcoDeli → {{ annonce?.adresseFin }}</p>
                </div>
              </template>
              <template #content>
                <div v-if="candidaturesData.segment2.length === 0" class="text-center p-4">
                  <i class="pi pi-inbox text-4xl text-400 mb-3"></i>
                  <p class="text-600">Aucune candidature pour ce segment</p>
                </div>

                <div v-else class="flex flex-column gap-3">
                  <div
                    v-for="candidature in candidaturesData.segment2"
                    :key="candidature.idCandidatureLivraison"
                    class="border-round border-1 surface-border p-3"
                  >
                    <div class="flex justify-content-between align-items-start mb-3">
                      <div>
                        <div class="font-medium text-lg">
                          {{ candidature.livreur.prenom }} {{ candidature.livreur.nom }}
                        </div>
                        <div class="text-sm text-600 mb-2">{{ candidature.livreur.email }}</div>
                        <Tag
                          :value="getStatutLabel(candidature.statut)"
                          :severity="getStatutSeverity(candidature.statut)"
                        />
                      </div>
                    </div>

                    <div v-if="candidature.messageLivreur" class="mb-3">
                      <div class="font-medium text-600 mb-1">Message du livreur:</div>
                      <p class="text-sm m-0">{{ candidature.messageLivreur }}</p>
                    </div>

                    <div v-if="candidature.statut === 'EN_ATTENTE'" class="flex gap-2">
                      <Button
                        label="Accepter"
                        icon="pi pi-check"
                        severity="success"
                        size="small"
                        @click="openCommentDialog(candidature, 'accepter')"
                      />
                      <Button
                        label="Refuser"
                        icon="pi pi-times"
                        severity="danger"
                        size="small"
                        outlined
                        @click="openCommentDialog(candidature, 'refuser')"
                      />
                    </div>

                    <div v-if="candidature.commentaireClient" class="mt-3 p-3 surface-100 border-round">
                      <div class="font-medium text-600 mb-1">Votre commentaire:</div>
                      <p class="text-sm m-0">{{ candidature.commentaireClient }}</p>
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
   
    <!-- Confirm Dialog -->
    <ConfirmDialog />

    <!-- Dialog commentaire -->
    <Dialog
      v-model:visible="showCommentDialog"
      :header="actionCandidature?.action === 'accepter' ? 'Accepter la candidature' : 'Refuser la candidature'"
      :modal="true"
      :closable="true"
      :style="{ width: '400px' }"
      @hide="closeCommentDialog"
    >
      <div class="p-4">
        <div v-if="actionCandidature" class="mb-4">
          <p>
            {{ actionCandidature.action === 'accepter' ? 'Accepter' : 'Refuser' }} la candidature de 
            <strong>{{ actionCandidature.candidature.livreur.prenom }} {{ actionCandidature.candidature.livreur.nom }}</strong> 
            pour le {{ getSegmentLabel(actionCandidature.candidature.segmentLivraison) }} ?
          </p>
        </div>

        <div class="field">
          <label for="commentaire" class="font-medium text-900 block mb-2">
            Commentaire {{ actionCandidature?.action === 'accepter' ? '(optionnel)' : '(requis)' }}
          </label>
          <Textarea
            id="commentaire"
            v-model="commentaire"
            :placeholder="actionCandidature?.action === 'accepter' ? 'Merci pour votre candidature...' : 'Motif du refus...'"
            rows="3"
            class="w-full"
            :class="{ 'p-invalid': actionCandidature?.action === 'refuser' && !commentaire.trim() }"
          />
        </div>

        <div class="flex justify-content-end gap-2 mt-4">
          <Button
            label="Annuler"
            severity="secondary"
            outlined
            @click="closeCommentDialog"
          />
          <Button
            :label="actionCandidature?.action === 'accepter' ? 'Accepter' : 'Refuser'"
            :icon="actionCandidature?.action === 'accepter' ? 'pi pi-check' : 'pi pi-times'"
            :severity="actionCandidature?.action === 'accepter' ? 'success' : 'danger'"
            @click="submitAction"
            :disabled="actionCandidature?.action === 'refuser' && !commentaire.trim()"
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

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
}

@media (min-width: 1024px) {
  .lg\:col-6 { grid-column: span 6; }
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
