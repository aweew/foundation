<template>
  <el-container class="admin-shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">
        <span class="brand-mark">F</span>
        <span>Foundation</span>
      </div>
      <el-menu :default-active="route.path" router class="sidebar-menu">
        <el-menu-item v-for="item in visibleNavItems" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="breadcrumb">
          管理台
          <span>/</span>
          {{ (route.meta.title as string) || '工作台' }}
        </div>
        <el-dropdown @command="logout">
          <span class="user-menu">
            <el-avatar :size="30">{{ displayName.slice(0, 1) }}</el-avatar>
            {{ displayName }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="page-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const displayName = computed(
  () => auth.userInfo?.nickName || auth.userInfo?.realName || auth.userInfo?.phone || '管理员',
);
const navItems = [
  { path: '/dashboard', title: '工作台', icon: 'HomeFilled', permission: '' },
  { path: '/system/user', title: '用户管理', icon: 'User', permission: 'sys:user:list' },
  { path: '/system/role', title: '角色管理', icon: 'UserFilled', permission: 'sys:role:list' },
  { path: '/system/menu', title: '菜单管理', icon: 'Menu', permission: 'sys:menu:list' },
  { path: '/system/operation-log', title: '操作审计', icon: 'Document', permission: 'sys:operation-log:list' },
  { path: '/system/storage-provider', title: '云存储配置', icon: 'Files', permission: 'sys:storage:list' },
  { path: '/system/storage-file', title: '文件管理', icon: 'FolderOpened', permission: 'sys:storage:file:list' },
];
const menuPaths = computed(() => {
  const paths = new Set<string>();
  const visit = (items: typeof auth.menus) => {
    for (const item of items) {
      if (item.path) paths.add(item.path.startsWith('/') ? item.path : `/${item.path}`);
      if (item.childList?.length) visit(item.childList);
    }
  };
  visit(auth.menus);
  return paths;
});
const visibleNavItems = computed(() =>
  navItems.filter((item) => {
    if (!item.permission || auth.isSuperAdmin || auth.hasPermission('*')) return true;
    if (menuPaths.value.size > 0) return menuPaths.value.has(item.path);
    return auth.hasPermission(item.permission);
  }),
);

/**
 * 清理当前会话并返回登录页
 */
const logout = () => {
  void auth.signOut();
  router.replace('/login');
};
</script>
