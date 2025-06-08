<script>
export default {
  name: 'CommercantAnnonceEditView',
  data() {
    return {
      annonce: {
        idAnnonce: null,
        titre: '',
        description: '',
        prixUnitaire: null,
        adresseDepart: '',
        adresseFin: '',
        dateDebut: '',
        dateFin: '',
        typeAnnonce: 'unique',
        statut: '',
        colis: {
          poids: null,
          longueur: null,
          largeur: null,
          hauteur: null,
          fragile: false,
          description: ''
        },
        expediteur: {
          nom: '',
          prenom: '',
          telephone: '',
          email: ''
        },
        destinataire: {
          nom: '',
          prenom: '',
          telephone: '',
          email: ''
        }
      },
      originalAnnonce: null,
      typeOptions: [
        { value: 'unique', label: 'Unique (livraison ponctuelle)' },
        { value: 'récurrente', label: 'Récurrente (service régulier)' }
      ],
      minDate: '',
      user: null,
      isLoading: true,
      isSaving: false,
      error: null,
      success: false
    }
  },
  computed: {
    canModify() {
      return this.annonce.statut === 'PUBLIEE';
    },
    isModified() {
      if (!this.originalAnnonce) return false;

      const basicFieldsChanged = this.annonce.titre !== this.originalAnnonce.titre ||
        this.annonce.description !== this.originalAnnonce.description ||
        parseFloat(this.annonce.prixUnitaire) !== parseFloat(this.originalAnnonce.prixUnitaire) ||
        this.annonce.adresseDepart !== this.originalAnnonce.adresseDepart ||
        this.annonce.adresseFin !== this.originalAnnonce.adresseFin ||
        this.annonce.dateDebut !== this.originalAnnonce.dateDebut ||
        this.annonce.dateFin !== this.originalAnnonce.dateFin ||
        this.annonce.typeAnnonce !== this.originalAnnonce.typeAnnonce;

      const colisChanged = this.haveDifferentValues(this.annonce.colis, this.originalAnnonce.colis);
      const destinataireChanged = this.haveDifferentValues(this.annonce.destinataire, this.originalAnnonce.destinataire);

      return basicFieldsChanged || colisChanged || destinataireChanged;
    }
  },
  methods: {
    haveDifferentValues(obj1, obj2) {
      if (!obj1 || !obj2) return obj1 !== obj2;
      for (const key in obj1) {
        if (obj1[key] !== obj2[key]) {
          return true;
        }
      }
      return false;
    },

    formatDateForInput(dateString) {
      if (!dateString) return '';
      if (dateString.includes('T')) {
        return dateString.slice(0, 16);
      }
      const date = new Date(dateString);
      return date.toISOString().slice(0, 16);
    },

    async fetchAnnonce() {
      this.isLoading = true;
      this.error = null;

      try {
        const annonceId = this.$route.params.id;
        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }

        const response = await fetch(`/api/commercants/${this.user.idUtilisateur}/annonces/${annonceId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          if (response.status === 404) {
            throw new Error('Annonce non trouvée');
          }
          throw new Error('Erreur lors du chargement de l\'annonce');
        }

        const data = await response.json();
        this.annonce = { ...data };
        this.originalAnnonce = { ...data };

        this.annonce.dateDebut = this.formatDateForInput(data.dateDebut);
        this.annonce.dateFin = this.formatDateForInput(data.dateFin);
        this.originalAnnonce.dateDebut = this.formatDateForInput(data.dateDebut);
        this.originalAnnonce.dateFin = this.formatDateForInput(data.dateFin);

      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
        setTimeout(() => {
          this.$router.push('/commercant/annonces');
        }, 3000);
      } finally {
        this.isLoading = false;
      }
    },

    async handleSave() {
      this.isSaving = true;
      this.error = null;
      this.success = false;

      try {
        if (!this.annonce.titre || !this.annonce.description || !this.annonce.prixUnitaire || 
            !this.annonce.adresseDepart || !this.annonce.adresseFin || !this.annonce.dateDebut || 
            !this.annonce.dateFin) {
          throw new Error('Veuillez remplir tous les champs obligatoires');
        }

        const dateDebut = new Date(this.annonce.dateDebut);
        const dateFin = new Date(this.annonce.dateFin);
        if (dateDebut >= dateFin) {
          throw new Error('La date de début doit être antérieure à la date de fin');
        }

        this.annonce.prixUnitaire = parseFloat(this.annonce.prixUnitaire);
        const token = localStorage.getItem('token');

        const response = await fetch(`/api/commercants/${this.user.idUtilisateur}/annonces/${this.annonce.idAnnonce}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(this.annonce)
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData?.message || 'Erreur lors de la sauvegarde');
        }

        const result = await response.json();
        this.success = true;
        this.originalAnnonce = { ...this.annonce };

        setTimeout(() => {
          this.$router.push('/commercant/annonces');
        }, 2000);

      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isSaving = false;
      }
    },

    async cancelAnnonce() {
      if (!confirm('Êtes-vous sûr de vouloir annuler cette annonce ? Cette action est irréversible.')) {
        return;
      }

      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/commercants/${this.user.idUtilisateur}/annonces/${this.annonce.idAnnonce}/annuler`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error('Erreur lors de l\'annulation');
        }

        this.$router.push('/commercant/annonces');
      } catch (err) {
        this.error = err.message || 'Erreur lors de l\'annulation';
      }
    },

    getStatutBadgeClass(statut) {
      const classes = {
        'PUBLIEE': 'badge-success',
        'EN_COURS': 'badge-warning',
        'TERMINEE': 'badge-info',
        'ANNULEE': 'badge-danger'
      };
      return classes[statut] || 'badge-secondary';
    },

    getStatutLabel(statut) {
      const labels = {
        'PUBLIEE': 'Publiée',
        'EN_COURS': 'En cours',
        'TERMINEE': 'Terminée',
        'ANNULEE': 'Annulée'
      };
      return labels[statut] || statut;
    }
  },

  mounted() {
    const now = new Date();
    this.minDate = now.toISOString().slice(0, 16);

    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
      this.fetchAnnonce();
    } else {
      this.$router.push('/login');
    }
  }
}
</script>

<template>
  <div class="edit-annonce-container">
    <div class="edit-annonce-header">
      <div class="header-content">
        <h1>Modifier l'annonce</h1>
        <div v-if="!isLoading && annonce.statut" class="status-badge" :class="getStatutBadgeClass(annonce.statut)">
          {{ getStatutLabel(annonce.statut) }}
        </div>
      </div>
      <router-link to="/commercant/annonces" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour aux annonces
      </router-link>
    </div>

    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
        <p>Chargement de l'annonce...</p>
      </div>
    </div>

    <div v-else-if="success" class="success-message">
      <i class="fas fa-check-circle"></i>
      <p>L'annonce a été modifiée avec succès!</p>
    </div>

    <div v-else class="annonce-form-container">
      <div v-if="error" class="error-message">
        <i class="fas fa-exclamation-circle"></i>
        <p>{{ error }}</p>
      </div>

      <div v-if="!canModify" class="warning-message">
        <i class="fas fa-info-circle"></i>
        <p>Cette annonce ne peut plus être modifiée car elle est {{ getStatutLabel(annonce.statut).toLowerCase() }}.</p>
      </div>

      <form @submit.prevent="handleSave" class="annonce-form">
        <!-- General Information -->
        <div class="form-section">
          <h2><i class="fas fa-info-circle"></i> Informations générales</h2>
          <div class="form-group">
            <label for="titre">Titre de l'annonce *</label>
            <input
              id="titre"
              v-model="annonce.titre"
              type="text"
              placeholder="Ex: Livraison de produits de ma boutique"
              required
              :disabled="!canModify"
            >
          </div>
          <div class="form-group">
            <label for="description">Description *</label>
            <textarea
              id="description"
              v-model="annonce.description"
              placeholder="Décrivez en détail ce qui doit être livré"
              rows="4"
              required
              :disabled="!canModify"
            ></textarea>
          </div>
        </div>

        <!-- Package Details -->
        <div class="form-section">
          <h2><i class="fas fa-box"></i> Détails du colis</h2>
          <div class="dimensions-container">
            <div class="form-group">
              <label for="poids">Poids (kg) *</label>
              <input
                id="poids"
                v-model="annonce.colis.poids"
                type="number"
                min="0.1"
                step="0.1"
                placeholder="Ex: 5.5"
                required
                :disabled="!canModify"
              >
            </div>
            <div class="dimensions-group">
              <div class="form-group">
                <label for="longueur">Longueur (cm) *</label>
                <input
                  id="longueur"
                  v-model="annonce.colis.longueur"
                  type="number"
                  min="1"
                  step="1"
                  placeholder="Ex: 30"
                  required
                  :disabled="!canModify"
                >
              </div>
              <div class="form-group">
                <label for="largeur">Largeur (cm) *</label>
                <input
                  id="largeur"
                  v-model="annonce.colis.largeur"
                  type="number"
                  min="1"
                  step="1"
                  placeholder="Ex: 20"
                  required
                  :disabled="!canModify"
                >
              </div>
              <div class="form-group">
                <label for="hauteur">Hauteur (cm) *</label>
                <input
                  id="hauteur"
                  v-model="annonce.colis.hauteur"
                  type="number"
                  min="1"
                  step="1"
                  placeholder="Ex: 15"
                  required
                  :disabled="!canModify"
                >
              </div>
            </div>
          </div>
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="annonce.colis.fragile" :disabled="!canModify">
              Colis fragile (nécessite une attention particulière)
            </label>
          </div>
          <div class="form-group">
            <label for="colis-description">Description du contenu</label>
            <textarea
              id="colis-description"
              v-model="annonce.colis.description"
              placeholder="Décrivez le contenu du colis"
              rows="2"
              :disabled="!canModify"
            ></textarea>
          </div>
        </div>

        <!-- Addresses and Contacts -->
        <div class="form-section">
          <h2><i class="fas fa-map-marker-alt"></i> Adresses et contacts</h2>

          <h3>Adresse de départ (votre boutique)</h3>
          <div class="form-group">
            <label for="adresseDepart">Adresse complète de départ *</label>
            <input
              id="adresseDepart"
              v-model="annonce.adresseDepart"
              type="text"
              placeholder="Numéro, rue, code postal, ville"
              required
              :disabled="!canModify"
            >
          </div>

          <h3>Expéditeur (vous)</h3>
          <div class="contact-form">
            <div class="form-group">
              <label for="exp-nom">Nom *</label>
              <input
                id="exp-nom"
                v-model="annonce.expediteur.nom"
                type="text"
                required
                :disabled="!canModify"
              >
            </div>
            <div class="form-group">
              <label for="exp-prenom">Prénom *</label>
              <input
                id="exp-prenom"
                v-model="annonce.expediteur.prenom"
                type="text"
                required
                :disabled="!canModify"
              >
            </div>
            <div class="form-group">
              <label for="exp-telephone">Téléphone *</label>
              <input
                id="exp-telephone"
                v-model="annonce.expediteur.telephone"
                type="tel"
                placeholder="Ex: 0601020304"
                required
                :disabled="!canModify"
              >
            </div>
            <div class="form-group">
              <label for="exp-email">Email</label>
              <input
                id="exp-email"
                v-model="annonce.expediteur.email"
                type="email"
                placeholder="Ex: nom@example.com"
                :disabled="!canModify"
              >
            </div>
          </div>

          <h3>Adresse de livraison</h3>
          <div class="form-group">
            <label for="adresseFin">Adresse complète de livraison *</label>
            <input
              id="adresseFin"
              v-model="annonce.adresseFin"
              type="text"
              placeholder="Numéro, rue, code postal, ville"
              required
              :disabled="!canModify"
            >
          </div>

          <h3>Destinataire</h3>
          <div class="contact-form">
            <div class="form-group">
              <label for="dest-nom">Nom *</label>
              <input
                id="dest-nom"
                v-model="annonce.destinataire.nom"
                type="text"
                required
                :disabled="!canModify"
              >
            </div>
            <div class="form-group">
              <label for="dest-prenom">Prénom *</label>
              <input
                id="dest-prenom"
                v-model="annonce.destinataire.prenom"
                type="text"
                required
                :disabled="!canModify"
              >
            </div>
            <div class="form-group">
              <label for="dest-telephone">Téléphone *</label>
              <input
                id="dest-telephone"
                v-model="annonce.destinataire.telephone"
                type="tel"
                placeholder="Ex: 0601020304"
                required
                :disabled="!canModify"
              >
            </div>
            <div class="form-group">
              <label for="dest-email">Email</label>
              <input
                id="dest-email"
                v-model="annonce.destinataire.email"
                type="email"
                placeholder="Ex: nom@example.com"
                :disabled="!canModify"
              >
            </div>
          </div>
        </div>

        <!-- Dates and Times -->
        <div class="form-section">
          <h2><i class="fas fa-calendar-alt"></i> Dates et horaires</h2>
          <p class="form-info">Indiquez quand le colis doit être pris en charge et livré</p>
          <div class="form-group date-group">
            <div>
              <label for="dateDebut">Date et heure de prise en charge *</label>
              <input
                id="dateDebut"
                v-model="annonce.dateDebut"
                type="datetime-local"
                :min="minDate"
                required
                :disabled="!canModify"
              >
              <small>À partir de quand le colis peut être récupéré</small>
            </div>
            <div>
              <label for="dateFin">Date et heure de livraison *</label>
              <input
                id="dateFin"
                v-model="annonce.dateFin"
                type="datetime-local"
                :min="annonce.dateDebut"
                required
                :disabled="!canModify"
              >
              <small>Date limite avant laquelle le colis doit être livré</small>
            </div>
          </div>
        </div>

        <!-- Price -->
        <div class="form-section">
          <h2><i class="fas fa-euro-sign"></i> Prix</h2>
          <div class="form-group price-group">
            <label for="prix">Prix proposé pour la livraison (€) *</label>
            <div class="price-input-container">
              <input
                id="prix"
                v-model="annonce.prixUnitaire"
                type="number"
                min="0.01"
                step="0.01"
                placeholder="25.00"
                required
                :disabled="!canModify"
              >
              <span class="euro-symbol">€</span>
            </div>
            <small>Montant que vous êtes prêt à payer pour cette livraison</small>
          </div>
        </div>

        <div class="form-actions">
          <button
            v-if="canModify"
            type="submit"
            class="btn-primary"
            :disabled="!isModified || isSaving"
          >
            <i v-if="isSaving" class="fas fa-spinner fa-spin"></i>
            <i v-else class="fas fa-save"></i>
            {{ isSaving ? 'Sauvegarde...' : 'Sauvegarder les modifications' }}
          </button>

          <button
            v-if="canModify"
            type="button"
            class="btn-danger"
            @click="cancelAnnonce"
          >
            <i class="fas fa-times"></i>
            Annuler cette annonce
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.edit-annonce-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.edit-annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e5e7eb;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-content h1 {
  color: #2563eb;
  margin: 0;
  font-size: 1.875rem;
  font-weight: 700;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-success { background: #dcfce7; color: #166534; }
.badge-warning { background: #fef3c7; color: #92400e; }
.badge-info { background: #dbeafe; color: #1e40af; }
.badge-danger { background: #fecaca; color: #dc2626; }
.badge-secondary { background: #f3f4f6; color: #374151; }

.btn-back {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #f3f4f6;
  color: #374151;
  text-decoration: none;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.btn-back:hover {
  background: #e5e7eb;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.loading-spinner {
  text-align: center;
  color: #6b7280;
}

.loading-spinner i {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.success-message, .error-message, .warning-message {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.success-message {
  background: #dcfce7;
  color: #166534;
}

.error-message {
  background: #fef2f2;
  color: #dc2626;
}

.warning-message {
  background: #fef3c7;
  color: #92400e;
}

.success-message i, .error-message i, .warning-message i {
  font-size: 1.25rem;
}

.annonce-form-container {
  margin-top: 1rem;
}

.form-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafafa;
}

.form-section h2 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.25rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-section h3 {
  color: #374151;
  margin: 1.5rem 0 1rem 0;
  font-size: 1.125rem;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #374151;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group input:disabled,
.form-group textarea:disabled,
.form-group select:disabled {
  background: #f9fafb;
  color: #6b7280;
  cursor: not-allowed;
}

.form-group small {
  display: block;
  margin-top: 0.25rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.dimensions-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.dimensions-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.contact-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.checkbox-group {
  display: flex;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: 500;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.date-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.price-group .price-input-container {
  position: relative;
  display: inline-block;
  width: 200px;
}

.price-input-container input {
  padding-right: 2rem;
}

.euro-symbol {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
  font-weight: 600;
}

.form-info {
  background: #f0f9ff;
  padding: 1rem;
  border-radius: 8px;
  color: #0c4a6e;
  margin-bottom: 1rem;
  border-left: 4px solid #0ea5e9;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-start;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.btn-primary, .btn-danger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.btn-danger {
  background: #dc2626;
  color: white;
}

.btn-danger:hover {
  background: #b91c1c;
  transform: translateY(-1px);
}

.fa-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive Design */
@media (max-width: 768px) {
  .edit-annonce-container {
    padding: 1rem;
    margin: 1rem;
  }

  .edit-annonce-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .header-content {
    flex-direction: column;
    gap: 0.5rem;
  }

  .header-content h1 {
    font-size: 1.5rem;
  }

  .dimensions-group,
  .contact-form,
  .date-group {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-primary,
  .btn-danger {
    width: 100%;
    justify-content: center;
  }
}

/* Dark theme support */
@media (prefers-color-scheme: dark) {
  .edit-annonce-container {
    background: #1f2937;
    color: #f9fafb;
  }

  .edit-annonce-header {
    border-bottom-color: #374151;
  }

  .header-content h1 {
    color: #60a5fa;
  }

  .btn-back {
    background: #374151;
    color: #d1d5db;
  }

  .btn-back:hover {
    background: #4b5563;
  }

  .form-section {
    background: #374151;
    border-color: #4b5563;
  }

  .form-section h2,
  .form-section h3 {
    color: #d1d5db;
    border-bottom-color: #4b5563;
  }

  .form-group input,
  .form-group textarea,
  .form-group select {
    background: #4b5563;
    border-color: #6b7280;
    color: #f9fafb;
  }

  .form-group input:focus,
  .form-group textarea:focus,
  .form-group select:focus {
    border-color: #60a5fa;
    box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1);
  }

  .form-group input:disabled,
  .form-group textarea:disabled,
  .form-group select:disabled {
    background: #374151;
    color: #9ca3af;
  }

  .form-info {
    background: #1e3a8a;
    color: #bfdbfe;
    border-left-color: #3b82f6;
  }

  .form-actions {
    border-top-color: #4b5563;
  }
}
</style>
