<script setup lang="ts">import SystemTable from './SystemTable.vue';
import {getUserPage} from '@/api/system'</script>
<template>
  <SystemTable title="用户管理" :loader="getUserPage"
               :columns="[{ prop: 'nickName', label: '昵称' }, { prop: 'phone', label: '电话' }, { prop: 'email', label: '邮箱' }, { prop: 'status', label: '状态' }]"/>
</template>
