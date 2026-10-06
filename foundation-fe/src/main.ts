import {createApp} from 'vue'
import {createPinia} from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './styles.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
Object.entries(ElementPlusIconsVue).forEach(([name, component]) => app.component(name, component))
app.use(createPinia()).use(router).use(ElementPlus).mount('#app')
