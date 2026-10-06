<template>
  <div class="page-heading">
    <div>
      <h1>工作台</h1>
      <p>欢迎回来，{{ auth.userInfo?.nickName || auth.userInfo?.realName || '管理员' }}</p>
    </div>
  </div>
  <div class="stat-grid">
    <el-card>
      <span class="stat-label">可访问菜单</span>
      <strong>{{ auth.menus.length }}</strong>
      <small>当前账号权限范围</small>
    </el-card>

    <el-card>
      <span class="stat-label">权限数量</span>
      <strong>{{ auth.permissions.length }}</strong>
      <small>按钮与接口权限</small>
    </el-card>

    <el-card>
      <span class="stat-label">系统状态</span>
      <strong class="status-ok">正常</strong>
      <small>服务连接正常</small>
    </el-card>
  </div>
  <el-card class="welcome-card">
    <h2>Foundation 管理台</h2>
    <p>系统基础能力已就绪，可以从左侧进入用户、角色和菜单管理。</p>
  </el-card>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>
