<template>
  <Dialog
    :visible="visible"
    @update:visible="$emit('update:visible', $event)"
    header="Modifier le Statut du Contrat"
    :style="{ width: '600px' }"
    modal
  >
    <div v-if="contrat" class="py-2">
      <div class="mb-4 p-3 bg-blue-50 border-round">
        <div class="flex justify-content-between align-items-center">
          <div>
            <h4 class="mt-0 mb-1 text-blue-800">{{ contrat.commercant.nomCommerce }}</h4>
            <p class="m-0 text-600">{{ contrat.commercant.prenom }} {{ contrat.commercant.nom }}</p>
          </div>
          <Badge
            :value="contrat.statutLibelle"
            :severity="getBadgeSeverity(contrat.statut)"
          />
        </div>
      </div>

      <div class="field mb-4">
        <label for="nouveau-statut" class="block font-semibold mb-2">
          Nouveau statut <span class="text-red-500">*</span>
        </label>
        <Dropdown
          id="nouveau-statut"
          v-model="formData.nouveauStatut"
          :options="statutsDisponibles"
          optionLabel="label"
          optionValue="value"
          placeholder="Sélectionner un statut"
          class="w-full"
          :class="{ 'p-invalid': erreurs.nouveauStatut }"
        />
        <small v-if="erreurs.nouveauStatut" class="p-error">{{ erreurs.nouveauStatut }}</small>
      </div>

      <div class="field mb-4">
        <label for="commentaire" class="block font-semibold mb-2">
          Commentaire 
          <span v-if="formData.nouveauStatut === 'REFUSE'" class="text-red-500">*</span>
          <span v-else class="text-600">(optionnel)</span>
        </label>
        <textarea
          id="commentaire"
          v-model="formData.commentaire"
          rows="4"
          class="w-full p-3 border-1 border-300 border-round"
          placeholder="Commentaire pour expliquer le changement de statut..."
          :class="{ 'p-invalid': erreurs.commentaire }"
        ></textarea>
        <small v-if="erreurs.commentaire" class="p-error">{{ erreurs.commentaire }}</small>
        <small v-else-if="formData.nouveauStatut === 'REFUSE'" class="text-600">
          Un commentaire est obligatoire en cas de refus pour expliquer les raisons.
        </small>
      </div>

      <div v-if="getAvertissementStatut(formData.nouveauStatut)" class="mb-4">
        <div
          :class="[
            'p-3 border-round border-left-3',
            {
              'bg-red-50 border-red-500 text-red-800': getAvertissementStatut(formData.nouveauStatut).severity === 'error',
              'bg-orange-50 border-orange-500 text-orange-800': getAvertissementStatut(formData.nouveauStatut).severity === 'warn',
              'bg-blue-50 border-blue-500 text-blue-800': getAvertissementStatut(formData.nouveauStatut).severity === 'info'
            }
          ]"
        >
          <i :class="getAvertissementStatut(formData.nouveauStatut).icon" class="mr-2"></i>
          {{ getAvertissementStatut(formData.nouveauStatut).message }}
        </div>
      </div>

      <div v-if="contrat.commentaireAdmin" class="mb-4">
        <h5 class="mb-2">Commentaire actuel :</h5>
        <div class="p-3 bg-gray-50 border-round border-left-3 border-gray-400">
          <p class="m-0 text-600">{{ contrat.commentaireAdmin }}</p>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-content-end gap-2">
        <Button
          label="Annuler"
          icon="pi pi-times"
          outlined
          @click="$emit('update:visible', false)"
        />
        <Button
          label="Modifier le Statut"
          icon="pi pi-check"
          :loading="loading"
          :severity="getButtonSeverity(formData.nouveauStatut)"
          @click="modifierStatut"
        />
      </div>
    </template>

    <Toast />
  </Dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useContratsAdminStore } from '@/stores/contrats'
import { useToast } from 'primevue/usetoast'

const props = defineProps({
  visible: Boolean,
  contrat: Object
})

const emit = defineEmits(['update:visible', 'statut-modifie'])

const contratsStore = useContratsAdminStore()
const toast = useToast()

const loading = ref(false)

const formData = reactive({
  nouveauStatut: '',
  commentaire: ''
})

const erreurs = reactive({
  nouveauStatut: '',
  commentaire: ''
})

const statutsDisponibles = computed(() => {
  if (!props.contrat) return []
  
  const tousLesStatuts = [
    { label: 'Demande envoyée', value: 'DEMANDE_ENVOYEE' },
    { label: 'En attente admin', value: 'EN_ATTENTE_ADMIN' },
    { label: 'Contrat créé', value: 'CONTRAT_CREE' },
    { label: 'Signé/Validé', value: 'SIGNE_VALIDE' },
    { label: 'Refusé', value: 'REFUSE' }
  ]
  
  const statutActuel = props.contrat.statut
  
  switch (statutActuel) {
    case 'DEMANDE_ENVOYEE':
      return tousLesStatuts.filter(s => 
        ['EN_ATTENTE_ADMIN', 'CONTRAT_CREE', 'REFUSE'].includes(s.value)
      )
    case 'EN_ATTENTE_ADMIN':
      return tousLesStatuts.filter(s => 
        ['DEMANDE_ENVOYEE', 'CONTRAT_CREE', 'REFUSE'].includes(s.value)
      )
    case 'CONTRAT_CREE':
      return tousLesStatuts.filter(s => 
        ['EN_ATTENTE_ADMIN', 'SIGNE_VALIDE', 'REFUSE'].includes(s.value)
      )
    case 'SIGNE_VALIDE':
      return tousLesStatuts.filter(s => 
        ['CONTRAT_CREE', 'REFUSE'].includes(s.value)
      )
    case 'REFUSE':
      return tousLesStatuts.filter(s => 
        ['DEMANDE_ENVOYEE', 'EN_ATTENTE_ADMIN', 'CONTRAT_CREE'].includes(s.value)
      )
    default:
      return tousLesStatuts
  }
})

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

const getButtonSeverity = (statut) => {
  switch (statut) {
    case 'REFUSE':
      return 'danger'
    case 'SIGNE_VALIDE':
      return 'success'
    default:
      return 'primary'
  }
}

const getAvertissementStatut = (statut) => {
  switch (statut) {
    case 'REFUSE':
      return {
        severity: 'error',
        icon: 'pi pi-exclamation-triangle',
        message: 'Le refus du contrat sera définitif. Le commerçant sera notifié par email.'
      }
    case 'SIGNE_VALIDE':
      return {
        severity: 'warn',
        icon: 'pi pi-info-circle',
        message: 'Attention : ce statut ne peut être utilisé que si le commerçant a effectivement signé le contrat.'
      }
    case 'CONTRAT_CREE':
      return {
        severity: 'info',
        icon: 'pi pi-info-circle',
        message: 'Le commerçant recevra une notification pour signer son contrat.'
      }
    default:
      return null
  }
}

const validerFormulaire = () => {
  let valide = true
  
  Object.keys(erreurs).forEach(key => erreurs[key] = '')
  
  if (!formData.nouveauStatut) {
    erreurs.nouveauStatut = 'Veuillez sélectionner un statut'
    valide = false
  }
  
  if (formData.nouveauStatut === 'REFUSE' && !formData.commentaire.trim()) {
    erreurs.commentaire = 'Un commentaire est obligatoire en cas de refus'
    valide = false
  }
  
  return valide
}

const modifierStatut = async () => {
  if (!validerFormulaire()) return
  
  loading.value = true
  
  try {
    const result = await contratsStore.mettreAJourStatutContrat(
      props.contrat.idContrat,
      formData.nouveauStatut,
      formData.commentaire
    )
    
    if (result.success) {
      emit('statut-modifie')
      resetFormulaire()
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error,
        life: 3000
      })
    }
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Une erreur inattendue s\'est produite',
      life: 3000
    })
  } finally {
    loading.value = false
  }
}

const resetFormulaire = () => {
  formData.nouveauStatut = ''
  formData.commentaire = ''
  Object.keys(erreurs).forEach(key => erreurs[key] = '')
}

watch(() => props.visible, (newVal) => {
  if (newVal && props.contrat) {
    formData.commentaire = props.contrat.commentaireAdmin || ''
  } else {
    resetFormulaire()
  }
})

watch(() => props.contrat, (newContrat) => {
  if (newContrat && props.visible) {
    formData.commentaire = newContrat.commentaireAdmin || ''
  }
})
</script>

<style scoped>
.field {
  margin-bottom: 1rem;
}
</style>