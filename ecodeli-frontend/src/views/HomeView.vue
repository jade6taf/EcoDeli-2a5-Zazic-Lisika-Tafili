<script>
import { useI18n } from 'vue-i18n'

export default {
  setup() {
    const { t } = useI18n()
    return { t }
  },
  data() {
    return {
      hoveredService: -1,
      activeProfile: 0,
      services: [
        {
          title: this.t('homeview.ship'),
          description: this.t('homeview.send'),
          icon: "fas fa-box"
        },
        {
          title: this.t('homeview.carts'),
          description: this.t('homeview.shopping-at-home'),
          icon: "fas fa-shopping-cart"
        },
        {
          title: this.t('homeview.transportation'),
          description: this.t('homeview.transportation-old'),
          icon: "fas fa-user-friends"
        },
        {
          title: this.t('homeview.purchase'),
          description: this.t('homeview.ask'),
          icon: "fas fa-globe-europe"
        },
        {
          title: this.t('homeview.pet'),
          description: this.t('homeview.pet-sitting'),
          icon: "fas fa-paw"
        },
        {
          title: this.t('homeview.home-service'),
          description: this.t('homeview.home-service-description'),
          icon: "fas fa-home"
        }
      ],
      profiles: [
        {
          type: this.t('homeview.client'),
          description: this.t('homeview.client-description'),
          largeIcon: "fas fa-user-circle",
          path: "/client",
          icon: "fas fa-user",
          benefits: [
            this.t('homeview.client-benefit-1'),
            this.t('homeview.client-benefit-2'),
            this.t('homeview.client-benefit-3'),
            this.t('homeview.client-benefit-4')
          ]
        },
        {
          type: this.t('homeview.merchant'),
          description: this.t('homeview.merchant-description'),
          largeIcon: "fas fa-store-alt",
          path: "/commerçant",
          icon: "fas fa-store",
          benefits: [
            this.t('homeview.merchant-benefit-1'),
            this.t('homeview.merchant-benefit-2'),
            this.t('homeview.merchant-benefit-3'),
            this.t('homeview.merchant-benefit-4')
          ]
        },
        {
          type: this.t('homeview.deliverer'),
          description: this.t('homeview.deliverer-description'),
          largeIcon: "fas fa-biking",
          path: "/livreur",
          icon: "fas fa-bicycle",
          benefits: [
            this.t('homeview.deliverer-benefit-1'),
            this.t('homeview.deliverer-benefit-2'),
            this.t('homeview.deliverer-benefit-3'),
            this.t('homeview.deliverer-benefit-4')
          ]
        },
        {
          type: this.t('homeview.service-provider'),
          description: this.t('homeview.service-provider-description'),
          largeIcon: "fas fa-hands-helping",
          path: "/prestataire",
          icon: "fas fa-tools",
          benefits: [
            this.t('homeview.service-provider-benefit-1'),
            this.t('homeview.service-provider-benefit-2'),
            this.t('homeview.service-provider-benefit-3'),
            this.t('homeview.service-provider-benefit-4')
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
            <span class="highlight">{{ t('homeview.reinvent')}}</span> {{ t('homeview.delivery')}}
          </h1>
          <h2 class="hero-subtitle">{{ t('homeview.e-c-l')}}</h2>
          <p class="hero-text">
            {{ t('homeview.at-ecodeli') }}
          </p>
          <div class="hero-buttons">
            <router-link to="/register" class="btn btn-primary">{{ t('homeview.join-us') }}</router-link>
            <router-link to="/services" class="btn btn-secondary">{{ t('homeview.discover-us') }}</router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="services" id="services">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">{{ t('homeview.our-services')}}</h2>
          <p class="section-description">
            <strong>{{ t('homeview.ecodeli')}}</strong> {{ t('homeview.offers')}}
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
              <router-link to="/services" class="service-link">{{ t('homeview.learn-more') }}</router-link>
            </div>
          </div>
        </div>
      </div>

      <div class="eco-banner">
        <div class="eco-banner-content">
          <h3>{{ t('homeview.service-flexible') }} <span>{{ t('homeview.flexible') }}</span>, <span>{{ t('homeview.ethical') }}</span> {{ t('homeview.and-adapted-to') }} <span>{{ t('homeview.everyone') }}</span> !</h3>
          <p>{{ t('homeview.commitment-sustainable') }}</p>
        </div>
      </div>
    </section>

    <section class="profiles" id="profiles">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">{{ t('homeview.who-can-use') }}</h2>
          <p class="section-description">
            {{ t('homeview.platform-adapts') }}
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
                    {{ t('homeview.i-am') }} {{ profiles[activeProfile].type.toLowerCase() }}
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
        <h2>{{ t('homeview.ready-to-join') }}</h2>
        <p>{{ t('homeview.register-now') }}</p>
        <router-link to="/register" class="btn btn-large">{{ t('homeview.register-free') }}</router-link>
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