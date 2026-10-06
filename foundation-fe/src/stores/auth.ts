import { defineStore } from 'pinia'
import { getUserInfo, login } from '@/api/auth'
import type { LoginRequest, UserInfo } from '@/api/auth/types'
import type { MenuItem } from '@/api/system/types'
import { tokenStorage } from '@/utils/storage'

export const useAuthStore = defineStore('auth', {
  state: () => ({ token: tokenStorage.get(), userInfo: null as UserInfo | null }),
  getters: {
    menus: (state): MenuItem[] => state.userInfo?.menus || [],
    permissions: (state) => state.userInfo?.permissions || [],
  },
  actions: {
    async signIn(payload: LoginRequest) {
      const { data } = await login(payload)
      this.token = data.data.access_token
      tokenStorage.set(this.token)
      await this.loadUserInfo()
    },
    async loadUserInfo() {
      const { data } = await getUserInfo()
      this.userInfo = data.data
    },
    signOut() {
      this.token = ''
      this.userInfo = null
      tokenStorage.clear()
    },
    hasPermission(code: string) {
      return this.permissions.includes(code) || this.permissions.includes('*')
    },
  },
})
