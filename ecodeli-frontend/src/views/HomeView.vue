<script>
export default {
  data() {
    return {
      hoveredService: -1,
      activeProfile: 0,
      services: [
        {
          title: "Livraison de colis",
          description: "Expédiez vos colis à moindre coût grâce à notre réseau de livreurs occasionnels.",
          icon: "fas fa-box"
        },
        {
          title: "Lâcher vos chariots",
          description: "Faites vos courses chez nos commerçants partenaires et faites-vous livrer à domicile.",
          icon: "fas fa-shopping-cart"
        },
        {
          title: "Transport de personnes",
          description: "Un service de transport pour les personnes âgées, à mobilités réduites.",
          icon: "fas fa-user-friends"
        },
        {
          title: "Achats à l'étranger",
          description: "Demandez à un voyageur de vous rapporter des produits introuvables en France.",
          icon: "fas fa-globe-europe"
        },
        {
          title: "Garde d'animaux",
          description: "Prise en charge de vos compagnons pendant vos absences.",
          icon: "fas fa-paw"
        },
        {
          title: "Aide à domicile",
          description: "Petits travaux, jardinage et assistance ponctuelle.",
          icon: "fas fa-home"
        }
      ],
      profiles: [
        {
          type: "Client",
          description: "Bénéficiez de services de livraison économiques et écologiques directement à votre domicile.",
          largeIcon: "fas fa-user-circle",
          path: "/client",
          icon: "fas fa-user",
          benefits: [
            "Livraison à domicile rapide",
            "Prix avantageux",
            "Tracking en temps réel",
            "Service client réactif"
          ]
        },
        {
          type: "Commerçant",
          description: "Étendez votre zone de chalandise et proposez la livraison à vos clients sans investissement.",
          largeIcon: "fas fa-store-alt",
          path: "/commerçant",
          icon: "fas fa-store",
          benefits: [
            "Élargissement de votre clientèle",
            "Aucun coût fixe",
            "Intégration simple",
            "Visibilité accrue"
          ]
        },
        {
          type: "Livreur",
          description: "Gagnez un revenu complémentaire en effectuant des livraisons lors de vos déplacements habituels.",
          largeIcon: "fas fa-biking",
          path: "/livreur",
          icon: "fas fa-bicycle",
          benefits: [
            "Revenu complémentaire",
            "Flexibilité totale",
            "Choix des missions",
            "Paiement rapide"
          ]
        },
        {
          type: "Prestataire",
          description: "Proposez vos services et compétences aux personnes de votre quartier en toute simplicité.",
          largeIcon: "fas fa-hands-helping",
          path: "/prestataire",
          icon: "fas fa-tools",
          benefits: [
            "Visibilité locale",
            "Gestion simple des rendez-vous",
            "Paiements sécurisés",
            "Évaluations clients"
          ]
        }
      ]
    };
  },
  methods: {
    hoverService(index) {
      this.hoveredService = index;
    },
    setActiveProfile(index) {
      this.activeProfile = index;
    }
  },
  mounted() {
    if (!document.getElementById('font-awesome')) {
      const link = document.createElement('link');
      link.id = 'font-awesome';
      link.rel = 'stylesheet';
      link.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css';
      document.head.appendChild(link);
    }
  }
};
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="hero-background">
        <img src="@/assets/accueil_screen.png" alt="EcoDeli - Livraison écoresponsable" />
      </div>
      <div class="hero-container">
        <div class="hero-content">
          <h1 class="hero-title">
            <span class="highlight">Réinventons</span> la livraison ensemble
          </h1>
          <h2 class="hero-subtitle">Écoresponsable, solidaire et locale</h2>
          <p class="hero-text">
            Chez EcoDeli, chacun devient acteur de la mobilité durable. En livrant un colis ou en rendant un service, 
            participez à un réseau collaboratif qui crée du lien et réduit l'impact écologique.
          </p>
          <div class="hero-buttons">
            <router-link to="/register" class="btn btn-primary">Rejoignez-nous</router-link>
            <router-link to="/services" class="btn btn-secondary">Découvrir nos services</router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="services" id="services">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">Nos Services</h2>
          <p class="section-description">
            <strong>EcoDeli</strong> vous propose une gamme complète de services locaux et écoresponsables
          </p>
        </div>

        <div class="services-grid">
          <div
            v-for="(service, index) in services"
            :key="index"
            class="service-card"
            :class="{ 'active': hoveredService === index }"
            @mouseover="hoverService(index)"
            @mouseleave="hoverService(-1)">
            <div class="service-icon">
              <i :class="service.icon"></i>
            </div>
            <div class="service-content">
              <h3 class="service-title">{{ service.title }}</h3>
              <p class="service-description">{{ service.description }}</p>
              <router-link to="/services" class="service-link">En savoir plus</router-link>
            </div>
          </div>
        </div>
      </div>

      <div class="eco-banner">
        <div class="eco-banner-content">
          <h3>Un service <span>flexible</span>, <span>éthique</span> et adapté à <span>tous</span> !</h3>
          <p>Nous nous engageons pour un monde plus durable</p>
        </div>
      </div>
    </section>

    <section class="profiles" id="profiles">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">Qui peut utiliser EcoDeli ?</h2>
          <p class="section-description">
            Notre plateforme s'adapte à tous les profils et à tous les besoins
          </p>
        </div>

        <div class="profiles-wrapper">
          <div class="profiles-tabs">
            <div
              v-for="(profile, index) in profiles"
              :key="profile.type"
              class="profile-tab"
              :class="{ 'active': activeProfile === index }"
              @click="setActiveProfile(index)">
              <i :class="profile.icon"></i>
              <span>{{ profile.type }}</span>
            </div>
          </div>

          <div class="profile-content">
            <transition name="fade" mode="out-in">
              <div :key="activeProfile" class="profile-details">
                <div class="profile-icon-container">
                  <i :class="profiles[activeProfile].largeIcon" class="profile-icon-large"></i>
                </div>
                <div class="profile-info">
                  <h3>{{ profiles[activeProfile].type }}</h3>
                  <p>{{ profiles[activeProfile].description }}</p>
                  <ul class="profile-benefits">
                    <li v-for="(benefit, i) in profiles[activeProfile].benefits" :key="i">
                      <i class="fas fa-check-circle"></i> {{ benefit }}
                    </li>
                  </ul>
                  <router-link :to="profiles[activeProfile].path" class="btn btn-primary">
                    Je suis {{ profiles[activeProfile].type.toLowerCase() }}
                  </router-link>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </section>

    <section class="cta">
      <div class="cta-container">
        <h2>Prêt à rejoindre la communauté EcoDeli ?</h2>
        <p>Inscrivez-vous dès maintenant et participez à la révolution de la livraison écoresponsable</p>
        <router-link to="/register" class="btn btn-large">S'inscrire gratuitement</router-link>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  background-color: var(--bg-tertiary);
  min-height: 100vh;
  font-family: "Open Sans", sans-serif;
  color: var(--text-color);
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.section-title {
  color: var(--secondary-color);
  font-size: 2.5rem;
  margin-bottom: 1rem;
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: "";
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background-color: var(--secondary-color);
  border-radius: 2px;
}

.section-description {
  color: var(--text-secondary);
  font-size: 1.2rem;
  max-width: 700px;
  margin: 0 auto;
  line-height: 1.6;
}

.btn {
  display: inline-block;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  text-align: center;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.btn-secondary {
  background-color: transparent;
  color: var(--primary-color);
  border: 2px solid var(--primary-color);
  margin-left: 0;
}

.btn-secondary:hover {
  background-color: var(--hover-bg);
  transform: translateY(-2px);
}

.btn-large {
  padding: 16px 32px;
  font-size: 1.2rem;
}

.hero {
  position: relative;
  overflow: hidden;
  padding: 8rem 0;
  min-height: 600px;
  display: flex;
  align-items: center;
  background-color: var(--bg-color);
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.hero-background::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, rgba(32, 91, 1, 0.85), rgba(32, 91, 1, 0.6));
  z-index: 2;
}

.hero-background img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.hero::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(to right, var(--bg-tertiary), rgba(241, 245, 244, 0.5));
  z-index: 1;
}

.hero-container {
  max-width: 1300px;
  margin: 0 auto;
  position: relative;
  z-index: 3;
  padding: 0 2rem;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-content {
  max-width: 700px;
  background-color: var(--bg-color);
  padding: 2.5rem;
  border-radius: 15px;
  box-shadow: var(--shadow-hover);
  text-align: center;
  border: 1px solid var(--border-color);
}

.hero-title {
  font-size: 3.5rem;
  color: var(--text-color);
  line-height: 1.2;
  margin-bottom: 1rem;
}

.hero-title .highlight {
  color: var(--secondary-color);
  position: relative;
  display: inline-block;
}

.hero-title .highlight::after {
  content: "";
  position: absolute;
  bottom: 5px;
  left: 0;
  width: 100%;
  height: 10px;
  background-color: var(--hover-bg);
  z-index: -1;
}

.hero-subtitle {
  font-size: 1.8rem;
  color: var(--text-secondary);
  margin-bottom: 2rem;
}

.hero-text {
  color: var(--text-secondary);
  font-size: 1.2rem;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.hero-buttons {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.services {
  padding: 6rem 0 4rem;
  background-color: var(--bg-tertiary);
  position: relative;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin: 2rem 0;
}

.service-card {
  background-color: var(--bg-color);
  border-radius: 15px;
  padding: 2rem;
  box-shadow: var(--shadow);
  transition: all 0.4s ease;
  text-align: center;
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.service-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 0;
  background-color: var(--secondary-color);
  transition: height 0.3s ease;
}

.service-card:hover::before, .service-card.active::before {
  height: 100%;
}

.service-card:hover, .service-card.active {
  transform: translateY(-10px);
  box-shadow: var(--shadow-hover);
  background-color: var(--bg-secondary);
}

.service-icon {
  margin-bottom: 1.5rem;
}

.service-icon i {
  font-size: 3rem;
  color: var(--primary-color);
}

.service-title {
  font-size: 1.5rem;
  color: var(--text-color);
  margin-bottom: 1rem;
  transition: color 0.3s;
}

.service-card:hover .service-title, .service-card.active .service-title {
  color: var(--primary-color);
}

.service-description {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: 1.5rem;
}

.service-link {
  color: var(--primary-color);
  font-weight: 600;
  text-decoration: none;
  position: relative;
  transition: all 0.3s;
}

.service-link::after {
  content: "→";
  margin-left: 5px;
  transition: transform 0.3s;
}

.service-link:hover::after {
  transform: translateX(5px);
}

.eco-banner {
  background-color: var(--bg-secondary);
  color: var(--text-color);
  padding: 3rem 2rem;
  text-align: center;
  margin-top: 5rem;
  border: 1px solid var(--border-color);
}

.eco-banner-content h3 {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.eco-banner-content span {
  color: var(--primary-color);
  font-weight: 700;
}

.eco-banner-content p {
  font-size: 1.2rem;
  opacity: 0.9;
}

.profiles {
  padding: 6rem 0;
  background-color: var(--bg-color);
}

.profiles-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 1000px;
  margin: 0 auto;
}

.profiles-tabs {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.profile-tab {
  padding: 1rem 2rem;
  background-color: var(--bg-tertiary);
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.profile-tab i {
  color: var(--text-secondary);
}

.profile-tab.active {
  background-color: var(--primary-color);
  color: white;
}

.profile-tab.active i {
  color: white;
}

.profile-content {
  min-height: 400px;
}

.profile-details {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 2rem;
  background-color: var(--bg-secondary);
  border-radius: 15px;
  overflow: hidden;
  box-shadow: var(--shadow);
  border: 1px solid var(--border-color);
}

.profile-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-tertiary);
  position: relative;
  overflow: hidden;
}

.profile-icon-container::before {
  content: "";
  position: absolute;
  width: 250px;
  height: 250px;
  background-color: var(--hover-bg);
  border-radius: 50%;
}

.profile-icon-large {
  font-size: 8rem;
  color: var(--primary-color);
  z-index: 1;
  padding: 2rem;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
  100% {
    transform: translateY(0px);
  }
}

.profile-info {
  padding: 2rem;
}

.profile-info h3 {
  font-size: 2rem;
  color: var(--text-color);
  margin-bottom: 1rem;
}

.profile-info p {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.profile-benefits {
  list-style: none;
  padding: 0;
  margin-bottom: 2rem;
}

.profile-benefits li {
  padding: 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.profile-benefits i {
  color: var(--primary-color);
}

.cta {
  background-color: var(--bg-secondary);
  color: var(--text-color);
  padding: 5rem 2rem;
  text-align: center;
  border: 1px solid var(--border-color);
}

.cta-container {
  max-width: 800px;
  margin: 0 auto;
}

.cta h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.cta p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 1024px) {
  .hero-container {
    flex-direction: column-reverse;
    text-align: center;
  }

  .hero-content {
    padding-right: 0;
    margin-top: 2rem;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .profile-details {
    grid-template-columns: 1fr;
  }

  .profile-icon-container {
    padding: 3rem;
  }

  .profile-icon-large {
    font-size: 6rem;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 5rem 0;
  }

  .hero-content {
    padding: 1.5rem;
  }

  .hero-title {
    font-size: 2.2rem;
  }

  .hero-subtitle {
    font-size: 1.4rem;
  }
  .services-grid {
    grid-template-columns: 1fr;
  }

  .profiles-tabs {
    flex-wrap: wrap;
  }

  .section-title {
    font-size: 2rem;
  }
}
</style>