<template>
  <div id="app">
    <router-view />
    <Toast />
  </div>
</template>

<script setup>
</script>

<style>
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  overflow: hidden;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  background-color: var(--admin-content-bg);
}

.p-button.btn-primary {
  background-color: var(--ecodeli-green) !important;
  border-color: var(--ecodeli-green) !important;
}

.p-button.btn-primary:hover {
  background-color: var(--ecodeli-green-dark) !important;
  border-color: var(--ecodeli-green-dark) !important;
}

.p-menubar {
  background-color: var(--admin-topbar-bg) !important;
}

.p-card {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1) !important;
}

.p-datatable .p-datatable-header {
  background-color: var(--ecodeli-green-light) !important;
  border-bottom: 2px solid var(--ecodeli-green) !important;
}

.p-badge.p-badge-danger {
  background-color: #ef4444 !important;
}
</style>
