<script setup lang="ts">import SystemTable from './SystemTable.vue';
import {getMenuPage} from '@/api/system'</script>
<template>
  <SystemTable title="菜单管理" :loader="getMenuPage"
               :columns="[{ prop: 'title', label: '菜单标题' }, { prop: 'path', label: '路由地址' }, { prop: 'component', label: '组件路径' }, { prop: 'type', label: '类型' }]"/>
</template>
