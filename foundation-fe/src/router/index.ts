import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/views/login/LoginView.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台', icon: 'HomeFilled' },
      },
      {
        path: 'system/user',
        component: () => import('@/views/system/UserView.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'system/role',
        component: () => import('@/views/system/RoleView.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' },
      },
      {
        path: 'system/menu',
        component: () => import('@/views/system/MenuView.vue'),
        meta: { title: '菜单管理', icon: 'Menu' },
      },
      {
        path: 'system/operation-log',
        component: () => import('@/views/system/OperationLogView.vue'),
        meta: { title: '操作审计', icon: 'Document' },
      },
      {
        path: 'system/storage-provider',
        component: () => import('@/views/system/StorageProviderView.vue'),
        meta: { title: '云存储配置', icon: 'Files' },
      },
      {
        path: 'system/storage-file',
        component: () => import('@/views/system/StorageFileView.vue'),
        meta: { title: '文件管理', icon: 'FolderOpened' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
];

const router = createRouter({ history: createWebHistory(), routes });
/**
 * 根据公开路由和登录令牌控制页面访问，并在首次进入后台时加载用户信息
 */
router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (to.meta.public) {
    if (to.path === '/login' && auth.token) return '/dashboard';
    return true;
  }
  if (!auth.token) return '/login';
  if (!auth.userInfo) {
    try {
      await auth.loadUserInfo();
    } catch {
      auth.signOut();
      return '/login';
    }
  }
  return true;
});
export default router;
