import { defineStore } from 'pinia';
import { getUserInfo, login, logout, refreshToken } from '@/api/auth';
import type { LoginRequest, UserInfo } from '@/api/auth/types';
import type { MenuItem } from '@/api/system/types';
import { tokenStorage } from '@/utils/storage';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: tokenStorage.get(),
    refreshToken: tokenStorage.getRefresh(),
    userInfo: null as UserInfo | null,
  }),
  getters: {
    menus: (state): MenuItem[] => state.userInfo?.menus || [],
    permissions: (state) => state.userInfo?.permissions || [],
    isSuperAdmin: (state) =>
      state.userInfo?.roles?.some((role) => role.code?.trim().toUpperCase() === 'SUPER_ADMIN') || false,
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
      if (data.data.refresh_token) {
        this.refreshToken = data.data.refresh_token;
        tokenStorage.setRefresh(this.refreshToken);
      }
      await this.loadUserInfo();
    },
    /**
     * 续期当前登录令牌并更新本地会话
     */
    async renew() {
      const { data } = await refreshToken();
      this.token = data.data.access_token;
      tokenStorage.set(this.token);
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
    async signOut() {
      try {
        if (this.token) await logout();
      } catch {
        // 本地会话仍需清理，避免退出接口异常阻塞用户退出
      }
      this.token = '';
      this.refreshToken = '';
      this.userInfo = null;
      tokenStorage.clear();
      tokenStorage.clearRefresh();
    },
    /**
     * 判断当前用户是否拥有指定权限
     * @param code 权限编码
     */
    hasPermission(code: string) {
      return this.isSuperAdmin || this.permissions.includes(code) || this.permissions.includes('*');
    },
  },
});
