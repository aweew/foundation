<template>
  <el-container class="admin-shell">
    <el-aside
      :width="sidebarWidth"
      class="sidebar"
      :class="{ 'is-collapsed': sidebarCollapsed, 'is-animating': sidebarAnimating }"
    >
      <div class="brand">
        <span class="brand-mark">F</span>
        <span v-show="!sidebarCollapsed" class="brand-name">Foundation</span>
        <el-button
          class="sidebar-toggle"
          text
          circle
          :icon="sidebarCollapsed ? ArrowRight : ArrowLeft"
          :aria-label="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
          @click="toggleSidebar"
        />
      </div>
      <el-menu :default-active="route.path" router class="sidebar-menu">
        <el-menu-item v-for="item in visibleNavItems" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
      <el-tooltip :content="sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏'" placement="right" :show-after="2000">
        <el-button
          class="sidebar-center-toggle"
          :class="{ 'is-visible': centerToggleVisible }"
          text
          circle
          :icon="sidebarCollapsed ? ArrowRight : ArrowLeft"
          :aria-label="sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏'"
          @click.stop="toggleSidebar"
        />
      </el-tooltip>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="topbar-title">
          <strong>{{ pageTitle }}</strong>
          <span v-if="pageTitle !== '工作台'">维护平台基础数据</span>
        </div>
        <div class="topbar-actions">
          <el-tooltip :content="isDark ? '切换亮色主题' : '切换暗色主题'" placement="bottom">
            <el-button
              text
              circle
              :icon="isDark ? Sunny : Moon"
              :aria-label="isDark ? '切换亮色主题' : '切换暗色主题'"
              @click="toggleTheme"
            />
          </el-tooltip>
          <el-tooltip content="刷新当前页面" placement="bottom">
            <el-button text circle :icon="Refresh" aria-label="刷新当前页面" @click="refreshPage" />
          </el-tooltip>
          <el-tooltip :content="isFullscreen ? '退出全屏' : '进入全屏'" placement="bottom">
            <el-button
              text
              circle
              :icon="FullScreen"
              :aria-label="isFullscreen ? '退出全屏' : '进入全屏'"
              @click="toggleFullscreen"
            />
          </el-tooltip>
          <el-tooltip content="快捷导航" placement="bottom">
            <el-button text circle :icon="Search" aria-label="快捷导航" @click="openQuickSearch" />
          </el-tooltip>
          <el-popover placement="bottom-end" :width="180" trigger="click">
            <template #reference>
              <el-button text circle :icon="Setting" aria-label="标签页设置" />
            </template>
            <div class="tab-setting">
              <span>显示标签页</span>
              <el-switch :model-value="showPageTabs" @update:model-value="updatePageTabsVisibility" />
            </div>
          </el-popover>
          <el-dropdown @command="logout">
            <span class="user-menu">
              <el-avatar :size="30">{{ displayName.slice(0, 1) }}</el-avatar>
              <span class="user-name">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <nav v-if="showPageTabs" class="page-tabs" aria-label="已打开页面">
        <div class="page-tabs__items">
          <button
            v-for="tab in tabs"
            :key="tab.path"
            type="button"
            class="page-tab"
            :class="{ 'is-active': tab.path === route.path }"
            @click="router.push(tab.path)"
            @contextmenu.prevent.stop="openTabContextMenu($event, tab.path)"
          >
            <span>{{ tab.title }}</span>
            <el-icon v-if="tab.path !== '/dashboard'" class="page-tab__close" @click.stop="closeTab(tab.path)">
              <Close />
            </el-icon>
          </button>
        </div>
        <el-dropdown trigger="click" @command="handleTabCommand">
          <el-button text class="page-tabs__more" aria-label="标签页操作">
            <MoreFilled />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="closeOthers">关闭其他</el-dropdown-item>
              <el-dropdown-item command="closeAll">关闭全部</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </nav>
      <div
        v-if="tabContextMenu"
        class="tab-context-menu"
        :style="{ left: `${tabContextMenu.left}px`, top: `${tabContextMenu.top}px` }"
        role="menu"
        @pointerdown.stop
        @click.stop
      >
        <button
          type="button"
          role="menuitem"
          :disabled="tabContextMenu.path === '/dashboard'"
          @click="handleTabContextCommand('close')"
        >
          关闭当前
        </button>
        <button type="button" role="menuitem" @click="handleTabContextCommand('closeOthers')">关闭其他</button>
        <button type="button" role="menuitem" @click="handleTabContextCommand('closeAll')">关闭全部</button>
      </div>
      <el-dialog
        v-model="quickSearchVisible"
        title="快捷导航"
        width="min(520px, 92vw)"
        class="quick-search-dialog"
        @opened="focusQuickSearch"
      >
        <el-input
          ref="quickSearchInput"
          v-model="quickSearchKeyword"
          :prefix-icon="Search"
          clearable
          placeholder="搜索菜单名称或路径"
          aria-label="搜索菜单名称或路径"
          @keydown.enter="navigateQuickSearch(filteredQuickNav[0])"
        />
        <div class="quick-search-list" role="listbox" aria-label="快捷导航结果">
          <button
            v-for="item in filteredQuickNav"
            :key="item.path"
            type="button"
            class="quick-search-item"
            role="option"
            @click="navigateQuickSearch(item)"
          >
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
            <small>{{ item.path }}</small>
          </button>
          <el-empty v-if="!filteredQuickNav.length" description="没有匹配的菜单" :image-size="56" />
        </div>
      </el-dialog>
      <el-main class="page-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  Close,
  FullScreen,
  Moon,
  MoreFilled,
  Refresh,
  Search,
  Setting,
  Sunny,
} from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const sidebarCollapsed = ref(window.matchMedia('(max-width: 768px)').matches);
const centerToggleVisible = ref(false);
const sidebarAnimating = ref(false);
const isDark = ref(localStorage.getItem('foundation_theme') === 'dark');
const isFullscreen = ref(Boolean(document.fullscreenElement));
const showPageTabs = ref(localStorage.getItem('foundation_show_tabs') !== 'false');
const tabs = ref<{ path: string; title: string }[]>([]);
const tabContextMenu = ref<{ path: string; left: number; top: number } | null>(null);
const quickSearchVisible = ref(false);
const quickSearchKeyword = ref('');
const quickSearchInput = ref<{ focus: () => void }>();
const sidebarWidth = computed(() => (sidebarCollapsed.value ? '64px' : '208px'));
let sidebarAnimationTimer: number | undefined;
let centerToggleTimer: number | undefined;

/**
 * 切换侧边栏状态并锁定动画期间的重复交互
 */
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
  centerToggleVisible.value = true;
  if (centerToggleTimer) window.clearTimeout(centerToggleTimer);
  centerToggleTimer = window.setTimeout(() => {
    centerToggleVisible.value = false;
    centerToggleTimer = undefined;
  }, 1600);
  sidebarAnimating.value = true;
  if (sidebarAnimationTimer) window.clearTimeout(sidebarAnimationTimer);
  sidebarAnimationTimer = window.setTimeout(() => {
    sidebarAnimating.value = false;
    sidebarAnimationTimer = undefined;
  }, 280);
};
const displayName = computed(
  () => auth.userInfo?.nickName || auth.userInfo?.realName || auth.userInfo?.phone || '管理员',
);
const pageTitle = computed(() => (route.meta.title as string) || '工作台');
const navItems = [
  { path: '/dashboard', title: '工作台', icon: 'HomeFilled', permission: '' },
  { path: '/pro-table', title: '表格示例', icon: 'Grid', permission: '' },
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
const filteredQuickNav = computed(() => {
  const keyword = quickSearchKeyword.value.trim().toLowerCase();
  return visibleNavItems.value.filter(
    (item) => !keyword || `${item.title} ${item.path}`.toLowerCase().includes(keyword),
  );
});

const syncTheme = () => {
  document.documentElement.classList.toggle('dark', isDark.value);
  localStorage.setItem('foundation_theme', isDark.value ? 'dark' : 'light');
};
const toggleTheme = () => {
  isDark.value = !isDark.value;
  syncTheme();
};
const refreshPage = () => router.go(0);
const toggleFullscreen = async () => {
  if (document.fullscreenElement) await document.exitFullscreen();
  else await document.documentElement.requestFullscreen();
};
const openQuickSearch = () => {
  quickSearchKeyword.value = '';
  quickSearchVisible.value = true;
};
const focusQuickSearch = () => quickSearchInput.value?.focus();
const navigateQuickSearch = async (item?: (typeof navItems)[number]) => {
  if (!item) return;
  quickSearchVisible.value = false;
  await router.push(item.path);
};
const handleQuickSearchShortcut = (event: KeyboardEvent) => {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault();
    openQuickSearch();
  }
};
const updateFullscreen = () => {
  isFullscreen.value = Boolean(document.fullscreenElement);
};
const updatePageTabsVisibility = (visible: boolean) => {
  showPageTabs.value = visible;
  localStorage.setItem('foundation_show_tabs', String(visible));
  tabContextMenu.value = null;
};
const closeTab = async (path: string) => {
  const index = tabs.value.findIndex((tab) => tab.path === path);
  tabs.value = tabs.value.filter((tab) => tab.path !== path);
  if (route.path === path) await router.push(tabs.value[Math.max(0, index - 1)]?.path || '/dashboard');
};
const openTabContextMenu = (event: MouseEvent, path: string) => {
  const menuWidth = 148;
  const menuHeight = 132;
  tabContextMenu.value = {
    path,
    left: Math.min(event.clientX, window.innerWidth - menuWidth - 8),
    top: Math.min(event.clientY, window.innerHeight - menuHeight - 8),
  };
};
const handleTabCommand = async (command: string) => {
  if (command === 'closeAll') {
    tabs.value = tabs.value.filter((tab) => tab.path === '/dashboard');
    await router.push('/dashboard');
  } else if (command === 'closeOthers') {
    tabs.value = tabs.value.filter((tab) => tab.path === '/dashboard' || tab.path === route.path);
  }
};
const handleTabContextCommand = async (command: 'close' | 'closeOthers' | 'closeAll') => {
  const path = tabContextMenu.value?.path;
  tabContextMenu.value = null;
  if (command === 'close' && path && path !== '/dashboard') await closeTab(path);
  if (command === 'closeOthers' && path) {
    tabs.value = tabs.value.filter((tab) => tab.path === '/dashboard' || tab.path === path);
    if (route.path !== path) await router.push(path);
  }
  if (command === 'closeAll') await handleTabCommand('closeAll');
};
const closeTabContextMenu = () => {
  tabContextMenu.value = null;
};
watch(
  tabs,
  (value) => {
    localStorage.setItem('foundation_tabs', JSON.stringify(value));
  },
  { deep: true },
);
watch(
  () => route.path,
  (path) => {
    const title = (route.meta.title as string) || '当前页面';
    if (!tabs.value.some((tab) => tab.path === path)) tabs.value.push({ path, title });
  },
  { immediate: true },
);
onMounted(() => {
  try {
    const savedTabs = JSON.parse(localStorage.getItem('foundation_tabs') || '[]') as { path: string; title: string }[];
    if (savedTabs.some((tab) => tab.path === '/dashboard')) tabs.value = savedTabs;
  } catch {
    tabs.value = [];
  }
  syncTheme();
  document.addEventListener('fullscreenchange', updateFullscreen);
  document.addEventListener('pointerdown', closeTabContextMenu);
  document.addEventListener('keydown', handleQuickSearchShortcut);
});
onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', updateFullscreen);
  document.removeEventListener('pointerdown', closeTabContextMenu);
  document.removeEventListener('keydown', handleQuickSearchShortcut);
});

/**
 * 清理当前会话并返回登录页
 */
const logout = () => {
  void auth.signOut();
  router.replace('/login');
};
</script>
