<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const displayName = computed(() => auth.userInfo?.nickName || auth.userInfo?.realName || auth.userInfo?.phone || '管理员')
const navItems = [
  { path: '/dashboard', title: '工作台', icon: 'HomeFilled' },
  { path: '/system/user', title: '用户管理', icon: 'User' },
  { path: '/system/role', title: '角色管理', icon: 'UserFilled' },
  { path: '/system/menu', title: '菜单管理', icon: 'Menu' },
]

// 清理当前会话并返回登录页
const logout = () => {
  auth.signOut()
  router.replace('/login')
}
</script>
<template>
  <el-container class="admin-shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand"><span class="brand-mark">F</span><span>Foundation</span></div>
      <el-menu :default-active="route.path" router class="sidebar-menu">
        <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="breadcrumb">管理台 <span>/</span> {{ (route.meta.title as string) || '工作台' }}</div>
        <el-dropdown @command="logout"><span class="user-menu"><el-avatar :size="30">{{
            displayName.slice(0, 1)
          }}</el-avatar>{{ displayName }}<el-icon><ArrowDown /></el-icon></span>
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
