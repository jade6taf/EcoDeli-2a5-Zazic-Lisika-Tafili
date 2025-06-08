<script>
export default {
  name: 'CommercantAnnonceCreateView',
  data() {
    return {
      currentStep: 1,
      totalSteps: 5,
      annonce: {
        titre: '',
        description: '',
        prixUnitaire: null,
        adresseDepart: '',
        adresseFin: '',
        dateDebut: '',
        dateFin: '',
        typeAnnonce: 'unique',
        colis: {
          poids: null,
          longueur: null,
          largeur: null,
          hauteur: null,
          fragile: false,
          description: ''
        },
        livraisonPartielleAutorisee: false,
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
      typeOptions: [
        { value: 'unique', label: 'Unique (livraison ponctuelle)' },
        { value: 'récurrente', label: 'Récurrente (service régulier)' }
      ],
      minDate: '',
      user: null,
      isSubmitting: false,
      error: null,
      success: false,
      stepValidation: {
        1: false,
        2: false,
        3: false,
        4: false,
        5: false
      }
    }
  },
  computed: {
    isStepValid() {
      switch(this.currentStep) {
        case 1:
          return this.annonce.titre && this.annonce.description && this.annonce.typeAnnonce;
        case 2:
          return this.annonce.colis.poids && this.annonce.colis.longueur && 
                 this.annonce.colis.largeur && this.annonce.colis.hauteur;
        case 3:
          return this.annonce.adresseDepart && this.annonce.adresseFin && 
                 this.annonce.expediteur.nom && this.annonce.expediteur.prenom && 
                 this.annonce.expediteur.telephone && this.annonce.destinataire.nom && 
                 this.annonce.destinataire.prenom && this.annonce.destinataire.telephone;
        case 4:
          return this.annonce.dateDebut && this.annonce.dateFin;
        case 5:
          return this.annonce.prixUnitaire > 0;
        default:
          return false;
      }
    },
    progressPercentage() {
      return ((this.currentStep - 1) / this.totalSteps) * 100;
    }
  },
  methods: {
    formatDateForInput(date) {
      return date.toISOString().slice(0, 16);
    },

    nextStep() {
      if (this.currentStep < this.totalSteps) {
        this.stepValidation[this.currentStep] = true;
        this.currentStep++;
      }
    },

    prevStep() {
      if (this.currentStep > 1) {
        this.currentStep--;
      }
    },

    async handleSubmit() {
      this.isSubmitting = true;
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
        if (dateDebut < new Date()) {
          throw new Error('La date de début ne peut pas être dans le passé');
        }

        this.annonce.prixUnitaire = parseFloat(this.annonce.prixUnitaire);
        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }

        const annonceData = {
          ...this.annonce,
          idExpediteur: this.user.idUtilisateur,
          expediteur: {
            ...this.annonce.expediteur,
            idUtilisateur: this.user.idUtilisateur,
            type: "COMMERCANT"
          }
        };

        if (annonceData.destinataire) {
          annonceData.destinataire = {
            ...annonceData.destinataire,
            type: "CLIENT"
          };
        }

        // Use the merchant-specific endpoint
        const response = await fetch(`/api/commercants/${this.user.idUtilisateur}/annonces`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(annonceData)
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData?.message || 'Erreur lors de la création de l\'annonce');
        }

        const result = await response.json();
        this.success = true;
        setTimeout(() => {
          this.$router.push('/commercant/annonces');
        }, 2000);
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
      } finally {
        this.isSubmitting = false;
      }
    },

    goToStep(step) {
      for (let i = 1; i < step; i++) {
        if (!this.stepValidation[i] && !this.isStepValid && this.currentStep < i) {
          return;
        }
      }
      this.currentStep = step;
    }
  },

  mounted() {
    const now = new Date();
    this.minDate = this.formatDateForInput(now);

    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);

      // Pre-fill merchant information
      this.annonce.expediteur.nom = this.user.nom || '';
      this.annonce.expediteur.prenom = this.user.prenom || '';
      this.annonce.expediteur.email = this.user.email || '';
    } else {
      this.$router.push('/login');
    }
  }
}
</script>

<template>
  <div class="create-annonce-container">
    <div class="create-annonce-header">
      <h1>Créer une nouvelle annonce de livraison</h1>
      <router-link to="/commercant/annonces" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour aux annonces
      </router-link>
    </div>

    <div v-if="success" class="success-message">
      <i class="fas fa-check-circle"></i>
      <p>Votre annonce a été créée avec succès!</p>
    </div>

    <div v-else class="annonce-form-container">
      <div v-if="error" class="error-message">
        <i class="fas fa-exclamation-circle"></i>
        <p>{{ error }}</p>
      </div>

      <div class="progress-container">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
        </div>
        <div class="steps-indicator">
          <div
            v-for="step in totalSteps"
            :key="step"
            class="step-circle"
            :class="{
              'active': currentStep >= step,
              'current': currentStep === step,
              'clickable': stepValidation[step - 1] || step === 1
            }"
            @click="goToStep(step)"
          >
            {{ step }}
          </div>
        </div>
      </div>

      <form @submit.prevent="currentStep === totalSteps ? handleSubmit() : nextStep()" class="annonce-form">
        <!-- Step 1: General Information -->
        <div v-if="currentStep === 1" class="form-step">
          <h2>Étape 1: Informations générales</h2>
          <div class="form-section">
            <div class="form-group">
              <label for="titre">Titre de l'annonce *</label>
              <input
                id="titre"
                v-model="annonce.titre"
                type="text"
                placeholder="Ex: Livraison de produits de ma boutique"
                required
              >
              <small>Donnez un titre clair qui décrit votre besoin de livraison</small>
            </div>
            <div class="form-group">
              <label for="description">Description *</label>
              <textarea
                id="description"
                v-model="annonce.description"
                placeholder="Décrivez en détail ce qui doit être livré et toute instruction spéciale"
                rows="4"
                required
              ></textarea>
              <small>Précisez le type de produits, les instructions spéciales, etc.</small>
            </div>
          </div>
        </div>

        <!-- Step 2: Package Details -->
        <div v-if="currentStep === 2" class="form-step">
          <h2>Étape 2: Détails du colis</h2>
          <div class="form-section">
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
                  >
                </div>
              </div>
            </div>
            <div class="form-group checkbox-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="annonce.colis.fragile">
                Colis fragile (nécessite une attention particulière)
              </label>
            </div>
            <div class="form-group">
              <label for="colis-description">Description du contenu</label>
              <textarea
                id="colis-description"
                v-model="annonce.colis.description"
                placeholder="Décrivez le contenu du colis (produits alimentaires, vêtements, etc.)"
                rows="2"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Step 3: Addresses and Contacts -->
        <div v-if="currentStep === 3" class="form-step">
          <h2>Étape 3: Adresses et contacts</h2>
          <div class="form-section">
            <h3>Adresse de départ (votre boutique)</h3>
            <div class="form-group">
              <label for="adresseDepart">Adresse complète de départ *</label>
              <input
                id="adresseDepart"
                v-model="annonce.adresseDepart"
                type="text"
                placeholder="Numéro, rue, code postal, ville"
                required
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
                >
              </div>
              <div class="form-group">
                <label for="exp-prenom">Prénom *</label>
                <input
                  id="exp-prenom"
                  v-model="annonce.expediteur.prenom"
                  type="text"
                  required
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
                >
              </div>
              <div class="form-group">
                <label for="exp-email">Email</label>
                <input
                  id="exp-email"
                  v-model="annonce.expediteur.email"
                  type="email"
                  placeholder="Ex: nom@example.com"
                >
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>Adresse de livraison</h3>
            <div class="form-group">
              <label for="adresseFin">Adresse complète de livraison *</label>
              <input
                id="adresseFin"
                v-model="annonce.adresseFin"
                type="text"
                placeholder="Numéro, rue, code postal, ville"
                required
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
                >
              </div>
              <div class="form-group">
                <label for="dest-prenom">Prénom *</label>
                <input
                  id="dest-prenom"
                  v-model="annonce.destinataire.prenom"
                  type="text"
                  required
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
                >
              </div>
              <div class="form-group">
                <label for="dest-email">Email</label>
                <input
                  id="dest-email"
                  v-model="annonce.destinataire.email"
                  type="email"
                  placeholder="Ex: nom@example.com"
                >
              </div>
            </div>
          </div>
        </div>

        <!-- Step 4: Dates and Times -->
        <div v-if="currentStep === 4" class="form-step">
          <h2>Étape 4: Dates et horaires</h2>
          <div class="form-section">
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
                >
                <small>À partir de quand le colis peut être récupéré dans votre boutique</small>
              </div>
              <div>
                <label for="dateFin">Date et heure de livraison *</label>
                <input
                  id="dateFin"
                  v-model="annonce.dateFin"
                  type="datetime-local"
                  :min="annonce.dateDebut"
                  required
                >
                <small>Date limite avant laquelle le colis doit être livré</small>
              </div>
            </div>
          </div>
        </div>

        <!-- Step 5: Price and Validation -->
        <div v-if="currentStep === 5" class="form-step">
          <h2>Étape 5: Prix et validation</h2>
          <div class="form-section">
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
                >
                <span class="euro-symbol">€</span>
              </div>
              <small>Montant que vous êtes prêt à payer pour cette livraison</small>
            </div>

            <div class="form-group checkbox-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="annonce.livraisonPartielleAutorisee">
                Autoriser la livraison partielle si nécessaire
              </label>
            </div>

            <div class="summary-section">
              <h3>Récapitulatif de votre annonce</h3>
              <div class="summary-content">
                <p><strong>Titre:</strong> {{ annonce.titre }}</p>
                <p><strong>Description:</strong> {{ annonce.description }}</p>
                <p><strong>Poids:</strong> {{ annonce.colis.poids }} kg</p>
                <p><strong>Dimensions:</strong> {{ annonce.colis.longueur }} × {{ annonce.colis.largeur }} × {{ annonce.colis.hauteur }} cm</p>
                <p><strong>De:</strong> {{ annonce.adresseDepart }}</p>
                <p><strong>Vers:</strong> {{ annonce.adresseFin }}</p>
                <p><strong>Prise en charge:</strong> {{ new Date(annonce.dateDebut).toLocaleString('fr-FR') }}</p>
                <p><strong>Livraison avant:</strong> {{ new Date(annonce.dateFin).toLocaleString('fr-FR') }}</p>
                <p><strong>Prix:</strong> {{ annonce.prixUnitaire }}€</p>
              </div>
            </div>
          </div>
        </div>

        <div class="form-navigation">
          <button
            type="button"
            class="btn-secondary"
            @click="prevStep"
            v-if="currentStep > 1"
            :disabled="isSubmitting"
          >
            <i class="fas fa-arrow-left"></i> Précédent
          </button>

          <button
            type="submit"
            class="btn-primary"
            :disabled="!isStepValid || isSubmitting"
          >
            <i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i>
            <span v-if="currentStep < totalSteps">
              Suivant <i class="fas fa-arrow-right"></i>
            </span>
            <span v-else>
              {{ isSubmitting ? 'Création...' : 'Créer l\'annonce' }}
            </span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.create-annonce-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.create-annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e5e7eb;
}

.create-annonce-header h1 {
  color: #2563eb;
  margin: 0;
  font-size: 1.875rem;
  font-weight: 700;
}

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

.success-message {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #dcfce7;
  color: #166534;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.success-message i {
  font-size: 1.5rem;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #fef2f2;
  color: #dc2626;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.error-message i {
  font-size: 1.25rem;
}

.progress-container {
  margin-bottom: 2rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 1rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  transition: width 0.3s ease;
}

.steps-indicator {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.step-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  background: #e5e7eb;
  color: #6b7280;
  transition: all 0.3s ease;
}

.step-circle.active {
  background: #2563eb;
  color: white;
}

.step-circle.current {
  background: #3b82f6;
  color: white;
  transform: scale(1.1);
}

.step-circle.clickable {
  cursor: pointer;
}

.step-circle.clickable:hover {
  transform: scale(1.05);
}

.annonce-form {
  margin-top: 2rem;
}

.form-step {
  min-height: 400px;
}

.form-step h2 {
  color: #1f2937;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  font-weight: 600;
}

.form-section {
  margin-bottom: 2rem;
}

.form-section h3 {
  color: #374151;
  margin-bottom: 1rem;
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

.summary-section {
  background: #f9fafb;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.summary-content p {
  margin: 0.5rem 0;
  color: #374151;
}

.summary-content strong {
  color: #1f2937;
}

.form-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.btn-primary,
.btn-secondary {
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

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
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
  .create-annonce-container {
    padding: 1rem;
    margin: 1rem;
  }

  .create-annonce-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .create-annonce-header h1 {
    font-size: 1.5rem;
  }

  .steps-indicator {
    gap: 0.5rem;
  }

  .step-circle {
    width: 35px;
    height: 35px;
    font-size: 0.875rem;
  }

  .dimensions-group,
  .contact-form,
  .date-group {
    grid-template-columns: 1fr;
  }

  .form-navigation {
    flex-direction: column;
    gap: 1rem;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
    justify-content: center;
  }
}

/* Dark theme support */
@media (prefers-color-scheme: dark) {
  .create-annonce-container {
    background: #1f2937;
    color: #f9fafb;
  }

  .create-annonce-header {
    border-bottom-color: #374151;
  }

  .create-annonce-header h1 {
    color: #60a5fa;
  }

  .btn-back {
    background: #374151;
    color: #d1d5db;
  }

  .btn-back:hover {
    background: #4b5563;
  }

  .form-group input,
  .form-group textarea,
  .form-group select {
    background: #374151;
    border-color: #4b5563;
    color: #f9fafb;
  }

  .form-group input:focus,
  .form-group textarea:focus,
  .form-group select:focus {
    border-color: #60a5fa;
    box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.1);
  }

  .form-section h3 {
    color: #d1d5db;
    border-bottom-color: #4b5563;
  }

  .form-info {
    background: #1e3a8a;
    color: #bfdbfe;
    border-left-color: #3b82f6;
  }

  .summary-section {
    background: #374151;
    border-color: #4b5563;
  }

  .summary-content p,
  .summary-content strong {
    color: #f9fafb;
  }

  .form-navigation {
    border-top-color: #4b5563;
  }

  .btn-secondary {
    background: #4b5563;
    color: #d1d5db;
  }

  .btn-secondary:hover:not(:disabled) {
    background: #6b7280;
  }
}
</style>
