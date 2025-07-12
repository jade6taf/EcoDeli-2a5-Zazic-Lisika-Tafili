<script setup>
// Page de paramètres - TODO
</script>

<template>
  <div class="settings-page">
    <Card>
      <template #header>
        <div class="p-4">
          <h2 class="m-0">
            <i class="pi pi-cog mr-2"></i>
            Paramètres Système
          </h2>
        </div>
      </template>
      <template #content>
        <div class="text-center p-4">
          <i class="pi pi-cog text-4xl text-600 mb-3"></i>
          <h3>Configuration système</h3>
          <p class="text-600">Les paramètres de configuration seront bientôt disponibles.</p>
          <Button
            label="Retour au Dashboard"
            icon="pi pi-arrow-left"
            class="mt-3"
            @click="$router.push('/admin/dashboard')"
          />
        </div>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.settings-page {
  padding: 0;
}

.p-4 {
  padding: 1rem;
}

.m-0 {
  margin: 0;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mb-3 {
  margin-bottom: 0.75rem;
}

.mt-3 {
  margin-top: 0.75rem;
}

.text-center {
  text-align: center;
}

.text-4xl {
  font-size: 2.25rem;
}

.text-600 {
  color: var(--surface-600);
}
</style>
