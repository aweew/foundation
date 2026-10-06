import request from '@/utils/request'
import type { ApiResult, EnumDictionary, MenuItem, PageResponse } from '@/types/api'

export interface QueryPage {
  current: number;
  size: number;

  [key: string]: unknown
}

export const getEnumList = () => request.get<ApiResult<EnumDictionary>>('/enum/list')

export type SystemResource = 'user' | 'role' | 'menu'
export type SystemRecord = Record<string, string | number | boolean | null | undefined>

export interface SystemFormField {
  prop: string;
  label: string;
  type?: 'text' | 'textarea' | 'password' | 'number' | 'select' | 'switch';
  required?: boolean;
  createOnly?: boolean;
  defaultValue?: string | number | boolean;
  options?: { value: string | number; label: string }[]
}

export const getSystemItem = (resource: SystemResource, id: string | number) => request.get<ApiResult<SystemRecord | null>>(`/sys/${resource}/${id}`)
export const createSystemItem = (resource: SystemResource, payload: SystemRecord) => request.post<ApiResult<void>>(`/sys/${resource}`, payload)
export const updateSystemItem = (resource: SystemResource, payload: SystemRecord) => request.put<ApiResult<void>>(`/sys/${resource}`, payload)
export const deleteSystemItem = (resource: SystemResource, id: string | number) => request.delete<ApiResult<void>>(`/sys/${resource}/${id}`)

export const getUserPage = (params: QueryPage) => request.get<ApiResult<PageResponse<Record<string, unknown>>>>('/sys/user/page', { params })
export const getRolePage = (params: QueryPage) => request.get<ApiResult<PageResponse<Record<string, unknown>>>>('/sys/role/page', { params })
export const getMenuPage = (params: QueryPage) => request.get<ApiResult<PageResponse<MenuItem>>>('/sys/menu/page', { params })
