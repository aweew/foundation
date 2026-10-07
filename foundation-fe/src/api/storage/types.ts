export interface StorageProviderConfig {
  id: number;
  providerCode: string;
  providerName: string;
  enabled: boolean;
  isDefault: boolean;
  endpoint?: string;
  region?: string;
  serviceName?: string;
  accessDomain?: string;
  basePath?: string;
  privateBucket: boolean;
  credentialConfigured: boolean;
  remark?: string;
  configVersion: number;
  version?: number;
  updateTime?: string;
}

export interface StorageProviderConfigForm {
  id?: number;
  providerCode: string;
  providerName: string;
  enabled: boolean;
  endpoint: string;
  region: string;
  accessKey: string;
  secretAccessKey: string;
  serviceName: string;
  accessDomain: string;
  basePath: string;
  privateBucket: boolean;
  operator: string;
  password: string;
  remark: string;
  version?: number;
}

export interface StorageFile {
  id: number;
  originalName: string;
  objectKey: string;
  providerCode: string;
  contentType?: string;
  fileSize: number;
  extension?: string;
  businessType?: string;
  businessId?: string;
  status: string;
  accessUrl?: string;
  createTime?: string;
}

export interface StorageFileQuery extends QueryPage {
  originalName?: string;
  businessType?: string;
  businessId?: string;
  providerCode?: string;
  status?: string;
  startTime?: string;
  endTime?: string;
}
import type { QueryPage } from '@/api/system/types';
