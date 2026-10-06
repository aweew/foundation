import { reactive } from 'vue';
import { getEnumList } from '@/api/system';
import type { EnumDictionary } from '@/api/system/types';

const enumDictionary = reactive<EnumDictionary>({});
let loadingPromise: Promise<EnumDictionary> | undefined;

/**
 * 提供共享的枚举字典及加载方法
 */
export const useEnumDictionary = () => {
  /**
   * 加载枚举字典，并复用进行中的请求避免重复访问接口
   * @param enumName 可选的枚举名称，已加载时直接复用缓存
   */
  const loadEnums = async (enumName?: string) => {
    if (enumName && enumDictionary[enumName]) return enumDictionary;
    if (!enumName && Object.keys(enumDictionary).length > 0) return enumDictionary;
    if (!loadingPromise) {
      loadingPromise = getEnumList()
        .then((response) => {
          Object.assign(enumDictionary, response.data.data);
          return enumDictionary;
        })
        .finally(() => {
          loadingPromise = undefined;
        });
    }
    return loadingPromise;
  };

  return { enumDictionary, loadEnums };
};
