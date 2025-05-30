import { createI18n } from 'vue-i18n'
import fr from './fr.json'
import en from './en.json'

const i18n = createI18n({
    legacy: false,
    locale: 'en',
    fallbackLocale: 'fr',
    messages: {
        fr,
        en
    }
})

export default i18n
