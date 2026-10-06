<template>
  <SystemTable
    title="角色管理"
    resource="role"
    :loader="getRolePage"
    :fields="fields"
    :columns="[
      { prop: 'name', label: '角色名称' },
      { prop: 'code', label: '角色编码' },
      { prop: 'status', label: '状态', enumName: 'StatusEnum' },
    ]"
  />
</template>

<script setup lang="ts">
import SystemTable from './SystemTable.vue';
import { getRolePage } from '@/api/system';
import type { SystemFormField } from '@/api/system/types';

const fields: SystemFormField[] = [
  { prop: 'name', label: '角色名称', required: true },
  { prop: 'code', label: '角色编码', required: true },
  { prop: 'isSystem', label: '系统内置角色', type: 'switch', defaultValue: false },
  { prop: 'remark', label: '角色描述', type: 'textarea' },
];
</script>
