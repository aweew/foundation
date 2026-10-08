import request from '@/utils/request';
import { hashPassword } from '@/utils/password';
import type { ApiResult, PageResponse } from '@/types/api';
import type {
  EnumDictionary,
  MenuItem,
  OperationLog,
  OperationLogQuery,
  QueryPage,
  SystemPageRecord,
  SystemRecord,
  SystemResource,
  RoleMenuRelation,
  UserRoleRelation,
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
  return request<ApiResult<PageResponse<SystemPageRecord>>>({
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
  return request<ApiResult<PageResponse<SystemPageRecord>>>({
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

/**
 * 查询用户已分配的角色
 * @param userId 用户 ID
 */
export const getUserRoles = (userId: string | number) => {
  return request<ApiResult<UserRoleRelation[]>>({
    url: `/sys/user-role/user/${userId}`,
    method: 'get',
  });
};

/**
 * 分配角色给用户
 * @param userId 用户 ID
 * @param roleId 角色 ID
 */
export const assignUserRole = (userId: string | number, roleId: string | number) => {
  return request<ApiResult<void>>({
    url: '/sys/user-role',
    method: 'post',
    data: { userId, roleId },
  });
};

/**
 * 删除用户角色关联
 * @param id 关联 ID
 */
export const deleteUserRole = (id: string | number) => {
  return request<ApiResult<void>>({
    url: `/sys/user-role/${id}`,
    method: 'delete',
  });
};

/**
 * 查询全部启用菜单树
 */
export const getMenuTree = () => {
  return request<ApiResult<MenuItem[]>>({
    url: '/sys/menu/tree',
    method: 'get',
  });
};

/**
 * 查询角色已分配的菜单树
 * @param roleId 角色 ID
 */
export const getRoleMenuTree = (roleId: string | number) => {
  return request<ApiResult<MenuItem[]>>({
    url: `/sys/menu/tree/role/${roleId}`,
    method: 'get',
  });
};

/**
 * 查询角色菜单关联记录
 * @param roleId 角色 ID
 */
export const getRoleMenus = (roleId: string | number) => {
  return request<ApiResult<RoleMenuRelation[]>>({
    url: `/sys/role-menu/role/${roleId}`,
    method: 'get',
  });
};

/**
 * 分配菜单给角色
 * @param roleId 角色 ID
 * @param menuId 菜单 ID
 */
export const assignRoleMenu = (roleId: string | number, menuId: string | number) => {
  return request<ApiResult<void>>({
    url: '/sys/role-menu',
    method: 'post',
    data: { roleId, menuId },
  });
};

/**
 * 删除角色菜单关联
 * @param id 关联 ID
 */
export const deleteRoleMenu = (id: string | number) => {
  return request<ApiResult<void>>({
    url: `/sys/role-menu/${id}`,
    method: 'delete',
  });
};
