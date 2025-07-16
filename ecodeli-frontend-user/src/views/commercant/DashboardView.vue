<template>
  <div class="ecodeli-layout">
    <header class="ecodeli-header">
      <Menubar :model="menuItems" class="border-0">
        <template #start>
          <div class="flex align-items-center">
            <i class="pi pi-shop text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-semibold ecodeli-title">EcoDeli - Commerçant</span>
          </div>
        </template>

        <template #end>
          <div class="flex align-items-center gap-2">
            <Badge 
              :value="statutContratBadge.label" 
              :severity="statutContratBadge.severity"
              class="mr-2"
            />
            <span class="text-sm text-600">{{ authStore.userName }}</span>
            <Avatar :label="userInitials" class="bg-primary text-white" shape="circle" />
            <Button icon="pi pi-sign-out" text rounded @click="handleLogout" />
          </div>
        </template>
      </Menubar>
    </header>

    <main class="ecodeli-content">
      <div class="grid">
        <!-- Section Bienvenue -->
        <div class="col-12">
          <Card>
            <template #content>
              <div class="flex justify-content-between align-items-center">
                <div>
                  <h1 class="text-3xl font-bold ecodeli-title m-0">
                    Bienvenue {{ authStore.user?.prenom }} !
                  </h1>
                  <p class="text-600 mt-2">Gérez votre commerce avec EcoDeli</p>
                </div>
                <div class="flex align-items-center gap-3">
                  <i class="pi pi-shop text-6xl text-green-500"></i>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Statistiques des annonces -->
        <div class="col-12 md:col-3">
          <Card class="stats-card">
            <template #content>
              <div class="flex align-items-center justify-content-between">
                <div>
                  <div class="text-2xl font-bold text-primary">{{ totalAnnonces }}</div>
                  <div class="text-sm text-600">Total annonces</div>
                </div>
                <div class="stats-icon bg-blue-100">
                  <i class="pi pi-file text-blue-500"></i>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-3">
          <Card class="stats-card">
            <template #content>
              <div class="flex align-items-center justify-content-between">
                <div>
                  <div class="text-2xl font-bold text-green-500">{{ annoncesActives }}</div>
                  <div class="text-sm text-600">Annonces actives</div>
                </div>
                <div class="stats-icon bg-green-100">
                  <i class="pi pi-check-circle text-green-500"></i>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-3">
          <Card class="stats-card">
            <template #content>
              <div class="flex align-items-center justify-content-between">
                <div>
                  <div class="text-2xl font-bold text-orange-500">{{ annoncesEnAttente }}</div>
                  <div class="text-sm text-600">En attente</div>
                </div>
                <div class="stats-icon bg-orange-100">
                  <i class="pi pi-clock text-orange-500"></i>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <div class="col-12 md:col-3">
          <Card class="stats-card">
            <template #content>
              <div class="flex align-items-center justify-content-between">
                <div>
                  <div class="text-2xl font-bold text-purple-500">{{ categoriesUtilisees }}</div>
                  <div class="text-sm text-600">Catégories utilisées</div>
                </div>
                <div class="stats-icon bg-purple-100">
                  <i class="pi pi-tags text-purple-500"></i>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Section Contrat -->
        <div class="col-12 md:col-4">
          <Card>
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-file text-green-500"></i>
                <span>Mon Contrat</span>
              </div>
            </template>
            <template #content>
              <div v-if="contratsStore.loading" class="text-center py-4">
                <ProgressSpinner size="50px" strokeWidth="8" />
                <p class="mt-2 text-600">Chargement...</p>
              </div>

              <div v-else-if="!contratsStore.hasContrat" class="text-center py-4">
                <i class="pi pi-file-o text-4xl text-400 mb-3"></i>
                <p class="text-600 mb-4">Aucun contrat</p>
                <Button 
                  label="Faire ma demande" 
                  icon="pi pi-send"
                  size="small"
                  :loading="loadingContrat"
                  @click="demanderContrat"
                />
              </div>

              <div v-else class="py-2">
                <div class="flex justify-content-between align-items-center mb-3">
                  <span class="font-semibold">Statut</span>
                  <Badge 
                    :value="contratsStore.statutLibelle" 
                    :severity="statutContratBadge.severity"
                    class="text-sm"
                  />
                </div>

                <div class="mb-3">
                  <div class="text-sm text-600 mb-1">Date de demande</div>
                  <div class="text-sm">
                    {{ new Date(contratsStore.contrat?.dateDemande).toLocaleDateString('fr-FR') }}
                  </div>
                </div>

                <div class="flex gap-2">
                  <Button 
                    label="Consulter" 
                    icon="pi pi-eye"
                    size="small"
                    outlined
                    @click="showContratDialog = true"
                  />
                  <Button 
                    v-if="contratsStore.peutSignerContrat"
                    label="Signer" 
                    icon="pi pi-check"
                    size="small"
                    :loading="loadingContrat"
                    @click="signerContrat"
                  />
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Actions principales -->
        <div class="col-12 md:col-8">
          <Card>
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-plus text-green-500"></i>
                <span>Créer une nouvelle annonce</span>
              </div>
            </template>
            <template #content>
              <div class="announcement-categories">
                <div class="category-grid">
                  <div 
                    v-for="category in categories" 
                    :key="category.code"
                    class="category-card"
                    @click="creerAnnonce(category.code)"
                  >
                    <div class="category-icon">
                      <i :class="getCategoryIcon(category.code)"></i>
                    </div>
                    <div class="category-content">
                      <h4>{{ category.libelle }}</h4>
                      <p>{{ category.description }}</p>
                    </div>
                    <div class="category-stats">
                      <span class="stats-badge">{{ getAnnoncesByCategory(category.code) }} annonces</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Mes annonces récentes -->
        <div class="col-12">
          <Card>
            <template #title>
              <div class="flex align-items-center justify-content-between">
                <div class="flex align-items-center gap-2">
                  <i class="pi pi-list text-blue-500"></i>
                  <span>Mes annonces récentes</span>
                </div>
                <Button 
                  label="Voir toutes" 
                  icon="pi pi-arrow-right"
                  text
                  @click="router.push('/commercant/annonces')"
                />
              </div>
            </template>
            <template #content>
              <div v-if="annonceStore.loading" class="text-center py-4">
                <ProgressSpinner size="50px" strokeWidth="8" />
                <p class="mt-2 text-600">Chargement des annonces...</p>
              </div>

              <div v-else-if="mesAnnonces.length === 0" class="text-center py-4">
                <i class="pi pi-inbox text-4xl text-400 mb-3"></i>
                <p class="text-600 mb-4">Vous n'avez encore aucune annonce</p>
                <Button 
                  label="Créer ma première annonce" 
                  icon="pi pi-plus"
                  @click="showCategorySelection = true"
                />
              </div>

              <div v-else class="annonces-list">
                <div 
                  v-for="annonce in mesAnnonces.slice(0, 5)" 
                  :key="annonce.idAnnonceCommercant"
                  class="annonce-item"
                >
                  <div class="annonce-content">
                    <div class="annonce-header">
                      <div class="annonce-category">
                        <i :class="getCategoryIcon(annonce.categorie)"></i>
                        <span>{{ getCategoryLabel(annonce.categorie) }}</span>
                      </div>
                      <Badge 
                        :value="getStatutLabel(annonce.statut)" 
                        :severity="getStatutSeverity(annonce.statut)"
                        class="text-xs"
                      />
                    </div>
                    
                    <h4 class="annonce-title">{{ annonce.titre }}</h4>
                    <p class="annonce-description">{{ annonce.description }}</p>
                    
                    <div class="annonce-footer">
                      <div class="annonce-meta">
                        <span class="text-sm text-600">
                          {{ new Date(annonce.dateCreation).toLocaleDateString('fr-FR') }}
                        </span>
                        <span v-if="annonce.prixEstime" class="text-sm font-semibold text-green-600">
                          {{ annonce.prixEstime }}€
                        </span>
                      </div>
                      <div class="annonce-actions">
                        <Button 
                          icon="pi pi-eye" 
                          size="small" 
                          text 
                          @click="voirAnnonce(annonce)"
                        />
                        <Button 
                          icon="pi pi-pencil" 
                          size="small" 
                          text
                          @click="modifierAnnonce(annonce)"
                        />
                        <Button 
                          icon="pi pi-trash" 
                          size="small" 
                          text 
                          severity="danger"
                          @click="supprimerAnnonce(annonce)"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Actions rapides -->
        <div class="col-12 md:col-6">
          <Card>
            <template #title>
              <div class="flex align-items-center gap-2">
                <i class="pi pi-bolt text-orange-500"></i>
                <span>Actions rapides</span>
              </div>
            </template>
            <template #content>
              <div class="flex flex-column gap-2">
                <Button 
                  label="Créer une annonce" 
                  icon="pi pi-plus"
                  class="w-full justify-content-start"
                  @click="showCategorySelection = true"
                />
                <Button 
                  label="Mes annonces" 
                  icon="pi pi-list"
                  outlined
                  class="w-full justify-content-start"
                  @click="router.push('/commercant/annonces')"
                />
                <Button 
                  label="Mon profil" 
                  icon="pi pi-user"
                  outlined
                  class="w-full justify-content-start"
                  @click="router.push('/profile')"
                />
                <Button 
                  label="Support" 
                  icon="pi pi-question-circle"
                  outlined
                  class="w-full justify-content-start"
                />
              </div>
            </template>
          </Card>
        </div>
      </div>
    </main>

    <!-- Dialog Création d'annonce -->
    <Dialog 
      v-model:visible="showCategorySelection" 
      header="Créer une nouvelle annonce"
      :style="{ width: '90vw', maxWidth: '1200px' }"
      modal
    >
      <CategorySelection 
        @category-selected="handleCategorySelected"
        @cancel="showCategorySelection = false"
      />
    </Dialog>

    <!-- Dialog Formulaire d'annonce -->
    <Dialog 
      v-model:visible="showAnnonceForm" 
      :header="'Créer une annonce - ' + getCategoryLabel(selectedCategory)"
      :style="{ width: '90vw', maxWidth: '1000px' }"
      modal
    >
      <DynamicAnnonceForm 
        :selected-category="selectedCategory"
        @back="retourCategorySelection"
        @created="handleAnnonceCreated"
        @draft-saved="handleDraftSaved"
      />
    </Dialog>

    <!-- Dialog Contrat -->
    <Dialog 
      v-model:visible="showContratDialog" 
      :header="contratsStore.contrat ? 'Contrat EcoDeli' : 'Aucun contrat'"
      :style="{ width: '80vw', maxWidth: '800px' }"
      modal
    >
      <div v-if="!contratsStore.contrat" class="text-center py-4">
        <i class="pi pi-file-o text-6xl text-400 mb-3"></i>
        <p class="text-600">Aucun contrat disponible</p>
      </div>

      <div v-else class="py-2">
        <div class="mb-4">
          <div class="flex justify-content-between align-items-center mb-3">
            <h3 class="m-0">Statut : {{ contratsStore.statutLibelle }}</h3>
            <Badge 
              :value="contratsStore.statutLibelle" 
              :severity="statutContratBadge.severity"
            />
          </div>
        </div>

        <div v-if="contratsStore.contrat.contenuContrat" class="mb-4">
          <h4>Contenu du contrat :</h4>
          <div 
            class="border-1 border-300 border-round p-3 bg-gray-50"
            style="max-height: 400px; overflow-y: auto;"
            v-html="contratsStore.contrat.contenuContrat"
          ></div>
        </div>

        <div v-if="contratsStore.peutSignerContrat" class="mb-4">
          <Divider />
          <div class="flex align-items-center gap-2 mb-3">
            <Checkbox 
              v-model="acceptationContrat" 
              inputId="acceptation"
              :binary="true"
            />
            <label for="acceptation" class="text-sm">
              J'accepte les termes et conditions de ce contrat
            </label>
          </div>
        </div>

        <div class="flex justify-content-end gap-2">
          <Button 
            label="Fermer" 
            icon="pi pi-times"
            outlined
            @click="showContratDialog = false"
          />
          <Button 
            v-if="contratsStore.peutSignerContrat"
            label="Signer le contrat" 
            icon="pi pi-check"
            :loading="loadingContrat"
            :disabled="!acceptationContrat"
            @click="signerContrat"
          />
        </div>
      </div>
    </Dialog>

    <Toast />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useContratsStore } from '@/stores/contrats'
import { useAnnonceCommercantStore } from '@/stores/annonceCommercant'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import CategorySelection from '@/components/annonces/CategorySelection.vue'
import DynamicAnnonceForm from '@/components/annonces/DynamicAnnonceForm.vue'

const router = useRouter()
const authStore = useAuthStore()
const contratsStore = useContratsStore()
const annonceStore = useAnnonceCommercantStore()
const toast = useToast()
const confirm = useConfirm()

const showContratDialog = ref(false)
const showCategorySelection = ref(false)
const showAnnonceForm = ref(false)
const loadingContrat = ref(false)
const acceptationContrat = ref(false)
const selectedCategory = ref(null)

const menuItems = [
  { label: 'Tableau de bord', icon: 'pi pi-home', command: () => router.push('/commercant/dashboard') },
  { label: 'Mes annonces', icon: 'pi pi-list', command: () => router.push('/commercant/annonces') },
  { label: 'Mon commerce', icon: 'pi pi-shop' },
  { label: 'Mon contrat', icon: 'pi pi-file', command: () => showContratDialog.value = true }
]

const userInitials = computed(() => {
  if (!authStore.user) return '?'
  const prenom = authStore.user.prenom || ''
  const nom = authStore.user.nom || ''
  return (prenom.charAt(0) + nom.charAt(0)).toUpperCase()
})

const statutContratBadge = computed(() => {
  const statut = contratsStore.statutContrat
  switch (statut) {
    case 'AUCUN_CONTRAT':
      return { severity: 'secondary', label: 'Aucun contrat' }
    case 'DEMANDE_ENVOYEE':
      return { severity: 'info', label: 'Demande envoyée' }
    case 'EN_ATTENTE_ADMIN':
      return { severity: 'warn', label: 'En attente admin' }
    case 'CONTRAT_CREE':
      return { severity: 'warn', label: 'À signer' }
    case 'SIGNE_VALIDE':
      return { severity: 'success', label: 'Contrat signé' }
    case 'REFUSE':
      return { severity: 'danger', label: 'Refusé' }
    default:
      return { severity: 'secondary', label: 'Inconnu' }
  }
})

const mesAnnonces = computed(() => {
  return annonceStore.annonces.filter(a => a.commercant?.idUtilisateur === authStore.user?.id)
})

const categories = computed(() => annonceStore.getCategoriesList)

const totalAnnonces = computed(() => mesAnnonces.value.length)

const annoncesActives = computed(() => 
  mesAnnonces.value.filter(a => a.statut === 'ACTIVE').length
)

const annoncesEnAttente = computed(() => 
  mesAnnonces.value.filter(a => a.statutApprobation === 'EN_ATTENTE').length
)

const categoriesUtilisees = computed(() => {
  const cats = new Set(mesAnnonces.value.map(a => a.categorie))
  return cats.size
})

const getCategoryIcon = (categoryCode) => {
  const icons = {
    'LIVRAISON_PONCTUELLE': 'pi pi-clock',
    'SERVICE_CHARIOT': 'pi pi-shopping-cart',
    'TRANSPORT_MARCHANDISES': 'pi pi-truck'
  }
  return icons[categoryCode] || 'pi pi-circle'
}

const getCategoryLabel = (categoryCode) => {
  const labels = {
    'LIVRAISON_PONCTUELLE': 'Livraison ponctuelle',
    'SERVICE_CHARIOT': 'Service chariot',
    'TRANSPORT_MARCHANDISES': 'Transport marchandises'
  }
  return labels[categoryCode] || categoryCode
}

const getStatutLabel = (statut) => {
  const labels = {
    'BROUILLON': 'Brouillon',
    'EN_ATTENTE_APPROBATION': 'En attente',
    'APPROUVEE': 'Approuvée',
    'PUBLIEE': 'Publiée',
    'ACTIVE': 'Active',
    'SUSPENDUE': 'Suspendue',
    'EXPIREE': 'Expirée',
    'ANNULEE': 'Annulée'
  }
  return labels[statut] || statut
}

const getStatutSeverity = (statut) => {
  const severities = {
    'BROUILLON': 'secondary',
    'EN_ATTENTE_APPROBATION': 'warning',
    'APPROUVEE': 'info',
    'PUBLIEE': 'success',
    'ACTIVE': 'success',
    'SUSPENDUE': 'warning',
    'EXPIREE': 'danger',
    'ANNULEE': 'danger'
  }
  return severities[statut] || 'secondary'
}

const getAnnoncesByCategory = (categoryCode) => {
  return mesAnnonces.value.filter(a => a.categorie === categoryCode).length
}

const getCategoryPercentage = (categoryCode) => {
  const total = totalAnnonces.value
  if (total === 0) return 0
  return (getAnnoncesByCategory(categoryCode) / total) * 100
}

const creerAnnonce = (categoryCode) => {
  selectedCategory.value = categoryCode
  showCategorySelection.value = false
  showAnnonceForm.value = true
}

const handleCategorySelected = (categoryCode) => {
  selectedCategory.value = categoryCode
  showCategorySelection.value = false
  showAnnonceForm.value = true
}

const retourCategorySelection = () => {
  showAnnonceForm.value = false
  showCategorySelection.value = true
}

const handleAnnonceCreated = (annonce) => {
  showAnnonceForm.value = false
  selectedCategory.value = null
  
  toast.add({
    severity: 'success',
    summary: 'Succès',
    detail: 'Annonce créée avec succès',
    life: 3000
  })
  
  chargerMesAnnonces()
}

const handleDraftSaved = (annonce) => {
  showAnnonceForm.value = false
  selectedCategory.value = null
  
  toast.add({
    severity: 'info',
    summary: 'Brouillon sauvegardé',
    detail: 'Votre annonce a été sauvegardée en brouillon',
    life: 3000
  })
  
  chargerMesAnnonces()
}

const voirAnnonce = (annonce) => {
  router.push(`/commercant/annonces/${annonce.idAnnonceCommercant}`)
}

const modifierAnnonce = (annonce) => {
  router.push(`/commercant/annonces/${annonce.idAnnonceCommercant}/edit`)
}

const supprimerAnnonce = (annonce) => {
  confirm.require({
    message: 'Êtes-vous sûr de vouloir supprimer cette annonce ?',
    header: 'Confirmer la suppression',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        const result = await annonceStore.deleteAnnonce(annonce.idAnnonceCommercant)
        
        if (result.success) {
          toast.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Annonce supprimée avec succès',
            life: 3000
          })
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
          detail: 'Erreur lors de la suppression',
          life: 3000
        })
      }
    }
  })
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const demanderContrat = async () => {
  if (!authStore.user?.id) {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Utilisateur non connecté',
      life: 3000
    })
    return
  }

  loadingContrat.value = true
  
  try {
    const result = await contratsStore.demanderContrat(authStore.user.id)
    
    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Succès',
        detail: result.message,
        life: 5000
      })
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error,
        life: 5000
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
    loadingContrat.value = false
  }
}

const signerContrat = async () => {
  if (!contratsStore.contrat?.idContrat || !authStore.user?.id) {
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Informations manquantes pour signer le contrat',
      life: 3000
    })
    return
  }

  loadingContrat.value = true
  
  try {
    const result = await contratsStore.signerContrat(
      contratsStore.contrat.idContrat,
      authStore.user.id
    )
    
    if (result.success) {
      toast.add({
        severity: 'success',
        summary: 'Succès',
        detail: result.message,
        life: 5000
      })
      showContratDialog.value = false
      acceptationContrat.value = false
    } else {
      toast.add({
        severity: 'error',
        summary: 'Erreur',
        detail: result.error,
        life: 5000
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
    loadingContrat.value = false
  }
}

const chargerContrat = async () => {
  if (!authStore.user?.id) return
  
  try {
    await contratsStore.getContratCommercant(authStore.user.id)
  } catch (error) {
    console.error('Erreur lors du chargement du contrat:', error)
  }
}

const chargerMesAnnonces = async () => {
  if (!authStore.user?.id) return
  
  try {
    await annonceStore.fetchAnnoncesByCommercant(authStore.user.id)
  } catch (error) {
    console.error('Erreur lors du chargement des annonces:', error)
  }
}

onMounted(async () => {
  await chargerContrat()
  await annonceStore.fetchCategories()
  await chargerMesAnnonces()
})
</script>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(12, 1fr); gap: 1rem; }
.col-12 { grid-column: span 12; }
.col-8 { grid-column: span 8; }
.col-6 { grid-column: span 6; }
.col-4 { grid-column: span 4; }
.col-3 { grid-column: span 3; }
.bg-primary { background: var(--ecodeli-green) !important; }

.stats-card {
  height: 100%;
}

.stats-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stats-icon i {
  font-size: 1.5rem;
}

.announcement-categories {
  margin-bottom: 1rem;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.category-card {
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.category-card:hover {
  border-color: #4CAF50;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.category-icon {
  text-align: center;
  margin-bottom: 1rem;
}

.category-icon i {
  font-size: 2rem;
  color: #4CAF50;
}

.category-content h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.category-content p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.category-stats {
  text-align: center;
  margin-top: 1rem;
}

.stats-badge {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.annonces-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.annonce-item {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.3s ease;
}

.annonce-item:hover {
  border-color: #4CAF50;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.annonce-category {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #6c757d;
}

.annonce-category i {
  color: #4CAF50;
}

.annonce-title {
  margin: 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.annonce-description {
  margin: 0 0 1rem 0;
  color: #6c757d;
  font-size: 0.9rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.annonce-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.annonce-meta {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.annonce-actions {
  display: flex;
  gap: 0.5rem;
}

.category-stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.category-stat-item:last-child {
  border-bottom: none;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8f5e8;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon i {
  color: #4CAF50;
  font-size: 1.2rem;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 0.8rem;
  color: #6c757d;
}

.stat-progress {
  width: 60px;
  height: 6px;
  background: #e9ecef;
  border-radius: 3px;
  overflow: hidden;
}

.stat-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
  transition: width 0.3s ease;
}

@media (max-width: 768px) {
  .grid { grid-template-columns: 1fr; }
  .col-12, .col-8, .col-6, .col-4, .col-3 { grid-column: span 1; }
  
  .category-grid {
    grid-template-columns: 1fr;
  }
  
  .annonce-footer {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .annonce-meta {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
}
</style>
