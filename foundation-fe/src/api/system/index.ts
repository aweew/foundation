import request from '@/utils/request'
import type { ApiResult, PageResponse } from '@/types/api'
import type { EnumDictionary, MenuItem, QueryPage, SystemRecord, SystemResource } from './types'

/**
 * 查询枚举字典
 */
export const getEnumList = () => {
  return request<ApiResult<EnumDictionary>>({
    url: '/enum/list',
    method: 'get',
  });
};

/**
 * 查询系统资源详情
 * @param resource 资源类型
 * @param id 资源ID
 */
export const getSystemItem = (resource: SystemResource, id: string | number) => {
  return request<ApiResult<SystemRecord | null>>({
    url: `/sys/${resource}/${id}`,
    method: 'get',
  });
};

/**
 * 新增系统资源
 * @param resource 资源类型
 * @param payload 资源表单
 */
export const createSystemItem = (resource: SystemResource, payload: SystemRecord) => {
  return request<ApiResult<void>>({
    url: `/sys/${resource}`,
    method: 'post',
    data: payload,
  });
};

/**
 * 修改系统资源
 * @param resource 资源类型
 * @param payload 资源表单
 */
export const updateSystemItem = (resource: SystemResource, payload: SystemRecord) => {
  return request<ApiResult<void>>({
    url: `/sys/${resource}`,
    method: 'put',
    data: payload,
  });
};

/**
 * 删除系统资源
 * @param resource 资源类型
 * @param id 资源ID
 */
export const deleteSystemItem = (resource: SystemResource, id: string | number) => {
  return request<ApiResult<void>>({
    url: `/sys/${resource}/${id}`,
    method: 'delete',
  });
};

/**
 * 分页查询用户列表
 * @param params 分页查询参数
 */
export const getUserPage = (params: QueryPage) => {
  return request<ApiResult<PageResponse<Record<string, unknown>>>>({
    url: '/sys/user/page',
    method: 'get',
    params,
  });
};

/**
 * 分页查询角色列表
 * @param params 分页查询参数
 */
export const getRolePage = (params: QueryPage) => {
  return request<ApiResult<PageResponse<Record<string, unknown>>>>({
    url: '/sys/role/page',
    method: 'get',
    params,
  });
};

/**
 * 分页查询菜单列表
 * @param params 分页查询参数
 */
export const getMenuPage = (params: QueryPage) => {
  return request<ApiResult<PageResponse<MenuItem>>>({
    url: '/sys/menu/page',
    method: 'get',
    params,
  });
};
