<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import type {QueryPage} from '@/api/system'

const props = defineProps<{
  title: string;
  loader: (params: QueryPage) => Promise<any>;
  columns: { prop: string; label: string }[]
}>()
const loading = ref(false);
const rows = ref<Record<string, unknown>[]>([]);
const total = ref(0);
const page = ref({current: 1, size: 10})

async function load() {
  loading.value = true;
  try {
    const response = await props.loader(page.value);
    rows.value = response.data.data.records || [];
    total.value = response.data.data.total || 0
  } catch {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
<template>
  <div class="page-heading">
    <div><h1>{{ title }}</h1>
      <p>维护平台基础数据</p></div>
    <el-button type="primary" :icon="Plus">新增</el-button>
  </div>
  <el-card>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label"
                       min-width="150"/>
      <el-table-column label="操作" width="160">
        <template #default>
          <el-button link type="primary">编辑</el-button>
          <el-button link type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size"
                     layout="total, sizes, prev, pager, next" :total="total" @change="load"/>
    </div>
  </el-card>
</template>
