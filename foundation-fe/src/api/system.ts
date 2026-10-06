import request from '@/utils/request'
import type {ApiResult, MenuItem, PageResponse} from '@/types/api'

export interface QueryPage {
    current: number;
    size: number;

    [key: string]: unknown
}

export const getUserPage = (params: QueryPage) => request.get<ApiResult<PageResponse<Record<string, unknown>>>>('/sys/user/page', {params})
export const getRolePage = (params: QueryPage) => request.get<ApiResult<PageResponse<Record<string, unknown>>>>('/sys/role/page', {params})
export const getMenuPage = (params: QueryPage) => request.get<ApiResult<PageResponse<MenuItem>>>('/sys/menu/page', {params})
