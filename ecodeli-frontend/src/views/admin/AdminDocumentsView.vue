<script>
import apiServices from '@/services/apiServices'

export default {
  name: 'AdminDocumentsView',
  data() {
    return {
      documents: [],
      filterType: 'pending',
      documentType: 'all',
      showRejectModal: false,
      selectedDocument: null,
      rejectComment: '',
      isLoading: false,
      message: null
    }
  },
  computed: {
    filteredDocuments() {
      if (this.documentType === 'all') return this.documents;
      return this.documents.filter(doc => doc.typeJustificatif === this.documentType);
    }
  },
  methods: {
    async loadDocuments() {
      try {
        this.isLoading = true;
        const response = await apiServices.get(`admin/documents/${this.filterType}`);
        if (!response.ok) throw new Error('Erreur lors du chargement des documents');
        this.documents = await response.json();
      } catch (error) {
        this.showError(error.message);
      } finally {
        this.isLoading = false;
      }
    },
    async approveDocument(id) {
      try {
        const response = await apiServices.put(`admin/documents/${id}/approve`);
        if (!response.ok) throw new Error('Erreur lors de la validation du document');
        this.showSuccess('Document approuvé avec succès');
        await this.loadDocuments();
      } catch (error) {
        this.showError(error.message);
      }
    },
    openRejectModal(document) {
      this.selectedDocument = document;
      this.rejectComment = '';
      this.showRejectModal = true;
    },
    async rejectDocument() {
      if (!this.rejectComment.trim()) {
        this.showError('Le commentaire est obligatoire');
        return;
      }

      try {
        const response = await apiServices.put(
          `admin/documents/${this.selectedDocument.idJustificatif}/reject`,
          { commentaire: this.rejectComment }
        );
        if (!response.ok)
          throw new Error('Erreur lors du rejet du document');

        this.showSuccess('Document rejeté avec succès');
        this.closeRejectModal();
        await this.loadDocuments();
      } catch (error) {
        this.showError(error.message);
      }
    },
    closeRejectModal() {
      this.showRejectModal = false;
      this.selectedDocument = null;
      this.rejectComment = '';
    },
    showSuccess(text) {
      this.message = { type: 'success', text };
      setTimeout(() => this.message = null, 3000);
    },
    showError(text) {
      this.message = { type: 'error', text };
      setTimeout(() => this.message = null, 3000);
    },
    getDocumentUrl(documentId) {
      const token = localStorage.getItem('token');
      return `/api/admin/documents/view/${documentId}?token=${token}`;
    },
    downloadDocument(documentId) {
      window.open(this.getDocumentUrl(documentId), '_blank');
    },
    formatDate(date) {
      return new Date(date).toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  },
  created() {
    this.loadDocuments();
  },
  watch: {
    filterType() {
      this.loadDocuments();
    }
  }
}
</script>

<template>
  <div class="admin-documents">
    <div class="header">
      <h1>Gestion des Documents</h1>
      <div class="filters">
        <div class="filter-group">
          <label>Statut :</label>
          <select v-model="filterType">
            <option value="pending">En attente</option>
            <option value="approved">Approuvés</option>
            <option value="rejected">Rejetés</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Type :</label>
          <select v-model="documentType">
            <option value="all">Tous</option>
            <option value="CNI">Carte d'identité</option>
            <option value="PERMIS">Permis de conduire</option>
          </select>
        </div>
      </div>
    </div>

    <div v-if="message" :class="['alert', message.type]">
      {{ message.text }}
    </div>

    <div v-if="isLoading" class="loading">
      Chargement des documents...
    </div>

    <div v-else-if="documents.length === 0" class="no-documents">
      Aucun document {{ filterType === 'pending' ? 'en attente' : filterType === 'approved' ? 'approuvé' : 'rejeté' }}
    </div>

    <div v-else class="documents-grid">
      <div v-for="doc in filteredDocuments" :key="doc.idJustificatif" class="document-card">
        <div class="document-header">
          <h3>{{ doc.typeJustificatif === 'CNI' ? 'Carte d\'identité' : 'Permis de conduire' }}</h3>
          <span class="date">Soumis le {{ formatDate(doc.dateDebut) }}</span>
        </div>

        <div class="user-info">
          <i class="fas fa-user"></i>
          <div>
            <p>{{ doc.utilisateur.prenom }} {{ doc.utilisateur.nom }}</p>
            <small>{{ doc.utilisateur.email }}</small>
          </div>
        </div>

        <div class="document-preview">
          <object
            :data="getDocumentUrl(doc.idJustificatif)"
            type="application/pdf"
            class="pdf-viewer"
          >
            <p>Impossible d'afficher le PDF. <a :href="getDocumentUrl(doc.idJustificatif)" target="_blank">Cliquez ici pour le télécharger</a></p>
          </object>
        </div>

        <div class="document-actions">
          <button @click="downloadDocument(doc.idJustificatif)" class="action-button download">
            <i class="fas fa-download"></i> Télécharger
          </button>

          <template v-if="filterType === 'pending'">
            <button @click="approveDocument(doc.idJustificatif)" class="action-button approve">
              <i class="fas fa-check"></i> Approuver
            </button>
            <button @click="openRejectModal(doc)" class="action-button reject">
              <i class="fas fa-times"></i> Rejeter
            </button>
          </template>

          <div v-if="filterType === 'rejected'" class="reject-reason">
            <strong>Motif du rejet :</strong>
            <p>{{ doc.commentaire }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showRejectModal" class="modal-overlay">
      <div class="modal">
        <h2>Rejeter le document</h2>
        <p>Veuillez indiquer le motif du rejet :</p>
        <textarea
          v-model="rejectComment"
          placeholder="Motif du rejet..."
          rows="4"
        ></textarea>
        <div class="modal-actions">
          <button @click="closeRejectModal" class="cancel-button">
            Annuler
          </button>
          <button @click="rejectDocument" class="reject-button">
            Confirmer le rejet
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-documents {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

h1 {
  color: #333;
  margin: 0;
}

.filters {
  display: flex;
  gap: 1rem;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 2rem;
}

.document-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 1.5rem;
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
}

.date {
  color: #666;
  font-size: 0.9rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 4px;
}

.user-info i {
  color: #4CAF50;
  font-size: 1.2rem;
}

.user-info p {
  margin: 0;
  font-weight: 500;
}

.user-info small {
  color: #666;
}

.document-preview {
  margin-bottom: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.document-preview object.pdf-viewer {
  width: 100%;
  height: 500px;
  border: none;
}

.document-preview p {
  text-align: center;
  padding: 1rem;
}

.document-preview a {
  color: #4CAF50;
  text-decoration: none;
}

.document-preview a:hover {
  text-decoration: underline;
}

.document-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.action-button {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-weight: 500;
  transition: all 0.3s;
}

.download {
  background-color: #f8f9fa;
  color: #333;
}

.approve {
  background-color: #4CAF50;
  color: white;
}

.reject {
  background-color: #dc3545;
  color: white;
}

.download:hover { background-color: #e9ecef; }
.approve:hover { background-color: #45a049; }
.reject:hover { background-color: #c82333; }

.reject-reason {
  margin-top: 1rem;
  padding: 1rem;
  background: #fff3cd;
  border-radius: 4px;
  color: #856404;
}

.reject-reason p {
  margin: 0.5rem 0 0;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  width: 90%;
  max-width: 500px;
}

.modal h2 {
  margin: 0 0 1rem;
  color: #333;
}

.modal textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 1rem 0;
  font-family: inherit;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}

.cancel-button, .reject-button {
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.cancel-button {
  background: white;
  border: 1px solid #ddd;
  color: #666;
}

.reject-button {
  background: #dc3545;
  border: none;
  color: white;
}

.alert {
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  font-weight: 500;
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

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.no-documents {
  text-align: center;
  padding: 3rem;
  color: #666;
  background: #f8f9fa;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
  }

  .filters {
    width: 100%;
    flex-direction: column;
  }

  .filter-group {
    width: 100%;
  }

  select {
    width: 100%;
  }

  .documents-grid {
    grid-template-columns: 1fr;
  }

  .document-actions {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }
}
</style>
