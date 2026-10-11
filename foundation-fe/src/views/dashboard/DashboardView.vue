<template>
  <div class="page-heading">
    <div>
      <!--<h1>工作台</h1>-->
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
  <section class="dashboard-shortcuts" aria-label="常用功能">
    <div class="dashboard-shortcuts__heading">
      <div>
        <h2>常用功能</h2>
        <p>快速进入日常维护模块</p>
      </div>
      <el-tag type="info" effect="plain">{{ quickActions.length }} 个入口</el-tag>
    </div>
    <div class="dashboard-shortcuts__grid">
      <button
        v-for="item in quickActions"
        :key="item.path"
        type="button"
        class="dashboard-shortcut"
        @click="router.push(item.path)"
      >
        <span class="dashboard-shortcut__icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </span>
        <span class="dashboard-shortcut__body">
          <strong>{{ item.title }}</strong>
          <small>{{ item.description }}</small>
        </span>
        <el-icon class="dashboard-shortcut__arrow">
          <ArrowRight />
        </el-icon>
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowRight, Document, Files, FolderOpened, Menu, User, UserFilled } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();
const quickActions = computed(() =>
  [
    { path: '/system/user', title: '用户管理', description: '维护账号与状态', icon: User, permission: 'sys:user:list' },
    {
      path: '/system/role',
      title: '角色管理',
      description: '配置角色权限范围',
      icon: UserFilled,
      permission: 'sys:role:list',
    },
    {
      path: '/system/menu',
      title: '菜单管理',
      description: '维护菜单与权限编码',
      icon: Menu,
      permission: 'sys:menu:list',
    },
    {
      path: '/system/operation-log',
      title: '操作审计',
      description: '查看关键操作记录',
      icon: Document,
      permission: 'sys:operation-log:list',
    },
    {
      path: '/system/storage-provider',
      title: '云存储配置',
      description: '管理文件存储服务',
      icon: Files,
      permission: 'sys:storage:list',
    },
    {
      path: '/system/storage-file',
      title: '文件管理',
      description: '查看与维护上传文件',
      icon: FolderOpened,
      permission: 'sys:storage:file:list',
    },
  ].filter((item) => auth.isSuperAdmin || auth.hasPermission('*') || auth.hasPermission(item.permission)),
);
</script>

<style scoped>
.dashboard-shortcuts {
  margin-top: 20px;
  padding: 22px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
}

.dashboard-shortcuts__heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.dashboard-shortcuts h2 {
  margin: 0 0 4px;
  color: var(--text);
  font-size: 16px;
}

.dashboard-shortcuts p {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
}

.dashboard-shortcuts__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.dashboard-shortcut {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
  padding: 14px;
  color: var(--text);
  text-align: left;
  background: var(--surface-muted);
  border: 1px solid transparent;
  border-radius: 7px;
  cursor: pointer;
  transition:
    border-color 160ms ease,
    background-color 160ms ease,
    transform 160ms ease;
}

.dashboard-shortcut:hover,
.dashboard-shortcut:focus-visible {
  border-color: rgb(22 119 255 / 30%);
  background: var(--surface);
  outline: none;
  transform: translateY(-1px);
}

.dashboard-shortcut__icon {
  display: grid;
  flex: 0 0 34px;
  width: 34px;
  height: 34px;
  place-items: center;
  color: #1677ff;
  background: rgb(22 119 255 / 10%);
  border-radius: 7px;
}

.dashboard-shortcut__body {
  display: grid;
  min-width: 0;
  gap: 4px;
}

.dashboard-shortcut__body strong {
  overflow: hidden;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dashboard-shortcut__body small {
  overflow: hidden;
  color: var(--text-muted);
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dashboard-shortcut__arrow {
  margin-left: auto;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .dashboard-shortcuts__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .dashboard-shortcuts {
    padding: 16px;
  }

  .dashboard-shortcuts__grid {
    grid-template-columns: 1fr;
  }
}
</style>
