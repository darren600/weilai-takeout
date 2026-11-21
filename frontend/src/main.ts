import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

import './assets/main.css'
import './styles/theme.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
// ElementPlus 使用中文语言包，全局配置尺寸为默认
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
