<script>
export default {
  name: 'AnnonceCreateView',
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
            type: "CLIENT"
          }
        };
        if (annonceData.destinataire) {
          annonceData.destinataire = {
            ...annonceData.destinataire,
            type: "CLIENT"
          };
        }
        const response = await fetch(`/api/annonces?idExpediteur=${this.user.idUtilisateur}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(annonceData)
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la création de l\'annonce');
        }
        const result = await response.json();
        this.success = true;
        setTimeout(() => {
          this.$router.push('/client/annonces');
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
      <h1>Créer une nouvelle annonce</h1>
      <router-link to="/client/annonces" class="btn-back">
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
        <div v-if="currentStep === 1" class="form-step">
          <h2>Étape 1: Informations générales</h2>
          <div class="form-section">
            <div class="form-group">
              <label for="titre">Titre de l'annonce *</label>
              <input
                id="titre"
                v-model="annonce.titre"
                type="text"
                placeholder="Ex: Livraison de produits bio"
                required
              >
              <small>Donnez un titre clair qui décrit votre besoin</small>
            </div>
            <div class="form-group">
              <label for="description">Description *</label>
              <textarea
                id="description"
                v-model="annonce.description"
                placeholder="Décrivez votre annonce en détail"
                rows="4"
                required
              ></textarea>
              <small>Précisez tout détail important pour les livreurs potentiels</small>
            </div>
            <!-- <div class="form-group">
              <label for="type">Type d'annonce *</label>
              <select id="type" v-model="annonce.typeAnnonce" required>
                <option v-for="option in typeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div> -->
          </div>
        </div>

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
                Colis fragile
              </label>
            </div>
            <div class="form-group">
              <label for="colis-description">Description du contenu</label>
              <textarea
                id="colis-description"
                v-model="annonce.colis.description"
                placeholder="Décrivez le contenu du colis"
                rows="2"
              ></textarea>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 3" class="form-step">
          <h2>Étape 3: Adresses et contacts</h2>
          <div class="form-section">
            <h3>Adresse de départ</h3>
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

            <h3>Expéditeur</h3>
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
            <h3>Adresse d'arrivée</h3>
            <div class="form-group">
              <label for="adresseFin">Adresse complète d'arrivée *</label>
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
                >
                <small>Date limite avant laquelle le colis doit être livré</small>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 5" class="form-step">
          <h2>Étape 5: Prix et validation</h2>
          <div class="form-section">
            <div class="form-group price-group">
              <label for="prix">Prix proposé (€) *</label>
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
              <small>Proposez un prix équitable pour votre livraison</small>
            </div>

            <div class="form-group checkbox-group livraison-partielle">
              <label class="checkbox-label">
                <input type="checkbox" v-model="annonce.livraisonPartielleAutorisee">
                Autoriser la livraison partielle
              </label>
              <small class="delivery-info">
                La livraison partielle permet à deux livreurs différents de prendre en charge votre colis :
                <br>• <strong>Segment 1</strong> : De votre adresse de départ vers un entrepôt intermédiaire
                <br>• <strong>Segment 2</strong> : De l'entrepôt vers l'adresse de destination finale
                <br>Cela peut réduire les coûts et améliorer la disponibilité des livreurs.
              </small>
            </div>

            <div class="summary-container">
              <h3>Récapitulatif de l'annonce</h3>
              <div class="summary-item">
                <strong>Titre:</strong> {{ annonce.titre }}
              </div>
              <div class="summary-item">
                <strong>Type:</strong> {{ annonce.typeAnnonce === 'unique' ? 'Livraison unique' : 'Livraison récurrente' }}
              </div>
              <div class="summary-item">
                <strong>Départ:</strong> {{ annonce.adresseDepart }}
              </div>
              <div class="summary-item">
                <strong>Arrivée:</strong> {{ annonce.adresseFin }}
              </div>
              <div class="summary-item">
                <strong>Dimensions:</strong> {{ annonce.colis.longueur }}×{{ annonce.colis.largeur }}×{{ annonce.colis.hauteur }} cm
              </div>
              <div class="summary-item">
                <strong>Poids:</strong> {{ annonce.colis.poids }} kg
              </div>
              <div class="summary-item">
                <strong>Prix:</strong> {{ annonce.prixUnitaire }} €
              </div>
            </div>
          </div>
        </div>

        <div class="form-navigation">
          <button
            type="button"
            v-if="currentStep > 1"
            @click="prevStep"
            class="btn-prev"
          >
            <i class="fas fa-arrow-left"></i> Précédent
          </button>
          <button
            type="submit"
            class="btn-submit"
            :disabled="!isStepValid || (currentStep === totalSteps && isSubmitting)"
          >
            <template v-if="currentStep < totalSteps">
              Suivant <i class="fas fa-arrow-right"></i>
            </template>
            <template v-else>
              <i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i>
              <span v-else>Créer l'annonce</span>
            </template>
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
}

.create-annonce-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  color: #555;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  background-color: #f0f0f0;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #e0e0e0;
}

.btn-back i {
  margin-right: 0.5rem;
}

.progress-container {
  margin-bottom: 2rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
  margin-bottom: 1rem;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #4CAF50;
  transition: width 0.3s ease;
}

.steps-indicator {
  display: flex;
  justify-content: space-between;
  margin: 0 10px;
}

.step-circle {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #e0e0e0;
  color: #555;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  position: relative;
}

.step-circle.active {
  background-color: #4CAF50;
  color: white;
}

.step-circle.current {
  transform: scale(1.1);
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.3);
}

.step-circle.clickable {
  cursor: pointer;
}

.annonce-form-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.error-message, .success-message {
  display: flex;
  align-items: center;
  padding: 1rem;
  margin-bottom: 1.5rem;
  border-radius: 4px;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
}

.success-message {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.error-message i, .success-message i {
  font-size: 1.5rem;
  margin-right: 1rem;
}

.annonce-form {
  padding: 1.5rem;
}

.form-step {
  min-height: 300px;
}

.form-step h2 {
  color: #4CAF50;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.form-section {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
}

.form-section h3 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.1rem;
}

.form-info {
  margin-bottom: 1rem;
  color: #666;
  font-style: italic;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group small {
  display: block;
  margin-top: 0.25rem;
  color: #666;
  font-size: 0.8rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input, 
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus, 
.form-group textarea:focus,
.form-group select:focus {
  border-color: #4CAF50;
  outline: none;
}

.dimensions-container {
  background-color: #f9f9f9;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.dimensions-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-top: 1rem;
}

.checkbox-group {
  margin-top: 1rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-label input {
  width: auto;
  margin-right: 0.5rem;
}

.livraison-partielle {
  background-color: #f0f8ff;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #b3d9ff;
  margin-top: 2rem;
}

.livraison-partielle .checkbox-label {
  font-weight: 600;
  color: #1976d2;
  margin-bottom: 1rem;
}

.delivery-info {
  background-color: #e3f2fd;
  padding: 1rem;
  border-radius: 4px;
  border-left: 4px solid #2196f3;
  margin-top: 1rem;
  line-height: 1.6;
}

.delivery-info strong {
  color: #1976d2;
}

.contact-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.date-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.price-group {
  max-width: 200px;
}

.price-input-container {
  position: relative;
}

.price-input-container input {
  padding-right: 2rem;
}

.euro-symbol {
  position: absolute;
  right: 0.8rem;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
}

.summary-container {
  background-color: #f5f5f5;
  padding: 1.5rem;
  border-radius: 4px;
  margin-top: 2rem;
}

.summary-item {
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px dashed #ddd;
}

.summary-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.form-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.btn-prev, .btn-submit {
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  display: flex;
  align-items: center;
}

.btn-prev {
  background-color: #f5f5f5;
  color: #555;
}

.btn-prev:hover:not(:disabled) {
  background-color: #e0e0e0;
}

.btn-prev i {
  margin-right: 0.5rem;
}

.btn-submit {
  background-color: #4CAF50;
  color: white;
  font-weight: 500;
}

.btn-submit:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background-color: #a5d6a7;
}

.btn-submit i:first-child {
  margin-right: 0.5rem;
}

.btn-submit i:last-child {
  margin-left: 0.5rem;
}

@media (max-width: 768px) {
  .date-group,
  .contact-form,
  .dimensions-group {
    grid-template-columns: 1fr;
  }

  .create-annonce-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .form-navigation {
    flex-direction: column;
    gap: 1rem;
  }

  .btn-prev, .btn-submit {
    width: 100%;
    justify-content: center;
  }
}
</style>
