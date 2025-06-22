<script>
export default {
  name: 'DemandeServiceFormView',
  data() {
    return {
      currentStep: 1,
      isSubmitting: false,
      confirmSubmission: false,
      budgetNegociable: false,
      steps: [
        { title: 'Catégorie' },
        { title: 'Description' },
        { title: 'Budget' },
        { title: 'Résumé' }
      ],
      categories: [
        {
          code: 'TRANSPORT_LIVRAISON',
          icon: '🚚',
          libelle: 'TRANSPORT & LIVRAISON',
          description: 'Transport de personnes, livraison de colis, déménagement, courses urgentes'
        },
        {
          code: 'SERVICES_DOMICILE',
          icon: '🏠',
          libelle: 'SERVICES À DOMICILE',
          description: 'Ménage, garde d\'enfants/animaux, jardinage, assistance aux personnes âgées'
        },
        {
          code: 'TRAVAUX_REPARATIONS',
          icon: '🔧',
          libelle: 'TRAVAUX & RÉPARATIONS',
          description: 'Bricolage, plomberie, électricité, rénovation, assemblage de meubles'
        },
        {
          code: 'COURSES_ACHATS',
          icon: '🛒',
          libelle: 'COURSES & ACHATS',
          description: 'Courses alimentaires, achats divers, recherche de produits spécifiques'
        },
        {
          code: 'SERVICES_PERSONNELS',
          icon: '👥',
          libelle: 'SERVICES PERSONNELS',
          description: 'Assistance administrative, organisation d\'événements, secrétariat, conciergerie'
        },
        {
          code: 'EDUCATION_FORMATION',
          icon: '🎓',
          libelle: 'ÉDUCATION & FORMATION',
          description: 'Cours particuliers, formation professionnelle, coaching, soutien scolaire'
        }
      ],
      formData: {
        titre: '',
        description: '',
        categorieService: '',
        typeServiceSpecifique: '',
        servicePersonnalise: false,
        adresseDepart: '',
        adresseArrivee: '',
        dateSouhaitee: '',
        creneauHoraire: '',
        budgetMin: null,
        budgetMax: null,
        photosUrls: []
      }
    }
  },
  computed: {
    canProceedToNextStep() {
      switch (this.currentStep) {
        case 1:
          return this.formData.categorieService !== '';
        case 2:
          return this.formData.titre && this.formData.description;
        case 3:
          return true; // Budget est optionnel
        default:
          return false;
      }
    }
  },
  methods: {
    selectCategory(categoryCode) {
      this.formData.categorieService = categoryCode;
    },

    getCategoryLabel(code) {
      const category = this.categories.find(c => c.code === code);
      return category ? category.libelle : '';
    },

    nextStep() {
      if (this.canProceedToNextStep && this.currentStep < 4) {
        this.currentStep++;
      }
    },

    previousStep() {
      if (this.currentStep > 1) {
        this.currentStep--;
      }
    },

    handleFileUpload(event) {
      // Pour l'instant, on simule juste l'upload
      // En production, il faudrait implémenter l'upload vers un service de stockage
      const files = Array.from(event.target.files);
      this.formData.photosUrls = files.map(file => URL.createObjectURL(file));
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    async handleSubmit() {
      if (!this.confirmSubmission) return;

      this.isSubmitting = true;

      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/demandes-services', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(this.formData)
        });

        if (response.ok) {
          this.$router.push('/client/demandes-services');
        } else {
          const error = await response.json();
          alert('Erreur: ' + error.error);
        }
      } catch (error) {
        console.error('Erreur lors de la création de la demande:', error);
        alert('Erreur lors de la création de la demande');
      } finally {
        this.isSubmitting = false;
      }
    }
  }
}
</script>

<template>
  <div class="demande-service-form-container">
    <h1>Nouvelle demande de service</h1>

    <div class="steps-indicator">
      <div
        v-for="(step, index) in steps"
        :key="index"
        class="step"
        :class="{
          'active': currentStep === index + 1,
          'completed': currentStep > index + 1
        }"
      >
        <div class="step-number">{{ index + 1 }}</div>
        <div class="step-title">{{ step.title }}</div>
      </div>
    </div>

    <form @submit.prevent="handleSubmit" class="demande-form">

      <div v-if="currentStep === 1" class="step-content">
        <h2>Étape 1: Choisissez votre catégorie de service</h2>

        <div class="categories-grid">
          <div
            v-for="category in categories"
            :key="category.code"
            class="category-card"
            :class="{ 'selected': formData.categorieService === category.code }"
            @click="selectCategory(category.code)"
          >
            <div class="category-icon">{{ category.icon }}</div>
            <h3>{{ category.libelle }}</h3>
            <p>{{ category.description }}</p>
          </div>
        </div>

        <div v-if="formData.categorieService" class="service-type-selection">
          <h3>Type de service spécifique</h3>
          <div class="form-group">
            <input
              type="text"
              v-model="formData.typeServiceSpecifique"
              placeholder="Décrivez le type de service précis (ex: Montage de meuble IKEA)"
              class="form-input"
            >
          </div>
          <div class="checkbox-group">
            <input
              type="checkbox"
              id="servicePersonnalise"
              v-model="formData.servicePersonnalise"
            >
            <label for="servicePersonnalise">Service personnalisé (non standard)</label>
          </div>
        </div>
      </div>

      <div v-if="currentStep === 2" class="step-content">
        <h2>Étape 2: Décrivez votre besoin</h2>

        <div class="form-group">
          <label for="titre">Titre de votre demande *</label>
          <input
            type="text"
            id="titre"
            v-model="formData.titre"
            placeholder="Ex: Montage d'un canapé d'angle"
            class="form-input"
            required
          >
        </div>

        <div class="form-group">
          <label for="description">Description détaillée *</label>
          <textarea
            id="description"
            v-model="formData.description"
            placeholder="Décrivez précisément ce que vous attendez du prestataire..."
            class="form-textarea"
            rows="5"
            required
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="adresseDepart">Adresse de départ</label>
            <input
              type="text"
              id="adresseDepart"
              v-model="formData.adresseDepart"
              placeholder="Où le prestataire doit-il se rendre ?"
              class="form-input"
            >
          </div>
          <div class="form-group">
            <label for="adresseArrivee">Adresse d'arrivée</label>
            <input
              type="text"
              id="adresseArrivee"
              v-model="formData.adresseArrivee"
              placeholder="Destination finale (si applicable)"
              class="form-input"
            >
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="dateSouhaitee">Date souhaitée</label>
            <input
              type="datetime-local"
              id="dateSouhaitee"
              v-model="formData.dateSouhaitee"
              class="form-input"
            >
          </div>
          <div class="form-group">
            <label for="creneauHoraire">Créneau horaire</label>
            <select v-model="formData.creneauHoraire" class="form-select">
              <option value="">Pas de préférence</option>
              <option value="matin">Matin (8h-12h)</option>
              <option value="apres-midi">Après-midi (12h-18h)</option>
              <option value="soir">Soir (18h-20h)</option>
              <option value="weekend">Weekend uniquement</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>Photos (optionnel)</label>
          <input
            type="file"
            multiple
            accept="image/*"
            @change="handleFileUpload"
            class="form-input"
          >
          <small class="form-hint">Ajoutez des photos pour aider les prestataires à mieux comprendre votre besoin</small>
        </div>
      </div>

      <div v-if="currentStep === 3" class="step-content">
        <h2>Étape 3: Budget (optionnel mais recommandé)</h2>

        <div class="budget-section">
          <p>Indiquer un budget aide les prestataires à mieux calibrer leurs offres</p>

          <div class="form-row">
            <div class="form-group">
              <label for="budgetMin">Budget minimum (€)</label>
              <input
                type="number"
                id="budgetMin"
                v-model="formData.budgetMin"
                placeholder="50"
                min="0"
                step="5"
                class="form-input"
              >
            </div>
            <div class="form-group">
              <label for="budgetMax">Budget maximum (€)</label>
              <input
                type="number"
                id="budgetMax"
                v-model="formData.budgetMax"
                placeholder="150"
                min="0"
                step="5"
                class="form-input"
              >
            </div>
          </div>

          <div class="checkbox-group">
            <input
              type="checkbox"
              id="budgetNegociable"
              v-model="budgetNegociable"
            >
            <label for="budgetNegociable">Budget négociable</label>
          </div>
        </div>
      </div>

      <div v-if="currentStep === 4" class="step-content">
        <h2>Étape 4: Résumé de votre demande</h2>

        <div class="summary-card">
          <h3>{{ formData.titre }}</h3>

          <div class="summary-section">
            <h4>📋 Service demandé</h4>
            <p><strong>Catégorie:</strong> {{ getCategoryLabel(formData.categorieService) }}</p>
            <p v-if="formData.typeServiceSpecifique"><strong>Type:</strong> {{ formData.typeServiceSpecifique }}</p>
            <p v-if="formData.servicePersonnalise" class="custom-service">✨ Service personnalisé</p>
          </div>

          <div class="summary-section">
            <h4>📝 Description</h4>
            <p>{{ formData.description }}</p>
          </div>

          <div v-if="formData.adresseDepart || formData.adresseArrivee" class="summary-section">
            <h4>📍 Adresses</h4>
            <p v-if="formData.adresseDepart"><strong>Départ:</strong> {{ formData.adresseDepart }}</p>
            <p v-if="formData.adresseArrivee"><strong>Arrivée:</strong> {{ formData.adresseArrivee }}</p>
          </div>

          <div v-if="formData.dateSouhaitee || formData.creneauHoraire" class="summary-section">
            <h4>🕒 Timing</h4>
            <p v-if="formData.dateSouhaitee"><strong>Date:</strong> {{ formatDate(formData.dateSouhaitee) }}</p>
            <p v-if="formData.creneauHoraire"><strong>Créneau:</strong> {{ formData.creneauHoraire }}</p>
          </div>

          <div v-if="formData.budgetMin || formData.budgetMax" class="summary-section">
            <h4>💰 Budget</h4>
            <p v-if="formData.budgetMin && formData.budgetMax">
              Entre {{ formData.budgetMin }}€ et {{ formData.budgetMax }}€
            </p>
            <p v-else-if="formData.budgetMin">
              À partir de {{ formData.budgetMin }}€
            </p>
            <p v-else-if="formData.budgetMax">
              Maximum {{ formData.budgetMax }}€
            </p>
          </div>
        </div>

        <div class="confirmation-checkbox">
          <input
            type="checkbox"
            id="confirmSubmission"
            v-model="confirmSubmission"
            required
          >
          <label for="confirmSubmission">
            Je confirme que les informations sont correctes et je souhaite publier cette demande
          </label>
        </div>
      </div>

      <div class="form-navigation">
        <button
          type="button"
          @click="previousStep"
          class="btn btn-secondary"
          v-if="currentStep > 1"
        >
          ← Précédent
        </button>

        <button
          type="button"
          @click="nextStep"
          class="btn btn-primary"
          v-if="currentStep < 4"
          :disabled="!canProceedToNextStep"
        >
          Suivant →
        </button>

        <button
          type="submit"
          class="btn btn-success"
          v-if="currentStep === 4"
          :disabled="isSubmitting || !confirmSubmission"
        >
          {{ isSubmitting ? 'Publication...' : 'Publier la demande' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.demande-service-form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.steps-indicator {
  display: flex;
  justify-content: space-between;
  margin: 2rem 0;
  padding: 0 1rem;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  left: 60%;
  right: -40%;
  height: 2px;
  background-color: var(--border-color);
  z-index: 1;
}

.step.completed:not(:last-child)::after {
  background-color: var(--primary-color);
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--border-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  position: relative;
  z-index: 2;
}

.step.active .step-number {
  background-color: var(--primary-color);
}

.step.completed .step-number {
  background-color: var(--success-color);
}

.step-title {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-align: center;
}

.step.active .step-title {
  color: var(--primary-color);
  font-weight: bold;
}

.demande-form {
  background-color: var(--card-bg);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: var(--shadow);
}

.step-content h2 {
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.category-card {
  padding: 1.5rem;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.category-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.category-card.selected {
  border-color: var(--primary-color);
  background-color: var(--primary-color-light);
}

.category-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.category-card h3 {
  color: var(--text-primary);
  margin: 0.5rem 0;
  font-size: 1rem;
}

.category-card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

.service-type-selection {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: var(--background-color);
  border-radius: 8px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
}

.form-input, .form-textarea, .form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-input:focus, .form-textarea:focus, .form-select:focus {
  outline: none;
  border-color: var(--primary-color);
}

.form-hint {
  color: var(--text-secondary);
  font-size: 0.8rem;
  margin-top: 0.25rem;
  display: block;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 1rem 0;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
}

.budget-section {
  background-color: var(--background-color);
  padding: 1.5rem;
  border-radius: 8px;
}

.summary-card {
  background-color: var(--background-color);
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.summary-card h3 {
  color: var(--primary-color);
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.summary-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.summary-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.summary-section h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.summary-section p {
  color: var(--text-secondary);
  margin: 0.25rem 0;
}

.custom-service {
  color: var(--warning-color) !important;
  font-style: italic;
}

.confirmation-checkbox {
  background-color: var(--warning-color-light);
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid var(--warning-color);
}

.form-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-color-dark);
}

.btn-secondary {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background-color: var(--text-secondary);
}

.btn-success {
  background-color: var(--success-color);
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: var(--success-color-dark);
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .categories-grid {
    grid-template-columns: 1fr;
  }
  
  .steps-indicator {
    flex-direction: column;
    gap: 1rem;
  }
  
  .step::after {
    display: none;
  }
}
</style>
