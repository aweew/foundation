import type { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { tokenStorage } from './storage';

let refreshPromise: Promise<string> | undefined;
let lastTraceId = '';

export const getLastTraceId = () => lastTraceId;

type RetryRequestConfig = AxiosRequestConfig & { _retry?: boolean };

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
    lastTraceId = String(response.headers?.['traceId'] || response.headers?.['trace-id'] || response.data?.traceId || '');
    const result = response.data;
    if (typeof result?.code === 'number' && result.code !== 0) {
      const error = new Error(result.msg || '请求失败');
      Object.assign(error, { businessError: true, code: result.code });
      return Promise.reject(error);
    }
    return response;
  },
  // 统一展示接口错误，并将认证失效导回登录页
  async (error: AxiosError<{ msg?: string }>) => {
    const config = error.config as RetryRequestConfig | undefined;
    if (error.response?.status === 401 && config && !config._retry && !config.url?.endsWith('/auth/refresh')) {
      config._retry = true;
      refreshPromise ??= axios
        .post(`${import.meta.env.VITE_API_BASE_URL}/auth/refresh`, undefined, {
          headers: { satoken: tokenStorage.get() },
        })
        .then((response) => response.data.data.access_token as string)
        .finally(() => {
          refreshPromise = undefined;
        });
      try {
        const token = await refreshPromise;
        tokenStorage.set(token);
        config.headers = { ...config.headers, satoken: token } as typeof config.headers;
        return request(config);
      } catch {
        tokenStorage.clear();
        tokenStorage.clearRefresh();
        if (location.pathname !== '/login') location.href = '/login';
      }
    }
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常');
    return Promise.reject(error);
  },
);

export default request;
