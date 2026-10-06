export interface QueryPage {
  current: number;
  size: number;

  [key: string]: unknown
}

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

export interface EnumOption {
  name: string;
  value: string | number;
  label: string
}

export type EnumDictionary = Record<string, EnumOption[]>

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
