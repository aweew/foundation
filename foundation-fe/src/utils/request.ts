import type {AxiosError, AxiosResponse} from 'axios'
import axios from 'axios'
import {ElMessage} from 'element-plus'
import {tokenStorage} from './storage'

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 15000,
    headers: {'Content-Type': 'application/json'}
})

request.interceptors.request.use((config) => {
    const token = tokenStorage.get()
    if (token) config.headers.set('satoken', token)
    return config
})

request.interceptors.response.use((response: AxiosResponse) => {
    const result = response.data
    if (typeof result?.code === 'number' && result.code !== 0) {
        ElMessage.error(result.msg || '请求失败')
        return Promise.reject(new Error(result.msg || '请求失败'))
    }
    return response
}, (error: AxiosError<{ msg?: string }>) => {
    if (error.response?.status === 401) {
        tokenStorage.clear()
        if (location.pathname !== '/login') location.href = '/login'
    }
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    return Promise.reject(error)
})

export default request
