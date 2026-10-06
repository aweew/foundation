import request from '@/utils/request'
import type { ApiResult, LoginRequest, LoginResponse, UserInfo } from '@/types/api'

export const login = (payload: LoginRequest) => request.post<ApiResult<LoginResponse>>('/auth/login', payload)
export const getUserInfo = () => request.get<ApiResult<UserInfo>>('/auth/userInfo')
