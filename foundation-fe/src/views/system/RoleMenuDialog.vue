<template>
  <el-dialog v-model="visible" title="分配权限" width="min(620px, 94vw)" destroy-on-close>
    <el-tree
      ref="treeRef"
      v-loading="loading"
      node-key="id"
      show-checkbox
      default-expand-all
      :data="menuTree"
      :props="{ label: 'title', children: 'childList' }"
      empty-text="暂无可分配权限"
    />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-permission="'sys:role-menu:save'" type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { ElMessage, type TreeInstance } from 'element-plus';
import { assignRoleMenu, deleteRoleMenu, getMenuTree, getRoleMenuTree, getRoleMenus } from '@/api/system';
import type { MenuItem, RoleMenuRelation, SystemPageRecord } from '@/api/system/types';

const visible = defineModel<boolean>({ default: false });
const props = defineProps<{ role?: SystemPageRecord }>();
const treeRef = ref<TreeInstance>();
const loading = ref(false);
const saving = ref(false);
const menuTree = ref<MenuItem[]>([]);
const currentRelations = ref<RoleMenuRelation[]>([]);

const flattenIds = (menus: MenuItem[]): (string | number)[] => {
  const ids: (string | number)[] = [];
  for (const menu of menus) {
    ids.push(menu.id);
    if (menu.childList?.length) ids.push(...flattenIds(menu.childList));
  }
  return ids;
};

const load = async () => {
  if (!props.role?.id) return;
  loading.value = true;
  try {
    const [allMenuResponse, selectedMenuResponse, relationResponse] = await Promise.all([
      getMenuTree(),
      getRoleMenuTree(props.role.id),
      getRoleMenus(props.role.id),
    ]);
    menuTree.value = allMenuResponse.data.data || [];
    currentRelations.value = relationResponse.data.data || [];
    await Promise.resolve();
    treeRef.value?.setCheckedKeys(flattenIds(selectedMenuResponse.data.data || []));
  } finally {
    loading.value = false;
  }
};

const save = async () => {
  if (!props.role?.id || saving.value || !treeRef.value) return;
  saving.value = true;
  try {
    const selectedIds = treeRef.value.getCheckedKeys(false) as (string | number)[];
    const selected = new Set(selectedIds.map(String));
    const removed = currentRelations.value.filter((relation) => !selected.has(String(relation.menuId)));
    const existing = new Set(currentRelations.value.map((relation) => String(relation.menuId)));
    await Promise.all(removed.map((relation) => deleteRoleMenu(relation.id)));
    await Promise.all(
      selectedIds
        .filter((menuId) => !existing.has(String(menuId)))
        .map((menuId) => assignRoleMenu(props.role!.id, menuId)),
    );
    ElMessage.success('权限分配成功');
    visible.value = false;
  } catch {
    ElMessage.error('权限分配失败');
  } finally {
    saving.value = false;
  }
};

watch([visible, () => props.role?.id], ([isVisible]) => {
  if (isVisible) void load();
});
</script>
