<template>
  <section class="pro-table">
    <div v-if="$slots.search" class="pro-table__search">
      <slot name="search" />
    </div>

    <div class="pro-table__toolbar">
      <div v-if="$slots.summary" class="pro-table__summary">
        <slot name="summary" />
      </div>
      <div class="pro-table__tools">
        <slot name="toolbar" />
        <el-tooltip content="刷新数据" placement="top">
          <el-button class="pro-table__tool" text :icon="Refresh" aria-label="刷新数据" @click="emit('refresh')">刷新</el-button>
        </el-tooltip>
        <el-popover v-if="hasConfigurableColumns" placement="bottom-end" :width="248" trigger="click">
          <template #reference>
            <el-button class="pro-table__tool" text :icon="Setting" aria-label="列设置">列设置</el-button>
          </template>
          <div class="column-setting">
            <div class="column-setting__header">
              <strong>表格列</strong>
              <el-button text size="small" @click="resetColumns">恢复默认</el-button>
            </div>
            <div class="column-setting__list">
              <label
                v-for="column in columnState"
                :key="column.key"
                class="column-setting__item"
                draggable="true"
                @dragstart="startColumnDrag(column.key)"
                @dragover.prevent="moveColumn(column.key)"
                @drop="finishColumnDrag"
                @dragend="finishColumnDrag"
              >
                <el-checkbox v-model="column.visible" :disabled="column.required" @change="saveColumns" />
                <span>{{ column.label }}</span>
                <el-icon class="column-setting__drag"><Rank /></el-icon>
              </label>
            </div>
          </div>
        </el-popover>
      </div>
    </div>

    <div class="pro-table__surface">
      <el-table
        ref="tableRef"
        v-loading="loading"
        class="pro-table__table"
        :data="data"
        :row-key="rowKey"
        :height="tableHeight"
        :stripe="stripe"
        :header-cell-style="headerCellStyle"
        @selection-change="handleSelectionChange"
        @sort-change="(sort: SortChange) => emit('sort-change', sort)"
      >
        <el-table-column
          v-for="column in visibleColumns"
          :key="column.key"
          :type="column.type === 'selection' ? 'selection' : column.type === 'index' ? 'index' : undefined"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || (column.type === 'action' ? 92 : 120)"
          :fixed="column.fixed"
          :align="column.align || (column.type === 'number' ? 'right' : 'left')"
          :sortable="column.sortable === 'custom' ? 'custom' : Boolean(column.sortable)"
          :filters="column.filters"
          :filter-method="column.filterMethod"
          :filter-multiple="false"
          :class-name="column.className"
        >
          <template v-if="column.filters?.length" #filter-icon>
            <el-icon class="pro-table__filter-icon"><Filter /></el-icon>
          </template>
          <template #default="scope">
            <slot :name="column.slot || column.prop || column.key" v-bind="scope">
              <el-checkbox
                v-if="column.type === 'selection'"
                :model-value="isRowSelected(scope.row)"
                aria-label="选择当前行"
                @update:model-value="toggleRowSelection(scope.row, Boolean($event))"
              />
              <span v-else-if="column.type === 'index'" class="pro-table__index">{{ scope.$index + 1 }}</span>
              <template v-else-if="column.type === 'status'">
                <span class="pro-table__status" :class="statusClass(column, scope.row)">
                  <i class="pro-table__status-dot" />
                  {{ statusLabel(column, scope.row) }}
                </span>
              </template>
              <el-image
                v-else-if="column.type === 'image'"
                class="pro-table__image"
                :style="{ width: `${column.imageSize || 36}px`, height: `${column.imageSize || 36}px` }"
                :src="String(scope.row[column.prop as string] || '')"
                fit="cover"
              />
              <span v-else-if="column.type === 'action'" class="pro-table__actions"><slot name="action" v-bind="scope" /></span>
              <span v-else class="pro-table__cell" :class="{ 'is-number': column.type === 'number' }">
                <span>{{ formatValue(column, scope.row) }}</span>
                <el-tooltip
                  v-if="column.copyable && scope.row[column.prop as string]"
                  content="复制"
                  placement="top"
                  :show-after="3000"
                >
                  <el-button
                    text
                    class="pro-table__copy"
                    :class="{ 'is-copied': copiedKey === getCopyKey(scope.row, column) }"
                    :icon="copiedKey === getCopyKey(scope.row, column) ? Check : CopyDocument"
                    aria-label="复制"
                    @click.stop="copyValue(scope.row[column.prop as string], getCopyKey(scope.row, column))"
                  />
                </el-tooltip>
              </span>
            </slot>
          </template>
        </el-table-column>
        <template #empty>
          <div v-if="error" class="pro-table__empty pro-table__empty--error">
            <el-icon><WarningFilled /></el-icon>
            <strong>加载失败</strong>
            <span>{{ error }}</span>
            <el-button text type="primary" @click="emit('retry')">重新加载</el-button>
          </div>
          <div v-else class="pro-table__empty">
            <el-icon><Document /></el-icon>
            <strong>{{ emptyDescription }}</strong>
            <span>调整筛选条件后再试试</span>
          </div>
        </template>
      </el-table>
    </div>

    <div v-if="pagination" class="pro-table__pagination">
      <span class="pro-table__pagination-total">共 {{ (total || 0).toLocaleString() }} 条</span>
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        background
        layout="sizes, prev, pager, next"
        :total="total || 0"
        :page-sizes="[10, 20, 50]"
        @change="emit('pagination-change', pagination)"
      />
    </div>
  </section>
</template>

<script setup lang="ts" generic="T extends Record<string, unknown>">
import { computed, onMounted, ref, watch } from 'vue';
import { type TableInstance } from 'element-plus';
import { Check, CopyDocument, Document, Filter, Rank, Refresh, Setting, WarningFilled } from '@element-plus/icons-vue';
import { PRO_TABLE_STATUS_CLASS } from './constants';
import type { ProTableColumn, ProTablePagination, ProTableProps } from './types';

interface ColumnState<T extends Record<string, unknown>> extends ProTableColumn<T> {
  key: string;
  required: boolean;
}

interface SortChange {
  prop: string | undefined;
  order: 'ascending' | 'descending' | null;
}

const props = withDefaults(defineProps<ProTableProps<T>>(), {
  loading: false,
  total: undefined,
  pagination: undefined,
  rowKey: 'id',
  storageKey: undefined,
  error: '',
  emptyDescription: '暂无数据',
  tableHeight: undefined,
  selectable: false,
  stripe: false,
  headerCellStyle: undefined,
});

const emit = defineEmits<{
  refresh: [];
  retry: [];
  'selection-change': [rows: T[]];
  'pagination-change': [pagination: ProTablePagination];
  'sort-change': [sort: SortChange];
}>();

const tableRef = ref<TableInstance>();
const draggedColumnKey = ref<string>();
const copiedKey = ref<string>();
const selectedRows = ref<T[]>([]);
const columnState = ref<ColumnState<T>[]>([]);
const hasConfigurableColumns = computed(() => columnState.value.some((column) => !column.required));
const visibleColumns = computed(() => columnState.value.filter((column) => column.visible || column.required));

const buildColumnState = () =>
  props.columns.map((column, index) => ({
    ...column,
    key: `${column.prop || column.type || 'column'}-${index}`,
    required: column.type === 'action',
    visible: column.visible !== false,
  }));

const loadColumns = () => {
  const defaults = buildColumnState();
  if (!props.storageKey) {
    columnState.value = defaults;
    return;
  }
  try {
    const saved = JSON.parse(localStorage.getItem(props.storageKey) || 'null') as { key: string; visible: boolean }[] | null;
    if (!saved?.length) {
      columnState.value = defaults;
      return;
    }
    const byKey = new Map(defaults.map((column) => [column.key, column]));
    columnState.value = saved.map((item) => ({ ...byKey.get(item.key)!, visible: item.visible })).filter(Boolean);
    defaults.forEach((column) => {
      if (!columnState.value.some((item) => item.key === column.key)) columnState.value = [...columnState.value, column];
    });
  } catch {
    columnState.value = defaults;
  }
};

const saveColumns = () => {
  if (props.storageKey) {
    localStorage.setItem(props.storageKey, JSON.stringify(columnState.value.map(({ key, visible }) => ({ key, visible }))));
  }
};

const resetColumns = () => {
  columnState.value = buildColumnState();
  saveColumns();
};

const startColumnDrag = (key: string) => {
  draggedColumnKey.value = key;
};

const moveColumn = (targetKey: string) => {
  if (!draggedColumnKey.value || draggedColumnKey.value === targetKey) return;
  const sourceIndex = columnState.value.findIndex((column) => column.key === draggedColumnKey.value);
  const targetIndex = columnState.value.findIndex((column) => column.key === targetKey);
  if (sourceIndex < 0 || targetIndex < 0) return;
  const nextColumns = [...columnState.value];
  const [source] = nextColumns.splice(sourceIndex, 1);
  nextColumns.splice(targetIndex, 0, source);
  columnState.value = nextColumns;
  saveColumns();
};

const finishColumnDrag = () => {
  draggedColumnKey.value = undefined;
  saveColumns();
};

const formatValue = (column: ProTableColumn<T>, row: T) => {
  const value = column.prop ? row[column.prop] : undefined;
  if (column.formatter) return column.formatter(row, column, value);
  if (value === undefined || value === null || value === '') return '-';
  if (column.type === 'number' && typeof value === 'number') return value.toLocaleString();
  if (column.type === 'datetime' && typeof value === 'string') {
    const normalizedValue = value.replace('T', ' ');
    if (normalizedValue.length === 10) return normalizedValue;
    if (normalizedValue.length === 16) return `${normalizedValue}:00`;
    return normalizedValue.slice(0, 19);
  }
  return String(value);
};

const statusLabel = (column: ProTableColumn<T>, row: T) => {
  const value = column.prop ? row[column.prop] : undefined;
  return column.statusMap?.[String(value)]?.label || String(value ?? '-');
};

const statusClass = (column: ProTableColumn<T>, row: T) => {
  const value = column.prop ? row[column.prop] : undefined;
  const type = column.statusMap?.[String(value)]?.type || 'info';
  return PRO_TABLE_STATUS_CLASS[type];
};

const getCopyKey = (row: T, column: ProTableColumn<T>) => {
  const rowIdentifier = typeof props.rowKey === 'function' ? props.rowKey(row) : row[props.rowKey];
  return `${String(rowIdentifier)}-${column.prop || column.type || 'column'}`;
};

const copyValue = async (value: unknown, key: string) => {
  try {
    await navigator.clipboard.writeText(String(value));
    copiedKey.value = key;
  } catch {
    copiedKey.value = undefined;
  }
};

const isRowSelected = (row: T) => selectedRows.value.some((selectedRow) => selectedRow === row);

const toggleRowSelection = (row: T, selected: boolean) => {
  tableRef.value?.toggleRowSelection(row, selected);
};

const handleSelectionChange = (rows: T[]) => {
  selectedRows.value = rows;
  emit('selection-change', rows);
};

watch(() => props.columns, loadColumns, { deep: true });
onMounted(loadColumns);

defineExpose({
  tableRef,
  clearSelection: () => tableRef.value?.clearSelection(),
  toggleRowSelection: (row: T, selected?: boolean) => tableRef.value?.toggleRowSelection(row, selected),
});
</script>

<style scoped>
.pro-table {
  --table-text-primary: #1d2939;
  --table-text-secondary: #667085;
  --table-text-tertiary: #98a2b3;
  --table-border: #f0f2f5;
  --table-hover-bg: #f8fafc;
  --table-selected-bg: #f5f8ff;
  --table-blue: #356ae6;
  color: var(--table-text-primary);
}

.pro-table__search {
  margin-bottom: 12px;
  padding: 16px 18px;
  border: 1px solid var(--table-border);
  border-radius: 8px;
  background: #fff;
}

.pro-table__toolbar,
.pro-table__pagination,
.pro-table__tools,
.pro-table__summary,
.pro-table__actions,
.column-setting__header {
  display: flex;
  align-items: center;
}

.pro-table__toolbar {
  min-height: 32px;
  justify-content: flex-end;
  gap: 16px;
  margin-bottom: 4px;
}

.pro-table__summary { gap: 12px; color: var(--table-text-secondary); font-size: 13px; }
.pro-table__tools { gap: 2px; }
.pro-table__tool { color: var(--table-text-secondary); }
.pro-table__tool:hover { color: var(--table-blue); background: #f5f8ff; }
.pro-table__surface { overflow: hidden; border-top: 1px solid var(--table-border); }
.pro-table__table { width: 100%; --el-table-border-color: var(--table-border); --el-table-row-hover-bg-color: var(--table-hover-bg); --el-table-current-row-bg-color: var(--table-selected-bg); }
.pro-table__table :deep(.el-table__inner-wrapper::before) { display: none; }
.pro-table__table :deep(.el-table__cell) { border-bottom-color: var(--table-border); }
.pro-table__table :deep(th.el-table__cell) { height: 44px; padding: 0 16px; border-bottom: 2px solid #e6e9ee; background: #f8fafc; color: var(--table-text-secondary); font-size: 12px; font-weight: 600; }
.pro-table__table :deep(th.el-table__cell .cell) { display: flex; align-items: center; padding: 0; }
.pro-table__table :deep(th.el-table__cell.is-center .cell) { justify-content: center; }
.pro-table__table :deep(th.el-table__cell.is-right .cell) { justify-content: flex-end; }
.pro-table__table :deep(td.el-table__cell) { height: 56px; color: var(--table-text-primary); font-size: 13px; }
.pro-table__table :deep(.el-table__fixed-right::before), .pro-table__table :deep(.el-table__fixed::before) { display: none; }
.pro-table__table :deep(.el-table__fixed-right) { box-shadow: -4px 0 10px rgb(16 24 40 / 4%); }
.pro-table__table :deep(.el-checkbox__inner) { border-color: #cbd5e1; }
.pro-table__filter-icon { color: var(--table-text-tertiary); }
.pro-table__table :deep(.el-table__column-filter-trigger) { position: static; display: inline-flex; align-items: center; margin-left: 8px; }
.pro-table__table :deep(.el-table__column-filter-trigger:hover) .pro-table__filter-icon { color: var(--table-blue); }
.pro-table__cell { display: inline-flex; align-items: center; max-width: 100%; color: inherit; }
.pro-table__cell.is-number { font-variant-numeric: tabular-nums; }
.pro-table__copy { margin-left: 4px; padding: 2px; color: var(--table-text-tertiary); opacity: 0; transition: opacity 120ms ease, color 120ms ease; }
.pro-table__table :deep(.el-table__row:hover) .pro-table__copy { opacity: 1; }
.pro-table__copy.is-copied { opacity: 1; color: #287d5a; }
.pro-table__copy:hover { color: var(--table-blue); }
.pro-table__status { display: inline-flex; align-items: center; gap: 7px; color: var(--table-text-secondary); }
.pro-table__status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.pro-table__status.is-success { color: #287d5a; }.pro-table__status.is-warning { color: #b7791f; }.pro-table__status.is-danger { color: #c2413b; }.pro-table__status.is-primary { color: #356ae6; }.pro-table__status.is-info { color: #667085; }
.pro-table__actions { justify-content: center; gap: 4px; }
.pro-table__image { display: block; border-radius: 6px; }
.pro-table__empty { display: flex; min-height: 220px; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: var(--table-text-tertiary); }
.pro-table__empty .el-icon { margin-bottom: 4px; font-size: 28px; color: #cbd5e1; }
.pro-table__empty strong { color: var(--table-text-secondary); font-size: 13px; font-weight: 500; }
.pro-table__empty span { font-size: 12px; }
.pro-table__empty--error strong { color: #c2413b; }
.pro-table__empty--error .el-icon { color: #c2413b; }
.pro-table__pagination { justify-content: flex-end; gap: 16px; padding-top: 16px; }
.pro-table__pagination-total { margin-right: auto; color: var(--table-text-tertiary); font-size: 12px; }
.pro-table__pagination :deep(.el-pagination) { --el-pagination-button-bg-color: transparent; --el-pagination-hover-color: var(--table-blue); }
.column-setting__header { justify-content: space-between; margin-bottom: 8px; }
.column-setting__header strong { color: var(--table-text-primary); font-size: 13px; }
.column-setting__list { display: grid; gap: 2px; }
.column-setting__item { display: flex; align-items: center; gap: 6px; min-height: 32px; cursor: default; color: var(--table-text-secondary); font-size: 13px; }
.column-setting__drag { margin-left: auto; color: #cbd5e1; cursor: move; }

@media (max-width: 768px) {
  .pro-table__search { padding: 12px; }
  .pro-table__toolbar { align-items: flex-start; }
  .pro-table__pagination { flex-wrap: wrap; justify-content: flex-start; }
  .pro-table__pagination-total { width: 100%; }
}
</style>
