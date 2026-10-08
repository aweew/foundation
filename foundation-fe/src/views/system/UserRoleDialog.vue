<template>
  <el-dialog v-model="visible" title="分配角色" width="min(520px, 94vw)" destroy-on-close>
    <div v-loading="loading">
      <el-empty v-if="!roles.length" description="暂无可分配角色" />
      <el-checkbox-group v-else v-model="selectedRoleIds">
        <el-checkbox v-for="role in roles" :key="String(role.id)" :value="role.id">
          {{ String(role.name || role.code || role.id) }}
        </el-checkbox>
      </el-checkbox-group>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-permission="'sys:user-role:save'" type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { assignUserRole, deleteUserRole, getRolePage, getUserRoles } from '@/api/system';
import type { SystemPageRecord } from '@/api/system/types';

const visible = defineModel<boolean>({ default: false });
const props = defineProps<{ user?: SystemPageRecord }>();
const loading = ref(false);
const saving = ref(false);
const roles = ref<SystemPageRecord[]>([]);
const currentRelations = ref<{ id: string | number; roleId: string | number }[]>([]);
const selectedRoleIds = ref<(string | number)[]>([]);

const load = async () => {
  if (!props.user?.id) return;
  loading.value = true;
  try {
    const [roleResponse, relationResponse] = await Promise.all([
      getRolePage({ current: 1, size: 200 }),
      getUserRoles(props.user.id),
    ]);
    roles.value = roleResponse.data.data.records || [];
    currentRelations.value = relationResponse.data.data || [];
    selectedRoleIds.value = currentRelations.value.map((relation) => relation.roleId);
  } finally {
    loading.value = false;
  }
};

const save = async () => {
  if (!props.user?.id || saving.value) return;
  saving.value = true;
  try {
    const selected = new Set(selectedRoleIds.value.map(String));
    const removed = currentRelations.value.filter((relation) => !selected.has(String(relation.roleId)));
    const existing = new Set(currentRelations.value.map((relation) => String(relation.roleId)));
    await Promise.all(removed.map((relation) => deleteUserRole(relation.id)));
    await Promise.all(
      selectedRoleIds.value
        .filter((roleId) => !existing.has(String(roleId)))
        .map((roleId) => assignUserRole(props.user!.id, roleId)),
    );
    ElMessage.success('角色分配成功');
    visible.value = false;
  } catch {
    ElMessage.error('角色分配失败');
  } finally {
    saving.value = false;
  }
};

watch([visible, () => props.user?.id], ([isVisible]) => {
  if (isVisible) void load();
});
</script>

<style scoped>
.el-checkbox-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 18px;
}
</style>
