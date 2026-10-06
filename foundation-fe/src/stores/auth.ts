import { defineStore } from 'pinia';
import { getUserInfo, login } from '@/api/auth';
import type { LoginRequest, UserInfo } from '@/api/auth/types';
import type { MenuItem } from '@/api/system/types';
import { tokenStorage } from '@/utils/storage';

export const useAuthStore = defineStore('auth', {
  state: () => ({ token: tokenStorage.get(), userInfo: null as UserInfo | null }),
  getters: {
    menus: (state): MenuItem[] => state.userInfo?.menus || [],
    permissions: (state) => state.userInfo?.permissions || [],
  },
  actions: {
    /**
     * 提交登录信息并初始化当前用户数据
     * @param payload 登录请求参数
     */
    async signIn(payload: LoginRequest) {
      const { data } = await login(payload);
      this.token = data.data.access_token;
      tokenStorage.set(this.token);
      await this.loadUserInfo();
    },
    /**
     * 查询并缓存当前登录用户信息
     */
    async loadUserInfo() {
      const { data } = await getUserInfo();
      this.userInfo = data.data;
    },
    /**
     * 清理令牌和用户信息，结束当前会话
     */
    signOut() {
      this.token = '';
      this.userInfo = null;
      tokenStorage.clear();
    },
    /**
     * 判断当前用户是否拥有指定权限
     * @param code 权限编码
     */
    hasPermission(code: string) {
      return this.permissions.includes(code) || this.permissions.includes('*');
    },
  },
});
