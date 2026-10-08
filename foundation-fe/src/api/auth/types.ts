import type { MenuItem } from '@/api/system/types';

export interface LoginRequest {
  clientId: string;
  clientKey?: string;
  clientSecret?: string;
  grantType: string;
  phone: string;
  password: string;
}

export interface LoginResponse {
  access_token: string;
  refresh_token?: string;
  expire_in?: number;
  refresh_expire_in?: number;
  client_id?: string;
  scope?: string;
}

export interface UserInfo {
  id: string | number;
  phone?: string;
  nickName?: string;
  realName?: string;
  avatar?: string;
  roles: RoleInfo[];
  menus: MenuItem[];
  permissions: string[];
}

export interface RoleInfo {
  id?: string | number;
  code?: string;
  name?: string;
}
