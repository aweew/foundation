<script setup lang="ts">import SystemTable from './SystemTable.vue';
import {getRolePage} from '@/api/system'</script>
<template>
  <SystemTable title="角色管理" :loader="getRolePage"
               :columns="[{ prop: 'name', label: '角色名称' }, { prop: 'code', label: '角色编码' }, { prop: 'status', label: '状态' }]"/>
</template>
