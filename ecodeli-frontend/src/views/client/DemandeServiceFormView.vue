<script>
import DemandeServiceWizard from '@/components/demande-service/DemandeServiceWizard.vue'

export default {
  name: 'DemandeServiceFormView',
  components: {
    DemandeServiceWizard
  },
  data() {
    return {
      isEditing: false,
      editId: null,
      initialData: {}
    }
  },
  async created() {
    if (this.$route.params.id) {
      this.isEditing = true
      this.editId = this.$route.params.id
    }
  },
  methods: {
    async handleSubmit(formData) {
      try {
        const token = localStorage.getItem('token')

        if (!token) {
          throw new Error('Vous devez être connecté pour créer une demande')
        }

        console.log('Token présent:', !!token)
        console.log('Données à envoyer (brutes):', formData)

        const processedData = {
          ...formData
        }
        delete processedData.budgetNegociable
        if (processedData.detailsSpecifiques && typeof processedData.detailsSpecifiques === 'object') {
          console.log('Conversion detailsSpecifiques en JSON string:', processedData.detailsSpecifiques)
          processedData.detailsSpecifiques = JSON.stringify(processedData.detailsSpecifiques)
        }

        console.log('Données à envoyer (traitées):', processedData)

        const url = this.isEditing
          ? `http://localhost:8080/api/demandes-services/${this.editId}`
          : 'http://localhost:8080/api/demandes-services'

        const method = this.isEditing ? 'PUT' : 'POST'

        const response = await fetch(url, {
          method,
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(processedData)
        })

        console.log('Statut de la réponse:', response.status)
        console.log('Headers de la réponse:', [...response.headers.entries()])

        if (response.ok) {
          localStorage.removeItem('demande-service-draft')
          this.$router.push('/client/demandes-services')
          this.$emit('show-notification', {
            type: 'success',
            message: this.isEditing
              ? 'Demande modifiée avec succès !'
              : 'Demande créée avec succès !'
          })
        } else {
          if (response.status === 403) {
            throw new Error('Accès refusé. Veuillez vous reconnecter.')
          }

          let errorMessage = 'Erreur lors de la soumission'
          try {
            const contentType = response.headers.get('content-type')
            if (contentType && contentType.includes('application/json')) {
              const error = await response.json()
              errorMessage = error.error || error.message || errorMessage
            } else {
              const textError = await response.text()
              console.log('Réponse non-JSON:', textError)
              errorMessage = `Erreur ${response.status}: ${response.statusText}`
            }
          } catch (parseError) {
            console.error('Erreur de parsing de la réponse:', parseError)
            errorMessage = `Erreur ${response.status}: ${response.statusText}`
          }

          throw new Error(errorMessage)
        }
      } catch (error) {
        console.error('Erreur lors de la soumission:', error)
        this.$emit('show-notification', {
          type: 'error',
          message: error.message || 'Erreur lors de la soumission de la demande'
        })
      }
    },

    handleCancel() {
      if (confirm('Êtes-vous sûr de vouloir annuler ? Les modifications non sauvegardées seront perdues.')) {
        this.$router.go(-1)
      }
    }
  }
}
</script>

<template>
  <div class="demande-service-form-view">
    <DemandeServiceWizard
      :is-editing="isEditing"
      :initial-data="initialData"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </div>
</template>

<style scoped>
</style>
