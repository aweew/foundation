/**
 * 生成密码摘要，避免密码原文进入接口请求体
 * @param password 密码原文
 */
export const hashPassword = async (password: string) => {
  const passwordBytes = new TextEncoder().encode(password);
  const digest = await crypto.subtle.digest('SHA-256', passwordBytes);
  return Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, '0')).join('');
};
