<template>
  <div class="portefeuille-prestataire">
    <!-- Header avec navigation -->
    <Card class="mb-4">
      <template #content>
        <div class="flex justify-content-between align-items-center">
          <div>
            <h1 class="text-3xl font-bold ecodeli-title m-0">Mon Portefeuille</h1>
            <p class="text-600 mt-2 mb-0">
              Gérez vos gains et effectuez vos retraits
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
              @click="chargerPortefeuille"
              :loading="loading"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Soldes et statistiques -->
    <div class="grid mb-4">
      <div class="col-12 md:col-4">
        <Card class="text-center h-full">
          <template #content>
            <div class="text-primary">
              <i class="pi pi-wallet text-4xl mb-3"></i>
              <div class="text-3xl font-bold">{{ portefeuille.soldeDisponible || 0 }}€</div>
              <div class="text-600">Solde disponible</div>
            </div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-4">
        <Card class="text-center h-full">
          <template #content>
            <div class="text-green-500">
              <i class="pi pi-chart-line text-4xl mb-3"></i>
              <div class="text-2xl font-bold">{{ portefeuille.totalGagne || 0 }}€</div>
              <div class="text-600">Total gagné</div>
            </div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-4">
        <Card class="text-center h-full">
          <template #content>
            <div class="text-blue-500">
              <i class="pi pi-money-bill text-4xl mb-3"></i>
              <div class="text-2xl font-bold">{{ portefeuille.totalRetire || 0 }}€</div>
              <div class="text-600">Total retiré</div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Formulaire de retrait -->
    <Card class="mb-4" v-if="portefeuille.soldeDisponible > 0">
      <template #title>
        <div class="flex align-items-center">
          <i class="pi pi-download mr-2"></i>
          Demander un retrait
        </div>
      </template>
      <template #content>
        <div class="grid">
          <div class="col-12 md:col-6">
            <div class="field mb-3">
              <label for="montantRetrait" class="font-semibold mb-2 block">Montant à retirer *</label>
              <div class="p-inputgroup">
                <InputNumber 
                  id="montantRetrait"
                  v-model="retraitForm.montant"
                  :min="1"
                  :max="portefeuille.soldeDisponible"
                  :maxFractionDigits="2"
                  placeholder="Montant en euros"
                  class="w-full"
                />
                <span class="p-inputgroup-addon">€</span>
              </div>
              <small class="text-500">Maximum : {{ portefeuille.soldeDisponible }}€</small>
            </div>
            
            <div class="field mb-3">
              <label for="ibanRetrait" class="font-semibold mb-2 block">IBAN de destination *</label>
              <InputText 
                id="ibanRetrait"
                v-model="retraitForm.iban"
                placeholder="FR76 1234 5678 9012 3456 789"
                class="w-full"
                :value="portefeuille.ibanMasque || 'FR76 1234 5678 9012 3456 789'"
              />
              <small class="text-500">Virement bancaire fictif</small>
            </div>
          </div>
          
          <div class="col-12 md:col-6">
            <div class="retrait-info p-3 surface-50 border-round">
              <h5 class="mt-0 mb-3">Informations de retrait</h5>
              <div class="flex justify-content-between mb-2">
                <span>Montant demandé :</span>
                <strong>{{ retraitForm.montant || 0 }}€</strong>
              </div>
              <div class="flex justify-content-between mb-2">
                <span>Frais de virement :</span>
                <strong>0.00€ (gratuit)</strong>
              </div>
              <Divider />
              <div class="flex justify-content-between mb-3">
                <span class="font-semibold">Montant final :</span>
                <strong class="text-primary">{{ retraitForm.montant || 0 }}€</strong>
              </div>
              <div class="text-sm text-600">
                <p class="mb-1">⏱️ Délai : 24-48h ouvrées</p>
                <p class="mb-1">🏦 Virement SEPA fictif</p>
                <p class="mb-0">📧 Confirmation par email</p>
              </div>
            </div>
          </div>
        </div>
        
        <div class="flex gap-2 mt-4">
          <Button 
            label="Demander le retrait"
            icon="pi pi-download"
            :disabled="!retraitValide"
            @click="demanderRetrait"
            :loading="loadingRetrait"
          />
          <Button 
            label="Annuler"
            severity="secondary"
            outlined
            @click="reinitialiserRetrait"
          />
        </div>
      </template>
    </Card>

    <!-- Message si pas de solde -->
    <Card v-else class="mb-4" style="background: #fef3c7;">
      <template #content>
        <div class="flex align-items-center">
          <i class="pi pi-info-circle text-yellow-600 text-2xl mr-3"></i>
          <div>
            <h4 class="text-lg font-medium mb-1 text-yellow-800">Aucun solde disponible</h4>
            <p class="text-yellow-700 mb-0">Réalisez des missions pour commencer à gagner de l'argent.</p>
          </div>
        </div>
      </template>
    </Card>

    <!-- Historique des transactions -->
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <div class="flex align-items-center">
            <i class="pi pi-history mr-2"></i>
            Historique des transactions ({{ transactions.length }})
          </div>
          <div class="flex gap-2">
            <Button 
              :label="showAllTransactions ? 'Voir moins' : 'Voir toutes'"
              icon="pi pi-eye"
              text
              @click="toggleShowAll"
            />
          </div>
        </div>
      </template>
      <template #content>
        <div v-if="loadingTransactions" class="text-center p-4">
          <ProgressSpinner />
          <p class="mt-2">Chargement des transactions...</p>
        </div>
        
        <div v-else-if="transactions.length === 0" class="text-center p-4 text-600">
          <i class="pi pi-history text-4xl mb-3"></i>
          <p>Aucune transaction pour le moment</p>
          <small>Vos gains et retraits apparaîtront ici</small>
        </div>
        
        <div v-else class="space-y-3">
          <div
            v-for="transaction in transactionsAffichees"
            :key="transaction.idTransaction"
            class="transaction-card p-4 border-round surface-100"
          >
            <div class="flex justify-content-between align-items-start">
              <div class="flex align-items-center gap-3 flex-1">
                <div class="transaction-icon">
                  <i 
                    :class="getTransactionIcon(transaction)"
                    class="text-2xl"
                    :style="{ color: getTransactionColor(transaction) }"
                  ></i>
                </div>
                
                <div class="flex-1">
                  <div class="flex justify-content-between align-items-center mb-1">
                    <h5 class="m-0 font-semibold">{{ transaction.description }}</h5>
                    <div class="text-right">
                      <div 
                        class="text-lg font-bold"
                        :style="{ color: getTransactionColor(transaction) }"
                      >
                        {{ transaction.isCredit ? '+' : '-' }}{{ transaction.montant }}€
                      </div>
                      <Tag 
                        :value="transaction.statutLabel"
                        :severity="getStatutSeverity(transaction.statut)"
                        size="small"
                      />
                    </div>
                  </div>
                  
                  <div class="flex justify-content-between align-items-center text-sm text-600">
                    <span>{{ formatDate(transaction.dateTransaction) }}</span>
                    <span v-if="transaction.reference">Ref: {{ transaction.reference }}</span>
                  </div>
                  
                  <div v-if="transaction.missionTitre" class="text-sm text-500 mt-1">
                    Mission : {{ transaction.missionTitre }}
                  </div>
                  
                  <div v-if="transaction.ibanDestination" class="text-sm text-500 mt-1">
                    Vers : {{ transaction.ibanDestination }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <Toast />
    <ConfirmDialog />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()
const confirm = useConfirm()

const portefeuille = ref({
  soldeDisponible: 0,
  soldeEnAttente: 0,
  totalGagne: 0,
  totalRetire: 0,
  nombreTransactions: 0,
  ibanMasque: '',
  nomTitulaire: ''
})

const transactions = ref([])
const loading = ref(false)
const loadingTransactions = ref(false)
const loadingRetrait = ref(false)
const showAllTransactions = ref(false)

const retraitForm = ref({
  montant: null,
  iban: ''
})

const prestataireId = computed(() => {
  const id = authStore.user?.id || authStore.user?.idUtilisateur
  
  if (!id) {
    console.warn('ID prestataire non trouvé, utilisation fallback pour développement')
    return 2
  }
  
  return id
})

const retraitValide = computed(() => {
  return retraitForm.value.montant > 0 && 
         retraitForm.value.montant <= portefeuille.value.soldeDisponible &&
         retraitForm.value.iban && 
         retraitForm.value.iban.length > 10
})

const transactionsAffichees = computed(() => {
  return showAllTransactions.value ? transactions.value : transactions.value.slice(0, 10)
})

const formatDate = (dateString) => {
  if (!dateString) return 'Date non spécifiée'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'Date invalide'
  }
}

const getTransactionIcon = (transaction) => {
  if (transaction.isCredit) {
    return transaction.type === 'CREDIT_MISSION' ? 'pi pi-plus-circle' : 'pi pi-arrow-up'
  } else {
    return transaction.type === 'RETRAIT_PRESTATAIRE' ? 'pi pi-download' : 'pi pi-arrow-down'
  }
}

const getTransactionColor = (transaction) => {
  return transaction.isCredit ? '#10b981' : '#ef4444'
}

const getStatutSeverity = (statut) => {
  const severities = {
    'EFFECTUE': 'success',
    'EN_COURS': 'warning',
    'EN_ATTENTE': 'info',
    'ECHOUE': 'danger'
  }
  return severities[statut] || 'secondary'
}

const chargerPortefeuille = async () => {
  loading.value = true
  
  try {
    if (!prestataireId.value || prestataireId.value === 2) {
      console.warn('Utilisation ID prestataire de développement')
    }
    
    const response = await axios.get(`http://localhost:8080/api/paiement/portefeuille/prestataire/${prestataireId.value}`)
    
    if (response.data) {
      portefeuille.value = response.data
      
      if (response.data.ibanMasque && !retraitForm.value.iban) {
        retraitForm.value.iban = response.data.ibanMasque
      }
    }
    
  } catch (error) {
    console.error('Erreur chargement portefeuille:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger le portefeuille',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

const chargerTransactions = async () => {
  loadingTransactions.value = true
  
  try {
    const limit = showAllTransactions.value ? 100 : 20
    const response = await axios.get(`http://localhost:8080/api/paiement/portefeuille/prestataire/${prestataireId.value}/transactions?limit=${limit}`)
    
    if (response.data) {
      transactions.value = response.data
    }
    
  } catch (error) {
    console.error('Erreur chargement transactions:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de charger les transactions',
      life: 3000
    })
  } finally {
    loadingTransactions.value = false
  }
}

const demanderRetrait = () => {
  confirm.require({
    message: `Confirmer le retrait de ${retraitForm.value.montant}€ vers votre compte bancaire ?`,
    header: 'Confirmer le retrait',
    icon: 'pi pi-download',
    acceptClass: 'p-button-warning',
    acceptLabel: 'Confirmer le retrait',
    rejectLabel: 'Annuler',
    accept: async () => {
      await executerRetrait()
    }
  })
}

const executerRetrait = async () => {
  loadingRetrait.value = true
  
  try {
    const retraitData = {
      montant: retraitForm.value.montant,
      iban: retraitForm.value.iban
    }
    
    const response = await axios.post(`http://localhost:8080/api/paiement/portefeuille/prestataire/${prestataireId.value}/retrait`, retraitData)
    
    if (response.data.success) {
      toast.add({
        severity: 'success',
        summary: 'Retrait effectué !',
        detail: `${response.data.montantRetrait}€ seront virés sous ${response.data.delaiVirement}`,
        life: 5000
      })
      
      await chargerPortefeuille()
      await chargerTransactions()
      
      reinitialiserRetrait()
      
    } else {
      throw new Error(response.data.error)
    }
    
  } catch (error) {
    console.error('Erreur retrait:', error)
    toast.add({
      severity: 'error',
      summary: 'Erreur retrait',
      detail: error.response?.data?.error || error.message || 'Erreur lors du retrait',
      life: 3000
    })
  } finally {
    loadingRetrait.value = false
  }
}

const reinitialiserRetrait = () => {
  retraitForm.value = {
    montant: null,
    iban: portefeuille.value.ibanMasque || ''
  }
}

const toggleShowAll = () => {
  showAllTransactions.value = !showAllTransactions.value
  if (showAllTransactions.value) {
    chargerTransactions()
  }
}

const retourDashboard = () => {
  router.push('/prestataire')
}

onMounted(async () => {
  await chargerPortefeuille()
  await chargerTransactions()
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
.col-4 { grid-column: span 4; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-4 { grid-column: span 4; }
}

.space-y-3 > * + * {
  margin-top: 0.75rem;
}

.transaction-card {
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
}

.transaction-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.text-primary {
  color: var(--ecodeli-green) !important;
}

.h-full {
  height: 100%;
}

.retrait-info {
  background: #f8fafc;
}

.field {
  margin-bottom: 1rem;
}

.flex-1 { 
  flex: 1; 
}
</style>