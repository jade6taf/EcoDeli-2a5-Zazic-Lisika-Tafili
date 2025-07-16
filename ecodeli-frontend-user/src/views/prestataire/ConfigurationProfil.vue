<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePrestataireStore } from '@/stores/prestataire'

const router = useRouter()
const authStore = useAuthStore()
const prestataireStore = usePrestataireStore()

const loading = ref(false)
const justificatifs = ref([])
const uploadFiles = ref([])

const menuItems = [
  {
    label: 'Dashboard',
    icon: 'pi pi-home',
    command: () => router.push('/prestataire')
  },
  {
    label: 'Demandes disponibles',
    icon: 'pi pi-inbox',
    command: () => router.push('/prestataire/demandes')
  },
  {
    label: 'Mon Profil',
    icon: 'pi pi-user',
    command: () => router.push('/prestataire/profil')
  },
  {
    label: 'Mes candidatures',
    icon: 'pi pi-send',
    command: () => router.push('/prestataire/candidatures')
  }
]

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const statutSeverity = computed(() => {
  if (!prestataireStore.statutValidation) return 'secondary'
  
  switch (prestataireStore.statutValidation.statutGlobal) {
    case 'VALIDE': return 'success'
    case 'REJETE': return 'danger'
    case 'SUSPENDU': return 'secondary'
    default: return 'warning'
  }
})

const statutLabel = computed(() => {
  if (!prestataireStore.statutValidation) return 'En attente'
  
  switch (prestataireStore.statutValidation.statutGlobal) {
    case 'VALIDE': return 'Validé'
    case 'REJETE': return 'Rejeté'
    case 'SUSPENDU': return 'Suspendu'
    default: return 'En attente'
  }
})

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

const getJustificatifStatutSeverity = (statut) => {
  if (statut === true) return 'success'
  if (statut === false) return 'danger'
  return 'warning'
}

const getJustificatifStatutLabel = (statut) => {
  if (statut === true) return 'Validé'
  if (statut === false) return 'Rejeté'
  return 'En attente'
}

const loadData = async () => {
  loading.value = true
  
  try {
    await prestataireStore.getStatutValidation()
    
    const result = await prestataireStore.getJustificatifs()
    if (result.success) {
      justificatifs.value = result.data
    }
  } catch (error) {
    console.error('Erreur lors du chargement:', error)
  } finally {
    loading.value = false
  }
}

const fileInputRef = ref(null)
const uploading = ref(false)

const ouvrirExplorateur = () => {
  fileInputRef.value?.click()
}

const onFileSelect = async (event) => {
  const files = event.target.files
  if (!files || files.length === 0) return

  uploading.value = true
  
  try {
    for (const file of files) {
      if (!validerFichier(file)) {
        continue
      }
      
      const result = await prestataireStore.uploadJustificatif(file, file.name)
      if (result.success) {
        console.log('Upload réussi:', file.name)
      } else {
        console.error('Échec upload:', file.name)
      }
    }
    
    event.target.value = ''
    
    await loadData()
  } catch (error) {
    console.error('Erreur upload:', error)
  } finally {
    uploading.value = false
  }
}

const validerFichier = (file) => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    alert('Le fichier est trop volumineux (max 10MB)')
    return false
  }
  
  const allowedTypes = ['pdf', 'jpg', 'jpeg', 'png']
  const extension = file.name.split('.').pop().toLowerCase()
  if (!allowedTypes.includes(extension)) {
    alert('Type de fichier non autorisé. Utilisez PDF, JPG ou PNG')
    return false
  }
  
  return true
}

const supprimerJustificatif = async (justificatifId) => {
  try {
    const result = await prestataireStore.supprimerJustificatif(justificatifId)
    if (result.success) {
      await loadData()
    }
  } catch (error) {
    console.error('Erreur suppression:', error)
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="ecodeli-layout">
    <header class="ecodeli-header">
      <Menubar :model="menuItems" class="border-0">
        <template #start>
          <div class="flex align-items-center">
            <i class="pi pi-wrench text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-semibold ecodeli-title">EcoDeli - Mon Profil</span>
          </div>
        </template>

        <template #end>
          <div class="flex align-items-center gap-2">
            <span class="text-sm text-600">{{ authStore.userName }}</span>
            <Avatar :label="userInitials" class="bg-primary text-white" shape="circle" />
            <Button icon="pi pi-sign-out" text rounded @click="handleLogout" />
          </div>
        </template>
      </Menubar>
    </header>

    <main class="ecodeli-content">
      <div class="grid">
        
        <!-- Informations personnelles -->
        <div class="col-12">
          <Card class="mb-4">
            <template #title>
              <div class="flex align-items-center">
                <i class="pi pi-user mr-2"></i>
                Informations Personnelles
              </div>
            </template>
            <template #content>
              <div class="grid">
                <div class="col-12 md:col-6">
                  <div class="mb-3">
                    <label class="font-semibold">Nom complet</label>
                    <div class="mt-1">{{ authStore.user?.prenom }} {{ authStore.user?.nom }}</div>
                  </div>
                  <div class="mb-3">
                    <label class="font-semibold">Email</label>
                    <div class="mt-1">{{ authStore.user?.email }}</div>
                  </div>
                </div>
                <div class="col-12 md:col-6">
                  <div class="mb-3">
                    <label class="font-semibold">Domaine d'expertise</label>
                    <div class="mt-1">
                      {{ prestataireStore.statutValidation?.domaineExpertise ? 
                         getCategorieLabel(prestataireStore.statutValidation.domaineExpertise) : 
                         'Non défini' }}
                    </div>
                  </div>
                  <div class="mb-3">
                    <label class="font-semibold">Date d'inscription</label>
                    <div class="mt-1">{{ new Date().toLocaleDateString('fr-FR') }}</div>
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Statut de validation -->
        <div class="col-12">
          <Card class="mb-4">
            <template #title>
              <div class="flex align-items-center">
                <i class="pi pi-shield mr-2"></i>
                Statut de Validation
              </div>
            </template>
            <template #content>
              <div class="flex justify-content-between align-items-center">
                <div>
                  <Tag :value="statutLabel" :severity="statutSeverity" class="text-lg" />
                  <div class="mt-2 text-600">
                    <div v-if="prestataireStore.statutValidation?.peutCandidater">
                      ✅ Votre profil est validé, vous pouvez candidater aux demandes
                    </div>
                    <div v-else>
                      ⏳ Votre profil est en cours de validation par notre équipe
                    </div>
                  </div>
                </div>
                <div v-if="prestataireStore.statutValidation?.peutCandidater" class="text-right">
                  <div class="text-lg font-semibold text-green-600">
                    Tarif validé : {{ prestataireStore.statutValidation?.tarifHoraire || 'À définir' }}€/h
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Upload justificatifs -->
        <div class="col-12">
          <Card>
            <template #title>
              <div class="flex align-items-center">
                <i class="pi pi-file mr-2"></i>
                Justificatifs
              </div>
            </template>
            <template #content>
              <div class="mb-4">
                <p class="text-600 mb-3">
                  Uploadez vos diplômes, certificats et preuves d'expérience pour validation par notre équipe.
                </p>

                <!-- Input file caché -->
                <input
                  ref="fileInputRef"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  multiple
                  style="display: none"
                  @change="onFileSelect"
                />
                
                <!-- Interface upload simple -->
                <div class="upload-area p-4 border-2 border-dashed border-round text-center">
                  <i class="pi pi-cloud-upload text-4xl text-500 mb-3"></i>
                  <p class="text-600 mb-3">
                    Ajoutez vos diplômes, certificats et preuves d'expérience
                  </p>
                  <Button
                    icon="pi pi-plus"
                    label="Choisir un fichier"
                    @click="ouvrirExplorateur"
                    :loading="uploading"
                    :disabled="uploading"
                  />
                  <div class="mt-2">
                    <small class="text-500">Formats acceptés : PDF, JPG, PNG (max 10MB)</small>
                  </div>
                </div>
              </div>

              <!-- Liste des justificatifs -->
              <div v-if="justificatifs.length > 0">
                <h4>Fichiers uploadés</h4>
                <div class="space-y-3">
                  <div 
                    v-for="justificatif in justificatifs" 
                    :key="justificatif.idJustificatif"
                    class="flex justify-content-between align-items-center p-3 border-round surface-100"
                  >
                    <div class="flex align-items-center">
                      <i class="pi pi-file-pdf mr-2 text-xl text-red-500"></i>
                      <div>
                        <div class="font-semibold">{{ justificatif.typeJustificatif }}</div>
                        <small class="text-500">{{ justificatif.commentaire }}</small>
                      </div>
                    </div>
                    <div class="flex align-items-center gap-2">
                      <Tag 
                        :value="getJustificatifStatutLabel(justificatif.validationParAd)"
                        :severity="getJustificatifStatutSeverity(justificatif.validationParAd)"
                      />
                      <Button 
                        icon="pi pi-trash" 
                        severity="danger" 
                        size="small"
                        text
                        @click="supprimerJustificatif(justificatif.idJustificatif)"
                      />
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center text-600 p-4">
                <i class="pi pi-inbox text-4xl mb-3"></i>
                <p>Aucun justificatif uploadé</p>
                <small>Commencez par uploader vos documents pour validation</small>
              </div>
            </template>
          </Card>
        </div>

      </div>
    </main>

    <Toast />
  </div>
</template>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(12, 1fr); gap: 1rem; }
.col-12 { grid-column: span 12; }
.bg-primary { background: var(--ecodeli-green) !important; }
.space-y-3 > * + * { margin-top: 0.75rem; }

.upload-area {
  border-color: #d1d5db;
  background-color: #f9fafb;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #9ca3af;
  background-color: #f3f4f6;
}

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
}
</style>