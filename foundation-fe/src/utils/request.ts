import type { AxiosError, AxiosResponse } from 'axios';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { tokenStorage } from './storage';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求阶段统一注入登录令牌，避免各业务 API 重复处理认证信息
request.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) config.headers.set('satoken', token);
  return config;
});

request.interceptors.response.use(
  // 响应阶段保持统一响应结构，调用方只处理业务数据
  (response: AxiosResponse) => {
    const result = response.data;
    if (typeof result?.code === 'number' && result.code !== 0) {
      ElMessage.error(result.msg || '请求失败');
      return Promise.reject(new Error(result.msg || '请求失败'));
    }
    return response;
  },
  // 统一展示接口错误，并将认证失效导回登录页
  (error: AxiosError<{ msg?: string }>) => {
    if (error.response?.status === 401) {
      tokenStorage.clear();
      if (location.pathname !== '/login') location.href = '/login';
    }
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常');
    return Promise.reject(error);
  },
);

export default request;
