import { expect, test } from '@playwright/test';

test('S3 配置校验、保存与编辑保留凭证', async ({ page }, testInfo) => {
  let records = [];
  const mutations = [];
  await page.addInitScript(() => localStorage.setItem('foundation_access_token', 'test-token'));
  await page.route('**/foundation/**', async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    let result = null;
    if (path.includes('/auth/userInfo')) result = { nickName: '管理员', roles: [], menus: [], permissions: ['*'] };
    else if (path.endsWith('/storage/provider/list')) result = records;
    else if (request.method() === 'POST' || request.method() === 'PUT') {
      const payload = request.postDataJSON();
      mutations.push(payload);
      const { accessKey, secretAccessKey, operator, password, ...config } = payload;
      records = [{ ...config, id: 1, credentialConfigured: true, isDefault: false, configVersion: 1 }];
    }
    await route.fulfill({ json: { code: 0, msg: '成功', data: result } });
  });
  await page.goto(
    new URL('/system/storage-provider', process.env.FOUNDATION_TEST_URL || testInfo.project.use.baseURL).href,
  );
  await page.getByRole('button', { name: '新增配置', exact: true }).click();
  let dialog = page.getByRole('dialog');
  await dialog.locator('.el-select__wrapper').click();
  await page.getByRole('option', { name: 'S3 兼容存储（又拍云 S3）' }).click();
  await expect(dialog.getByLabel('厂商名称', { exact: true })).toHaveValue('又拍云 S3');
  await expect(dialog.getByLabel('操作员', { exact: true })).toHaveCount(0);
  await dialog.getByRole('button', { name: '保存', exact: true }).click();
  await expect(dialog.getByText('请输入 S3 服务端点', { exact: true })).toBeVisible();
  await expect(dialog.getByText('请输入 SecretAccessKey', { exact: true })).toBeVisible();
  expect(mutations).toHaveLength(0);
  await dialog.getByLabel('服务名/空间', { exact: true }).fill('test-bucket');
  await dialog.getByLabel('API 端点', { exact: true }).fill('https://s3.example.com');
  await dialog.getByLabel('Region', { exact: true }).fill('us-east-1');
  await dialog.getByLabel('AccessKey', { exact: true }).fill('test-access-key');
  await dialog.getByLabel('SecretAccessKey', { exact: true }).fill('test-secret-key');
  await dialog.getByLabel('备注', { exact: true }).focus();
  await page.screenshot({ path: testInfo.outputPath('s3-desktop.png'), animations: 'disabled' });
  await dialog.getByRole('button', { name: '保存', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  expect(mutations[0]).toMatchObject({
    providerCode: 's3',
    serviceName: 'test-bucket',
    region: 'us-east-1',
    accessKey: 'test-access-key',
    secretAccessKey: 'test-secret-key',
  });
  await page.getByRole('button', { name: '编辑', exact: true }).click();
  dialog = page.getByRole('dialog');
  await expect(dialog.getByLabel('AccessKey', { exact: true })).toHaveValue('');
  await expect(dialog.getByLabel('SecretAccessKey', { exact: true })).toHaveValue('');
  await expect(dialog.getByLabel('Region', { exact: true })).toHaveValue('us-east-1');
  await dialog.getByLabel('备注', { exact: true }).fill('保留原凭证');
  await dialog.getByRole('button', { name: '保存', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  expect(mutations[1]).toMatchObject({ accessKey: '', secretAccessKey: '', remark: '保留原凭证' });
  await page.setViewportSize({ width: 390, height: 844 });
  await page.getByRole('button', { name: '编辑', exact: true }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await page.getByRole('dialog').getByLabel('Region', { exact: true }).focus();
  await page.screenshot({ path: testInfo.outputPath('s3-mobile.png'), animations: 'disabled' });
  const bounds = await page.getByRole('dialog').boundingBox();
  expect(bounds.x).toBeGreaterThanOrEqual(0);
  expect(bounds.x + bounds.width).toBeLessThanOrEqual(390);
});
