import type { App, Directive } from 'vue';
import { useAuthStore } from '@/stores/auth';

const permission: Directive<HTMLElement, string | string[]> = {
  mounted(element, binding) {
    const auth = useAuthStore();
    const required = Array.isArray(binding.value) ? binding.value : [binding.value];
    if (required.length > 0 && !required.some((code) => auth.hasPermission(code))) element.remove();
  },
};

export const registerPermissionDirective = (app: App) => {
  app.directive('permission', permission);
};
