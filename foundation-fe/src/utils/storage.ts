const TOKEN_KEY = 'foundation_access_token';
const REFRESH_TOKEN_KEY = 'foundation_refresh_token';

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY) || '',
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_TOKEN_KEY) || '',
  setRefresh: (token: string) => localStorage.setItem(REFRESH_TOKEN_KEY, token),
  clearRefresh: () => localStorage.removeItem(REFRESH_TOKEN_KEY),
};
