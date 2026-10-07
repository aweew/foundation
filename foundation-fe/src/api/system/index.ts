import request from '@/utils/request';
import { hashPassword } from '@/utils/password';
import type { ApiResult, PageResponse } from '@/types/api';
import type {
  EnumDictionary,
  MenuItem,
  OperationLog,
  OperationLogQuery,
  QueryPage,
  SystemRecord,
  SystemResource,
} from './types';

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
export const createSystemItem = async (resource: SystemResource, payload: SystemRecord) => {
  const requestPayload = await prepareUserPassword(resource, payload);
  return request<ApiResult<void>>({
    url: `/sys/${resource}`,
    method: 'post',
    data: requestPayload,
  });
};

/**
 * 修改系统资源
 * @param resource 资源类型
 * @param payload 资源表单
 */
export const updateSystemItem = async (resource: SystemResource, payload: SystemRecord) => {
  const requestPayload = await prepareUserPassword(resource, payload);
  return request<ApiResult<void>>({
    url: `/sys/${resource}`,
    method: 'put',
    data: requestPayload,
  });
};

/**
 * 为用户新增或修改请求生成密码摘要
 * @param resource 系统资源类型
 * @param payload 请求数据
 */
const prepareUserPassword = async (resource: SystemResource, payload: SystemRecord) => {
  if (resource !== 'user' || typeof payload.password !== 'string') {
    return payload;
  }
  return {
    ...payload,
    password: await hashPassword(payload.password),
  };
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

/**
 * 分页查询操作审计日志
 * @param params 审计日志筛选和分页参数
 */
export const getOperationLogPage = (params: OperationLogQuery) => {
  return request<ApiResult<PageResponse<OperationLog>>>({
    url: '/sys/operation-log/page',
    method: 'get',
    params,
  });
};
