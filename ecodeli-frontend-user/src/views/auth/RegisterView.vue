<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'primevue/usetoast'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const userTypes = [
  {
    value: 'CLIENT',
    label: 'Client',
    icon: 'pi pi-user',
    description: 'Envoyer des colis et utiliser des services'
  },
  {
    value: 'LIVREUR',
    label: 'Livreur',
    icon: 'pi pi-truck',
    description: 'Effectuer des livraisons et gagner de l\'argent'
  },
  {
    value: 'PRESTATAIRE',
    label: 'Prestataire',
    icon: 'pi pi-wrench',
    description: 'Proposer des services à la personne'
  },
  {
    value: 'COMMERCANT',
    label: 'Commerçant',
    icon: 'pi pi-shop',
    description: 'Proposer des livraisons pour votre commerce'
  }
]

const domainesExpertise = [
  {
    value: 'TRANSPORT_LIVRAISON',
    label: 'Transport & Livraison',
    icon: '🚚',
    description: 'Transport de personnes, livraison de colis'
  },
  {
    value: 'SERVICES_DOMICILE',
    label: 'Services à domicile',
    icon: '🏠',
    description: 'Ménage, garde d\'enfants/animaux, jardinage'
  },
  {
    value: 'TRAVAUX_REPARATIONS',
    label: 'Travaux & Réparations',
    icon: '🔧',
    description: 'Bricolage, plomberie, électricité'
  },
  {
    value: 'COURSES_ACHATS',
    label: 'Courses & Achats',
    icon: '🛒',
    description: 'Courses alimentaires, achats divers'
  },
  {
    value: 'SERVICES_PERSONNELS',
    label: 'Services personnels',
    icon: '👥',
    description: 'Assistance administrative, organisation'
  },
  {
    value: 'EDUCATION_FORMATION',
    label: 'Éducation & Formation',
    icon: '🎓',
    description: 'Cours particuliers, formation'
  }
]

const form = reactive({
  prenom: '',
  nom: '',
  email: '',
  motDePasse: '',
  userType: '',
  nomEntreprise: '',
  siret: '',
  domaineExpertise: ''
})

// États
const errors = ref({})
const registerError = ref('')
const registerSuccess = ref('')

onMounted(() => {
  if (route.query.type) {
    form.userType = route.query.type
  }
})

const validateForm = () => {
  errors.value = {}

  if (!form.prenom.trim()) {
    errors.value.prenom = 'Prénom requis'
  }

  if (!form.nom.trim()) {
    errors.value.nom = 'Nom requis'
  }

  if (!form.email.trim()) {
    errors.value.email = 'Email requis'
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.value.email = 'Email invalide'
  }

  if (!form.motDePasse) {
    errors.value.motDePasse = 'Mot de passe requis'
  } else if (form.motDePasse.length < 6) {
    errors.value.motDePasse = 'Le mot de passe doit contenir au moins 6 caractères'
  }

  if (!form.userType) {
    errors.value.userType = 'Type de profil requis'
  }

  if (form.userType === 'PRESTATAIRE') {
    if (!form.nomEntreprise.trim()) {
      errors.value.nomEntreprise = 'Le nom de l\'entreprise est requis pour les prestataires'
    }

    if (form.siret && !/^[0-9]{14}$/.test(form.siret)) {
      errors.value.siret = 'Le SIRET doit contenir exactement 14 chiffres'
    }
  }

  return Object.keys(errors.value).length === 0
}

const handleRegister = async () => {
  registerError.value = ''
  registerSuccess.value = ''

  if (!validateForm()) {
    return
  }

  try {
    const result = await authStore.register(form)

    if (result.success) {
      registerSuccess.value = 'Compte créé avec succès ! Redirection en cours...'

      toast.add({
        severity: 'success',
        summary: 'Inscription réussie',
        detail: `Bienvenue ${result.user.prenom} ! Votre compte ${result.user.userType.toLowerCase()} a été créé.`,
        life: 4000
      })

      const role = result.user.userType.toLowerCase()
      setTimeout(() => {
        router.push(`/${role}`)
      }, 2000)
    } else {
      registerError.value = result.message || 'Erreur lors de l\'inscription'
    }

  } catch (error) {
    console.error('Erreur lors de l\'inscription:', error)
    registerError.value = 'Une erreur inattendue s\'est produite'
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-container">
    <div class="register-wrapper">
      <Card class="register-card">
        <template #header>
          <div class="register-header">
            <h1 class="ecodeli-title">
              <i class="pi pi-leaf mr-2"></i>
              Rejoindre EcoDeli
            </h1>
            <p class="ecodeli-subtitle">Créez votre compte en quelques minutes</p>
          </div>
        </template>

        <template #content>
          <form @submit.prevent="handleRegister" class="register-form">
            <!-- Type de profil -->
            <div class="field">
              <label for="userType" class="field-label">
                <i class="pi pi-users mr-2"></i>
                Type de profil *
              </label>
              <Dropdown
                id="userType"
                v-model="form.userType"
                :options="userTypes"
                optionLabel="label"
                optionValue="value"
                placeholder="Choisissez votre profil"
                :class="{ 'p-invalid': errors.userType }"
                class="w-full"
                showClear
              >
                <template #option="slotProps">
                  <div class="profile-option">
                    <i :class="slotProps.option.icon" class="mr-2"></i>
                    <div>
                      <div class="font-medium">{{ slotProps.option.label }}</div>
                      <div class="text-sm text-600">{{ slotProps.option.description }}</div>
                    </div>
                  </div>
                </template>
              </Dropdown>
              <small v-if="errors.userType" class="p-error">{{ errors.userType }}</small>
            </div>

            <div class="field-group">
              <!-- Prénom -->
              <div class="field">
                <label for="prenom" class="field-label">Prénom *</label>
                <InputText
                  id="prenom"
                  v-model="form.prenom"
                  placeholder="Votre prénom"
                  :class="{ 'p-invalid': errors.prenom }"
                  class="w-full"
                />
                <small v-if="errors.prenom" class="p-error">{{ errors.prenom }}</small>
              </div>

              <!-- Nom -->
              <div class="field">
                <label for="nom" class="field-label">Nom *</label>
                <InputText
                  id="nom"
                  v-model="form.nom"
                  placeholder="Votre nom"
                  :class="{ 'p-invalid': errors.nom }"
                  class="w-full"
                />
                <small v-if="errors.nom" class="p-error">{{ errors.nom }}</small>
              </div>
            </div>

            <!-- Email -->
            <div class="field">
              <label for="email" class="field-label">
                <i class="pi pi-envelope mr-2"></i>
                Email *
              </label>
              <InputText
                id="email"
                v-model="form.email"
                type="email"
                placeholder="votre.email@exemple.com"
                :class="{ 'p-invalid': errors.email }"
                class="w-full"
              />
              <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
            </div>

            <!-- Mot de passe -->
            <div class="field">
              <label for="motDePasse" class="field-label">
                <i class="pi pi-lock mr-2"></i>
                Mot de passe *
              </label>
              <Password
                id="motDePasse"
                v-model="form.motDePasse"
                placeholder="Votre mot de passe"
                :class="{ 'p-invalid': errors.motDePasse }"
                class="w-full"
                :feedback="true"
                toggleMask
                :promptLabel="'Entrez un mot de passe'"
                :weakLabel="'Faible'"
                :mediumLabel="'Moyen'"
                :strongLabel="'Fort'"
              />
              <small v-if="errors.motDePasse" class="p-error">{{ errors.motDePasse }}</small>
            </div>

            <!-- Champs spécifiques aux prestataires -->
            <div v-if="form.userType === 'PRESTATAIRE'" class="prestataire-fields">
              <Divider>
                <span class="text-sm text-600">Informations professionnelles</span>
              </Divider>

              <!-- Nom de l'entreprise -->
              <div class="field">
                <label for="nomEntreprise" class="field-label">
                  <i class="pi pi-building mr-2"></i>
                  Nom de l'entreprise *
                </label>
                <InputText
                  id="nomEntreprise"
                  v-model="form.nomEntreprise"
                  placeholder="Nom de votre entreprise"
                  :class="{ 'p-invalid': errors.nomEntreprise }"
                  class="w-full"
                />
                <small v-if="errors.nomEntreprise" class="p-error">{{ errors.nomEntreprise }}</small>
              </div>

              <!-- SIRET -->
              <div class="field">
                <label for="siret" class="field-label">
                  <i class="pi pi-id-card mr-2"></i>
                  Numéro SIRET (optionnel)
                </label>
                <InputText
                  id="siret"
                  v-model="form.siret"
                  placeholder="14 chiffres"
                  :class="{ 'p-invalid': errors.siret }"
                  class="w-full"
                  maxlength="14"
                />
                <small v-if="errors.siret" class="p-error">{{ errors.siret }}</small>
                <small class="text-xs text-600">Le SIRET doit contenir exactement 14 chiffres</small>
              </div>

              <!-- Domaine d'expertise -->
              <div class="field">
                <label for="domaineExpertise" class="field-label">
                  <i class="pi pi-star mr-2"></i>
                  Domaine d'expertise
                </label>
                <Dropdown
                  id="domaineExpertise"
                  v-model="form.domaineExpertise"
                  :options="domainesExpertise"
                  optionLabel="label"
                  optionValue="value"
                  placeholder="Choisissez votre domaine"
                  class="w-full"
                  showClear
                >
                  <template #option="slotProps">
                    <div class="expertise-option">
                      <span>{{ slotProps.option.icon }}</span>
                      <div class="ml-2">
                        <div class="font-medium">{{ slotProps.option.label }}</div>
                        <div class="text-sm text-600">{{ slotProps.option.description }}</div>
                      </div>
                    </div>
                  </template>
                </Dropdown>
              </div>
            </div>

            <!-- Message d'erreur global -->
            <InlineMessage
              v-if="registerError"
              severity="error"
              class="w-full mb-3"
            >
              <i class="pi pi-exclamation-triangle mr-2"></i>
              {{ registerError }}
            </InlineMessage>

            <!-- Message de succès -->
            <InlineMessage
              v-if="registerSuccess"
              severity="success"
              class="w-full mb-3"
            >
              <i class="pi pi-check mr-2"></i>
              {{ registerSuccess }}
            </InlineMessage>

            <Button
              type="submit"
              :label="authStore.isLoading ? 'Création en cours...' : 'Créer mon compte'"
              :loading="authStore.isLoading"
              class="w-full register-button"
              size="large"
            />
          </form>
        </template>

        <template #footer>
          <Divider />
          <div class="register-footer">
            <p class="text-sm text-center">
              Déjà un compte ?
              <a href="#" @click.prevent="goToLogin" class="text-primary">Se connecter</a>
            </p>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 50%, #ecfdf5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.register-wrapper {
  width: 100%;
  max-width: 500px;
}

.register-card {
  border: none;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.register-header {
  text-align: center;
  padding: 2rem 2rem 1rem;
}

.register-header h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: var(--ecodeli-green-dark);
}

.register-header p {
  margin: 0;
  color: var(--surface-600);
}

.register-form {
  padding: 0 2rem 1rem;
}

.field {
  margin-bottom: 1.5rem;
}

.field-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.field-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--surface-700);
}

.profile-option {
  display: flex;
  align-items: center;
  padding: 0.5rem;
}

.register-button {
  margin-top: 1rem;
  background: var(--ecodeli-green);
  border-color: var(--ecodeli-green);
}

.register-button:hover {
  background: var(--ecodeli-green-dark);
  border-color: var(--ecodeli-green-dark);
}

.register-footer {
  padding: 1rem 2rem 2rem;
}

.w-full {
  width: 100%;
}

.text-primary {
  color: var(--ecodeli-green);
  text-decoration: none;
}

.text-primary:hover {
  color: var(--ecodeli-green-dark);
  text-decoration: underline;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mb-3 {
  margin-bottom: 0.75rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-center {
  text-align: center;
}

.text-600 {
  color: var(--surface-600);
}

.font-medium {
  font-weight: 500;
}

.prestataire-fields {
  background: var(--surface-50);
  border-radius: 0.5rem;
  padding: 1rem;
  margin: 1rem 0;
  border: 1px solid var(--surface-200);
}

.expertise-option {
  display: flex;
  align-items: center;
  padding: 0.5rem;
}

.expertise-option span {
  font-size: 1.2rem;
  margin-right: 0.5rem;
}

.ml-2 {
  margin-left: 0.5rem;
}

.text-xs {
  font-size: 0.75rem;
}

@media (max-width: 640px) {
  .field-group {
    grid-template-columns: 1fr;
  }
  
  .register-container {
    padding: 1rem;
  }
  
  .prestataire-fields {
    margin: 0.5rem 0;
    padding: 0.75rem;
  }
}
</style>
