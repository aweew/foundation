import { expect, test } from '@playwright/test';

const resources = [
  {
    resource: 'user',
    title: '用户管理',
    label: '昵称',
    nameField: 'nickName',
    record: {
      id: '9007199254740993',
      nickName: '测试用户',
      phone: '13800000000',
      password: 'existing-password',
      registerTime: '2026-10-01 12:00:00',
      status: 'ENABLE',
    },
  },
  {
    resource: 'role',
    title: '角色管理',
    label: '角色名称',
    nameField: 'name',
    record: { id: '2', name: '测试角色', code: 'tester', isSystem: false, status: 'ENABLE' },
  },
  {
    resource: 'menu',
    title: '菜单管理',
    label: '菜单标题',
    nameField: 'title',
    record: {
      id: '3',
      name: 'testMenu',
      title: '测试菜单',
      code: 'test:menu',
      type: 1,
      parentId: '0',
      level: 1,
      sort: 0,
      isVisible: true,
      isCache: false,
      isFrame: false,
      status: 'ENABLE',
    },
  },
];

async function mockApi(page, config, failSave = false) {
  let records = [{ ...config.record }];
  const mutations = [];
  await page.addInitScript(() => localStorage.setItem('foundation_access_token', 'test-token'));
  await page.route('**/foundation/**', async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    let result = null;
    if (path.includes('/enum/list'))
      result = {
        StatusEnum: [
          {
            name: 'ENABLE',
            value: 1,
            label: '启用',
          },
          { name: 'DISABLE', value: 2, label: '禁用' },
        ],
      };
    else if (path.includes('/auth/userInfo')) result = { nickName: '管理员', roles: [], menus: [], permissions: ['*'] };
    else if (request.method() === 'GET' && path.endsWith('/tree')) result = records;
    else if (request.method() === 'GET' && path.endsWith('/page'))
      result = {
        records,
        total: records.length,
        current: 1,
        size: 10,
      };
    else if (request.method() === 'GET') result = records.find((record) => path.endsWith('/' + record.id));
    else {
      mutations.push({ method: request.method(), path, body: request.postDataJSON() });
      if (failSave && request.method() !== 'DELETE') {
        await route.fulfill({ json: { code: 400, msg: '保存失败', data: null } });
        return;
      }
      if (request.method() === 'POST') records.push({ ...request.postDataJSON(), id: '4' });
      if (request.method() === 'PUT')
        records = records.map((record) =>
          record.id === request.postDataJSON().id ? { ...record, ...request.postDataJSON() } : record,
        );
      if (request.method() === 'DELETE') records = records.filter((record) => !path.endsWith('/' + record.id));
    }
    await route.fulfill({ json: { code: 0, msg: '成功', data: result } });
  });
  await page.goto(`http://127.0.0.1:3000/system/${config.resource}`);
  await expect(page.getByRole('heading', { name: config.title })).toBeVisible();
  await expect(page.getByRole('cell', { name: config.record[config.nameField], exact: true })).toBeVisible();
  return mutations;
}

for (const config of resources) {
  test(`${config.title}: 新增、编辑和删除`, async ({ page }, testInfo) => {
    const mutations = await mockApi(page, config);
    await page.getByRole('button', { name: '新增', exact: true }).click();
    let dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible();
    await dialog.getByLabel(config.label, { exact: true }).fill('新增记录');
    if (config.resource === 'user') {
      await dialog.getByLabel('电话', { exact: true }).fill('13900000000');
      await dialog.getByLabel('初始密码', { exact: true }).fill('new-password');
    }
    if (config.resource === 'role') await dialog.getByLabel('角色编码', { exact: true }).fill('new_role');
    if (config.resource === 'menu') {
      await dialog.getByLabel('权限名称', { exact: true }).fill('newMenu');
      await dialog.getByLabel('权限编码', { exact: true }).fill('new:menu');
    }
    await dialog.getByRole('button', { name: '保存', exact: true }).click();
    await expect(dialog).not.toBeVisible();
    await expect(page.getByRole('cell', { name: '新增记录', exact: true })).toBeVisible();
    expect(mutations[0].method).toBe('POST');
    expect(mutations[0].path).toBe(`/foundation/sys/${config.resource}`);
    if (config.resource === 'user') {
      expect(mutations[0].body.password).not.toBe('new-password');
      expect(mutations[0].body.password).toMatch(/^[0-9a-f]{64}$/);
    }

    const row = page.getByRole('row').filter({ hasText: config.record[config.nameField] });
    await row.getByRole('button', { name: '编辑', exact: true }).click();
    dialog = page.getByRole('dialog');
    await expect(dialog.getByLabel(config.label, { exact: true })).toHaveValue(config.record[config.nameField]);
    await page.screenshot({
      path: testInfo.outputPath(`${config.resource}-editor.png`),
      fullPage: true,
      animations: 'disabled',
    });
    await dialog.getByLabel(config.label, { exact: true }).fill('修改记录');
    await dialog.getByRole('button', { name: '保存', exact: true }).click();
    await expect(dialog).not.toBeVisible();
    expect(mutations[1].method).toBe('PUT');
    expect(mutations[1].body.id).toBe(config.record.id);
    if (config.resource === 'user') {
      expect(mutations[1].body).not.toHaveProperty('password');
      expect(mutations[1].body.registerTime).toBe(config.record.registerTime);
    }
    expect(mutations[1].body).not.toHaveProperty('status');
    const updatedRow = page.getByRole('row').filter({ hasText: '修改记录' });
    await updatedRow.getByRole('button', { name: '删除', exact: true }).click();
    await page.getByRole('button', { name: '取消', exact: true }).click();
    expect(mutations).toHaveLength(2);
    await updatedRow.getByRole('button', { name: '删除', exact: true }).click();
    await page.getByRole('button', { name: '确定', exact: true }).click();
    await expect(page.getByRole('cell', { name: '修改记录', exact: true })).not.toBeVisible();
    expect(mutations[2].path).toBe(`/foundation/sys/${config.resource}/${config.record.id}`);
  });
}

test('校验阻止空表单提交，保存失败保留弹窗和输入', async ({ page }) => {
  const mutations = await mockApi(page, resources[1], true);
  await page.getByRole('button', { name: '新增', exact: true }).click();
  const dialog = page.getByRole('dialog');
  await dialog.getByRole('button', { name: '保存', exact: true }).click();
  await expect(dialog.getByText('请输入角色名称', { exact: true })).toBeVisible();
  expect(mutations).toHaveLength(0);
  await dialog.getByLabel('角色名称', { exact: true }).fill('保留输入');
  await dialog.getByLabel('角色编码', { exact: true }).fill('test');
  await dialog.getByRole('button', { name: '保存', exact: true }).click();
  await expect(page.getByText('保存失败', { exact: true })).toBeVisible();
  await expect(dialog).toBeVisible();
  await expect(dialog.getByLabel('角色名称', { exact: true })).toHaveValue('保留输入');
});

test('取消新增不发请求，删除失败保留记录', async ({ page }) => {
  const mutations = await mockApi(page, resources[1]);
  await page.getByRole('button', { name: '新增', exact: true }).click();
  await page.getByRole('dialog').getByRole('button', { name: '取消', exact: true }).click();
  await expect(page.getByRole('dialog')).not.toBeVisible();
  expect(mutations).toHaveLength(0);
  await page.route('**/sys/role/2', async (route) => {
    if (route.request().method() === 'DELETE') await route.fulfill({ status: 500, json: { msg: '删除失败' } });
    else await route.fallback();
  });
  await page.getByRole('button', { name: '删除', exact: true }).click();
  await page.getByRole('button', { name: '确定', exact: true }).click();
  await expect(page.getByText('删除失败', { exact: true })).toBeVisible();
  await expect(page.getByRole('cell', { name: '测试角色', exact: true })).toBeVisible();
});

test('记录不存在时关闭编辑，避免误提交', async ({ page }) => {
  const mutations = await mockApi(page, resources[1]);
  await page.route('**/sys/role/2', (route) => route.fulfill({ json: { code: 0, data: null } }));
  await page.getByRole('button', { name: '编辑', exact: true }).click();
  await expect(page.getByText('记录不存在，请刷新列表', { exact: true })).toBeVisible();
  await expect(page.getByRole('dialog')).not.toBeVisible();
  expect(mutations).toHaveLength(0);
});
