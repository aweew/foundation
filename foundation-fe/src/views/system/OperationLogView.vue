<template>
  <div class="page-heading">
    <div>
      <h1>操作审计</h1>
      <p>查询系统操作、登录、退出和异常记录</p>
    </div>
  </div>

  <el-card class="filter-panel">
    <el-form :model="filters" inline @submit.prevent="search">
      <el-form-item label="日志类型">
        <el-select v-model="filters.logType" clearable placeholder="全部类型" class="filter-control">
          <el-option label="操作" value="OPERATION" />
          <el-option label="登录" value="LOGIN" />
          <el-option label="退出" value="LOGOUT" />
          <el-option label="异常" value="EXCEPTION" />
        </el-select>
      </el-form-item>

      <el-form-item label="请求路径">
        <el-input v-model="filters.requestPath" clearable placeholder="输入路径" class="filter-control" />
      </el-form-item>

      <el-form-item label="请求IP">
        <el-input v-model="filters.requestIp" clearable placeholder="输入IP" class="filter-control" />
      </el-form-item>

      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="filters.includeArchived">包含已归档</el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :icon="Search" :loading="loading" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column prop="logType" label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="logTypeMeta(row.logType).type" effect="light">
            {{ logTypeMeta(row.logType).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationName" label="操作" min-width="150" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="requestMethod" label="方法" width="90" />
      <el-table-column prop="requestPath" label="请求路径" min-width="220" show-overflow-tooltip />
      <el-table-column prop="requestIp" label="IP" width="145" />
      <el-table-column prop="clientType" label="客户端" width="100" />
      <el-table-column prop="durationMs" label="耗时" width="90">
        <template #default="{ row }">{{ row.durationMs ?? 0 }} ms</template>
      </el-table-column>
      <el-table-column prop="resultMessage" label="结果" min-width="120" show-overflow-tooltip />
      <el-table-column prop="errorMessage" label="异常摘要" min-width="220" show-overflow-tooltip />
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @change="load"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh, Search } from '@element-plus/icons-vue';
import { getOperationLogPage } from '@/api/system';
import type { OperationLog, OperationLogQuery } from '@/api/system/types';

const loading = ref(false);
const rows = ref<OperationLog[]>([]);
const total = ref(0);
const page = reactive({ current: 1, size: 10 });
const dateRange = ref<[string, string]>();
const filters = reactive<OperationLogQuery>({
  current: page.current,
  size: page.size,
  logType: undefined,
  requestPath: undefined,
  requestIp: undefined,
  includeArchived: false,
});

const logTypeMeta = (type: string) => {
  const metadata: Record<string, { label: string; type: 'success' | 'warning' | 'danger' | 'info' }> = {
    OPERATION: { label: '操作', type: 'info' },
    LOGIN: { label: '登录', type: 'success' },
    LOGOUT: { label: '退出', type: 'warning' },
    EXCEPTION: { label: '异常', type: 'danger' },
  };
  return metadata[type] ?? { label: type || '未知', type: 'info' };
};

const load = async () => {
  loading.value = true;
  try {
    filters.current = page.current;
    filters.size = page.size;
    filters.startTime = dateRange.value?.[0];
    filters.endTime = dateRange.value?.[1];
    const response = await getOperationLogPage({ ...filters });
    rows.value = response.data.data.records || [];
    total.value = response.data.data.total || 0;
  } catch {
    ElMessage.error('审计日志加载失败');
  } finally {
    loading.value = false;
  }
};

const search = async () => {
  page.current = 1;
  await load();
};

const reset = async () => {
  filters.logType = undefined;
  filters.requestPath = undefined;
  filters.requestIp = undefined;
  filters.includeArchived = false;
  dateRange.value = undefined;
  await search();
};

onMounted(load);
</script>

<style scoped>
.filter-panel {
  margin-bottom: 18px;
}

.filter-control {
  width: 160px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 18px;
}
</style>
