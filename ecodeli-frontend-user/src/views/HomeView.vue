<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import logoEcoDeli from '@/assets/logo_EcoDeli.png'
import accueilScreen from '@/assets/accueil_screen.png'

const router = useRouter()
const profilesSection = ref(null)

const menuItems = [
  {
    label: 'Accueil',
    icon: 'pi pi-home'
  },
  {
    label: 'Services',
    icon: 'pi pi-cog'
  },
  {
    label: 'À propos',
    icon: 'pi pi-info-circle'
  }
]

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const registerAs = (profileType) => {
  router.push({
    path: '/register',
    query: { type: profileType }
  })
}
</script>

<template>
  <div class="home-page">
    <!-- Header avec navigation -->
    <header class="home-header">
      <Menubar :model="menuItems" class="border-0 bg-white shadow-2">
        <template #start>
          <div class="flex align-items-center">
            <img
              :src="logoEcoDeli"
              alt="EcoDeli Logo"
              class="logo-image mr-3"
            />
          </div>
        </template>

        <template #end>
          <div class="flex align-items-center gap-3">
            <Button
              label="Se connecter"
              outlined
              @click="goToLogin"
              class="px-4"
            />
            <Button
              label="S'inscrire"
              @click="goToRegister"
              class="px-4"
            />
          </div>
        </template>
      </Menubar>
    </header>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-background">
        <img
          :src="accueilScreen"
          alt="EcoDeli Background"
          class="hero-background-image"
        />
        <div class="hero-overlay"></div>
      </div>
      <div class="hero-content">
        <div class="container">
          <div class="hero-text-center">
            <h1 class="hero-brand-title">EcoDeli</h1>
            <h2 class="hero-title">
              La révolution du crowdshipping écologique
            </h2>
            <p class="hero-subtitle">
              EcoDeli connecte particuliers, commerçants et livreurs pour une livraison
              responsable qui réduit l'impact environnemental et favorise l'économie collaborative.
            </p>
            <div class="hero-actions">
              <Button
                label="Commencer maintenant"
                size="large"
                @click="goToRegister"
                class="hero-btn-primary"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Section des profils -->
    <section class="profiles-section" ref="profilesSection">
      <div class="container">
        <div class="text-center mb-6">
          <h2 class="section-title">Rejoignez la communauté EcoDeli</h2>
        </div>

        <div class="grid">
          <!-- Client -->
          <div class="col-12 md:col-6 lg:col-3">
            <Card class="profile-card h-full">
              <template #header>
                <div class="profile-icon">
                  <Avatar icon="pi pi-user" size="xlarge" class="bg-blue-500" />
                </div>
              </template>
              <template #title>Client</template>
              <template #content>
                <ul class="profile-features">
                  <li>Expédier vos colis facilement</li>
                  <li>Services à la personne sur-mesure</li>
                  <li>Courses et achats à l'étranger</li>
                  <li>Transport de personnes</li>
                  <li>Suivi en temps réel</li>
                </ul>
              </template>
              <template #footer>
                <Button
                  label="Devenir client"
                  outlined
                  class="w-full"
                  @click="registerAs('CLIENT')"
                />
              </template>
            </Card>
          </div>

          <!-- Livreur -->
          <div class="col-12 md:col-6 lg:col-3">
            <Card class="profile-card h-full">
              <template #header>
                <div class="profile-icon">
                  <Avatar icon="pi pi-truck" size="xlarge" class="bg-green-500" />
                </div>
              </template>
              <template #title>Livreur</template>
              <template #content>
                <ul class="profile-features">
                  <li>Monétisez vos trajets</li>
                  <li>Livraisons flexibles</li>
                  <li>Paiements sécurisés</li>
                  <li>Planning adaptatif</li>
                  <li>Assurance incluse</li>
                </ul>
              </template>
              <template #footer>
                <Button
                  label="Devenir livreur"
                  outlined
                  class="w-full"
                  @click="registerAs('LIVREUR')"
                />
              </template>
            </Card>
          </div>

          <!-- Prestataire -->
          <div class="col-12 md:col-6 lg:col-3">
            <Card class="profile-card h-full">
              <template #header>
                <div class="profile-icon">
                  <Avatar icon="pi pi-wrench" size="xlarge" class="bg-orange-500" />
                </div>
              </template>
              <template #title>Prestataire</template>
              <template #content>
                <ul class="profile-features">
                  <li>Services à la personne</li>
                  <li>Travaux et jardinage</li>
                  <li>Garde d'animaux</li>
                  <li>Évaluations clients</li>
                  <li>Facturation automatique</li>
                </ul>
              </template>
              <template #footer>
                <Button
                  label="Devenir prestataire"
                  outlined
                  class="w-full"
                  @click="registerAs('PRESTATAIRE')"
                />
              </template>
            </Card>
          </div>

          <!-- Commerçant -->
          <div class="col-12 md:col-6 lg:col-3">
            <Card class="profile-card h-full">
              <template #header>
                <div class="profile-icon">
                  <Avatar icon="pi pi-shop" size="xlarge" class="bg-purple-500" />
                </div>
              </template>
              <template #title>Commerçant</template>
              <template #content>
                <ul class="profile-features">
                  <li>Livraison à domicile</li>
                  <li>Lâcher de chariot</li>
                  <li>Gestion des contrats</li>
                  <li>Facturation intégrée</li>
                  <li>Réseau de partenaires</li>
                </ul>
              </template>
              <template #footer>
                <Button
                  label="Devenir partenaire"
                  outlined
                  class="w-full"
                  @click="registerAs('COMMERCANT')"
                />
              </template>
            </Card>
          </div>
        </div>
      </div>
    </section>

    <!-- Section avantages -->
    <section class="benefits-section">
      <div class="container">
        <div class="text-center mb-6">
          <h2 class="section-title">Pourquoi choisir EcoDeli ?</h2>
        </div>

        <div class="grid">
          <div class="col-12 md:col-4 text-center">
            <div class="benefit-item">
              <i class="pi pi-heart text-6xl text-green-500 mb-3"></i>
              <h3>Écologique</h3>
              <p>Réduisez l'impact carbone en optimisant les trajets existants</p>
            </div>
          </div>
          <div class="col-12 md:col-4 text-center">
            <div class="benefit-item">
              <i class="pi pi-users text-6xl text-blue-500 mb-3"></i>
              <h3>Collaboratif</h3>
              <p>Favorisez l'économie locale et luttez contre l'isolement</p>
            </div>
          </div>
          <div class="col-12 md:col-4 text-center">
            <div class="benefit-item">
              <i class="pi pi-shield text-6xl text-orange-500 mb-3"></i>
              <h3>Sécurisé</h3>
              <p>Livreurs validés, assurances incluses et paiements protégés</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="home-footer">
      <div class="container">
        <div class="text-center">
          <div class="flex align-items-center justify-content-center mb-4">
            <i class="pi pi-leaf text-2xl mr-2" style="color: var(--ecodeli-green)"></i>
            <span class="text-xl font-bold">EcoDeli</span>
          </div>
          <p>&copy; 2024 EcoDeli - La révolution du crowdshipping écologique</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
}

.home-header {
  position: sticky;
  top: 0;
  z-index: 1000;
}

.hero-section {
  position: relative;
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.hero-background-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(0, 0, 0, 0.4) 0%,
    rgba(22, 163, 74, 0.3) 50%,
    rgba(0, 0, 0, 0.5) 100%
  );
  z-index: 2;
}

.hero-content {
  position: relative;
  z-index: 3;
  width: 100%;
}

.hero-text-center {
  text-align: center;
  color: white;
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.hero-brand-title {
  font-size: 5rem;
  font-weight: 900;
  background: linear-gradient(135deg, #ffffff 0%, #22c55e 50%, #ffffff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(255, 255, 255, 0.5);
  margin-bottom: 1rem;
  animation: glow 3s ease-in-out infinite alternate;
  letter-spacing: 0.1em;
}

@keyframes glow {
  from {
    text-shadow: 0 0 20px rgba(255, 255, 255, 0.5), 0 0 30px rgba(34, 197, 94, 0.3);
  }
  to {
    text-shadow: 0 0 30px rgba(255, 255, 255, 0.8), 0 0 40px rgba(34, 197, 94, 0.5);
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 1.5rem;
  line-height: 1.2;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.7);
}

.hero-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2rem;
  line-height: 1.6;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.5);
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  margin-top: 3rem;
}

/* Logo dans le header */
.logo-image {
  height: 40px;
  width: auto;
  object-fit: contain;
}

/* Hero Section Améliorée */
.hero-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
  padding-right: 2rem;
}

.hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 2rem;
}

.marketing-image {
  max-width: 100%;
  height: auto;
  max-height: 500px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: float 3s ease-in-out infinite;
  transition: transform 0.3s ease;
}

.marketing-image:hover {
  transform: scale(1.02);
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

.hero-btn-primary {
  background: var(--ecodeli-green) !important;
  border-color: var(--ecodeli-green) !important;
  color: white !important;
  padding: 1rem 2rem;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.hero-btn-primary:hover {
  background: var(--ecodeli-green-dark) !important;
  border-color: var(--ecodeli-green-dark) !important;
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

/* Section App Preview */
.app-preview-section {
  padding: 5rem 0;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
}

.app-showcase {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  margin-top: 3rem;
}

.app-image-container {
  position: relative;
}

.app-screenshot {
  width: 100%;
  height: auto;
  border-radius: 20px;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.12);
  transform: perspective(1000px) rotateY(-5deg);
  transition: transform 0.3s ease;
}

.app-screenshot:hover {
  transform: perspective(1000px) rotateY(0deg) scale(1.02);
}

.app-features {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  padding: 1.5rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--ecodeli-green) 0%, #22c55e 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.feature-content h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--surface-800);
  margin: 0 0 0.5rem 0;
}

.feature-content p {
  color: var(--surface-600);
  line-height: 1.6;
  margin: 0;
}

.profiles-section {
  padding: 4rem 0;
  background: white;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 600;
  color: var(--ecodeli-green-dark);
  margin-bottom: 1rem;
}

.section-subtitle {
  font-size: 1.125rem;
  color: var(--surface-600);
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  text-align: center;
  transition: transform 0.3s ease;
  border: 1px solid var(--surface-200);
}

.profile-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}

.profile-icon {
  padding: 2rem;
  background: var(--surface-50);
}

.profile-features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.profile-features li {
  padding: 0.5rem 0;
  color: var(--surface-700);
  border-bottom: 1px solid var(--surface-100);
}

.profile-features li:last-child {
  border-bottom: none;
}

.benefits-section {
  padding: 4rem 0;
  background: var(--surface-50);
}

.benefit-item {
  padding: 2rem;
}

.benefit-item h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 1rem 0;
  color: var(--surface-800);
}

.benefit-item p {
  color: var(--surface-600);
  line-height: 1.6;
}

.home-footer {
  background: var(--ecodeli-green-dark);
  color: white;
  padding: 2rem 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1.5rem;
}

.col-12 { grid-column: span 12; }
.col-6 { grid-column: span 6; }
.col-4 { grid-column: span 4; }
.col-3 { grid-column: span 3; }

@media (min-width: 768px) {
  .md\:col-6 { grid-column: span 6; }
  .md\:col-4 { grid-column: span 4; }
}

@media (min-width: 1024px) {
  .lg\:col-6 { grid-column: span 6; }
  .lg\:col-3 { grid-column: span 3; }
}

/* Responsive Design */
@media (max-width: 1024px) {
  .app-showcase {
    grid-template-columns: 1fr;
    gap: 3rem;
  }
  
  .app-screenshot {
    transform: none;
    max-width: 500px;
    margin: 0 auto;
  }
}

@media (max-width: 768px) {
  .hero-brand-title {
    font-size: 3.5rem;
  }
  
  .hero-title {
    font-size: 1.75rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .hero-text-center {
    padding: 1rem;
  }
  
  .logo-image {
    height: 35px;
  }
  
  .app-preview-section {
    padding: 3rem 0;
  }
  
  .app-showcase {
    gap: 2rem;
  }
  
  .feature-item {
    padding: 1rem;
  }
  
  .feature-icon {
    width: 50px;
    height: 50px;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .hero-actions {
    margin-top: 2rem;
  }
  
  .hero-btn-primary {
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 1rem;
  }
  
  .hero-brand-title {
    font-size: 2.5rem;
    letter-spacing: 0.05em;
  }
  
  .hero-title {
    font-size: 1.5rem;
  }
  
  .hero-subtitle {
    font-size: 0.9rem;
    margin-bottom: 1.5rem;
  }
  
  .hero-text-center {
    padding: 0.5rem;
  }
  
  .app-screenshot {
    max-width: 300px;
  }
  
  .feature-item {
    flex-direction: column;
    text-align: center;
  }
  
  .hero-btn-primary {
    width: 100%;
    padding: 1rem;
  }
}
</style>
