<script>
export default {
  name: 'AnnonceEditView',
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
        statut: ''
      },
      originalAnnonce: null,
      typeOptions: [
        { value: 'unique', label: 'Unique (livraison ponctuelle)' },
        { value: 'multiple', label: 'Récurrente (service régulier)' }
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
      if (!this.originalAnnonce)
        return false;
      return this.annonce.titre !== this.originalAnnonce.titre ||
        this.annonce.description !== this.originalAnnonce.description ||
        parseFloat(this.annonce.prixUnitaire) !== parseFloat(this.originalAnnonce.prixUnitaire) ||
        this.annonce.adresseDepart !== this.originalAnnonce.adresseDepart ||
        this.annonce.adresseFin !== this.originalAnnonce.adresseFin ||
        this.annonce.dateDebut !== this.originalAnnonce.dateDebut ||
        this.annonce.dateFin !== this.originalAnnonce.dateFin ||
        this.annonce.typeAnnonce !== this.originalAnnonce.typeAnnonce;
    }
  },
  methods: {
    formatDateForInput(dateString) {
      if (!dateString)
        return '';
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

        if (!annonceId) {
          throw new Error('ID d\'annonce non spécifié');
        }
        const token = localStorage.getItem('token');
        if (!token) {
          this.$router.push('/login');
          return;
        }
        const response = await fetch(`/api/annonces/${annonceId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (!response.ok) {
          throw new Error('Erreur lors de la récupération de l\'annonce');
        }
        const annonceData = await response.json();
        if (annonceData.expediteur && this.user &&
            annonceData.expediteur.idUtilisateur !== this.user.idUtilisateur) {
          this.$router.push('/client/annonces');
          throw new Error('Vous n\'êtes pas autorisé à modifier cette annonce');
        }
        if (annonceData.dateDebut) {
          annonceData.dateDebut = this.formatDateForInput(annonceData.dateDebut);
        }
        if (annonceData.dateFin) {
          annonceData.dateFin = this.formatDateForInput(annonceData.dateFin);
        }

        // Mettre à jour l'annonce
        this.annonce = { ...annonceData };
        this.originalAnnonce = { ...annonceData };
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
        console.error('Erreur:', err);
      } finally {
        this.isLoading = false;
      }
    },

    async handleSubmit() {
      if (!this.isModified) {
        this.$router.push('/client/annonces');
        return;
      }
      this.isSaving = true;
      this.error = null;
      this.success = false;

      try {
        if (!this.annonce.titre || !this.annonce.description ||
            !this.annonce.prixUnitaire || !this.annonce.adresseDepart ||
            !this.annonce.adresseFin || !this.annonce.dateDebut ||
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
        if (!token) {
          this.$router.push('/login');
          return;
        }
        const response = await fetch(`/api/annonces/${this.annonce.idAnnonce}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(this.annonce)
        });
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(errorData || 'Erreur lors de la mise à jour de l\'annonce');
        }
        const result = await response.json();
        this.success = true;
        setTimeout(() => {
          this.$router.push('/client/annonces');
        }, 2000);
      } catch (err) {
        this.error = err.message || 'Une erreur est survenue';
        console.error('Erreur:', err);
      } finally {
        this.isSaving = false;
      }
    }
  },
  mounted() {
    const now = new Date();
    this.minDate = this.formatDateForInput(now.toISOString());

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
      <h1>Modifier l'annonce</h1>
      <router-link to="/client/annonces" class="btn-back">
        <i class="fas fa-arrow-left"></i> Retour aux annonces
      </router-link>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin"></i>
      <p>Chargement de l'annonce...</p>
    </div>

    <div v-else-if="success" class="success-message">
      <i class="fas fa-check-circle"></i>
      <p>Votre annonce a été mise à jour avec succès!</p>
    </div>

    <div v-else-if="!canModify" class="error-message">
      <i class="fas fa-exclamation-triangle"></i>
      <p>Cette annonce ne peut plus être modifiée car son statut est "{{ annonce.statut }}".</p>
      <router-link to="/client/annonces" class="btn-primary mt-3">
        Retour à la liste
      </router-link>
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
            :disabled="isSaving"
          >
            Annuler
          </button>
          <button
            type="submit"
            class="btn-submit"
            :disabled="isSaving || !isModified"
          >
            <i v-if="isSaving" class="fas fa-spinner fa-spin"></i>
            <span v-else>Enregistrer les modifications</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.edit-annonce-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.edit-annonce-header {
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

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.loading i {
  font-size: 2rem;
  color: #4CAF50;
  margin-bottom: 1rem;
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

.btn-submit:disabled {
  background-color: #a5d6a7;
}

.mt-3 {
  margin-top: 1rem;
}

.btn-primary {
  display: inline-block;
  background-color: #4CAF50;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: #45a049;
}

@media (max-width: 768px) {
  .date-group {
    grid-template-columns: 1fr;
  }

  .edit-annonce-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>