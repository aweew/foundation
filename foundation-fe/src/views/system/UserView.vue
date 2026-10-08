<template>
  <SystemTable
    title="用户管理"
    resource="user"
    :loader="getUserPage"
    :fields="fields"
    :preserved-fields="['registerTime', 'lastLoginIp', 'lastLoginTime']"
    :columns="[
      { prop: 'nickName', label: '昵称' },
      { prop: 'phone', label: '电话' },
      { prop: 'email', label: '邮箱' },
      { prop: 'status', label: '状态', enumName: 'StatusEnum' },
    ]"
    :relation="{ label: '分配角色', permission: 'sys:user-role:save' }"
    @manage-relation="openRoleDialog"
  />
  <UserRoleDialog v-model="roleDialogVisible" :user="selectedUser" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import SystemTable from './SystemTable.vue';
import { getUserPage } from '@/api/system';
import type { SystemFormField } from '@/api/system/types';
import type { SystemPageRecord } from '@/api/system/types';
import UserRoleDialog from './UserRoleDialog.vue';

const roleDialogVisible = ref(false);
const selectedUser = ref<SystemPageRecord>();
const openRoleDialog = (row: SystemPageRecord) => {
  selectedUser.value = row;
  roleDialogVisible.value = true;
};

const fields: SystemFormField[] = [
  { prop: 'phone', label: '电话', required: true },
  { prop: 'password', label: '初始密码', type: 'password', required: true, createOnly: true },
  { prop: 'nickName', label: '昵称', required: true },
  { prop: 'realName', label: '真实姓名' },
  {
    prop: 'sex',
    label: '性别',
    type: 'select',
    options: [
      { value: 1, label: '男' },
      { value: 2, label: '女' },
    ],
  },
  { prop: 'email', label: '邮箱' },
  { prop: 'avatar', label: '头像地址' },
  { prop: 'remark', label: '备注', type: 'textarea' },
];
</script>
