<template>
  <div class="demo-page">
    <div class="page-heading">
      <div>
        <h1>货源管理</h1>
        <p>统一表格组件示例，展示筛选、状态、列设置与批量选择</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="publishSource">发布货源</el-button>
    </div>

    <ProTable
      ref="sourceTable"
      :columns="columns"
      :data="filteredRows"
      :loading="loading"
      :error="error"
      :total="filteredRows.length"
      v-model:pagination="pagination"
      storage-key="foundation-pro-table-source-columns"
      @refresh="loadRows"
      @retry="loadRows"
      @selection-change="selectedRows = $event"
      @pagination-change="loadRows"
    >
      <template #search>
        <el-form
          :inline="true"
          :model="filters"
          class="search-form"
          :class="{ 'has-more-filters': showMoreFilters }"
          @submit.prevent="queryRows"
        >
          <el-form-item label="关键词">
            <el-input v-model="filters.keyword" clearable placeholder="编号或路线" @keyup.enter="queryRows" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 150px">
              <el-option label="货源已发布" value="published" />
              <el-option label="匹配中" value="matching" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布日期">
            <div class="date-range-group">
              <el-icon class="date-range-group__icon">
                <Calendar />
              </el-icon>
              <el-date-picker
                v-model="filters.date"
                type="daterange"
                value-format="YYYY-MM-DD"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </div>
          </el-form-item>
          <el-form-item v-if="showMoreFilters" label="联系人">
            <el-input v-model="filters.owner" clearable placeholder="联系人姓名" />
          </el-form-item>
          <el-form-item class="search-form__actions">
            <el-button type="primary" @click="queryRows">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
            <el-button
              text
              class="filter-toggle"
              :icon="showMoreFilters ? ArrowUp : ArrowDown"
              @click="showMoreFilters = !showMoreFilters"
            >
              {{ showMoreFilters ? '收起筛选' : '更多筛选' }}
            </el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar>
        <el-button text :icon="Download" @click="exportRows">导出</el-button>
        <el-button v-if="selectedRows.length" text type="danger" @click="clearSelection">
          批量关闭 ({{ selectedRows.length }})
        </el-button>
      </template>

      <template #route="{ row }">
        <div class="route-cell">
          <span>{{ row.from }}</span>
          <span class="route-cell__arrow">→</span>
          <span>{{ row.to }}</span>
        </div>
      </template>

      <template #action="{ row }">
        <el-tooltip content="查看" placement="top" :show-after="300">
          <el-button text type="primary" class="action-icon" :icon="View" aria-label="查看" @click="viewSource(row)" />
        </el-tooltip>
        <el-dropdown trigger="click" @command="handleAction($event, row)">
          <el-tooltip content="更多操作" placement="top" :show-after="300">
            <el-button text class="action-icon more-action" :icon="MoreFilled" aria-label="更多操作" />
          </el-tooltip>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit">编辑</el-dropdown-item>
              <el-dropdown-item command="copy">复制</el-dropdown-item>
              <el-dropdown-item command="close" divided>关闭货源</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </ProTable>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, ArrowUp, Calendar, Download, MoreFilled, Plus, View } from '@element-plus/icons-vue';
import ProTable from '@/components/ProTable/index.vue';
import type { ProTableColumn, ProTableFilterOption, ProTablePagination } from '@/components/ProTable/types';

interface ProTableExposed {
  clearSelection: () => void;
}

interface SourceRow extends Record<string, unknown> {
  id: number;
  code: string;
  from: string;
  to: string;
  quantity: number;
  status: string;
  publishTime: string;
  owner: string;
}

const statusMap = {
  published: { label: '货源已发布', type: 'success' as const },
  matching: { label: '匹配中', type: 'warning' as const },
  completed: { label: '已完成', type: 'info' as const },
};

const columns: ProTableColumn<SourceRow>[] = [
  { type: 'selection', label: '多选', width: 48, fixed: 'left' },
  { prop: 'code', label: '货源编号', minWidth: 166, copyable: true, sortable: true, fixed: 'left' },
  { prop: 'route', label: '运输路线', minWidth: 180, slot: 'route' },
  {
    prop: 'quantity',
    label: '货量',
    width: 140,
    type: 'number',
    sortable: true,
    formatter: (_row, _column, value) => `${Number(value).toLocaleString()} 吨`,
  },
  {
    prop: 'status',
    label: '状态',
    width: 140,
    type: 'status',
    statusMap,
    filters: [
      { text: '货源已发布', value: 'published' },
      { text: '匹配中', value: 'matching' },
      { text: '已完成', value: 'completed' },
    ] as ProTableFilterOption[],
    filterMethod: (value, row) => row.status === value,
  },
  { prop: 'publishTime', label: '发布时间', width: 168, type: 'datetime', sortable: true },
  { prop: 'owner', label: '联系人', minWidth: 110, visible: false },
  { type: 'action', label: '操作', width: 140, align: 'center', fixed: 'right', slot: 'action' },
];

const sourceRows: SourceRow[] = [
  {
    id: 1,
    code: 'YD20261008001',
    from: '南京',
    to: '重庆',
    quantity: 5000,
    status: 'published',
    publishTime: '2026-10-08 09:20:00',
    owner: '王海',
  },
  {
    id: 2,
    code: 'YD20261008002',
    from: '芜湖',
    to: '武汉',
    quantity: 3200,
    status: 'matching',
    publishTime: '2026-10-07 15:42:00',
    owner: '李宁',
  },
  {
    id: 3,
    code: 'YD20261008003',
    from: '上海',
    to: '南通',
    quantity: 1800,
    status: 'completed',
    publishTime: '2026-10-06 11:08:00',
    owner: '陈晨',
  },
  {
    id: 4,
    code: 'YD20261008004',
    from: '宁波',
    to: '成都',
    quantity: 7600,
    status: 'published',
    publishTime: '2026-10-05 16:30:00',
    owner: '周扬',
  },
  {
    id: 5,
    code: 'YD20261008005',
    from: '武汉',
    to: '长沙',
    quantity: 2400,
    status: 'matching',
    publishTime: '2026-10-04 10:12:00',
    owner: '王海',
  },
  {
    id: 6,
    code: 'YD20261008006',
    from: '苏州',
    to: '合肥',
    quantity: 1200,
    status: 'completed',
    publishTime: '2026-10-03 14:25:00',
    owner: '李宁',
  },
];

const loading = ref(false);
const error = ref('');
const showMoreFilters = ref(false);
const selectedRows = ref<SourceRow[]>([]);
const sourceTable = ref<ProTableExposed>();
const pagination = ref<ProTablePagination>({ current: 1, size: 10 });
const filters = reactive<{ keyword: string; status: string; date: string[] | null; owner: string }>({
  keyword: '',
  status: '',
  date: null,
  owner: '',
});
const filteredRows = computed(() =>
  sourceRows.filter((row) => {
    const keyword = filters.keyword.trim();
    const owner = filters.owner.trim();
    const matchesKeyword = !keyword || `${row.code}${row.from}${row.to}`.includes(keyword);
    const matchesOwner = !owner || row.owner.includes(owner);
    const [startDate, endDate] = filters.date || [];
    const publishDate = row.publishTime.slice(0, 10);
    const matchesDate = !startDate || !endDate || (publishDate >= startDate && publishDate <= endDate);
    return matchesKeyword && matchesOwner && matchesDate && (!filters.status || row.status === filters.status);
  }),
);

const loadRows = async () => {
  loading.value = true;
  error.value = '';
  await new Promise((resolve) => window.setTimeout(resolve, 380));
  loading.value = false;
};

const queryRows = () => {
  pagination.value.current = 1;
  loadRows();
};

const resetFilters = () => {
  filters.keyword = '';
  filters.status = '';
  filters.date = null;
  filters.owner = '';
  pagination.value.current = 1;
  loadRows();
};

const clearSelection = async () => {
  try {
    await ElMessageBox.confirm(`确认关闭选中的 ${selectedRows.value.length} 条货源吗？`, '关闭货源', {
      type: 'warning',
    });
    ElMessage.success('已关闭选中货源');
    sourceTable.value?.clearSelection();
    selectedRows.value = [];
  } catch {
    // 用户取消关闭时保持当前选择
  }
};

const viewSource = (row: SourceRow) => ElMessage.info(`查看 ${row.code}`);
const publishSource = () => ElMessage.info('打开发布货源流程');
const handleAction = (action: string, row: SourceRow) => ElMessage.info(`${action}：${row.code}`);
const exportRows = () => ElMessage.success(`已准备导出 ${filteredRows.value.length} 条货源`);

onMounted(loadRows);
</script>

<style scoped>
.demo-page {
  min-width: 0;
}

.page-heading {
  margin-bottom: 24px;
}

.search-form {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 0.8fr) minmax(0, 1.2fr) auto;
  align-items: center;
  gap: 0 24px;
  width: 100%;
}

.search-form.has-more-filters {
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.7fr) minmax(0, 1.1fr) minmax(0, 0.7fr) auto;
}

.search-form :deep(.el-form-item) {
  display: flex;
  width: 100%;
  min-width: 0;
  align-items: center;
  margin-bottom: 0;
}

.search-form :deep(.el-form-item__label) {
  display: flex;
  height: 32px;
  align-items: center;
  padding-bottom: 0;
  line-height: 32px;
}

.search-form :deep(.el-form-item__content) {
  display: flex;
  flex: 1;
  min-width: 0;
  min-height: 32px;
  align-items: center;
}

.search-form :deep(.el-input),
.search-form :deep(.el-select),
.search-form :deep(.date-range-group) {
  width: 100%;
}

.search-form__actions {
  display: flex;
  align-items: center;
  width: auto !important;
  min-width: max-content;
  gap: 8px;
  margin-left: 0;
  white-space: nowrap;
}

.filter-toggle {
  color: #667085;
}

.filter-toggle:hover {
  color: #356ae6;
}

.date-range-group {
  display: flex;
  min-width: 0;
  height: 32px;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  transition: border-color 180ms ease;
}

.date-range-group:focus-within {
  border-color: #409eff;
}

.date-range-group__icon {
  flex: 0 0 auto;
  color: #98a2b3;
}

.date-range-group :deep(.el-date-editor) {
  width: 100%;
  height: 30px;
  padding: 0;
  border: 0;
  box-shadow: none;
}

.date-range-group :deep(.el-range__icon) {
  display: none;
}

.date-range-group :deep(.el-range-input) {
  min-width: 0;
  color: #1d2939;
}

.date-range-group :deep(.el-range-separator) {
  padding: 0 6px;
  color: #667085;
}

.route-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1d2939;
}

.route-cell__arrow {
  color: #98a2b3;
}

.action-icon {
  width: 30px;
  height: 30px;
  padding: 0;
  color: #356ae6;
}

.more-action {
  color: #667085;
}

@media (max-width: 1400px) {
  .search-form,
  .search-form.has-more-filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px 18px;
  }

  .search-form :deep(.el-form-item) {
    width: 100%;
  }

  .search-form__actions {
    grid-column: 1 / -1;
    justify-self: stretch;
    min-width: 0;
    width: 100% !important;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

@media (max-width: 900px) {
  .search-form,
  .search-form.has-more-filters {
    grid-template-columns: minmax(0, 1fr);
    gap: 12px;
  }

  .search-form :deep(.el-select) {
    width: 100% !important;
  }

  .search-form__actions {
    grid-column: 1;
    justify-self: stretch;
    width: 100% !important;
    justify-content: flex-start;
  }
}
</style>
