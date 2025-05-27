<script>
import Header from '@/components/layout/Header.vue'
import '@/assets/css/themes.css'

export default {
  components: {
    Header
  }
}
</script>

<template>
  <div class="app">
    <Header />
    <router-view />
    <router-link to="/login"></router-link> |
    <router-link to="/register"></router-link>
  </div>
</template>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Montserrat', sans-serif;
  line-height: 1.6;
  color: var(--text-color);
  background-color: var(--bg-color);
}

.app {
  background-color: var(--bg-color);
  color: var(--text-color);
  min-height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
}

.hero {
  background-color: var(--bg-tertiary);
  padding: 4rem 2rem;
  text-align: center;
}

.hero-content h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.hero-content p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  color: var(--text-secondary);
}

.cta-button {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  font-size: 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.cta-button:hover {
  background-color: var(--primary-hover);
}

.features {
  padding: 4rem 2rem;
  background-color: var(--bg-color);
}

.feature-card {
  background-color: var(--bg-secondary);
  border-radius: 8px;
  box-shadow: var(--shadow);
  padding: 2rem;
  width: 300px;
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid var(--border-color);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
}

.feature-card h3 {
  margin-bottom: 1rem;
  color: var(--text-color);
}

.footer {
  background-color: var(--bg-secondary);
  text-align: center;
  padding: 1.5rem;
  color: var(--text-secondary);
  border-top: 1px solid var(--border-color);
}
</style>
