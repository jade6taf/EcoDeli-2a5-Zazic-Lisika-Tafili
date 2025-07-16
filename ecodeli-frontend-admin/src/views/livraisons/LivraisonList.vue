<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useLivraisonsStore } from '@/stores/livraisons'

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()
const livraisonsStore = useLivraisonsStore()

const filters = ref({
  search: '',
  statut: null,
  typeLivraison: null
})

const selectedLivraison = ref(null)
const livraisonDetailDialog = ref(false)
const changeStatutDialog = ref(false)
const newStatut = ref(null)

const statutOptions = [
  { label: 'Validée', value: 'VALIDEE' },
  { label: 'En cours', value: 'EN_COURS' },
  { label: 'Attente segment 2', value: 'ATTENTE_SEGMENT_2' },
  { label: 'Segment 2 en cours', value: 'SEGMENT_2_EN_COURS' },
  { label: 'Arrivé', value: 'ARRIVED' },
  { label: 'Terminée', value: 'TERMINEE' },
  { label: 'Annulée', value: 'ANNULEE' }
]

const typeOptions = [
  { label: 'Directe', value: 'DIRECTE' },
  { label: 'Partielle', value: 'PARTIELLE' }
]

const loading = computed(() => livraisonsStore.loading)
const livraisons = computed(() => livraisonsStore.livraisons)

const filteredLivraisons = computed(() => {
  let result = livraisons.value

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(livraison =>
      livraison.adresseEnvoi?.toLowerCase().includes(search) ||
      livraison.adresseDeLivraison?.toLowerCase().includes(search) ||
      livraison.expediteur?.email?.toLowerCase().includes(search) ||
      livraison.destinataire?.email?.toLowerCase().includes(search) ||
      livraison.annonce?.titre?.toLowerCase().includes(search) ||
      livraison.idLivraison?.toString().includes(search)
    )
  }

  if (filters.value.statut) {
    result = result.filter(livraison => livraison.statut === filters.value.statut)
  }

  if (filters.value.typeLivraison) {
    result = result.filter(livraison => livraison.typeLivraison === filters.value.typeLivraison)
  }

  return result
})

const loadLivraisons = async () => {
  const result = await livraisonsStore.fetchLivraisons()
  if (!result.success) {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
}

const resetFilters = () => {
  filters.value.search = ''
  filters.value.statut = null
  filters.value.typeLivraison = null
}

const viewLivraison = (livraison) => {
  selectedLivraison.value = livraison
  livraisonDetailDialog.value = true
}

const changeStatut = (livraison) => {
  selectedLivraison.value = livraison
  newStatut.value = livraison.statut
  changeStatutDialog.value = true
}

const confirmChangeStatut = async () => {
  if (!selectedLivraison.value || !newStatut.value) return

  const result = await livraisonsStore.updateStatutLivraison(
    selectedLivraison.value.idLivraison,
    newStatut.value
  )

  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Succès',
      detail: result.message,
      life: 3000
    })
    changeStatutDialog.value = false
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
}

const annulerLivraison = (livraison) => {
  confirm.require({
    message: `Voulez-vous vraiment annuler la livraison #${livraison.idLivraison} ?`,
    header: 'Confirmer l\'annulation',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    acceptLabel: 'Annuler la livraison',
    rejectLabel: 'Garder',
    accept: async () => {
      const result = await livraisonsStore.annulerLivraison(livraison.idLivraison)
      if (result.success) {
        toast.add({
          severity: 'success',
          summary: 'Livraison annulée',
          detail: result.message,
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
    }
  })
}

const exportLivraisons = () => {
  toast.add({
    severity: 'info',
    summary: 'Export',
    detail: 'Fonctionnalité d\'export en cours de développement',
    life: 3000
  })
}

onMounted(() => {
  loadLivraisons()
})
</script>

<template>
  <div class="livraisons-page">
    <div class="page-header mb-4">
      <div class="flex justify-content-between align-items-center">
        <div>
          <h1 class="text-3xl font-bold text-800 m-0">
            <i class="pi pi-truck mr-3 text-blue-500"></i>
            Gestion des Livraisons
          </h1>
          <p class="text-600 mt-2">Gérer toutes les livraisons directes et partielles</p>
        </div>
      </div>
    </div>

    <Card class="mb-4">
      <template #content>
        <div class="grid">
          <div class="col-12 md:col-4">
            <div class="field">
              <label for="search" class="field-label">Rechercher</label>
              <InputText
                id="search"
                v-model="filters.search"
                placeholder="ID, adresse, email..."
                class="w-full"
                @input="onFilter"
              />
            </div>
          </div>
          <div class="col-12 md:col-3">
            <div class="field">
              <label for="statut" class="field-label">Statut</label>
              <Dropdown
                id="statut"
                v-model="filters.statut"
                :options="statutOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Tous les statuts"
                class="w-full"
                showClear
                @change="onFilter"
              />
            </div>
          </div>
          <div class="col-12 md:col-3">
            <div class="field">
              <label for="typeLivraison" class="field-label">Type</label>
              <Dropdown
                id="typeLivraison"
                v-model="filters.typeLivraison"
                :options="typeOptions"
                optionLabel="label"
                optionValue="value"
                placeholder="Tous les types"
                class="w-full"
                showClear
                @change="onFilter"
              />
            </div>
          </div>
          <div class="col-12 md:col-2">
            <div class="field">
              <label class="field-label">&nbsp;</label>
              <div class="flex gap-2">
                <Button
                  label="Reset"
                  icon="pi pi-refresh"
                  class="p-button-outlined"
                  @click="resetFilters"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <Card class="admin-datatable">
      <template #header>
        <div class="flex justify-content-between align-items-center">
          <h3 class="m-0">Liste des Livraisons</h3>
          <div class="flex gap-2">
            <Button
              icon="pi pi-refresh"
              class="p-button-outlined p-button-sm"
              @click="loadLivraisons"
              :loading="loading"
            />
            <Button
              icon="pi pi-download"
              class="p-button-outlined p-button-sm"
              @click="exportLivraisons"
            />
          </div>
        </div>
      </template>

      <template #content>
        <DataTable
          :value="filteredLivraisons"
          :paginator="true"
          :rows="10"
          :loading="loading"
          dataKey="idLivraison"
          responsiveLayout="scroll"
        >
          <Column field="idLivraison" header="ID" :sortable="true" style="min-width: 80px">
            <template #body="slotProps">
              <Tag :value="'#' + slotProps.data.idLivraison" severity="secondary" />
            </template>
          </Column>

          <Column field="statut" header="Statut" :sortable="true" style="min-width: 140px">
            <template #body="slotProps">
              <Tag
                :value="livraisonsStore.formatStatutLabel(slotProps.data.statut)"
                :severity="livraisonsStore.getStatutSeverity(slotProps.data.statut)"
              />
            </template>
          </Column>

          <Column field="typeLivraison" header="Type" :sortable="true" style="min-width: 100px">
            <template #body="slotProps">
              <Tag
                :value="livraisonsStore.formatTypeLabel(slotProps.data.typeLivraison)"
                :severity="livraisonsStore.getTypeSeverity(slotProps.data.typeLivraison)"
              />
            </template>
          </Column>

          <Column field="adresseEnvoi" header="Départ" :sortable="true" style="min-width: 200px">
            <template #body="slotProps">
              <div class="text-sm">
                {{ slotProps.data.adresseEnvoi }}
                <br>
                <span class="text-500">{{ slotProps.data.codePostalEnvoi }}</span>
              </div>
            </template>
          </Column>

          <Column field="adresseDeLivraison" header="Arrivée" :sortable="true" style="min-width: 200px">
            <template #body="slotProps">
              <div class="text-sm">
                {{ slotProps.data.adresseDeLivraison }}
                <br>
                <span class="text-500">{{ slotProps.data.codePostalLivraison }}</span>
              </div>
            </template>
          </Column>

          <Column field="prix" header="Prix" :sortable="true" style="min-width: 100px">
            <template #body="slotProps">
              {{ livraisonsStore.formatPrice(slotProps.data.prix) }}
            </template>
          </Column>

          <Column field="dateDebut" header="Date début" :sortable="true" style="min-width: 140px">
            <template #body="slotProps">
              {{ livraisonsStore.formatDate(slotProps.data.dateDebut) }}
            </template>
          </Column>

          <Column header="Actions" style="min-width: 150px">
            <template #body="slotProps">
              <div class="flex gap-2">
                <Button
                  icon="pi pi-eye"
                  class="p-button-sm p-button-outlined"
                  @click="viewLivraison(slotProps.data)"
                />
                <Button
                  icon="pi pi-cog"
                  class="p-button-sm p-button-outlined"
                  @click="changeStatut(slotProps.data)"
                />
                <Button
                  icon="pi pi-times"
                  class="p-button-sm p-button-outlined p-button-danger"
                  @click="annulerLivraison(slotProps.data)"
                  :disabled="slotProps.data.statut === 'TERMINEE' || slotProps.data.statut === 'ANNULEE'"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Dialog Détails Livraison -->
    <Dialog
      v-model:visible="livraisonDetailDialog"
      modal
      header="Détails de la livraison"
      :style="{ width: '80vw', maxWidth: '800px' }"
    >
      <div v-if="selectedLivraison" class="livraison-details">
        <div class="grid">
          <div class="col-12 md:col-6">
            <h4>Informations générales</h4>
            <div class="detail-row">
              <strong>ID:</strong> #{{ selectedLivraison.idLivraison }}
            </div>
            <div class="detail-row">
              <strong>Statut:</strong>
              <Tag
                :value="livraisonsStore.formatStatutLabel(selectedLivraison.statut)"
                :severity="livraisonsStore.getStatutSeverity(selectedLivraison.statut)"
                class="ml-2"
              />
            </div>
            <div class="detail-row">
              <strong>Type:</strong>
              <Tag
                :value="livraisonsStore.formatTypeLabel(selectedLivraison.typeLivraison)"
                :severity="livraisonsStore.getTypeSeverity(selectedLivraison.typeLivraison)"
                class="ml-2"
              />
            </div>
            <div class="detail-row">
              <strong>Prix:</strong> {{ livraisonsStore.formatPrice(selectedLivraison.prix) }}
            </div>
            <div class="detail-row">
              <strong>Date début:</strong> {{ livraisonsStore.formatDate(selectedLivraison.dateDebut) }}
            </div>
            <div class="detail-row">
              <strong>Date fin:</strong> {{ livraisonsStore.formatDate(selectedLivraison.dateFin) }}
            </div>
          </div>

          <div class="col-12 md:col-6">
            <h4>Adresses</h4>
            <div class="detail-row">
              <strong>Départ:</strong>
              <div>{{ selectedLivraison.adresseEnvoi }}</div>
              <div class="text-500">{{ selectedLivraison.codePostalEnvoi }}</div>
            </div>
            <div class="detail-row">
              <strong>Arrivée:</strong>
              <div>{{ selectedLivraison.adresseDeLivraison }}</div>
              <div class="text-500">{{ selectedLivraison.codePostalLivraison }}</div>
            </div>
          </div>

          <div class="col-12" v-if="selectedLivraison.expediteur || selectedLivraison.destinataire">
            <h4>Personnes impliquées</h4>
            <div class="grid">
              <div class="col-12 md:col-6" v-if="selectedLivraison.expediteur">
                <div class="detail-section">
                  <strong>Expéditeur:</strong>
                  <div>{{ selectedLivraison.expediteur.prenom }} {{ selectedLivraison.expediteur.nom }}</div>
                  <div class="text-500">{{ selectedLivraison.expediteur.email }}</div>
                </div>
              </div>
              <div class="col-12 md:col-6" v-if="selectedLivraison.destinataire">
                <div class="detail-section">
                  <strong>Destinataire:</strong>
                  <div>{{ selectedLivraison.destinataire.prenom }} {{ selectedLivraison.destinataire.nom }}</div>
                  <div class="text-500">{{ selectedLivraison.destinataire.email }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-12" v-if="selectedLivraison.typeLivraison === 'PARTIELLE'">
            <h4>Informations livraison partielle</h4>
            <div class="detail-row" v-if="selectedLivraison.entrepotVille">
              <strong>Entrepôt:</strong> {{ selectedLivraison.entrepotVille }}
            </div>
            <div class="detail-row" v-if="selectedLivraison.dateDepotEntrepot">
              <strong>Date dépôt entrepôt:</strong> {{ livraisonsStore.formatDate(selectedLivraison.dateDepotEntrepot) }}
            </div>
            <div class="detail-row" v-if="selectedLivraison.dateCollecteEntrepot">
              <strong>Date collecte entrepôt:</strong> {{ livraisonsStore.formatDate(selectedLivraison.dateCollecteEntrepot) }}
            </div>
            <div class="grid" v-if="selectedLivraison.livreurSegment1 || selectedLivraison.livreurSegment2">
              <div class="col-12 md:col-6" v-if="selectedLivraison.livreurSegment1">
                <div class="detail-section">
                  <strong>Livreur segment 1:</strong>
                  <div>{{ selectedLivraison.livreurSegment1.prenom }} {{ selectedLivraison.livreurSegment1.nom }}</div>
                </div>
              </div>
              <div class="col-12 md:col-6" v-if="selectedLivraison.livreurSegment2">
                <div class="detail-section">
                  <strong>Livreur segment 2:</strong>
                  <div>{{ selectedLivraison.livreurSegment2.prenom }} {{ selectedLivraison.livreurSegment2.nom }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-12" v-if="selectedLivraison.annonce">
            <h4>Annonce associée</h4>
            <div class="detail-row">
              <strong>Titre:</strong> {{ selectedLivraison.annonce.titre }}
            </div>
            <div class="detail-row" v-if="selectedLivraison.annonce.description">
              <strong>Description:</strong> {{ selectedLivraison.annonce.description }}
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <Button
          label="Fermer"
          icon="pi pi-times"
          @click="livraisonDetailDialog = false"
          class="p-button-text"
        />
      </template>
    </Dialog>

    <!-- Dialog Changement de Statut -->
    <Dialog
      v-model:visible="changeStatutDialog"
      modal
      header="Changer le statut"
      :style="{ width: '400px' }"
    >
      <div v-if="selectedLivraison">
        <p>Livraison #{{ selectedLivraison.idLivraison }}</p>
        <p class="mb-3">
          Statut actuel:
          <Tag
            :value="livraisonsStore.formatStatutLabel(selectedLivraison.statut)"
            :severity="livraisonsStore.getStatutSeverity(selectedLivraison.statut)"
            class="ml-2"
          />
        </p>

        <div class="field">
          <label for="newStatut" class="field-label">Nouveau statut</label>
          <Dropdown
            id="newStatut"
            v-model="newStatut"
            :options="statutOptions"
            optionLabel="label"
            optionValue="value"
            placeholder="Sélectionner un statut"
            class="w-full"
          />
        </div>
      </div>

      <template #footer>
        <Button
          label="Annuler"
          icon="pi pi-times"
          @click="changeStatutDialog = false"
          class="p-button-text"
        />
        <Button
          label="Confirmer"
          icon="pi pi-check"
          @click="confirmChangeStatut"
          :disabled="!newStatut || newStatut === selectedLivraison?.statut"
        />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.livraisons-page {
  padding: 0;
}

.page-header {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  padding: 2rem;
  border-radius: 12px;
}

.field {
  margin-bottom: 1rem;
}

.field-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--surface-700);
}

.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1rem;
}

.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }
.col-4 { grid-column: span 4; }
.col-3 { grid-column: span 3; }
.col-2 { grid-column: span 2; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-4 { grid-column: span 4; }
  .md\:col-3 { grid-column: span 3; }
  .md\:col-2 { grid-column: span 2; }
}

.flex {
  display: flex;
}

.justify-content-between {
  justify-content: space-between;
}

.align-items-center {
  align-items: center;
}

.gap-2 {
  gap: 0.5rem;
}

.w-full {
  width: 100%;
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mb-3 {
  margin-bottom: 1rem;
}

.mt-2 {
  margin-top: 0.5rem;
}

.mr-3 {
  margin-right: 0.75rem;
}

.ml-2 {
  margin-left: 0.5rem;
}

.m-0 {
  margin: 0;
}

.text-3xl {
  font-size: 1.875rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-500 {
  color: var(--surface-500);
}

.text-600 {
  color: var(--surface-600);
}

.text-800 {
  color: var(--surface-800);
}

.font-bold {
  font-weight: 700;
}

/* Styles pour les détails */
.livraison-details {
  padding: 1rem 0;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--surface-200);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row strong {
  min-width: 120px;
  color: var(--surface-700);
  margin-right: 1rem;
}

.detail-section {
  margin-bottom: 1rem;
}

.detail-section strong {
  display: block;
  margin-bottom: 0.25rem;
  color: var(--surface-700);
}
</style>
