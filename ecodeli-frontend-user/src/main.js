import './assets/main.css'
import './assets/theme.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import 'primeicons/primeicons.css'

import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Card from 'primevue/card'
import Menu from 'primevue/menu'
import Avatar from 'primevue/avatar'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'
import Sidebar from 'primevue/sidebar'
import Menubar from 'primevue/menubar'
import Dropdown from 'primevue/dropdown'
import InlineMessage from 'primevue/inlinemessage'
import Divider from 'primevue/divider'
import Skeleton from 'primevue/skeleton'
import Message from 'primevue/message'
import Textarea from 'primevue/textarea'
import InputNumber from 'primevue/inputnumber'
import Checkbox from 'primevue/checkbox'
import ProgressSpinner from 'primevue/progressspinner'
import Tag from 'primevue/tag'
import ConfirmDialog from 'primevue/confirmdialog'
import ConfirmationService from 'primevue/confirmationservice'
import Dialog from 'primevue/dialog'
import InputOtp from 'primevue/inputotp'
import Calendar from 'primevue/calendar'

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

app.component('Button', Button)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Card', Card)
app.component('Menu', Menu)
app.component('Avatar', Avatar)
app.component('Toast', Toast)
app.component('Sidebar', Sidebar)
app.component('Menubar', Menubar)
app.component('Dropdown', Dropdown)
app.component('InlineMessage', InlineMessage)
app.component('Divider', Divider)
app.component('Skeleton', Skeleton)
app.component('Message', Message)
app.component('Textarea', Textarea)
app.component('InputNumber', InputNumber)
app.component('Checkbox', Checkbox)
app.component('ProgressSpinner', ProgressSpinner)
app.component('Tag', Tag)
app.component('ConfirmDialog', ConfirmDialog)
app.component('Dialog', Dialog)
app.component('InputOtp', InputOtp)
app.component('Calendar', Calendar)

app.use(createPinia())
app.use(router)

app.mount('#app')
