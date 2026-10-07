import request from '@/utils/request'
import { hashPassword } from '@/utils/password'
import type { ApiResult } from '@/types/api'
import type { LoginRequest, LoginResponse, UserInfo } from './types'

/**
 * 用户登录
 * @param payload 登录参数
 */
export const login = async (payload: LoginRequest) => {
  const requestPayload = {
    ...payload,
    password: await hashPassword(payload.password),
  }
  return request<ApiResult<LoginResponse>>({
    url: '/auth/login',
    method: 'post',
    data: requestPayload,
  })
}

/**
 * 查询当前用户信息
 */
export const getUserInfo = () => {
  return request<ApiResult<UserInfo>>({
    url: '/auth/userInfo',
    method: 'get',
  })
}
