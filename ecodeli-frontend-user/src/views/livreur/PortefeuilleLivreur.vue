<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const loading = ref(false)
const withdrawing = ref(false)
const portefeuille = ref({
  soldePortefeuille: 0,
  totalGagnes: 0,
  ibanLivreur: ''
})
const retraits = ref([])
const showWithdrawDialog = ref(false)
const withdrawForm = ref({
  montant: null,
  iban: ''
})

const canWithdraw = computed(() => {
  return portefeuille.value.soldePortefeuille > 0 && 
         withdrawForm.value.montant > 0 && 
         withdrawForm.value.montant <= portefeuille.value.soldePortefeuille &&
         withdrawForm.value.iban.length >= 15
})

onMounted(async () => {
  await loadPortefeuille()
  await loadHistoriqueRetraits()
})

const loadPortefeuille = async () => {
  loading.value = true
  try {
    const response = await axios.get(`http://localhost:8080/api/portefeuille/livreur/${authStore.user.idUtilisateur}`)

    if (response.data.success) {
      portefeuille.value = response.data
      if (response.data.ibanLivreur) {
        withdrawForm.value.iban = response.data.ibanLivreur
      }
    }
  } catch (error) {
    console.error('Erreur lors du chargement du portefeuille:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les informations du portefeuille',
      life: 5000
    })
  } finally {
    loading.value = false
  }
}

const loadHistoriqueRetraits = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/portefeuille/retraits/${authStore.user.idUtilisateur}`)

    if (response.data.success) {
      retraits.value = response.data.retraits
    }
  } catch (error) {
    console.error('Erreur lors du chargement des retraits:', error)
  }
}

const openWithdrawDialog = () => {
  withdrawForm.value.montant = null
  withdrawForm.value.iban = portefeuille.value.ibanLivreur || ''
  showWithdrawDialog.value = true
}

const processWithdraw = async () => {
  if (!canWithdraw.value) return

  withdrawing.value = true
  try {
    const response = await axios.post(`http://localhost:8080/api/portefeuille/retrait/${authStore.user.idUtilisateur}`, {
      montant: withdrawForm.value.montant,
      iban: withdrawForm.value.iban
    })

    if (response.data.success) {
      toast.add({
        severity: 'success',
        summary: 'Retrait effectué !',
        detail: `${withdrawForm.value.montant}€ ont été virés vers votre compte`,
        life: 4000
      })

      await loadPortefeuille()
      await loadHistoriqueRetraits()

      showWithdrawDialog.value = false
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: response.data.message,
        life: 5000
      })
    }
  } catch (error) {
    console.error('Erreur lors du retrait:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Une erreur est survenue lors du retrait',
      life: 5000
    })
  } finally {
    withdrawing.value = false
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatIban = (iban) => {
  if (!iban) return 'N/A'
  return iban.substring(0, 8) + '****'
}

const goBack = () => {
  router.push('/livreur')
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
      <div class="max-w-6xl mx-auto">
        <!-- En-tête -->
        <Card class="mb-4">
          <template #content>
            <div class="text-center">
              <h1 class="text-3xl font-bold ecodeli-title m-0">
                <i class="pi pi-wallet mr-3" style="color: var(--ecodeli-green)"></i>
                Mon Portefeuille
              </h1>
              <p class="text-600 mt-2">Gérez vos gains et effectuez vos retraits</p>
            </div>
          </template>
        </Card>

        <!-- Loading -->
        <div v-if="loading" class="text-center p-6">
          <ProgressSpinner />
          <p class="mt-3">Chargement...</p>
        </div>

        <!-- Contenu principal -->
        <div v-else class="grid gap-4">

          <!-- Statistiques du portefeuille -->
          <div class="col-12">
            <div class="grid gap-4">
              <!-- Solde actuel -->
              <div class="col-12 md:col-4">
                <Card class="bg-green-50 border-green-200">
                  <template #content>
                    <div class="text-center">
                      <i class="pi pi-wallet text-4xl text-green-600 mb-3"></i>
                      <div class="text-2xl font-bold text-green-800">
                        {{ portefeuille.soldePortefeuille }}€
                      </div>
                      <div class="text-sm text-green-600">Solde disponible</div>
                    </div>
                  </template>
                </Card>
              </div>

              <!-- Total des gains -->
              <div class="col-12 md:col-4">
                <Card class="bg-blue-50 border-blue-200">
                  <template #content>
                    <div class="text-center">
                      <i class="pi pi-chart-line text-4xl text-blue-600 mb-3"></i>
                      <div class="text-2xl font-bold text-blue-800">
                        {{ portefeuille.totalGagnes }}€
                      </div>
                      <div class="text-sm text-blue-600">Total des gains</div>
                    </div>
                  </template>
                </Card>
              </div>

              <!-- Bouton retrait -->
              <div class="col-12 md:col-4">
                <Card class="h-full">
                  <template #content>
                    <div class="text-center flex flex-column justify-content-center h-full">
                      <Button
                        label="Effectuer un retrait"
                        icon="pi pi-download"
                        class="w-full"
                        :disabled="portefeuille.soldePortefeuille <= 0"
                        @click="openWithdrawDialog"
                      />
                      <small class="text-600 mt-2">
                        Retirez vos gains vers votre compte bancaire
                      </small>
                    </div>
                  </template>
                </Card>
              </div>
            </div>
          </div>

          <!-- Historique des retraits -->
          <div class="col-12">
            <Card>
              <template #header>
                <div class="p-4 pb-0">
                  <h2 class="text-xl font-semibold ecodeli-title m-0">
                    <i class="pi pi-history mr-2"></i>
                    Historique des retraits
                  </h2>
                </div>
              </template>
              <template #content>
                <div v-if="retraits.length === 0" class="text-center p-6">
                  <i class="pi pi-inbox text-4xl text-400 mb-3"></i>
                  <p class="text-600">Aucun retrait effectué</p>
                </div>

                <DataTable v-else :value="retraits" class="p-datatable-sm">
                  <Column field="dateDemande" header="Date">
                    <template #body="slotProps">
                      {{ formatDate(slotProps.data.dateDemande) }}
                    </template>
                  </Column>
                  <Column field="montant" header="Montant">
                    <template #body="slotProps">
                      <span class="font-semibold">{{ slotProps.data.montant }}€</span>
                    </template>
                  </Column>
                  <Column field="iban" header="IBAN">
                    <template #body="slotProps">
                      {{ formatIban(slotProps.data.iban) }}
                    </template>
                  </Column>
                  <Column field="statut" header="Statut">
                    <template #body="slotProps">
                      <Tag
                        :value="slotProps.data.statut"
                        :severity="slotProps.data.statut === 'TRAITE' ? 'success' : 'warning'"
                      />
                    </template>
                  </Column>
                </DataTable>
              </template>
            </Card>
          </div>

        </div>
      </div>
    </main>

    <!-- Dialog de retrait -->
    <Dialog
      v-model:visible="showWithdrawDialog"
      modal
      header="Effectuer un retrait"
      :style="{ width: '400px' }"
    >
      <div class="grid gap-4">
        <!-- Montant -->
        <div class="col-12">
          <label for="montant" class="font-medium text-900 mb-2 block">
            Montant à retirer (€)
          </label>
          <InputNumber
            id="montant"
            v-model="withdrawForm.montant"
            :min="1"
            :max="portefeuille.soldePortefeuille"
            :maxFractionDigits="2"
            placeholder="0.00"
            class="w-full"
          />
          <small class="text-600">
            Solde disponible: {{ portefeuille.soldePortefeuille }}€
          </small>
        </div>

        <!-- IBAN -->
        <div class="col-12">
          <label for="iban" class="font-medium text-900 mb-2 block">
            IBAN de destination (fictif)
          </label>
          <InputText
            id="iban"
            v-model="withdrawForm.iban"
            placeholder="FR76 1234 5678 9012 3456 7890 123"
            class="w-full"
          />
          <small class="text-600">
            <i class="pi pi-info-circle mr-1"></i>
            Saisissez un IBAN fictif pour la démonstration
          </small>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-content-end gap-2">
          <Button
            label="Annuler"
            icon="pi pi-times"
            severity="secondary"
            outlined
            @click="showWithdrawDialog = false"
            :disabled="withdrawing"
          />
          <Button
            :label="withdrawing ? 'Traitement...' : 'Confirmer le retrait'"
            icon="pi pi-check"
            :loading="withdrawing"
            :disabled="!canWithdraw"
            @click="processWithdraw"
          />
        </div>
      </template>
    </Dialog>

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

@media (min-width: 768px) {
  .md\:col-4 { grid-column: span 4; }
}

.max-w-6xl {
  max-width: 72rem;
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

.bg-blue-50 {
  background-color: #eff6ff;
}

.border-blue-200 {
  border-color: #bfdbfe;
}

.text-blue-800 {
  color: #1e40af;
}

.text-blue-600 {
  color: #2563eb;
}

.h-full {
  height: 100%;
}
</style>
