import request from '@/utils/request';
import type { ApiResult, PageResponse } from '@/types/api';
import type { StorageFile, StorageFileQuery, StorageProviderConfig, StorageProviderConfigForm } from './types';

/**
 * 查询云存储厂商配置
 */
export const getStorageProviderConfigs = () => {
  return request<ApiResult<StorageProviderConfig[]>>({
    url: '/sys/storage/provider/list',
    method: 'get',
  });
};

/**
 * 新增云存储厂商配置
 * @param payload 配置表单
 */
export const createStorageProviderConfig = (payload: StorageProviderConfigForm) => {
  return request<ApiResult<void>>({
    url: '/sys/storage/provider',
    method: 'post',
    data: payload,
  });
};

/**
 * 修改云存储厂商配置
 * @param payload 配置表单
 */
export const updateStorageProviderConfig = (payload: StorageProviderConfigForm) => {
  return request<ApiResult<void>>({
    url: '/sys/storage/provider',
    method: 'put',
    data: payload,
  });
};

/**
 * 激活云存储厂商配置
 * @param id 配置 ID
 */
export const activateStorageProviderConfig = (id: number) => {
  return request<ApiResult<void>>({
    url: `/sys/storage/provider/${id}/activate`,
    method: 'post',
  });
};

/**
 * 测试云存储厂商配置
 * @param id 配置 ID
 */
export const testStorageProviderConfig = (id: number) => {
  return request<ApiResult<void>>({
    url: `/sys/storage/provider/${id}/test`,
    method: 'post',
  });
};

/**
 * 删除云存储厂商配置
 * @param id 配置 ID
 */
export const deleteStorageProviderConfig = (id: number) => {
  return request<ApiResult<void>>({
    url: `/sys/storage/provider/${id}`,
    method: 'delete',
  });
};

/**
 * 分页查询文件
 * @param params 文件筛选和分页参数
 */
export const getStorageFilePage = (params: StorageFileQuery) => {
  return request<ApiResult<PageResponse<StorageFile>>>({
    url: '/sys/storage/files/page',
    method: 'get',
    params,
  });
};

/**
 * 获取文件最新访问地址
 * @param id 文件 ID
 */
export const getStorageFileAccessUrl = (id: number) => {
  return request<ApiResult<StorageFile>>({
    url: `/storage/files/${id}/access-url`,
    method: 'get',
  });
};

/**
 * 上传文件
 * @param file 文件
 * @param businessType 业务类型
 * @param businessId 业务 ID
 */
export const uploadStorageFile = (file: File, businessType?: string, businessId?: string) => {
  const formData = new FormData();
  formData.append('file', file);
  if (businessType) formData.append('businessType', businessType);
  if (businessId) formData.append('businessId', businessId);
  return request<ApiResult<StorageFile>>({
    url: '/storage/files',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 删除文件
 * @param id 文件 ID
 */
export const deleteStorageFile = (id: number) => {
  return request<ApiResult<void>>({
    url: `/sys/storage/files/${id}`,
    method: 'delete',
  });
};
