<script>
import { useI18n } from 'vue-i18n'
import { authStore } from '@/store/auth'

export default {
  name: 'CommercantDashboard',
  setup() {
    const { t } = useI18n()
    return { t }
  },
  computed: {
    user() {
      return authStore.user
    },
    nomCommerce() {
      return this.user?.nomCommerce || 'Votre commerce'
    }
  }
}
</script>

<template>
  <div class="commercant-dashboard">
    <div class="dashboard-header">
      <div class="header-content">
        <div class="welcome-section">
          <div class="welcome-icon">
            <i class="fas fa-store"></i>
          </div>
          <div class="welcome-text">
            <h1>{{ t('commercant.dashboard.welcome') }}</h1>
            <h2>{{ nomCommerce }} !</h2>
            <p>{{ t('commercant.dashboard.subtitle') }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="content-container">
        <div class="info-card">
          <div class="card-header">
            <i class="fas fa-info-circle"></i>
            <h3>{{ t('commercant.dashboard.development.title') }}</h3>
          </div>
          <div class="card-body">
            <p>{{ t('commercant.dashboard.development.message') }}</p>

            <div class="coming-soon">
              <h4>{{ t('commercant.dashboard.coming-soon') }}</h4>
              <ul class="features-list">
                <li>
                  <i class="fas fa-file-contract"></i>
                  {{ t('commercant.dashboard.features.contracts') }}
                </li>
                <li>
                  <i class="fas fa-bullhorn"></i>
                  {{ t('commercant.dashboard.features.announcements') }}
                </li>
                <li>
                  <i class="fas fa-file-invoice-dollar"></i>
                  {{ t('commercant.dashboard.features.billing') }}
                </li>
                <li>
                  <i class="fas fa-credit-card"></i>
                  {{ t('commercant.dashboard.features.payments') }}
                </li>
                <li>
                  <i class="fas fa-chart-line"></i>
                  {{ t('commercant.dashboard.features.analytics') }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="quick-stats">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="fas fa-store"></i>
            </div>
            <div class="stat-info">
              <h4>{{ t('commercant.dashboard.stats.business') }}</h4>
              <p>{{ t('commercant.dashboard.stats.business-desc') }}</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <i class="fas fa-handshake"></i>
            </div>
            <div class="stat-info">
              <h4>{{ t('commercant.dashboard.stats.partnership') }}</h4>
              <p>{{ t('commercant.dashboard.stats.partnership-desc') }}</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <i class="fas fa-leaf"></i>
            </div>
            <div class="stat-info">
              <h4>{{ t('commercant.dashboard.stats.eco') }}</h4>
              <p>{{ t('commercant.dashboard.stats.eco-desc') }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.commercant-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
}

.dashboard-header {
  background: white;
  box-shadow: var(--shadow);
  border-bottom: 3px solid var(--primary-color);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 2rem;
}

.welcome-section {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow);
}

.welcome-icon i {
  font-size: 2.5rem;
  color: white;
}

.welcome-text h1 {
  font-size: 2.5rem;
  color: var(--text-color);
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}

.welcome-text h2 {
  font-size: 1.8rem;
  color: var(--primary-color);
  margin: 0 0 1rem 0;
  font-weight: 600;
}

.welcome-text p {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}

.dashboard-content {
  padding: 3rem 0;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: grid;
  gap: 2rem;
}

.info-card {
  background: white;
  border-radius: 20px;
  box-shadow: var(--shadow);
  overflow: hidden;
  border: 2px solid var(--border-light);
}

.card-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  padding: 1.5rem 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.card-header i {
  font-size: 1.5rem;
}

.card-header h3 {
  font-size: 1.3rem;
  margin: 0;
  font-weight: 600;
}

.card-body {
  padding: 2rem;
}

.card-body p {
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 2rem;
}

.coming-soon h4 {
  color: var(--text-color);
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
  font-weight: 600;
}

.features-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 1rem;
}

.features-list li {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 12px;
  color: var(--text-color);
  transition: all 0.3s ease;
}

.features-list li:hover {
  transform: translateX(5px);
  box-shadow: var(--shadow);
}

.features-list i {
  color: var(--primary-color);
  font-size: 1.2rem;
  width: 20px;
  text-align: center;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.stat-card {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: var(--shadow);
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
  border-color: var(--primary-color);
}

.stat-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon i {
  font-size: 1.5rem;
  color: white;
}

.stat-info h4 {
  font-size: 1.2rem;
  color: var(--text-color);
  margin: 0 0 0.5rem 0;
  font-weight: 600;
}

.stat-info p {
  font-size: 0.95rem;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}

/* Responsive Design */
@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 1.5rem;
  }

  .welcome-text h1 {
    font-size: 2rem;
  }

  .welcome-text h2 {
    font-size: 1.5rem;
  }

  .header-content,
  .content-container {
    padding: 2rem 1rem;
  }

  .card-body {
    padding: 1.5rem;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
}

/* Dark Theme Support */
[data-theme="dark"] .commercant-dashboard {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d3e2d 100%);
}

[data-theme="dark"] .dashboard-header {
  background: var(--bg-secondary);
}

[data-theme="dark"] .info-card,
[data-theme="dark"] .stat-card {
  background: var(--bg-secondary);
}

[data-theme="dark"] .features-list li {
  background: var(--bg-color);
}
</style>
