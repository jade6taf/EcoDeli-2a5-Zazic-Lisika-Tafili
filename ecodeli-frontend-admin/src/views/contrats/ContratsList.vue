<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useContratsAdminStore } from '@/stores/contrats'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import CreerContratDialog from '@/components/contrats/CreerContratDialog.vue'
import ModifierStatutDialog from '@/components/contrats/ModifierStatutDialog.vue'

const router = useRouter()
const contratsStore = useContratsAdminStore()
const toast = useToast()
const confirm = useConfirm()

const showContratDialog = ref(false)
const showCreerContratDialog = ref(false)
const showModifierStatutDialog = ref(false)
const contratSelectionne = ref(null)
const filtreStatut = ref(null)
const rechercheTexte = ref('')

const statutOptions = [
  { label: 'Demande envoyée', value: 'DEMANDE_ENVOYEE' },
  { label: 'En attente admin', value: 'EN_ATTENTE_ADMIN' },
  { label: 'Contrat créé', value: 'CONTRAT_CREE' },
  { label: 'Signé/Validé', value: 'SIGNE_VALIDE' },
  { label: 'Refusé', value: 'REFUSE' }
]

const contratsFiltrés = computed(() => {
  let contrats = contratsStore.contrats
  
  if (filtreStatut.value) {
    contrats = contrats.filter(contrat => contrat.statut === filtreStatut.value)
  }
  
  return contrats
})

const chargerContrats = async () => {
  await contratsStore.chargerContrats(filtreStatut.value)
  await contratsStore.chargerStatistiques()
}

const voirContrat = (contrat) => {
  contratSelectionne.value = contrat
  showContratDialog.value = true
}

const creerContratDialog = (contrat) => {
  contratSelectionne.value = contrat
  showContratDialog.value = false
  showCreerContratDialog.value = true
}

const modifierStatutDialog = (contrat) => {
  contratSelectionne.value = contrat
  showContratDialog.value = false
  showModifierStatutDialog.value = true
}

const confirmerSuppression = (contrat) => {
  confirm.require({
    message: `Êtes-vous sûr de vouloir supprimer le contrat de ${contrat.commercant.nomCommerce} ?`,
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    acceptLabel: 'Supprimer',
    rejectLabel: 'Annuler',
    accept: () => supprimerContrat(contrat.idContrat)
  })
}

const supprimerContrat = async (idContrat) => {
  const result = await contratsStore.supprimerContrat(idContrat)
  
  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Succès',
      detail: result.message,
      life: 3000
    })
    await chargerContrats()
  } else {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: result.error,
      life: 3000
    })
  }
}

const onContratCree = async () => {
  showCreerContratDialog.value = false
  await chargerContrats()
  toast.add({
    severity: 'success',
    summary: 'Succès',
    detail: 'Contrat créé avec succès',
    life: 3000
  })
}

const onStatutModifie = async () => {
  showModifierStatutDialog.value = false
  await chargerContrats()
  toast.add({
    severity: 'success',
    summary: 'Succès',
    detail: 'Statut mis à jour avec succès',
    life: 3000
  })
}

const getBadgeSeverity = (statut) => {
  switch (statut) {
    case 'DEMANDE_ENVOYEE':
      return 'info'
    case 'EN_ATTENTE_ADMIN':
      return 'warning'
    case 'CONTRAT_CREE':
      return 'warning'
    case 'SIGNE_VALIDE':
      return 'success'
    case 'REFUSE':
      return 'danger'
    default:
      return 'secondary'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  chargerContrats()
})
</script>

<template>
  <div class="contrats-list">
    <div class="flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="text-3xl font-bold m-0">Gestion des Contrats</h1>
        <p class="text-600 mt-1">Gérez les demandes et contrats des commerçants</p>
      </div>
      <div class="flex align-items-center gap-2">
        <Button
          label="Actualiser"
          icon="pi pi-refresh"
          :loading="contratsStore.loading"
          @click="chargerContrats"
        />
      </div>
    </div>

    <!-- Statistiques -->
    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <Card class="bg-blue-50 border-left-3 border-blue-500">
          <template #content>
            <div class="flex justify-content-between">
              <div>
                <div class="text-blue-600 font-medium">Total Contrats</div>
                <div class="text-2xl font-bold text-900 mt-1">
                  {{ contratsStore.statistiques.totalContrats || 0 }}
                </div>
              </div>
              <i class="pi pi-file text-2xl text-blue-500"></i>
            </div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="bg-orange-50 border-left-3 border-orange-500">
          <template #content>
            <div class="flex justify-content-between">
              <div>
                <div class="text-orange-600 font-medium">En Attente</div>
                <div class="text-2xl font-bold text-900 mt-1">
                  {{ contratsStore.statistiques.demandesEnvoyees || 0 }}
                </div>
              </div>
              <i class="pi pi-clock text-2xl text-orange-500"></i>
            </div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="bg-cyan-50 border-left-3 border-cyan-500">
          <template #content>
            <div class="flex justify-content-between">
              <div>
                <div class="text-cyan-600 font-medium">À Signer</div>
                <div class="text-2xl font-bold text-900 mt-1">
                  {{ contratsStore.statistiques.contratsCreés || 0 }}
                </div>
              </div>
              <i class="pi pi-pencil text-2xl text-cyan-500"></i>
            </div>
          </template>
        </Card>
      </div>
      <div class="col-12 md:col-3">
        <Card class="bg-green-50 border-left-3 border-green-500">
          <template #content>
            <div class="flex justify-content-between">
              <div>
                <div class="text-green-600 font-medium">Signés</div>
                <div class="text-2xl font-bold text-900 mt-1">
                  {{ contratsStore.statistiques.signesValides || 0 }}
                </div>
              </div>
              <i class="pi pi-check text-2xl text-green-500"></i>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Filtres -->
    <Card class="mb-4">
      <template #content>
        <div class="flex flex-wrap gap-3 align-items-center">
          <div class="flex align-items-center gap-2">
            <label for="statut-filter" class="font-medium">Statut :</label>
            <Dropdown
              id="statut-filter"
              v-model="filtreStatut"
              :options="statutOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="Tous les statuts"
              showClear
              @change="chargerContrats"
              class="w-12rem"
            />
          </div>
          <div class="flex align-items-center gap-2">
            <label for="search" class="font-medium">Recherche :</label>
            <InputText
              id="search"
              v-model="rechercheTexte"
              placeholder="Nom commerce, email..."
              class="w-12rem"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Table des contrats -->
    <Card>
      <template #content>
        <DataTable
          :value="contratsFiltrés"
          :loading="contratsStore.loading"
          paginator
          :rows="10"
          dataKey="idContrat"
          responsiveLayout="scroll"
          :globalFilterFields="['commercant.nomCommerce', 'commercant.email', 'commercant.nom', 'commercant.prenom']"
          :globalFilter="rechercheTexte"
          sortMode="multiple"
          removableSort
        >
          <template #empty>
            <div class="text-center py-4">
              <i class="pi pi-inbox text-4xl text-400 mb-2"></i>
              <p class="text-600">Aucun contrat trouvé</p>
            </div>
          </template>

          <Column field="commercant.nomCommerce" header="Commerce" sortable>
            <template #body="{ data }">
              <div>
                <div class="font-semibold">{{ data.commercant.nomCommerce }}</div>
                <div class="text-sm text-600">{{ data.commercant.nom }} {{ data.commercant.prenom }}</div>
              </div>
            </template>
          </Column>

          <Column field="commercant.email" header="Contact" sortable>
            <template #body="{ data }">
              <div>
                <div class="text-sm">{{ data.commercant.email }}</div>
                <div class="text-sm text-600">{{ data.commercant.telephone || 'N/A' }}</div>
              </div>
            </template>
          </Column>

          <Column field="statut" header="Statut" sortable>
            <template #body="{ data }">
              <Badge
                :value="data.statutLibelle"
                :severity="getBadgeSeverity(data.statut)"
                class="text-sm"
              />
            </template>
          </Column>

          <Column field="dateDemande" header="Date Demande" sortable>
            <template #body="{ data }">
              {{ formatDate(data.dateDemande) }}
            </template>
          </Column>

          <Column field="dateCreationContrat" header="Date Création" sortable>
            <template #body="{ data }">
              {{ data.dateCreationContrat ? formatDate(data.dateCreationContrat) : '-' }}
            </template>
          </Column>

          <Column field="dateSignature" header="Date Signature" sortable>
            <template #body="{ data }">
              {{ data.dateSignature ? formatDate(data.dateSignature) : '-' }}
            </template>
          </Column>

          <Column header="Actions" :exportable="false">
            <template #body="{ data }">
              <div class="flex gap-1">
                <Button
                  icon="pi pi-eye"
                  size="small"
                  outlined
                  @click="voirContrat(data)"
                  :title="'Voir détails'"
                />
                <Button
                  v-if="data.statut === 'DEMANDE_ENVOYEE'"
                  icon="pi pi-file-edit"
                  size="small"
                  outlined
                  severity="info"
                  @click="creerContratDialog(data)"
                  :title="'Créer contrat'"
                />
                <Button
                  icon="pi pi-pencil"
                  size="small"
                  outlined
                  severity="warning"
                  @click="modifierStatutDialog(data)"
                  :title="'Modifier statut'"
                />
                <Button
                  icon="pi pi-trash"
                  size="small"
                  outlined
                  severity="danger"
                  @click="confirmerSuppression(data)"
                  :title="'Supprimer'"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Dialog Voir Contrat -->
    <Dialog
      v-model:visible="showContratDialog"
      header="Détails du Contrat"
      :style="{ width: '80vw', maxWidth: '900px' }"
      modal
    >
      <div v-if="contratSelectionne" class="py-2">
        <div class="grid mb-4">
          <div class="col-12 md:col-6">
            <h4 class="mt-0">Informations Commerçant</h4>
            <div class="field">
              <label class="font-semibold">Commerce :</label>
              <p class="mt-1">{{ contratSelectionne.commercant.nomCommerce }}</p>
            </div>
            <div class="field">
              <label class="font-semibold">Nom complet :</label>
              <p class="mt-1">{{ contratSelectionne.commercant.prenom }} {{ contratSelectionne.commercant.nom }}</p>
            </div>
            <div class="field">
              <label class="font-semibold">Email :</label>
              <p class="mt-1">{{ contratSelectionne.commercant.email }}</p>
            </div>
            <div class="field">
              <label class="font-semibold">SIRET :</label>
              <p class="mt-1">{{ contratSelectionne.commercant.siret || 'Non renseigné' }}</p>
            </div>
          </div>
          <div class="col-12 md:col-6">
            <h4 class="mt-0">Informations Contrat</h4>
            <div class="field">
              <label class="font-semibold">Statut :</label>
              <Badge
                :value="contratSelectionne.statutLibelle"
                :severity="getBadgeSeverity(contratSelectionne.statut)"
                class="ml-2"
              />
            </div>
            <div class="field">
              <label class="font-semibold">Date de demande :</label>
              <p class="mt-1">{{ formatDate(contratSelectionne.dateDemande) }}</p>
            </div>
            <div v-if="contratSelectionne.dateCreationContrat" class="field">
              <label class="font-semibold">Date de création :</label>
              <p class="mt-1">{{ formatDate(contratSelectionne.dateCreationContrat) }}</p>
            </div>
            <div v-if="contratSelectionne.dateSignature" class="field">
              <label class="font-semibold">Date de signature :</label>
              <p class="mt-1">{{ formatDate(contratSelectionne.dateSignature) }}</p>
            </div>
          </div>
        </div>

        <div v-if="contratSelectionne.commentaireAdmin" class="mb-4">
          <h4>Commentaire Admin</h4>
          <p class="p-3 bg-gray-50 border-round">{{ contratSelectionne.commentaireAdmin }}</p>
        </div>

        <div v-if="contratSelectionne.contenuContrat" class="mb-4">
          <h4>Contenu du Contrat</h4>
          <div
            class="border-1 border-300 border-round p-3 bg-gray-50"
            style="max-height: 300px; overflow-y: auto;"
            v-html="contratSelectionne.contenuContrat"
          ></div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-content-end gap-2">
          <Button
            label="Fermer"
            icon="pi pi-times"
            outlined
            @click="showContratDialog = false"
          />
          <Button
            v-if="contratSelectionne?.statut === 'DEMANDE_ENVOYEE'"
            label="Créer Contrat"
            icon="pi pi-file-edit"
            @click="creerContratDialog(contratSelectionne)"
          />
        </div>
      </template>
    </Dialog>

    <CreerContratDialog
      v-model:visible="showCreerContratDialog"
      :contrat="contratSelectionne"
      @contrat-cree="onContratCree"
    />

    <ModifierStatutDialog
      v-model:visible="showModifierStatutDialog"
      :contrat="contratSelectionne"
      @statut-modifie="onStatutModifie"
    />

    <ConfirmDialog />
    
    <Toast />
  </div>
</template>

<style scoped>
.contrats-list {
  padding: 1rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1rem;
}

.col-12 { grid-column: span 12; }
.col-3 { grid-column: span 3; }
.col-6 { grid-column: span 6; }

@media (max-width: 768px) {
  .grid { grid-template-columns: 1fr; }
  .col-12, .col-3, .col-6 { grid-column: span 1; }
}

.field {
  margin-bottom: 1rem;
}

.field label {
  display: block;
  margin-bottom: 0.25rem;
}

.field p {
  margin: 0;
  color: var(--text-color);
}
</style>