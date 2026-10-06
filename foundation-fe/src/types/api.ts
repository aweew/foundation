export interface ApiResult<T> {
    code: number;
    data: T;
    msg: string
}

export interface EnumOption {
    name: string;
    value: string | number;
    label: string
}

export type EnumDictionary = Record<string, EnumOption[]>

export interface LoginRequest {
    clientId: string
    clientKey?: string
    clientSecret?: string
    grantType: string
    phone: string
    password: string
}

export interface LoginResponse {
    access_token: string;
    refresh_token?: string;
    expire_in?: number;
    refresh_expire_in?: number;
    client_id?: string;
    scope?: string
}

export interface MenuItem {
    id: number;
    name: string;
    title: string;
    code?: string;
    parentId?: number;
    type: number;
    icon?: string;
    path?: string;
    component?: string;
    isCache?: boolean;
    isVisible?: boolean;
    sort?: number;
    childList?: MenuItem[]
}

export interface UserInfo {
    id: number;
    phone?: string;
    nickName?: string;
    realName?: string;
    avatar?: string;
    roles: unknown[];
    menus: MenuItem[];
    permissions: string[]
}

export interface PageResponse<T> {
    records: T[];
    total: number;
    size: number;
    current: number;
    pages: number
}
