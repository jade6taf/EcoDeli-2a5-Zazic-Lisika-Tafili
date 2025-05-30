import { reactive } from 'vue'
import i18n from '@/i18n'

export const languageStore = reactive({
  currentLanguage: 'fr',

  init() {
    const savedLanguage = localStorage.getItem('ecodeli-language')
    const browserLanguage = navigator.language.split('-')[0]

    if (savedLanguage && ['fr', 'en'].includes(savedLanguage)) {
      this.currentLanguage = savedLanguage
    } else if (['fr', 'en'].includes(browserLanguage)) {
      this.currentLanguage = browserLanguage
    } else {
      this.currentLanguage = 'fr'
    }
    i18n.global.locale.value = this.currentLanguage
  },

  toggleLanguage() {
    this.currentLanguage = this.currentLanguage === 'fr' ? 'en' : 'fr'
    localStorage.setItem('ecodeli-language', this.currentLanguage)
    i18n.global.locale.value = this.currentLanguage
  },

  setLanguage(language) {
    if (['fr', 'en'].includes(language)) {
      this.currentLanguage = language
      localStorage.setItem('ecodeli-language', this.currentLanguage)
      i18n.global.locale.value = this.currentLanguage
    }
  }
})
