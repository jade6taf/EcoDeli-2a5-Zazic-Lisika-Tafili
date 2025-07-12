<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLivraisonsStore } from '@/stores/livraisons'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'

const router = useRouter()
const authStore = useAuthStore()
const livraisonsStore = useLivraisonsStore()
const toast = useToast()

const livraisons = ref([])
const loading = ref(false)
const showOtpDialog = ref(false)
const selectedLivraison = ref(null)
const otpCode = ref('')
const otpError = ref('')

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const getStatutLabel = (statut) => {
  const labels = {
    'VALIDEE': 'Validée',
    'EN_COURS': 'En cours',
    'ATTENTE_SEGMENT_2': 'Segment 1 terminé',
    'SEGMENT_2_EN_COURS': 'Segment 2 en cours',
    'ARRIVED': 'Arrivé - En attente OTP',
    'TERMINEE': 'Terminée'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'VALIDEE': 'warning',
    'EN_COURS': 'info',
    'ATTENTE_SEGMENT_2': 'warning',
    'SEGMENT_2_EN_COURS': 'info',
    'ARRIVED': 'success',
    'TERMINEE': 'success'
  }
  return severities[statut] || 'secondary'
}

const getSegmentLabel = (segment) => {
  const labels = {
    'SEGMENT_1': 'Segment 1 - Vers entrepôt',
    'SEGMENT_2': 'Segment 2 - Vers destination'
  }
  return labels[segment] || ''
}

const getActionButton = (livraison) => {
  if (livraison.typeLivraison === 'DIRECTE') {
    switch (livraison.statut) {
      case 'VALIDEE':
        return { label: 'Commencer livraison', icon: 'pi pi-play', severity: 'success', action: 'start' }
      case 'EN_COURS':
        return { label: 'Terminer livraison', icon: 'pi pi-check', severity: 'success', action: 'complete' }
      case 'ARRIVED':
        return { label: 'Saisir OTP', icon: 'pi pi-key', severity: 'info', action: 'otp' }
      default:
        return null
    }
  }
  if (livraison.typeLivraison === 'PARTIELLE') {
    if (livraison.canStart) {
      return {
        label: `Démarrer ${getSegmentLabel(livraison.segment)}`,
        icon: 'pi pi-play',
        severity: 'success',
        action: 'start'
      }
    }

    if (livraison.statut === 'EN_COURS' && livraison.segment === 'SEGMENT_1') {
      return {
        label: 'Livrer à l\'entrepôt',
        icon: 'pi pi-check',
        severity: 'success',
        action: 'complete'
      }
    }

    if (livraison.statut === 'SEGMENT_2_EN_COURS') {
      return {
        label: 'Livrer au destinataire',
        icon: 'pi pi-check',
        severity: 'success',
        action: 'complete'
      }
    }

    if (livraison.statut === 'ARRIVED') {
      return { label: 'Saisir OTP', icon: 'pi pi-key', severity: 'info', action: 'otp' }
    }
  }

  return null
}

const getTimelineIcon = (livraison) => {
  if (livraison.typeLivraison === 'PARTIELLE') {
    if (livraison.segment === 'SEGMENT_1') {
      return 'pi pi-building'
    } else if (livraison.segment === 'SEGMENT_2') {
      return 'pi pi-flag'
    }
  }

  const icons = {
    'VALIDEE': 'pi pi-clock',
    'EN_COURS': 'pi pi-truck',
    'ATTENTE_SEGMENT_2': 'pi pi-pause',
    'SEGMENT_2_EN_COURS': 'pi pi-truck',
    'ARRIVED': 'pi pi-map-marker',
    'TERMINEE': 'pi pi-check-circle'
  }
  return icons[livraison.statut] || 'pi pi-circle'
}

const getTimelineColor = (livraison) => {
  if (livraison.canStart === false && livraison.typeLivraison === 'PARTIELLE') {
    return '#f57c00'
  }

  const colors = {
    'VALIDEE': '#ffa726',
    'EN_COURS': '#66bb6a',
    'ATTENTE_SEGMENT_2': '#42a5f5',
    'SEGMENT_2_EN_COURS': '#66bb6a',
    'ARRIVED': '#4caf50',
    'TERMINEE': '#4caf50'
  }
  return colors[livraison.statut] || '#9e9e9e'
}

const loadLivraisons = async () => {
  if (!authStore.user?.id && !authStore.user?.idUtilisateur) return

  loading.value = true
  try {
    const userId = authStore.user.id || authStore.user.idUtilisateur
    const result = await livraisonsStore.fetchLivraisonsByLivreur(userId)
    if (result.success) {
      livraisons.value = result.data
      console.log('Livraisons chargées:', result.data)
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

const handleLivraisonAction = async (livraison, action) => {
  const userId = authStore.user.id || authStore.user.idUtilisateur

  switch (action) {
    case 'start':
      await startLivraison(livraison, userId)
      break
    case 'complete':
      await completeLivraison(livraison, userId)
      break
    case 'otp':
      openOtpDialog(livraison)
      break
  }
}

const startLivraison = async (livraison, userId) => {
  const result = await livraisonsStore.startLivraison(livraison.idLivraison, userId)
  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Livraison démarrée',
      detail: livraison.typeLivraison === 'PARTIELLE'
        ? `${getSegmentLabel(livraison.segment)} démarré`
        : 'Livraison en cours',
      life: 3000
    })

    await loadLivraisons()
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
}

const completeLivraison = async (livraison, userId) => {
  const result = await livraisonsStore.completeLivraison(livraison.idLivraison, userId)
  if (result.success) {
    let message = 'Livraison terminée'
    if (livraison.typeLivraison === 'PARTIELLE') {
      if (livraison.segment === 'SEGMENT_1') {
        message = 'Colis livré à l\'entrepôt. Le livreur du segment 2 a été notifié.'
      } else {
        message = 'Segment 2 terminé. En attente de la validation OTP.'
      }
    }

    toast.add({
      severity: 'success',
      summary: 'Étape terminée',
      detail: message,
      life: 4000
    })

    await loadLivraisons()
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
}

const openOtpDialog = (livraison) => {
  selectedLivraison.value = livraison
  otpCode.value = ''
  otpError.value = ''
  showOtpDialog.value = true
}

const validateOtp = async () => {
  if (!selectedLivraison.value || !otpCode.value) return

  otpError.value = ''

  const result = await livraisonsStore.validateOTP(selectedLivraison.value.idLivraison, otpCode.value)
  if (result.success) {
    showOtpDialog.value = false

    toast.add({
      severity: 'success',
      summary: 'Livraison finalisée !',
      detail: `Félicitations ! Vous avez gagné ${selectedLivraison.value.prix}€${selectedLivraison.value.typeLivraison === 'PARTIELLE' ? ' (votre part de la livraison partielle)' : ''}`,
      life: 5000
    })
    await loadLivraisons()
  } else {
    otpError.value = result.data?.message || 'Code OTP incorrect ou expiré'
  }
}

const closeOtpDialog = () => {
  showOtpDialog.value = false
  selectedLivraison.value = null
  otpCode.value = ''
  otpError.value = ''
}

const goBack = () => {
  router.push('/livreur')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadLivraisons()
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
          <i class="pi pi-truck text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
          <span class="text-xl font-semibold ecodeli-title">Mes Livraisons</span>
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
                  <i class="pi pi-truck mr-3" style="color: var(--ecodeli-green)"></i>
                  Mes Livraisons
                </h1>
                <p class="text-600 mt-2">Livraisons complètes et partielles acceptées</p>
              </div>
              <Button
                icon="pi pi-refresh"
                class="p-button-outlined"
                @click="loadLivraisons"
                :loading="loading"
                label="Actualiser"
              />
            </div>
          </template>
        </Card>

        <!-- Liste des livraisons -->
        <div v-if="loading" class="text-center p-6">
          <ProgressSpinner style="width: 50px; height: 50px" />
          <p class="mt-3">Chargement des livraisons...</p>
        </div>

        <div v-else-if="livraisons.length === 0" class="text-center p-6">
          <Card>
            <template #content>
              <i class="pi pi-truck text-6xl text-400 mb-4"></i>
              <h3 class="text-xl font-semibold">Aucune livraison</h3>
              <p class="text-600 mb-4">Vous n'avez pas encore de livraisons acceptées</p>
              <Button
                label="Voir les annonces disponibles"
                icon="pi pi-search"
                @click="goBack"
              />
            </template>
          </Card>
        </div>

        <div v-else class="grid gap-4">
          <div
            v-for="livraison in livraisons"
            :key="livraison.idLivraison"
            class="col-12"
          >
            <Card>
              <template #content>
                <div class="flex align-items-start gap-4">
                  <!-- Timeline avec type de livraison -->
                  <div class="flex flex-column align-items-center">
                    <!-- Indicateur de type -->
                    <div class="mb-2">
                      <Tag
                        :value="livraison.typeLivraison === 'PARTIELLE' ? 'Partielle' : 'Directe'"
                        :severity="livraison.typeLivraison === 'PARTIELLE' ? 'info' : 'success'"
                        class="text-xs"
                      />
                    </div>

                    <!-- Icône principale -->
                    <div
                      class="flex align-items-center justify-content-center border-circle"
                      :style="{
                        width: '50px',
                        height: '50px',
                        backgroundColor: getTimelineColor(livraison),
                        color: 'white'
                      }"
                    >
                      <i :class="getTimelineIcon(livraison)" class="text-xl"></i>
                    </div>

                    <!-- Statut -->
                    <div class="mt-2 text-center">
                      <Tag
                        :value="getStatutLabel(livraison.statut)"
                        :severity="getStatutSeverity(livraison.statut)"
                        class="text-xs"
                      />
                    </div>
                  </div>

                  <!-- Contenu -->
                  <div class="flex-1">
                    <div class="flex justify-content-between align-items-start mb-3">
                      <div>
                        <h3 class="text-xl font-semibold ecodeli-title m-0 flex align-items-center gap-2">
                          {{ livraison.titre }}
                          <!-- Badge segment pour livraisons partielles -->
                          <span v-if="livraison.typeLivraison === 'PARTIELLE' && livraison.segment" 
                                class="bg-blue-100 text-blue-800 text-xs px-2 py-1 border-round">
                            {{ getSegmentLabel(livraison.segment) }}
                          </span>
                        </h3>
                        <p class="text-600 mt-1">{{ livraison.description }}</p>

                        <!-- Message de statut pour livraisons partielles -->
                        <div v-if="livraison.typeLivraison === 'PARTIELLE' && livraison.statusMessage" 
                             class="mt-2">
                          <div class="flex align-items-center gap-2">
                            <i class="pi pi-info-circle text-blue-500"></i>
                            <span class="text-sm font-medium text-blue-700">{{ livraison.statusMessage }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="text-right" :title="livraison.typeLivraison === 'PARTIELLE' ? `Prix total de la course: ${livraison.prixTotal}€` : ''">
                        <div class="text-2xl font-bold text-primary">{{ livraison.prix }}€</div>
                        <div class="text-sm text-600">
                          {{ livraison.typeLivraison === 'PARTIELLE' ? 'Votre part' : 'Rémunération' }}
                        </div>
                      </div>
                    </div>

                    <!-- Adresses -->
                    <div class="grid mb-4">
                      <div class="col-12 md:col-6">
                        <div class="flex align-items-center gap-2 mb-2">
                          <i class="pi pi-map-marker text-orange-500"></i>
                          <span class="font-medium text-600">
                            {{ livraison.typeLivraison === 'PARTIELLE' && livraison.segment === 'SEGMENT_2' ? 'Entrepôt' : 'Départ' }}
                          </span>
                        </div>
                        <p class="text-900 m-0">
                          {{ livraison.typeLivraison === 'PARTIELLE' && livraison.segment === 'SEGMENT_2' && livraison.entrepotVille 
                             ? `Entrepôt EcoDeli - ${livraison.entrepotVille}`
                             : livraison.adresseEnvoi }}
                        </p>
                      </div>
                      <div class="col-12 md:col-6">
                        <div class="flex align-items-center gap-2 mb-2">
                          <i class="pi pi-flag text-green-500"></i>
                          <span class="font-medium text-600">
                            {{ livraison.typeLivraison === 'PARTIELLE' && livraison.segment === 'SEGMENT_1' ? 'Entrepôt' : 'Arrivée' }}
                          </span>
                        </div>
                        <p class="text-900 m-0">
                          {{ livraison.typeLivraison === 'PARTIELLE' && livraison.segment === 'SEGMENT_1' && livraison.entrepotVille 
                             ? `Entrepôt EcoDeli - ${livraison.entrepotVille}`
                             : livraison.adresseDeLivraison }}
                        </p>
                      </div>
                    </div>

                    <!-- Informations supplémentaires livraison partielle -->
                    <div v-if="livraison.typeLivraison === 'PARTIELLE'" class="surface-100 border-round p-3 mb-4">
                      <h4 class="font-medium text-600 m-0 mb-2">
                        <i class="pi pi-clone mr-2"></i>
                        Livraison partielle
                      </h4>
                      <div class="grid">
                        <div class="col-12 md:col-6">
                          <span class="text-sm text-600">Votre segment:</span>
                          <div class="font-medium">{{ getSegmentLabel(livraison.segment) }}</div>
                        </div>
                        <div v-if="livraison.entrepotVille" class="col-12 md:col-6">
                          <span class="text-sm text-600">Entrepôt intermédiaire:</span>
                          <div class="font-medium">{{ livraison.entrepotVille }}</div>
                        </div>
                      </div>

                      <!-- Dates importantes -->
                      <div v-if="livraison.dateDepotEntrepot || livraison.dateCollecteEntrepot" class="mt-3">
                        <div v-if="livraison.dateDepotEntrepot" class="mb-2">
                          <span class="text-sm text-600">Dépôt entrepôt:</span>
                          <div class="font-medium">{{ new Date(livraison.dateDepotEntrepot).toLocaleString() }}</div>
                        </div>
                        <div v-if="livraison.dateCollecteEntrepot">
                          <span class="text-sm text-600">Collecte entrepôt:</span>
                          <div class="font-medium">{{ new Date(livraison.dateCollecteEntrepot).toLocaleString() }}</div>
                        </div>
                      </div>
                    </div>

                    <!-- Action -->
                    <div v-if="getActionButton(livraison)" class="text-right">
                      <Button
                        :label="getActionButton(livraison).label"
                        :icon="getActionButton(livraison).icon"
                        :severity="getActionButton(livraison).severity"
                        @click="handleLivraisonAction(livraison, getActionButton(livraison).action)"
                        :loading="livraisonsStore.loading"
                      />
                    </div>

                    <div v-else-if="livraison.statut === 'TERMINEE'" class="text-right">
                      <div class="text-green-600 font-medium">
                        <i class="pi pi-check-circle mr-2"></i>
                        {{ livraison.typeLivraison === 'PARTIELLE' ? 'Segment terminé avec succès !' : 'Livraison terminée avec succès !' }}
                      </div>
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

    <!-- Dialog OTP -->
    <Dialog
      v-model:visible="showOtpDialog"
      :header="`Finaliser: ${selectedLivraison?.titre}`"
      :modal="true"
      :closable="true"
      :style="{ width: '400px' }"
      @hide="closeOtpDialog"
    >
      <div class="text-center p-4">
        <i class="pi pi-key text-6xl text-primary mb-3"></i>
        <h3 class="text-xl font-semibold mb-3">Code OTP de validation</h3>
        <p class="text-600 mb-4">
          Le destinataire a reçu un code OTP par email. Demandez-lui le code pour finaliser la livraison.
        </p>

        <!-- InputOtp -->
        <div class="mb-4">
          <InputOtp
            v-model="otpCode"
            :length="6"
            class="justify-content-center"
          />
        </div>

        <!-- Erreur -->
        <div v-if="otpError" class="mb-4">
          <Message severity="error" :closable="false">
            {{ otpError }}
          </Message>
        </div>

        <!-- Info expiration -->
        <div class="mb-4">
          <Message severity="warn" :closable="false">
            <i class="pi pi-clock mr-2"></i>
            Le code expire dans 5 minutes
          </Message>
        </div>

        <!-- Boutons -->
        <div class="flex gap-2 justify-content-center">
          <Button
            label="Annuler"
            icon="pi pi-times"
            severity="secondary"
            outlined
            @click="closeOtpDialog"
          />
          <Button
            label="Valider"
            icon="pi pi-check"
            @click="validateOtp"
            :disabled="otpCode.length !== 6"
            :loading="livraisonsStore.loading"
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
</style>
