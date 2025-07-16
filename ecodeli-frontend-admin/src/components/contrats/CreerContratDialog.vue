<template>
  <Dialog
    :visible="visible"
    @update:visible="$emit('update:visible', $event)"
    header="Créer un Contrat"
    :style="{ width: '90vw', maxWidth: '800px' }"
    modal
  >
    <div v-if="contrat" class="py-2">
      <div class="mb-4 p-3 bg-blue-50 border-round">
        <h4 class="mt-0 mb-2 text-blue-800">Commerçant</h4>
        <div class="grid">
          <div class="col-12 md:col-6">
            <strong>Commerce :</strong> {{ contrat.commercant.nomCommerce }}
          </div>
          <div class="col-12 md:col-6">
            <strong>Contact :</strong> {{ contrat.commercant.prenom }} {{ contrat.commercant.nom }}
          </div>
          <div class="col-12 md:col-6">
            <strong>Email :</strong> {{ contrat.commercant.email }}
          </div>
          <div class="col-12 md:col-6">
            <strong>SIRET :</strong> {{ contrat.commercant.siret || 'Non renseigné' }}
          </div>
        </div>
      </div>

      <div class="field mb-4">
        <h4 class="text-lg font-semibold mb-3">Aperçu du contrat</h4>
        <div 
          class="border-1 border-300 border-round p-3 bg-gray-50"
          style="max-height: 400px; overflow-y: auto;"
          v-html="contenuContratGenere"
        ></div>
      </div>

      <div class="field mb-4">
        <label for="commentaire" class="block font-semibold mb-2">Commentaire pour le commerçant (optionnel)</label>
        <textarea
          id="commentaire"
          v-model="formData.commentaire"
          rows="3"
          class="w-full p-3 border-1 border-300 border-round"
          placeholder="Message personnalisé pour le commerçant..."
        ></textarea>
      </div>

      <div class="bg-green-50 p-3 border-round border-left-3 border-green-500 mb-4">
        <div class="flex align-items-center">
          <i class="pi pi-info-circle text-green-600 mr-2"></i>
          <div>
            <p class="m-0 text-green-800 font-semibold">Contrat automatique</p>
            <p class="m-0 text-green-700 text-sm">Un contrat standard sera généré automatiquement avec les informations du commerçant</p>
          </div>
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
          label="Créer le Contrat"
          icon="pi pi-check"
          :loading="loading"
          @click="creerContrat"
        />
      </div>
    </template>

    <Toast />
  </Dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useContratsAdminStore } from '@/stores/contrats'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'

const props = defineProps({
  visible: Boolean,
  contrat: Object
})

const emit = defineEmits(['update:visible', 'contrat-cree'])

const contratsStore = useContratsAdminStore()
const authStore = useAuthStore()
const toast = useToast()

const loading = ref(false)

const formData = reactive({
  commentaire: ''
})

const contenuContratGenere = computed(() => {
  if (!props.contrat) return ''
  
  const commercant = props.contrat.commercant
  const dateAujourdhui = new Date().toLocaleDateString('fr-FR')
  
  return `
    <h1>CONTRAT DE PARTENARIAT ECODELI</h1>
    
    <h2>INFORMATIONS DU PARTENAIRE</h2>
    <p>Nom du commerce : ${commercant.nomCommerce}</p>
    <p>Representant legal : ${commercant.prenom} ${commercant.nom}</p>
    <p>Email : ${commercant.email}</p>
    <p>Telephone : ${commercant.telephone || 'Non renseigne'}</p>
    <p>SIRET : ${commercant.siret || 'Non renseigne'}</p>
    
    <h2>ARTICLE 1 - OBJET DU CONTRAT</h2>
    <p>Le present contrat definit les conditions de partenariat entre ${commercant.nomCommerce} et EcoDeli pour les services de livraison eco-responsable.</p>
    
    <h2>ARTICLE 2 - SERVICES PROPOSES</h2>
    <p>EcoDeli fournit :</p>
    <p>- Plateforme de gestion des commandes</p>
    <p>- Reseau de livreurs eco-responsables</p>
    <p>- Suivi en temps reel</p>
    <p>- Support technique</p>
    
    <h2>ARTICLE 3 - ENGAGEMENTS DU PARTENAIRE</h2>
    <p>Le partenaire s'engage a :</p>
    <p>- Respecter les valeurs eco-responsables</p>
    <p>- Fournir des informations exactes</p>
    <p>- Preparer les commandes dans les delais</p>
    <p>- Respecter les standards qualite</p>
    
    <h2>SIGNATURES</h2>
    <p>Le Partenaire : ${commercant.prenom} ${commercant.nom}</p>
    <p>Commerce : ${commercant.nomCommerce}</p>
    <p>Date : ${dateAujourdhui}</p>
    
    <p>EcoDeli - Representant legal</p>
    <p>Date : ${dateAujourdhui}</p>
    
    <p>Contact EcoDeli : ecodeli.nepasrepondre@gmail.com</p>
  `
})

const creerContrat = async () => {
  loading.value = true
  
  try {
    const result = await contratsStore.creerContrat(
      props.contrat.idContrat,
      contenuContratGenere.value,
      authStore.user.id,
      formData.commentaire
    )
    
    if (result.success) {
      emit('contrat-cree')
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
  formData.commentaire = ''
}

watch(() => props.visible, (newVal) => {
  if (!newVal) {
    resetFormulaire()
  }
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

@media (max-width: 768px) {
  .grid { grid-template-columns: 1fr; }
  .col-12, .col-6 { grid-column: span 1; }
}

.field {
  margin-bottom: 1rem;
}
</style>