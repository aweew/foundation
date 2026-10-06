<script setup lang="ts">
import SystemTable from './SystemTable.vue'
import {getMenuPage, type SystemFormField} from '@/api/system'

const menuTypes = [{value: 1, label: '菜单'}, {value: 2, label: '按钮'}, {value: 3, label: '页面'}]
const fields: SystemFormField[] = [
  {prop: 'name', label: '权限名称', required: true},
  {prop: 'title', label: '菜单标题', required: true},
  {prop: 'code', label: '权限编码', required: true},
  {prop: 'parentId', label: '父级权限ID', defaultValue: '0'},
  {prop: 'type', label: '类型', type: 'select', required: true, options: menuTypes, defaultValue: 1},
  {prop: 'level', label: '层级', type: 'number', defaultValue: 1},
  {prop: 'sort', label: '排序', type: 'number', defaultValue: 0},
  {prop: 'path', label: '路由地址'},
  {prop: 'component', label: '组件路径'},
  {prop: 'url', label: '链接地址'},
  {prop: 'icon', label: '图标'},
  {
    prop: 'openType',
    label: '打开方式',
    type: 'select',
    options: [{value: 1, label: '当前页'}, {value: 2, label: '新标签'}],
    defaultValue: 1
  },
  {prop: 'queryParam', label: '路由参数'},
  {prop: 'isFrame', label: '外链', type: 'switch', defaultValue: false},
  {prop: 'isCache', label: '缓存', type: 'switch', defaultValue: false},
  {prop: 'isVisible', label: '显示', type: 'switch', defaultValue: true},
  {prop: 'remark', label: '备注', type: 'textarea'},
]
</script>
<template>
  <SystemTable title="菜单管理" resource="menu" :loader="getMenuPage" :fields="fields"
               :columns="[{ prop: 'title', label: '菜单标题' }, { prop: 'path', label: '路由地址' }, { prop: 'component', label: '组件路径' }, { prop: 'type', label: '类型', options: menuTypes }]"/>
</template>
