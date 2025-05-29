<script>
import { authStore } from '@/store/auth'
import apiServices from '@/services/apiServices'

export default {
  name: 'LivreurProfileView',
  data() {
    return {
      isEditing: false,
      isSaving: false,
      editedUser: null,
      message: null,
      documents: [],
      isUploading: false,
      numeroPermis: '',
      showUploadModal: false
    }
  },
  computed: {
    user() {
      return authStore.user
    },
    userInitials() {
      if (!this.user)
        return '';
      return `${this.user.prenom?.charAt(0) || ''}${this.user.nom?.charAt(0) || ''}`.toUpperCase();
    },
    formatAddress() {
      const parts = [];
      if (this.user.ville)
        parts.push(this.user.ville);
      if (this.user.codePostal)
        parts.push(this.user.codePostal);
      if (this.user.pays)
        parts.push(this.user.pays);
      return parts.length > 0 ? parts.join(', ') : 'Adresse non renseignée';
    }
  },
  methods: {
    formatDate(date) {
      if (!date)
        return 'Non renseignée'
      return new Date(date).toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },
    startEditing() {
      this.editedUser = {
        ...this.user,
        dateDeNaissance: this.user.dateDeNaissance ? this.user.dateDeNaissance.split('T')[0] : ''
      }
      this.isEditing = true
    },
    cancelEditing() {
      this.isEditing = false
      this.editedUser = null
      this.message = null
    },
    async saveChanges() {
      try {
        this.isSaving = true
        this.message = null

        const response = await apiServices.put(`utilisateurs/${this.user.idUtilisateur}`, this.editedUser)

        if (!response.ok) {
          const error = await response.json()
          throw new Error(error.message || 'Erreur lors de la mise à jour du profil')
        }

        const updatedUser = await response.json()
        authStore.updateUser(updatedUser)

        this.message = {
          type: 'success',
          text: 'Profil mis à jour avec succès'
        }
        this.isEditing = false
      } catch (error) {
        this.message = {
          type: 'error',
          text: error.message
        }
      } finally {
        this.isSaving = false
      }
    },
    async loadDocuments() {
      try {
        const response = await apiServices.get(`documents/${this.user.idUtilisateur}`);
        if (response.ok) {
          this.documents = await response.json();
        }
      } catch (error) {
        // Erreur silencieuse pour le chargement des documents
      }
    },
    hasDocument(type) {
      return this.documents.some(doc => doc.typeJustificatif === type);
    },
    hasValidDocument(type) {
      return this.documents.some(doc =>
        doc.typeJustificatif === type && doc.validationParAd === true
      );
    },
    getDocumentStatusClass(type) {
      const doc = this.documents.find(d => d.typeJustificatif === type);
      if (!doc)
        return 'status-missing';
      if (doc.validationParAd)
        return 'status-approved';
      return doc.commentaire ? 'status-rejected' : 'status-pending';
    },
    getDocumentStatusText(type) {
      const doc = this.documents.find(d => d.typeJustificatif === type);
      if (!doc)
        return 'Non fourni';
      if (doc.validationParAd)
        return 'Validé';
      return doc.commentaire ? 'Rejeté' : 'En attente';
    },
    getDocumentComment(type) {
      const doc = this.documents.find(d => d.typeJustificatif === type);
      return doc?.commentaire;
    },
    triggerFileInput(type) {
      this.$refs[`${type}Input`].click();
    },
    async handleFileUpload(event, type) {
      const file = event.target.files[0];
      if (!file) return;

      if (file.type !== 'application/pdf') {
        this.message = {
          type: 'error',
          text: 'Seuls les fichiers PDF sont acceptés'
        };
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.message = {
          type: 'error',
          text: 'La taille du fichier ne doit pas dépasser 5MB'
        };
        return;
      }

      try {
        this.isUploading = true;
        const formData = new FormData();
        formData.append('file', file);
        formData.append('type', type);
        formData.append('userId', this.user.idUtilisateur);

        if (type === 'PERMIS' && this.numeroPermis) {
          formData.append('numeroPermis', this.numeroPermis);
        }

        const response = await apiServices.postFormData('documents/upload', formData);

        if (!response.ok) throw new Error('Erreur lors de l\'upload du document');

        this.message = {
          type: 'success',
          text: 'Document envoyé avec succès'
        };
        await this.loadDocuments();
      } catch (error) {
        this.message = {
          type: 'error',
          text: error.message
        };
      } finally {
        this.isUploading = false;
        event.target.value = '';
      }
    }
  },
  async mounted() {
    await this.loadDocuments();
    if (this.user.numeroPermis) {
      this.numeroPermis = this.user.numeroPermis;
    }
  }
}
</script>

<template>
  <div class="profile-container">
    <div v-if="message" :class="['alert', message.type]" :key="message.text">
      {{ message.text }}
    </div>

    <div class="profile-grid">
      <!-- Colonne de gauche -->
      <div class="profile-left-column">
        <div class="avatar-section">
          <div class="avatar">
            <span v-if="!user.photoUrl">{{ userInitials }}</span>
            <img v-else :src="user.photoUrl" :alt="user.nom" />
          </div>
          <h2 class="user-name">{{ user.prenom }} {{ user.nom }}</h2>
          <p class="user-type">Livreur</p>
        </div>

        <div class="quick-info">
          <div class="info-item">
            <i class="fas fa-envelope"></i>
            <span>{{ user.email }}</span>
          </div>
          <div class="info-item">
            <i class="fas fa-phone"></i>
            <span>{{ user.telephone || 'Non renseigné' }}</span>
          </div>
          <div class="info-item">
            <i class="fas fa-map-marker-alt"></i>
            <span>{{ formatAddress }}</span>
          </div>
        </div>

        <button v-if="!isEditing" @click="startEditing" class="edit-button">
          <i class="fas fa-edit"></i> Modifier
        </button>
      </div>

      <!-- Colonne de droite -->
      <div class="profile-right-column">
        <div class="profile-card">
          <div class="card-header">
            <h2>Informations personnelles</h2>
            <div class="header-icon">
              <i class="fas fa-user-circle"></i>
            </div>
          </div>

          <form v-if="isEditing" @submit.prevent="saveChanges" class="edit-form">
            <div class="info-grid">
              <div class="info-group">
                <label>Nom</label>
                <input type="text" v-model="editedUser.nom" required minlength="2" maxlength="100" />
              </div>

              <div class="info-group">
                <label>Prénom</label>
                <input type="text" v-model="editedUser.prenom" required minlength="2" maxlength="100" />
              </div>

              <div class="info-group">
                <label>Email</label>
                <div class="readonly-input-group">
                  <input
                    type="email"
                    v-model="editedUser.email"
                    readonly
                    class="readonly-input"
                  />
                  <i class="fas fa-lock readonly-icon"></i>
                </div>
                <span class="readonly-info">L'adresse email ne peut pas être modifiée pour des raisons de sécurité</span>
              </div>

              <div class="info-group">
                <label>Téléphone</label>
                <input type="tel" v-model="editedUser.telephone" pattern="^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,10}$" />
              </div>

              <div class="info-group">
                <label>Genre</label>
                <select v-model="editedUser.genre">
                  <option :value="true">Homme</option>
                  <option :value="false">Femme</option>
                </select>
              </div>

              <div class="info-group">
                <label>Date de naissance</label>
                <input type="date" v-model="editedUser.dateDeNaissance" required />
              </div>

              <div class="info-group full-width">
                <label>Adresse</label>
                <input type="text" v-model="editedUser.adresse" />
              </div>

              <div class="info-group">
                <label>Ville</label>
                <input type="text" v-model="editedUser.ville" maxlength="100" />
              </div>

              <div class="info-group">
                <label>Code postal</label>
                <input type="text" v-model="editedUser.codePostal" pattern="^[0-9]{5}$" />
              </div>

              <div class="info-group">
                <label>Pays</label>
                <input type="text" v-model="editedUser.pays" maxlength="50" />
              </div>
            </div>

            <div class="form-buttons">
              <button type="submit" class="save-button" :disabled="isSaving">
                <i class="fas fa-save"></i> {{ isSaving ? 'Enregistrement...' : 'Enregistrer' }}
              </button>
              <button type="button" class="cancel-button" @click="cancelEditing" :disabled="isSaving">
                <i class="fas fa-times"></i> Annuler
              </button>
            </div>
          </form>

          <div v-else class="info-grid">
            <div class="info-group">
              <label>Nom</label>
              <p>{{ user.nom }}</p>
            </div>

            <div class="info-group">
              <label>Prénom</label>
              <p>{{ user.prenom }}</p>
            </div>

            <div class="info-group">
              <label>Email</label>
              <p>{{ user.email }}</p>
            </div>

            <div class="info-group">
              <label>Téléphone</label>
              <p>{{ user.telephone || 'Non renseigné' }}</p>
            </div>

            <div class="info-group">
              <label>Genre</label>
              <p>{{ user.genre ? 'Homme' : 'Femme' }}</p>
            </div>

            <div class="info-group">
              <label>Date de naissance</label>
              <p>{{ formatDate(user.dateDeNaissance) }}</p>
            </div>

            <div class="info-group full-width">
              <label>Adresse</label>
              <p>{{ user.adresse || 'Non renseignée' }}</p>
            </div>

            <div class="info-group">
              <label>Ville</label>
              <p>{{ user.ville || 'Non renseignée' }}</p>
            </div>

            <div class="info-group">
              <label>Code postal</label>
              <p>{{ user.codePostal || 'Non renseigné' }}</p>
            </div>

            <div class="info-group">
              <label>Pays</label>
              <p>{{ user.pays || 'Non renseigné' }}</p>
            </div>
          </div>
        </div>

        <div class="profile-card documents-section">
          <div class="card-header">
            <h2>Documents obligatoires</h2>
            <div class="header-icon">
              <i class="fas fa-file-pdf"></i>
            </div>
          </div>

          <div class="documents-grid">

            <div class="document-card">
              <div class="document-header">
                <h3>Carte d'identité</h3>
                <div class="document-status" :class="getDocumentStatusClass('CNI')">
                  {{ getDocumentStatusText('CNI') }}
                </div>
              </div>
              <div class="document-content">
                <i class="fas fa-id-card document-icon"></i>
                <p class="document-info">Format PDF uniquement - Max 5MB</p>
                <input
                  type="file"
                  accept="application/pdf"
                  @change="handleFileUpload($event, 'CNI')"
                  :disabled="isUploading"
                  ref="cniInput"
                  class="file-input"
                />
                <button
                  class="upload-button"
                  @click="triggerFileInput('cni')"
                  :disabled="isUploading || hasValidDocument('CNI')"
                >
                  <i class="fas fa-upload"></i>
                  {{ hasDocument('CNI') ? 'Modifier' : 'Ajouter' }}
                </button>
              </div>
              <div v-if="getDocumentComment('CNI')" class="document-comment">
                <i class="fas fa-comment-alt"></i>
                {{ getDocumentComment('CNI') }}
              </div>
            </div>

            <div class="document-card">
              <div class="document-header">
                <h3>Permis de conduire</h3>
                <div class="document-status" :class="getDocumentStatusClass('PERMIS')">
                  {{ getDocumentStatusText('PERMIS') }}
                </div>
              </div>
              <div class="document-content">
                <i class="fas fa-id-badge document-icon"></i>
                <p class="document-info">Format PDF uniquement - Max 5MB</p>
                <input
                  type="file"
                  accept="application/pdf"
                  @change="handleFileUpload($event, 'PERMIS')"
                  :disabled="isUploading"
                  ref="permisInput"
                  class="file-input"
                />
                <div class="permis-numero">
                  <label>Numéro de permis :</label>
                  <input
                    type="text"
                    v-model="numeroPermis"
                    pattern="^[0-9A-Z]{9}$"
                    placeholder="Ex: 123456789"
                    class="permis-input"
                  />
                </div>
                <button
                  class="upload-button"
                  @click="triggerFileInput('permis')"
                  :disabled="isUploading || hasValidDocument('PERMIS')"
                >
                  <i class="fas fa-upload"></i>
                  {{ hasDocument('PERMIS') ? 'Modifier' : 'Ajouter' }}
                </button>
              </div>
              <div v-if="getDocumentComment('PERMIS')" class="document-comment">
                <i class="fas fa-comment-alt"></i>
                {{ getDocumentComment('PERMIS') }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 2rem;
  margin-top: 2rem;
}

.profile-left-column {
  position: sticky;
  top: 2rem;
  align-self: start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 2rem;
}

.avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50, #45a049);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  cursor: pointer;
  transition: transform 0.3s ease;
  font-size: 3rem;
  color: white;
  overflow: hidden;
}

.avatar:hover {
  transform: scale(1.05);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 1.5rem;
  color: #333;
  margin: 0.5rem 0;
  border: none;
  padding: 0;
}

.user-type {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.quick-info {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 1.5rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item i {
  color: #4CAF50;
  font-size: 1.2rem;
  width: 24px;
}

.edit-button {
  width: 100%;
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: background-color 0.3s;
  font-weight: 500;
}

.edit-button:hover {
  background-color: #45a049;
}

.profile-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 2rem;
}

.documents-section {
  margin-top: 2rem;
}

.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.document-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.document-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.document-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.2rem;
}

.document-status {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.status-missing {
  background-color: #f5f5f5;
  color: #666;
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-approved {
  background-color: #d4edda;
  color: #155724;
}

.status-rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.document-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border: 2px dashed #ddd;
  border-radius: 8px;
  margin: 1rem 0;
}

.document-icon {
  font-size: 3rem;
  color: #4CAF50;
}

.document-info {
  color: #666;
  font-size: 0.9rem;
  text-align: center;
  margin: 0;
}

.file-input {
  display: none;
}

.upload-button {
  width: 100%;
  padding: 0.75rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: background-color 0.3s;
}

.upload-button:hover:not(:disabled) {
  background-color: #45a049;
}

.upload-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.document-comment {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #fff3cd;
  border-radius: 4px;
  color: #856404;
  font-size: 0.9rem;
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
}

.permis-numero {
  width: 100%;
  margin-bottom: 1rem;
}

.permis-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-top: 0.25rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-icon {
  background: #4CAF50;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon i {
  color: white;
  font-size: 1.2rem;
}

h2 {
  color: #333;
  font-size: 1.5rem;
  margin: 0;
  border: none;
  padding: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.info-group {
  display: flex;
  flex-direction: column;
}

.info-group.full-width {
  grid-column: 1 / -1;
}

label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.25rem;
}

p {
  font-size: 1.1rem;
  color: #333;
  margin: 0;
  padding: 0.5rem 0;
}

input, select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  width: 100%;
  transition: all 0.3s ease;
}

input:hover, select:hover {
  border-color: #4CAF50;
}

.readonly-input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.readonly-input {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.readonly-icon {
  position: absolute;
  right: 10px;
  color: #666;
  font-size: 0.9rem;
}

.readonly-info {
  display: block;
  font-size: 0.8rem;
  color: #666;
  margin-top: 0.25rem;
  font-style: italic;
}

input:focus, select:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.form-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  justify-content: flex-end;
}

.save-button, .cancel-button {
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  font-weight: 500;
}

.save-button {
  background-color: #4CAF50;
  color: white;
  border: none;
}

.save-button:hover:not(:disabled) {
  background-color: #45a049;
}

.cancel-button {
  background-color: white;
  color: #666;
  border: 1px solid #ddd;
}

.cancel-button:hover:not(:disabled) {
  background-color: #f5f5f5;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.alert {
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  font-weight: 500;
  animation: slideDown 0.3s ease-out;
}

.alert.success {
  background-color: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.alert.error {
  background-color: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

@keyframes slideDown {
  from {
    transform: translateY(-10px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 1024px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-left-column {
    position: static;
  }

  .quick-info {
    margin-bottom: 2rem;
  }
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .profile-card {
    padding: 1.5rem;
  }

  .form-buttons {
    flex-direction: column;
  }

  .save-button, .cancel-button {
    width: 100%;
    justify-content: center;
  }

  .user-name {
    font-size: 1.3rem;
  }

  .avatar {
    width: 120px;
    height: 120px;
    font-size: 2.5rem;
  }
}
</style>
