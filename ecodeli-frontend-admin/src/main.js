import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// PrimeVue imports
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import 'primeicons/primeicons.css'

// PrimeVue components pour admin
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'
import Menubar from 'primevue/menubar'
import Sidebar from 'primevue/sidebar'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Dropdown from 'primevue/dropdown'
import InlineMessage from 'primevue/inlinemessage'
import Divider from 'primevue/divider'
import Tag from 'primevue/tag'
import ConfirmDialog from 'primevue/confirmdialog'
import ConfirmationService from 'primevue/confirmationservice'

const app = createApp(App)

app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            prefix: 'p',
            darkModeSelector: '.p-dark',
            cssLayer: false
        }
    }
})

app.use(ToastService)
app.use(ConfirmationService)

// Composants globaux PrimeVue
app.component('Button', Button)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Card', Card)
app.component('DataTable', DataTable)
app.component('Column', Column)
app.component('Dialog', Dialog)
app.component('Toast', Toast)
app.component('Menubar', Menubar)
app.component('Sidebar', Sidebar)
app.component('Avatar', Avatar)
app.component('Badge', Badge)
app.component('Dropdown', Dropdown)
app.component('InlineMessage', InlineMessage)
app.component('Divider', Divider)
app.component('Tag', Tag)
app.component('ConfirmDialog', ConfirmDialog)

app.use(createPinia())
app.use(router)

app.mount('#app')
