<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authStore } from '@/store/auth'
import contratApi from '@/services/contratApi'
import apiServices from '@/services/apiServices'

export default {
  name: 'AdminContratFormView',
  setup() {
    const route = useRoute()
    const router = useRouter()

    const contrat = ref(null)
    const commercants = ref([])
    const isLoading = ref(true)
    const isSubmitting = ref(false)
    const error = ref(null)

    const form = ref({
      commercantId: '',
      commissionPourcentage: '',
      fraisFixeMensuel: 0,
      dateDebut: '',
      dateFin: '',
      conditionsService: ''
    })

    const user = computed(() => authStore.user)
    const isEdit = computed(() => !!route.params.id)
    const contratId = computed(() => parseInt(route.params.id))

    const selectedCommercant = computed(() => {
      if (!form.value.commercantId) return null
      return commercants.value.find(c => c.idUtilisateur === parseInt(form.value.commercantId))
    })

    const isFormValid = computed(() => {
      return form.value.commercantId &&
             form.value.commissionPourcentage &&
             form.value.commissionPourcentage >= 0 &&
             form.value.commissionPourcentage <= 100
    })

    const getJWTUserType = () => {
      if (!authStore.token) return null
      try {
        const tokenParts = authStore.token.split('.')
        const payload = JSON.parse(atob(tokenParts[1]))
        return payload.userType
      } catch (e) {
        return null
      }
    }

    const loadCommercants = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/admin/utilisateurs?type=COMMERCANT', {
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          const data = await response.json()
          commercants.value = data
        } else {
          const errorText = await response.text()
          if (response.status === 403) {
            error.value = `Accès refusé. Le backend ne reconnaît pas vos droits d'administrateur. Vérifiez la configuration de sécurité côté serveur. (Status: ${response.status})`
          } else {
            error.value = `Erreur lors du chargement des commerçants: ${response.status} - ${errorText}`
          }
        }
      } catch (err) {
        error.value = `Erreur de connexion: ${err.message}`
      }
    }

    const loadContrat = async () => {
      if (!isEdit.value) return

      try {
        const response = await fetch(`/api/admin/contrats/${contratId.value}`, {
          headers: {
            'Authorization': `Bearer ${authStore.token}`,
            'Content-Type': 'application/json'
          }
        })

        if (response.ok) {
          contrat.value = await response.json()

          form.value = {
            commercantId: contrat.value.commercant?.idUtilisateur || '',
            commissionPourcentage: contrat.value.commissionPourcentage || '',
            fraisFixeMensuel: contrat.value.fraisFixeMensuel || 0,
            dateDebut: contrat.value.dateDebut ? contrat.value.dateDebut.split('T')[0] : '',
            dateFin: contrat.value.dateFin ? contrat.value.dateFin.split('T')[0] : '',
            conditionsService: contrat.value.conditionsService || ''
          }
        } else {
          throw new Error('Contrat non trouvé')
        }
      } catch (err) {
        error.value = err.message
      }
    }

    const initializeForm = async () => {
      isLoading.value = true
      error.value = null

      try {
        await loadCommercants()

        if (isEdit.value) {
          await loadContrat()
        }
      } catch (err) {
        error.value = err.message
      } finally {
        isLoading.value = false
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      return new Date(dateString).toLocaleDateString('fr-FR')
    }

    const submitForm = async () => {
      if (!isFormValid.value) return

      isSubmitting.value = true
      error.value = null

      try {
        const contratData = {
          commercant: {
            idUtilisateur: parseInt(form.value.commercantId),
            type: 'COMMERCANT'
          },
          commissionPourcentage: parseFloat(form.value.commissionPourcentage),
          fraisFixeMensuel: parseFloat(form.value.fraisFixeMensuel) || 0,
          dateDebut: form.value.dateDebut || null,
          dateFin: form.value.dateFin || null,
          conditionsService: form.value.conditionsService || null
        }

        if (isEdit.value) {
          await contratApi.updateContrat(contratId.value, contratData, user.value.idUtilisateur)
        } else {
          await contratApi.createContrat(contratData, user.value.idUtilisateur)
        }

        router.push('/admin/contrats')
      } catch (err) {
        error.value = err.message
      } finally {
        isSubmitting.value = false
      }
    }

    const createAndSend = async () => {
      if (!isFormValid.value) return

      isSubmitting.value = true
      error.value = null

      try {
        const contratData = {
          commercant: {
            idUtilisateur: parseInt(form.value.commercantId),
            type: 'COMMERCANT'
          },
          commissionPourcentage: parseFloat(form.value.commissionPourcentage),
          fraisFixeMensuel: parseFloat(form.value.fraisFixeMensuel) || 0,
          dateDebut: form.value.dateDebut || null,
          dateFin: form.value.dateFin || null,
          conditionsService: form.value.conditionsService || null
        }

        const nouveauContrat = await contratApi.createContrat(contratData, user.value.idUtilisateur)

        await contratApi.envoyerPourSignature(nouveauContrat.idContrat, user.value.idUtilisateur)

        router.push('/admin/contrats')
      } catch (err) {
        error.value = err.message
      } finally {
        isSubmitting.value = false
      }
    }

    watch(() => form.value.dateFin, (newDate) => {
      if (newDate && form.value.dateDebut && newDate < form.value.dateDebut) {
        form.value.dateFin = ''
      }
    })

    onMounted(() => {
      initializeForm()
    })

    return {
      contrat,
      commercants,
      isLoading,
      isSubmitting,
      error,
      form,
      isEdit,
      selectedCommercant,
      isFormValid,
      formatDate,
      submitForm,
      createAndSend,
      authStore,
      getJWTUserType
    }
  }
}
</script>

<template>
  <div class="admin-contrat-form-view">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/admin/contrats" class="back-link">
            <i class="fas fa-arrow-left"></i>
          </router-link>
          <div>
            <h1>
              <i class="fas fa-file-contract"></i> 
              {{ isEdit ? 'Modifier le contrat' : 'Nouveau contrat' }}
            </h1>
            <p v-if="isEdit && contrat">
              Contrat {{ contrat.numeroContrat }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="content-container">
        <!-- Messages d'état -->
        <div v-if="error" class="error-message">
          <i class="fas fa-exclamation-triangle"></i>
          {{ error }}
        </div>

        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>{{ isEdit ? 'Chargement du contrat...' : 'Initialisation...' }}</p>
        </div>

        <!-- Formulaire -->
        <div v-if="!isLoading" class="form-container">
          <form @submit.prevent="submitForm" class="contrat-form">
            <!-- Sélection du commerçant -->
            <div class="form-section">
              <h3><i class="fas fa-store"></i> Commerçant</h3>

              <div class="form-group">
                <label for="commercant">Sélectionner un commerçant *</label>
                <div class="commercant-selector">
                  <select 
                    id="commercant" 
                    v-model="form.commercantId" 
                    required
                    :disabled="isEdit"
                  >
                    <option value="">-- Sélectionner un commerçant --</option>
                    <option
                      v-for="commercant in commercants"
                      :key="commercant.idUtilisateur"
                      :value="commercant.idUtilisateur"
                    >
                      {{ commercant.nom }} {{ commercant.prenom }} - {{ commercant.email }}
                    </option>
                  </select>
                  <div v-if="selectedCommercant" class="commercant-info">
                    <div class="commercant-details">
                      <div class="detail-item">
                        <strong>{{ selectedCommercant.nom }} {{ selectedCommercant.prenom }}</strong>
                      </div>
                      <div class="detail-item">
                        <i class="fas fa-envelope"></i> {{ selectedCommercant.email }}
                      </div>
                      <div class="detail-item" v-if="selectedCommercant.telephone">
                        <i class="fas fa-phone"></i> {{ selectedCommercant.telephone }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Conditions financières -->
            <div class="form-section">
              <h3><i class="fas fa-euro-sign"></i> Conditions financières</h3>
              <div class="form-grid">
                <div class="form-group">
                  <label for="commission">Commission (%) *</label>
                  <div class="input-with-unit">
                    <input 
                      id="commission"
                      type="number" 
                      v-model="form.commissionPourcentage"
                      min="0"
                      max="100"
                      step="0.1"
                      required
                      placeholder="Ex: 5.5"
                    />
                    <span class="unit">%</span>
                  </div>
                  <small class="form-help">
                    Pourcentage prélevé sur chaque transaction
                  </small>
                </div>

                <div class="form-group">
                  <label for="fraisMensuel">Frais fixes mensuels (€)</label>
                  <div class="input-with-unit">
                    <input
                      id="fraisMensuel"
                      type="number"
                      v-model="form.fraisFixeMensuel"
                      min="0"
                      step="0.01"
                      placeholder="Ex: 29.99"
                    />
                    <span class="unit">€</span>
                  </div>
                  <small class="form-help">
                    Frais fixes mensuels (optionnel)
                  </small>
                </div>
              </div>
            </div>

            <!-- Durée du contrat -->
            <div class="form-section">
              <h3><i class="fas fa-calendar-alt"></i> Durée du contrat</h3>
              <div class="form-grid">
                <div class="form-group">
                  <label for="dateDebut">Date de début</label>
                  <input
                    id="dateDebut"
                    type="date"
                    v-model="form.dateDebut"
                  />
                  <small class="form-help">
                    Date d'entrée en vigueur du contrat
                  </small>
                </div>

                <div class="form-group">
                  <label for="dateFin">Date de fin</label>
                  <input
                    id="dateFin"
                    type="date"
                    v-model="form.dateFin"
                    :min="form.dateDebut"
                  />
                  <small class="form-help">
                    Laissez vide pour un contrat à durée indéterminée
                  </small>
                </div>
              </div>
            </div>

            <!-- Conditions spécifiques -->
            <div class="form-section">
              <h3><i class="fas fa-list-ul"></i> Conditions spécifiques</h3>
              <div class="form-group">
                <label for="conditions">Conditions particulières</label>
                <textarea
                  id="conditions"
                  v-model="form.conditionsService"
                  rows="6"
                  placeholder="Décrivez les conditions particulières de ce contrat (optionnel)..."
                ></textarea>
                <small class="form-help">
                  Conditions spécifiques à ce commerçant (horaires, zones de livraison, etc.)
                </small>
              </div>
            </div>

            <!-- Aperçu du contrat -->
            <div v-if="form.commissionPourcentage" class="form-section preview-section">
              <h3><i class="fas fa-eye"></i> Aperçu du contrat</h3>
              <div class="contract-preview">
                <div class="preview-header">
                  <h4>Résumé des conditions</h4>
                </div>
                <div class="preview-content">
                  <div class="preview-item">
                    <span class="preview-label">Commerçant :</span>
                    <span class="preview-value">
                      {{ selectedCommercant ? `${selectedCommercant.nom} ${selectedCommercant.prenom}` : 'Non sélectionné' }}
                    </span>
                  </div>
                  <div class="preview-item">
                    <span class="preview-label">Commission :</span>
                    <span class="preview-value commission">{{ form.commissionPourcentage }}%</span>
                  </div>
                  <div v-if="form.fraisFixeMensuel > 0" class="preview-item">
                    <span class="preview-label">Frais mensuels :</span>
                    <span class="preview-value">{{ form.fraisFixeMensuel }}€</span>
                  </div>
                  <div class="preview-item">
                    <span class="preview-label">Durée :</span>
                    <span class="preview-value">
                      <span v-if="form.dateDebut">
                        Du {{ formatDate(form.dateDebut) }}
                        <span v-if="form.dateFin"> au {{ formatDate(form.dateFin) }}</span>
                        <span v-else> (durée indéterminée)</span>
                      </span>
                      <span v-else>Non définie</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="form-actions">
              <router-link to="/admin/contrats" class="btn-cancel">
                <i class="fas fa-times"></i> Annuler
              </router-link>

              <div class="primary-actions">
                <button
                  type="submit"
                  class="btn-save"
                  :disabled="isSubmitting || !isFormValid"
                >
                  <i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i>
                  <i v-else class="fas fa-save"></i>
                  {{ isEdit ? 'Modifier' : 'Créer' }} le contrat
                </button>

                <button
                  v-if="!isEdit && form.commercantId && form.commissionPourcentage"
                  type="button"
                  @click="createAndSend"
                  class="btn-create-send"
                  :disabled="isSubmitting || !isFormValid"
                >
                  <i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i>
                  <i v-else class="fas fa-paper-plane"></i>
                  Créer et envoyer
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-contrat-form-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
}

.page-header {
  background: white;
  box-shadow: var(--shadow);
  border-bottom: 3px solid var(--primary-color);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-link {
  width: 40px;
  height: 40px;
  background: var(--primary-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: all 0.3s ease;
}

.back-link:hover {
  background: var(--secondary-color);
  transform: translateX(-2px);
}

.header-left h1 {
  font-size: 2rem;
  color: var(--text-color);
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header-left p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 1rem;
}

.page-content {
  padding: 2rem 0;
}

.content-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Messages d'état */
.error-message {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 12px;
  padding: 1.5rem;
  color: #c33;
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.loading-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-secondary);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Formulaire */
.form-container {
  background: white;
  border-radius: 15px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.contrat-form {
  padding: 2rem;
}

.form-section {
  margin-bottom: 3rem;
}

.form-section:last-of-type {
  margin-bottom: 2rem;
}

.form-section h3 {
  color: var(--text-color);
  margin: 0 0 1.5rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--primary-color);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.3rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: var(--text-color);
  font-size: 0.95rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.8rem;
  border: 2px solid var(--border-light);
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.input-with-unit {
  position: relative;
  display: flex;
  align-items: center;
}

.input-with-unit input {
  flex: 1;
  padding-right: 3rem;
}

.unit {
  position: absolute;
  right: 1rem;
  color: var(--text-secondary);
  font-weight: 500;
  pointer-events: none;
}

.form-help {
  color: var(--text-secondary);
  font-size: 0.85rem;
  line-height: 1.4;
}

/* Sélecteur de commerçant */
.commercant-selector select {
  margin-bottom: 1rem;
}

.commercant-info {
  background: var(--bg-secondary);
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid var(--primary-color);
}

.commercant-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-color);
  font-size: 0.9rem;
}

/* Aperçu du contrat */
.preview-section {
  background: var(--bg-secondary);
  padding: 1.5rem;
  border-radius: 12px;
  border: 2px solid var(--primary-color);
}

.contract-preview {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.preview-header {
  background: var(--primary-color);
  color: white;
  padding: 1rem;
}

.preview-header h4 {
  margin: 0;
  font-size: 1.1rem;
}

.preview-content {
  padding: 1.5rem;
  display: grid;
  gap: 1rem;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border-light);
}

.preview-item:last-child {
  border-bottom: none;
}

.preview-label {
  font-weight: 500;
  color: var(--text-secondary);
}

.preview-value {
  color: var(--text-color);
  font-weight: 500;
}

.preview-value.commission {
  color: var(--primary-color);
  font-size: 1.2rem;
  font-weight: 600;
}

/* Actions */
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 2rem;
  border-top: 2px solid var(--border-light);
  margin-top: 2rem;
}

.primary-actions {
  display: flex;
  gap: 1rem;
}

.btn-cancel {
  background: #6c757d;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #5a6268;
  text-decoration: none;
  color: white;
}

.btn-save {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.btn-save:hover:not(:disabled) {
  background: var(--secondary-color);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-create-send {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.btn-create-send:hover:not(:disabled) {
  background: #218838;
}

.btn-create-send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .header-left {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .primary-actions {
    flex-direction: column;
  }

  .preview-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.3rem;
  }
}

/* Dark theme */
[data-theme="dark"] .admin-contrat-form-view {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .page-header,
[data-theme="dark"] .form-container {
  background: var(--bg-secondary);
}

[data-theme="dark"] .contract-preview {
  background: var(--bg-color);
}

[data-theme="dark"] .commercant-info,
[data-theme="dark"] .preview-section {
  background: var(--bg-color);
}
</style>
