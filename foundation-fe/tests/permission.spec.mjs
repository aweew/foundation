import { expect, test } from '@playwright/test';

test('路由和导航按权限限制访问', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('foundation_access_token', 'test-token'));
  await page.route('**/foundation/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    const data = path.endsWith('/auth/userInfo')
      ? { nickName: '普通用户', roles: [], menus: [], permissions: ['sys:user:list'] }
      : path.endsWith('/sys/user/page')
        ? { records: [], total: 0, current: 1, size: 10 }
        : null;
    await route.fulfill({ json: { code: 0, msg: '成功', data } });
  });

  await page.goto('/system/user');
  await expect(page.getByRole('heading', { name: '用户管理' })).toBeVisible();
  await expect(page.getByRole('menuitem', { name: '角色管理' })).toHaveCount(0);

  await page.goto('/system/role');
  await expect(page.getByText('无权访问', { exact: true })).toBeVisible();
});

test('超管不受菜单树过滤影响', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('foundation_access_token', 'test-token'));
  await page.route('**/foundation/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    const data = path.endsWith('/auth/userInfo')
      ? { nickName: '超管', roles: [{ code: 'SUPER_ADMIN', name: '超管' }], menus: [], permissions: [] }
      : path.endsWith('/sys/role/page')
        ? { records: [], total: 0, current: 1, size: 10 }
        : null;
    await route.fulfill({ json: { code: 0, msg: '成功', data } });
  });

  await page.goto('/system/role');
  await expect(page.getByRole('heading', { name: '角色管理' })).toBeVisible();
  await expect(page.getByRole('menuitem', { name: '文件管理' })).toBeVisible();
});
