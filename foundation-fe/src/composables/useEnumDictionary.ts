import {reactive} from 'vue'
import {getEnumList} from '@/api/system'
import type {EnumDictionary} from '@/types/api'

const enumDictionary = reactive<EnumDictionary>({})
let loadingPromise: Promise<EnumDictionary> | undefined

export const useEnumDictionary = () => {
  const loadEnums = async (enumName?: string) => {
    if (enumName && enumDictionary[enumName]) return enumDictionary
    if (!enumName && Object.keys(enumDictionary).length > 0) return enumDictionary
    if (!loadingPromise) {
      loadingPromise = getEnumList().then(response => {
        Object.assign(enumDictionary, response.data.data)
        return enumDictionary
      }).finally(() => {
        loadingPromise = undefined
      })
    }
    return loadingPromise
  }

  return {enumDictionary, loadEnums}
}
