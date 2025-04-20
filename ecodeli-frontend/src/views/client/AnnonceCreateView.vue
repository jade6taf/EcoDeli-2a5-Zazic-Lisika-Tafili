<script>
export default {
  name: 'AnnonceCreateView',
  data() {
    return {
      annonce: {
        titre: '',
        description: '',
        prixUnitaire: null,
        adresseDepart: '',
        adresseFin: '',
        dateDebut: '',
        dateFin: '',
        typeAnnonce: 'unique'
      },
      typeOptions: [
        { value: 'unique', label: 'Unique (livraison ponctuelle)' },
        { value: 'récurrente', label: 'Récurrente (service régulier)' }
      ],
      minDate: '',
      user: null,
      isSubmitting: false,
      error: null,
      success: false
    }
  },
  methods: {
    formatDateForInput(date) {
      // Format date as YYYY-MM-DDThh:mm
      return date.toISOString().slice(0, 16);
    },
    
    async handleSubmit() {
      this.isSubmitting = true;
      this.error = null;
      this.success = false;
      
      try {
        // Validation des champs
        if (!this.annonce.titre || !this.annonce.description || 
            !this.annonce.prixUnitaire || !this.annonce.adresseDepart || 
            !this.annonce.adresseFin || !this.annonce.dateDebut || 
            !this.annonce.dateFin) {
          throw new Error('Veuillez remplir tous les champs obligatoires');
        }
        
        // Validation des dates
        const dateDebut = new Date(this.annonce.dateDebut);
        const dateFin = new Date(this.annonce.dateFin);
        
        if (dateDebut >= dateFin) {
          throw new Error('La date de début doit être antérieure à la date de fin');
        }
        
        if (dateDebut < new Date()) {
          throw new Error('La date de début ne peut pas être dans le passé');
        }
        
        // Conversion du prix en nombre
        this.annonce.prixUnitaire = parseFloat(this.annonce.prixUnitaire);
        
        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }
        
        const response = await fetch(`/api/annonces?idExpediteur=${this.user.idUtilisateur}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(this.annonce)
        });
        
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la création de l\'annonce');
        }
        
        const result = await response.json();
        this.success = true;
        
        // Redirection après un court délai
        setTimeout(() => {
          this.$router.push('/client/annonces');
        }, 2000);
        
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
        console.error('Erreur:', err);
      } finally {
        this.isSubmitting = false;
      }
    }
  },
  mounted() {
    // Formatage de la date minimale (aujourd'hui)
    const now = new Date();
    this.minDate = this.formatDateForInput(now);
    
    // Récupération de l'utilisateur connecté
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
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
      
      <form @submit.prevent="handleSubmit" class="annonce-form">
        <div class="form-section">
          <h3>Informations générales</h3>
          
          <div class="form-group">
            <label for="titre">Titre de l'annonce *</label>
            <input 
              id="titre" 
              v-model="annonce.titre" 
              type="text" 
              placeholder="Ex: Livraison de produits bio"
              required
            >
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
          </div>
          
          <div class="form-group">
            <label for="type">Type d'annonce *</label>
            <select id="type" v-model="annonce.typeAnnonce" required>
              <option v-for="option in typeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label for="prix">Prix (€) *</label>
            <input 
              id="prix" 
              v-model="annonce.prixUnitaire" 
              type="number" 
              min="0.01" 
              step="0.01"
              placeholder="25.00"
              required
            >
          </div>
        </div>
        
        <div class="form-section">
          <h3>Adresses</h3>
          
          <div class="form-group">
            <label for="adresseDepart">Adresse de départ *</label>
            <input 
              id="adresseDepart" 
              v-model="annonce.adresseDepart" 
              type="text" 
              placeholder="Adresse complète"
              required
            >
          </div>
          
          <div class="form-group">
            <label for="adresseFin">Adresse d'arrivée *</label>
            <input 
              id="adresseFin" 
              v-model="annonce.adresseFin" 
              type="text" 
              placeholder="Adresse complète"
              required
            >
          </div>
        </div>
        
        <div class="form-section">
          <h3>Dates</h3>
          
          <div class="form-group date-group">
            <div>
              <label for="dateDebut">Date et heure de début *</label>
              <input 
                id="dateDebut" 
                v-model="annonce.dateDebut" 
                type="datetime-local"
                :min="minDate"
                required
              >
            </div>
            
            <div>
              <label for="dateFin">Date et heure de fin *</label>
              <input 
                id="dateFin" 
                v-model="annonce.dateFin" 
                type="datetime-local"
                :min="annonce.dateDebut"
                required
              >
            </div>
          </div>
        </div>
        
        <div class="form-actions">
          <button 
            type="button" 
            @click="$router.push('/client/annonces')" 
            class="btn-cancel"
            :disabled="isSubmitting"
          >
            Annuler
          </button>
          <button 
            type="submit" 
            class="btn-submit"
            :disabled="isSubmitting"
          >
            <i v-if="isSubmitting" class="fas fa-spinner fa-spin"></i>
            <span v-else>Créer l'annonce</span>
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

.form-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
}

.form-section h3 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.2rem;
}

.form-group {
  margin-bottom: 1.5rem;
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

.date-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-cancel, .btn-submit {
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #555;
}

.btn-cancel:hover:not(:disabled) {
  background-color: #e0e0e0;
}

.btn-submit {
  background-color: #4CAF50;
  color: white;
  font-weight: 500;
}

.btn-submit:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-cancel:disabled, .btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .date-group {
    grid-template-columns: 1fr;
  }
  
  .create-annonce-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>