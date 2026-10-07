import { expect, test } from '@playwright/test';

test('公共上传、图片弹窗、文本和未知文件预览', async ({ page }, testInfo) => {
  const records = [
    {
      id: 1,
      originalName: 'image.png',
      contentType: 'image/png',
      extension: 'png',
      fileSize: 68,
      accessUrl: '/preview/image.png',
    },
    {
      id: 2,
      originalName: 'notes.txt',
      contentType: 'text/plain',
      extension: 'txt',
      fileSize: 30,
      accessUrl: '/preview/notes.txt',
    },
    {
      id: 3,
      originalName: 'archive.zip',
      contentType: 'application/zip',
      extension: 'zip',
      fileSize: 100,
      accessUrl: '/preview/archive.zip',
    },
  ];
  let uploads = 0;
  let freshUrls = 0;
  await page.addInitScript(() => localStorage.setItem('foundation_access_token', 'test-token'));
  await page.route('**/preview/image.png', (route) =>
    route.fulfill({
      contentType: 'image/png',
      body: Buffer.from(
        'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=',
        'base64',
      ),
    }),
  );
  await page.route('**/preview/notes.txt', (route) =>
    route.fulfill({ contentType: 'text/plain', body: '<script>unsafe</script>\n文件正文' }),
  );
  await page.route('**/foundation/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    let result = null;
    if (path.endsWith('/auth/userInfo')) result = { nickName: '管理员', roles: [], menus: [], permissions: ['*'] };
    else if (path.endsWith('/storage/files/page')) result = { records, total: records.length };
    else if (path.endsWith('/access-url')) {
      freshUrls++;
      result = records.find((file) => path.includes(`/files/${file.id}/`));
    } else if (path.endsWith('/storage/files') && route.request().method() === 'POST') {
      uploads++;
      expect(route.request().headers()['content-type']).toContain('multipart/form-data');
      result = records[0];
    }
    await route.fulfill({ json: { code: 0, msg: '成功', data: result } });
  });
  await page.goto(
    new URL('/system/storage-file', process.env.FOUNDATION_TEST_URL || testInfo.project.use.baseURL).href,
  );
  await page.getByRole('button', { name: '预览 image.png', exact: true }).click();
  await expect(
    page.getByRole('row').filter({ hasText: 'image.png' }).getByRole('button', { name: '查看', exact: true }),
  ).toHaveCount(0);
  const dialog = page.getByRole('dialog');
  await expect(dialog).toBeVisible();
  await expect(dialog.locator('img')).toBeVisible();
  await expect.poll(() => dialog.locator('img').evaluate((image) => image.naturalWidth)).toBeGreaterThan(0);
  await dialog.getByRole('button', { name: '放大', exact: true }).click();
  await expect(dialog.locator('img')).toHaveAttribute('style', /scale\(1.25\)/);
  expect(page.context().pages()).toHaveLength(1);
  await page.screenshot({ path: testInfo.outputPath('image-desktop.png'), animations: 'disabled' });
  await page.keyboard.press('Escape');
  await expect(dialog).not.toBeVisible();
  await page.getByRole('row').filter({ hasText: 'notes.txt' }).getByRole('button', { name: '查看' }).click();
  await expect(dialog.locator('pre')).toContainText('<script>unsafe</script>');
  await expect(dialog.locator('script')).toHaveCount(0);
  await dialog.getByRole('button', { name: '关闭', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  await page.getByRole('row').filter({ hasText: 'archive.zip' }).getByRole('button', { name: '查看' }).click();
  await expect(dialog).toContainText('该文件暂不支持在线预览');
  await expect(dialog.getByRole('button', { name: '下载文件' })).toBeEnabled();
  await dialog.getByRole('button', { name: '关闭', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  await page
    .locator('input[type=file]')
    .setInputFiles({ name: 'upload.txt', mimeType: 'text/plain', buffer: Buffer.from('upload body') });
  await expect.poll(() => uploads).toBe(1);
  await page
    .locator('input[type=file]')
    .setInputFiles({ name: 'empty.txt', mimeType: 'text/plain', buffer: Buffer.alloc(0) });
  await expect(page.getByText('不能上传空文件', { exact: true })).toBeVisible();
  expect(uploads).toBe(1);
  await page.setViewportSize({ width: 390, height: 844 });
  await page.getByRole('button', { name: '预览 image.png', exact: true }).click();
  await expect(dialog).toBeVisible();
  const bounds = await dialog.boundingBox();
  expect(bounds.x).toBeGreaterThanOrEqual(0);
  expect(bounds.x + bounds.width).toBeLessThanOrEqual(390);
  await expect.poll(() => dialog.locator('img').evaluate((image) => image.naturalWidth)).toBeGreaterThan(0);
  await page.screenshot({ path: testInfo.outputPath('image-mobile.png'), animations: 'disabled' });
  await page.keyboard.press('Escape');
  await expect(dialog).not.toBeVisible();
  await page.evaluate(() => {
    Object.defineProperty(navigator.clipboard, 'writeText', {
      value: async (url) => {
        window.copiedFileUrl = url;
      },
    });
  });
  await page.getByRole('row').filter({ hasText: 'image.png' }).getByRole('button', { name: '复制链接' }).click();
  await expect.poll(() => page.evaluate(() => window.copiedFileUrl)).toBe('/preview/image.png');
  await page.route('**/preview/image.png', (route) => route.fulfill({ status: 404 }));
  await page.getByRole('button', { name: '预览 image.png', exact: true }).click();
  await expect(dialog).toContainText('图片加载失败，请检查访问地址');
  await dialog.getByRole('button', { name: '重试', exact: true }).click();
  await expect(dialog).toContainText('图片加载失败，请检查访问地址');
  expect(freshUrls).toBe(6);
});
