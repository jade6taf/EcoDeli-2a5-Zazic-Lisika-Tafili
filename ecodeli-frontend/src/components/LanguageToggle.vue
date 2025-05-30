<script>
import { languageStore } from '@/store/language.js'
import { useI18n } from 'vue-i18n'

export default {
  name: 'LanguageToggle',
  setup() {
    const { t } = useI18n()
    return { t }
  },
  computed: {
    currentLanguage() {
      return languageStore.currentLanguage
    },
    isEnglish() {
      return this.currentLanguage === 'en'
    },
    flagIcon() {
      return this.currentLanguage === 'fr' ? '🇫🇷' : '🇺🇸'
    },
    tooltipText() {
      return this.t('language.switch-to')
    }
  },
  methods: {
    toggleLanguage() {
      languageStore.toggleLanguage()
    }
  }
}
</script>

<template>
  <button
    @click="toggleLanguage"
    class="language-toggle"
    :title="tooltipText">
    <span class="flag-icon">{{ flagIcon }}</span>
    <span class="language-text">{{ currentLanguage.toUpperCase() }}</span>
    <span class="sr-only">
      {{ tooltipText }}
    </span>
  </button>
</template>

<style scoped>
.language-toggle {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  height: 40px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-color);
  gap: 6px;
  min-width: 60px;
}

.language-toggle:hover {
  background-color: var(--hover-bg);
  transform: scale(1.05);
  border-color: var(--primary-color);
}

.flag-icon {
  font-size: 1rem;
  transition: transform 0.3s ease;
}

.language-text {
  font-size: 0.75rem;
  font-weight: 600;
  transition: color 0.3s ease;
}

.language-toggle:hover .flag-icon {
  transform: scale(1.1);
}

.language-toggle:hover .language-text {
  color: var(--primary-color);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
