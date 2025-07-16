<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { usePrestataireStore } from '@/stores/prestataires'
import InputNumber from 'primevue/inputnumber'

const toast = useToast()
const confirm = useConfirm()
const prestataireStore = usePrestataireStore()

const filters = ref({
  search: '',
  categorieValidation: null,
  statutValidation: null
})

const selectedPrestataire = ref(null)
const prestataireDetailDialog = ref(false)
const validationDialog = ref(false)
const tarifDialog = ref(false)
const selectedCategorie = ref(null)
const nouveauTarif = ref(null)

const categorieOptions = [
  { label: 'Transport & Livraison', value: 'TRANSPORT_LIVRAISON' },
  { label: 'Services à Domicile', value: 'SERVICES_DOMICILE' },
  { label: 'Travaux & Réparations', value: 'TRAVAUX_REPARATIONS' },
  { label: 'Courses & Achats', value: 'COURSES_ACHATS' },
  { label: 'Services Personnels', value: 'SERVICES_PERSONNELS' },
  { label: 'Éducation & Formation', value: 'EDUCATION_FORMATION' }
]

const statutOptions = [
  { label: 'En attente', value: 'EN_ATTENTE' },
  { label: 'Validé', value: 'VALIDE' },
  { label: 'Rejeté', value: 'REJETE' }
]

const loading = computed(() => prestataireStore.loading)
const prestataires = computed(() => prestataireStore.prestataires)

const filteredPrestataires = computed(() => {
  let result = prestataires.value

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    result = result.filter(prestataire =>
      prestataire.nom.toLowerCase().includes(search) ||
      prestataire.prenom.toLowerCase().includes(search) ||
      prestataire.email.toLowerCase().includes(search) ||
      prestataire.nomEntreprise?.toLowerCase().includes(search)
    )
  }

  return result
})

const loadPrestataires = async () => {
  const result = await prestataireStore.fetchPrestataires()
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
  filters.value.categorieValidation = null
  filters.value.statutValidation = null
}

const viewPrestataire = (prestataire) => {
  selectedPrestataire.value = prestataire
  prestataireDetailDialog.value = true
}

const openValidationDialog = (prestataire, categorie) => {
  selectedPrestataire.value = prestataire
  selectedCategorie.value = categorie
  validationDialog.value = true
}

const openTarifDialog = (prestataire, categorie) => {
  selectedPrestataire.value = prestataire
  selectedCategorie.value = categorie
  nouveauTarif.value = prestataire.tarifHoraire || null
  tarifDialog.value = true
}

const validerCategorie = async (statut) => {
  const result = await prestataireStore.validerCategoriePrestataire(
    selectedPrestataire.value.idUtilisateur,
    selectedCategorie.value,
    statut
  )

  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Validation mise à jour',
      detail: `Catégorie ${statut.toLowerCase()} avec succès`,
      life: 3000
    })
    await loadPrestataires()
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
  
  validationDialog.value = false
}

const definirTarif = async () => {
  if (!nouveauTarif.value || nouveauTarif.value <= 0) {
    toast.add({
      severity: 'warn',
      summary: 'Tarif invalide',
      detail: 'Veuillez saisir un tarif valide',
      life: 3000
    })
    return
  }

  const result = await prestataireStore.definirTarifPrestataire(
    selectedPrestataire.value.idUtilisateur,
    selectedCategorie.value,
    nouveauTarif.value
  )

  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Tarif défini',
      detail: `Tarif de ${nouveauTarif.value}€/h défini avec succès`,
      life: 3000
    })
    await loadPrestataires()
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.message,
      life: 3000
    })
  }
  
  tarifDialog.value = false
}

const getStatutSeverity = (statut) => {
  const severities = {
    'EN_ATTENTE': 'warning',
    'VALIDE': 'success',
    'REJETE': 'danger'
  }
  return severities[statut] || 'secondary'
}

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

const getStatutGlobalLabel = (statut) => {
  const labels = {
    'EN_ATTENTE': 'En attente',
    'VALIDE': 'Validé',
    'REJETE': 'Rejeté',
    'SUSPENDU': 'Suspendu'
  }
  return labels[statut] || 'En attente'
}

const getStatutGlobalSeverity = (statut) => {
  const severities = {
    'EN_ATTENTE': 'warning',
    'VALIDE': 'success',
    'REJETE': 'danger',
    'SUSPENDU': 'secondary'
  }
  return severities[statut] || 'warning'
}

onMounted(() => {
  loadPrestataires()
})
</script>

<template>
  <div class="prestataires-page">
    <div class="page-header mb-4">
      <div class="flex justify-content-between align-items-center">
        <div>
          <h1 class="text-3xl font-bold text-800 m-0">
            <i class="pi pi-wrench mr-3 text-orange-500"></i>
            Gestion des Prestataires
          </h1>
          <p class="text-600 mt-2">Valider les prestataires et définir leurs tarifs</p>
        </div>
      </div>
    </div>

    <!-- Filtres -->
    <Card class="mb-4">
      <template #content>
        <div class="grid">
          <div class="col-12 md:col-6">
            <div class="field">
              <label for="search" class="field-label">Rechercher</label>
              <InputText
                id="search"
                v-model="filters.search"
                placeholder="Nom, email, entreprise..."
                class="w-full"
              />
            </div>
          </div>
          <div class="col-12 md:col-6">
            <div class="field">
              <label class="field-label">&nbsp;</label>
              <div class="flex gap-2">
                <Button
                  label="Reset"
                  icon="pi pi-refresh"
                  class="p-button-outlined"
                  @click="resetFilters"
                />
                <Button
                  icon="pi pi-refresh"
                  class="p-button-outlined"
                  @click="loadPrestataires"
                  :loading="loading"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Table des prestataires -->
    <Card class="admin-datatable">
      <template #header>
        <div class="flex justify-content-between align-items-center">
          <h3 class="m-0">Liste des Prestataires</h3>
        </div>
      </template>

      <template #content>
        <DataTable
          :value="filteredPrestataires"
          :paginator="true"
          :rows="10"
          :loading="loading"
          dataKey="idUtilisateur"
          responsiveLayout="scroll"
        >
          <Column field="idUtilisateur" header="ID" :sortable="true" style="min-width: 80px">
            <template #body="slotProps">
              <Tag :value="slotProps.data.idUtilisateur" severity="secondary" />
            </template>
          </Column>

          <Column field="nomEntreprise" header="Entreprise" :sortable="true" style="min-width: 150px">
            <template #body="slotProps">
              <div>
                <div class="font-semibold">{{ slotProps.data.nomEntreprise || 'Non renseigné' }}</div>
                <div class="text-sm text-600">{{ slotProps.data.prenom }} {{ slotProps.data.nom }}</div>
              </div>
            </template>
          </Column>

          <Column field="email" header="Contact" :sortable="true" style="min-width: 200px">
            <template #body="slotProps">
              <div>
                <div>{{ slotProps.data.email }}</div>
                <div class="text-sm text-600">{{ slotProps.data.telephone || 'Tél. non renseigné' }}</div>
              </div>
            </template>
          </Column>

          <Column header="Statut Global" :sortable="true" style="min-width: 120px">
            <template #body="slotProps">
              <Tag
                :value="getStatutGlobalLabel(slotProps.data.statutValidation)"
                :severity="getStatutGlobalSeverity(slotProps.data.statutValidation)"
              />
            </template>
          </Column>

          <Column header="Tarif Horaire" style="min-width: 100px">
            <template #body="slotProps">
              <div v-if="slotProps.data.tarifHoraire" class="font-semibold text-green-600">
                {{ slotProps.data.tarifHoraire }}€/h
              </div>
              <Tag v-else value="Non défini" severity="warning" size="small" />
            </template>
          </Column>

          <Column header="Domaine & Validation" style="min-width: 300px">
            <template #body="slotProps">
              <div v-if="slotProps.data.domaineExpertise" class="p-3 border-round surface-50">
                <div class="flex align-items-center justify-content-between">
                  <div class="flex flex-column">
                    <span class="font-medium text-900">{{ getCategorieLabel(slotProps.data.domaineExpertise) }}</span>
                    <span class="text-sm text-600">Domaine d'expertise</span>
                  </div>
                  <div class="flex gap-2 align-items-center">
                    <Tag
                      value="En attente"
                      severity="warning"
                      size="small"
                    />
                    <Button
                      icon="pi pi-check"
                      class="p-button-sm p-button-outlined p-button-success"
                      @click="openValidationDialog(slotProps.data, slotProps.data.domaineExpertise)"
                      v-tooltip="'Valider/Rejeter'"
                    />
                    <Button
                      icon="pi pi-euro"
                      class="p-button-sm p-button-outlined"
                      @click="openTarifDialog(slotProps.data, slotProps.data.domaineExpertise)"
                      v-tooltip="'Définir tarif'"
                    />
                  </div>
                </div>
              </div>
              <div v-else class="text-center text-600 p-3">
                <i class="pi pi-info-circle mr-2"></i>
                Aucun domaine d'expertise défini
              </div>
            </template>
          </Column>

          <Column header="Actions" style="min-width: 120px">
            <template #body="slotProps">
              <div class="flex gap-2">
                <Button
                  icon="pi pi-eye"
                  class="p-button-sm p-button-outlined"
                  @click="viewPrestataire(slotProps.data)"
                  v-tooltip="'Voir détails'"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Dialog détails prestataire -->
    <Dialog
      v-model:visible="prestataireDetailDialog"
      modal
      header="Détails du prestataire"
      :style="{ width: '600px' }"
    >
      <div v-if="selectedPrestataire" class="prestataire-details">
        <div class="grid">
          <div class="col-12 md:col-6">
            <div class="detail-row">
              <strong>Nom :</strong> {{ selectedPrestataire.prenom }} {{ selectedPrestataire.nom }}
            </div>
            <div class="detail-row">
              <strong>Email :</strong> {{ selectedPrestataire.email }}
            </div>
            <div class="detail-row">
              <strong>Téléphone :</strong> {{ selectedPrestataire.telephone || 'Non renseigné' }}
            </div>
          </div>
          <div class="col-12 md:col-6">
            <div class="detail-row">
              <strong>Entreprise :</strong> {{ selectedPrestataire.nomEntreprise || 'Non renseigné' }}
            </div>
            <div class="detail-row">
              <strong>SIRET :</strong> {{ selectedPrestataire.siret || 'Non renseigné' }}
            </div>
            <div class="detail-row">
              <strong>Domaine :</strong> {{ getCategorieLabel(selectedPrestataire.domaineExpertise) }}
            </div>
          </div>
        </div>
        
        <Divider />
        
        <div class="detail-row">
          <strong>Description :</strong> 
          <p class="mt-2">{{ selectedPrestataire.description || 'Aucune description fournie' }}</p>
        </div>
      </div>

      <template #footer>
        <Button
          label="Fermer"
          icon="pi pi-times"
          @click="prestataireDetailDialog = false"
          class="p-button-text"
        />
      </template>
    </Dialog>

    <!-- Dialog validation catégorie -->
    <Dialog
      v-model:visible="validationDialog"
      modal
      header="Validation de catégorie"
      :style="{ width: '400px' }"
    >
      <div v-if="selectedPrestataire && selectedCategorie">
        <p>Valider la catégorie <strong>{{ getCategorieLabel(selectedCategorie) }}</strong> pour :</p>
        <p class="font-semibold">{{ selectedPrestataire.prenom }} {{ selectedPrestataire.nom }}</p>
        <p class="text-sm text-600">{{ selectedPrestataire.nomEntreprise }}</p>
      </div>

      <template #footer>
        <Button
          label="Rejeter"
          icon="pi pi-times"
          class="p-button-danger"
          @click="validerCategorie('REJETE')"
        />
        <Button
          label="Valider"
          icon="pi pi-check"
          class="p-button-success"
          @click="validerCategorie('VALIDE')"
        />
        <Button
          label="Annuler"
          class="p-button-text"
          @click="validationDialog = false"
        />
      </template>
    </Dialog>

    <!-- Dialog définition tarif -->
    <Dialog
      v-model:visible="tarifDialog"
      modal
      header="Définir le tarif horaire"
      :style="{ width: '400px' }"
    >
      <div v-if="selectedPrestataire && selectedCategorie">
        <p>Définir le tarif pour <strong>{{ getCategorieLabel(selectedCategorie) }}</strong> :</p>
        <p class="font-semibold">{{ selectedPrestataire.prenom }} {{ selectedPrestataire.nom }}</p>
        
        <div class="field mt-3">
          <label for="tarif" class="field-label">Tarif horaire (€/h)</label>
          <InputNumber
            id="tarif"
            v-model="nouveauTarif"
            :min="5"
            :max="200"
            suffix=" €/h"
            class="w-full"
            placeholder="ex: 25"
          />
        </div>
      </div>

      <template #footer>
        <Button
          label="Annuler"
          class="p-button-text"
          @click="tarifDialog = false"
        />
        <Button
          label="Définir le tarif"
          icon="pi pi-check"
          @click="definirTarif"
        />
      </template>
    </Dialog>

    <Toast />
    <ConfirmDialog />
  </div>
</template>

<style scoped>
.prestataires-page {
  padding: 0;
}

.page-header {
  background: linear-gradient(135deg, #fef3e2, #fed7aa);
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

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
}

.flex {
  display: flex;
}

.flex-column {
  flex-direction: column;
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

.mt-2 {
  margin-top: 0.5rem;
}

.mt-3 {
  margin-top: 0.75rem;
}

.mr-3 {
  margin-right: 0.75rem;
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

.text-600 {
  color: var(--surface-600);
}

.text-800 {
  color: var(--surface-800);
}

.font-bold {
  font-weight: 700;
}

.font-semibold {
  font-weight: 600;
}

.prestataire-details {
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
  flex-shrink: 0;
}

.text-900 {
  color: var(--surface-900);
}

.text-green-600 {
  color: #059669;
}

.surface-50 {
  background-color: var(--surface-50);
}
</style>