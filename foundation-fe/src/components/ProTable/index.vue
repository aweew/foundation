<template>
  <section class="pro-table">
    <!-- 筛选区：承载页面传入的查询条件和筛选操作 -->
    <div v-if="$slots.search" class="pro-table__filter">
      <slot name="search" />
    </div>

    <!-- 工具栏：左侧放置摘要或新增操作，右侧放置表格级操作 -->
    <div class="pro-table__toolbar">
      <div v-if="$slots.summary" class="pro-table__summary">
        <slot name="summary" />
      </div>
      <div class="pro-table__tools">
        <slot name="toolbar" />
        <el-tooltip content="刷新数据" placement="top" :show-after="2000">
          <el-button class="pro-table__tool" text :icon="Refresh" aria-label="刷新数据" @click="emit('refresh')" />
        </el-tooltip>
        <el-popover v-if="hasConfigurableColumns" placement="bottom-end" :width="248" trigger="click">
          <template #reference>
            <el-button class="pro-table__tool" text :icon="Setting" title="列设置" aria-label="列设置" />
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
                <el-icon class="column-setting__drag">
                  <Rank />
                </el-icon>
              </label>
            </div>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 表格区域：包含表头、数据行、加载态和空数据状态 -->
    <div class="pro-table__table-region">
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
            <el-icon class="pro-table__filter-icon">
              <Filter />
            </el-icon>
          </template>
          <template #default="scope">
            <slot :name="column.slot || column.prop || column.key" v-bind="scope">
              <el-checkbox
                v-if="props.selectable && column.type === 'selection'"
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
              <span v-else-if="column.type === 'action'" class="pro-table__actions">
                <slot name="action" v-bind="scope" />
              </span>
              <span v-else class="pro-table__cell" :class="{ 'is-number': column.type === 'number' }">
                <span>{{ formatValue(column, scope.row) }}</span>
                <el-tooltip
                  v-if="column.copyable && hasCopyValue(scope.row[column.prop as string])"
                  content="复制"
                  placement="top"
                  :show-after="450"
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
            <el-icon>
              <WarningFilled />
            </el-icon>
            <strong>加载失败</strong>
            <span>{{ error }}</span>
            <el-button text type="primary" @click="emit('retry')">重新加载</el-button>
          </div>
          <div v-else class="pro-table__empty">
            <el-icon>
              <Document />
            </el-icon>
            <strong>{{ emptyDescription }}</strong>
            <span>调整筛选条件后再试试</span>
          </div>
        </template>
      </el-table>
    </div>

    <!-- 分页页脚：展示总条数和分页控制器 -->
    <footer v-if="pagination" class="pro-table__footer">
      <span class="pro-table__pagination-total">共 {{ (total || 0).toLocaleString() }} 条</span>
      <el-pagination
        :current-page="pagination.current"
        :page-size="pagination.size"
        background
        layout="sizes, prev, pager, next"
        :total="total || 0"
        :page-sizes="[10, 20, 50]"
        @update:current-page="handleCurrentPageChange"
        @update:page-size="handlePageSizeChange"
      />
    </footer>
  </section>
</template>

<script setup lang="ts" generic="T extends Record<string, unknown>">
import { computed, onMounted, ref, watch } from 'vue';
import { type TableInstance } from 'element-plus';
import { Check, CopyDocument, Document, Filter, Rank, Refresh, Setting, WarningFilled } from '@element-plus/icons-vue';
import type { ProTableColumn, ProTablePagination, ProTableProps } from './types';

interface ColumnState<T extends Record<string, unknown>> extends ProTableColumn<T> {
  key: string;
  required: boolean;
}

interface SortChange {
  prop: string | undefined;
  order: 'ascending' | 'descending' | null;
}

// 业务状态类型样式
const statusClassMap = {
  success: 'is-success',
  warning: 'is-warning',
  danger: 'is-danger',
  info: 'is-info',
  primary: 'is-primary',
} as const;

// 组件参数提供表格列、数据、分页和状态展示能力
const props = withDefaults(defineProps<ProTableProps<T>>(), {
  loading: false,
  total: undefined,
  pagination: undefined,
  rowKey: 'id',
  storageKey: undefined,
  error: '',
  emptyDescription: '暂无数据',
  tableHeight: undefined,
  selectable: true,
  stripe: false,
  headerCellStyle: undefined,
});

// 对外通知刷新、重试、选择、分页和排序等表格事件
const emit = defineEmits<{
  refresh: [];
  retry: [];
  'update:pagination': [pagination: ProTablePagination];
  'selection-change': [rows: T[]];
  'pagination-change': [pagination: ProTablePagination];
  'sort-change': [sort: SortChange];
}>();

// 维护表格实例、列配置、复制状态和当前选中行
const tableRef = ref<TableInstance>();
const draggedColumnKey = ref<string>();
const copiedKey = ref<string>();
const selectedRows = ref<T[]>([]);
const columnState = ref<ColumnState<T>[]>([]);
const hasConfigurableColumns = computed(() => columnState.value.some((column) => !column.required));
const visibleColumns = computed(() =>
  columnState.value.filter(
    (column) => (column.visible || column.required) && (props.selectable || column.type !== 'selection'),
  ),
);

/** 根据传入列定义建立可持久化的列状态 */
const buildColumnState = () =>
  props.columns.map((column, index) => ({
    ...column,
    key: `${column.prop || column.type || 'column'}-${index}`,
    required: column.type === 'action',
    visible: column.visible !== false,
  }));

/** 加载本地保存的列配置，并补齐当前新增列 */
const loadColumns = () => {
  const defaults = buildColumnState();
  if (!props.storageKey) {
    columnState.value = defaults;
    return;
  }
  try {
    const saved = JSON.parse(localStorage.getItem(props.storageKey) || 'null') as
      | {
          key: string;
          visible: boolean;
        }[]
      | null;
    if (!saved?.length) {
      columnState.value = defaults;
      return;
    }
    const byKey = new Map(defaults.map((column) => [column.key, column]));
    const restoredColumns: ColumnState<T>[] = [];
    saved.forEach((item) => {
      const column = byKey.get(item.key);
      if (column) restoredColumns.push({ ...column, visible: item.visible !== false });
    });
    const restoredKeys = new Set(restoredColumns.map((column) => column.key));
    const missingColumns = defaults.filter((column) => !restoredKeys.has(column.key));
    columnState.value = [...restoredColumns, ...missingColumns];
  } catch {
    columnState.value = defaults;
  }
};

/** 保存用户调整后的列显示状态和顺序 */
const saveColumns = () => {
  if (props.storageKey) {
    localStorage.setItem(
      props.storageKey,
      JSON.stringify(
        columnState.value.map(({ key, visible }) => ({
          key,
          visible,
        })),
      ),
    );
  }
};

/** 恢复列的默认显示状态和顺序 */
const resetColumns = () => {
  columnState.value = buildColumnState();
  saveColumns();
};

/** 记录列设置面板开始拖拽的列 */
const startColumnDrag = (key: string) => {
  draggedColumnKey.value = key;
};

/** 根据拖拽目标调整列顺序并即时保存 */
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

/** 清理拖拽状态并保存最终列顺序 */
const finishColumnDrag = () => {
  draggedColumnKey.value = undefined;
  saveColumns();
};

/** 格式化普通单元格的展示值 */
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

/** 根据状态配置获取单元格显示文本 */
const statusLabel = (column: ProTableColumn<T>, row: T) => {
  const value = column.prop ? row[column.prop] : undefined;
  return column.statusMap?.[String(value)]?.label || String(value ?? '-');
};

/** 根据状态配置获取单元格样式类型 */
const statusClass = (column: ProTableColumn<T>, row: T) => {
  const value = column.prop ? row[column.prop] : undefined;
  const type = column.statusMap?.[String(value)]?.type || 'info';
  return statusClassMap[type];
};

/** 获取行选择和复制功能使用的稳定行标识 */
const getRowIdentifier = (row: T) => {
  const identifier = typeof props.rowKey === 'function' ? props.rowKey(row) : row[props.rowKey];
  return identifier === undefined || identifier === null ? undefined : String(identifier);
};

/** 生成单元格复制状态使用的唯一键 */
const getCopyKey = (row: T, column: ProTableColumn<T>) => {
  const rowIdentifier = getRowIdentifier(row) || String(row);
  return `${String(rowIdentifier)}-${column.prop || column.type || 'column'}`;
};

/** 复制单元格内容并记录最近一次复制的单元格 */
const copyValue = async (value: unknown, key: string) => {
  try {
    await navigator.clipboard.writeText(String(value));
    copiedKey.value = key;
  } catch {
    copiedKey.value = undefined;
  }
};

/** 判断值是否适合展示复制按钮，保留数字 0 和布尔值 false */
const hasCopyValue = (value: unknown) => value !== undefined && value !== null && value !== '';

/** 判断指定行当前是否处于选中状态 */
const isRowSelected = (row: T) => {
  const rowIdentifier = getRowIdentifier(row);
  return selectedRows.value.some((selectedRow) => {
    if (selectedRow === row) return true;
    const selectedIdentifier = getRowIdentifier(selectedRow as T);
    return rowIdentifier !== undefined && rowIdentifier === selectedIdentifier;
  });
};

/** 切换指定行的选中状态 */
const toggleRowSelection = (row: T, selected: boolean) => {
  tableRef.value?.toggleRowSelection(row, selected);
};

/** 将 Element Plus 的选中结果同步给父组件 */
const handleSelectionChange = (rows: T[]) => {
  selectedRows.value = rows;
  emit('selection-change', rows);
};

/** 更新分页参数并通知父组件重新加载数据 */
const updatePagination = (current: number, size: number) => {
  const nextPagination = { current, size };
  emit('update:pagination', nextPagination);
  emit('pagination-change', nextPagination);
};

/** 处理当前页切换 */
const handleCurrentPageChange = (current: number) => {
  if (!props.pagination) return;
  updatePagination(current, props.pagination.size);
};

/** 处理每页条数切换 */
const handlePageSizeChange = (size: number) => {
  if (!props.pagination) return;
  updatePagination(props.pagination.current, size);
};

// 列定义变化时重新计算可见列，组件挂载时恢复本地列配置
watch(() => props.columns, loadColumns, { deep: true });
onMounted(loadColumns);

// 暴露表格实例和常用选择操作，供父组件按需控制表格
defineExpose({
  tableRef,
  clearSelection: () => {
    tableRef.value?.clearSelection();
    selectedRows.value = [];
  },
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
  --pro-table-section-gap: 12px;
  color: var(--table-text-primary);
}

.pro-table__filter {
  margin-bottom: var(--pro-table-section-gap);
  padding: 16px 18px;
  border: 1px solid var(--table-border);
  border-radius: 8px;
  background: #fff;
}

.pro-table__toolbar,
.pro-table__footer,
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
  padding: 0 18px;
  margin-bottom: var(--pro-table-section-gap);
}

.pro-table__toolbar:has(.pro-table__summary) {
  justify-content: space-between;
}

.pro-table__summary {
  gap: 12px;
  color: var(--table-text-secondary);
  font-size: 13px;
}

.pro-table__tools {
  margin-left: auto;
  padding: 2px;
  border: 1px solid var(--table-border);
  border-radius: 6px;
  background: #fff;
  gap: 0;
}

.pro-table__tools :deep(.pro-table__tool) {
  width: 30px;
  height: 30px;
  padding: 0;
  margin-left: 0 !important;
}

.pro-table__tool {
  color: var(--table-text-secondary);
}

.pro-table__tool:hover {
  color: var(--table-blue);
  background: #f5f8ff;
}

.pro-table__table-region {
  overflow: hidden;
  border-top: 1px solid var(--table-border);
}

.pro-table__table {
  width: 100%;
  --el-table-border-color: var(--table-border);
  --el-table-row-hover-bg-color: var(--table-hover-bg);
  --el-table-current-row-bg-color: var(--table-selected-bg);
}

.pro-table__table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.pro-table__table :deep(.el-table__cell) {
  border-bottom-color: var(--table-border);
}

.pro-table__table :deep(th.el-table__cell) {
  height: 44px;
  padding: 0 16px;
  border-bottom: 2px solid #e6e9ee;
  background: #f8fafc;
  color: var(--table-text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.pro-table__table :deep(th.el-table__cell .cell) {
  display: flex;
  align-items: center;
  padding: 0;
}

.pro-table__table :deep(th.el-table__cell.is-center .cell) {
  justify-content: center;
}

.pro-table__table :deep(th.el-table__cell.is-right .cell) {
  justify-content: flex-end;
}

.pro-table__table :deep(td.el-table__cell) {
  height: 56px;
  color: var(--table-text-primary);
  font-size: 13px;
}

.pro-table__table :deep(.el-table__fixed-right::before),
.pro-table__table :deep(.el-table__fixed::before) {
  display: none;
}

.pro-table__table :deep(.el-table__fixed-right) {
  box-shadow: -4px 0 10px rgb(16 24 40 / 4%);
}

.pro-table__table :deep(.el-checkbox__inner) {
  border-color: #cbd5e1;
}

.pro-table__filter-icon {
  color: var(--table-text-tertiary);
}

.pro-table__table :deep(.el-table__column-filter-trigger) {
  position: static;
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
}

.pro-table__table :deep(.el-table__column-filter-trigger:hover) .pro-table__filter-icon {
  color: var(--table-blue);
}

.pro-table__cell {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  color: inherit;
}

.pro-table__cell.is-number {
  font-variant-numeric: tabular-nums;
}

.pro-table__copy {
  margin-left: 4px;
  padding: 2px;
  color: var(--table-text-tertiary);
  opacity: 0;
  transition:
    opacity 180ms ease,
    color 180ms ease;
}

.pro-table__table :deep(.el-table__row:hover) .pro-table__copy {
  opacity: 1;
}

.pro-table__copy.is-copied {
  opacity: 1;
  color: #287d5a;
}

.pro-table__copy:hover {
  color: var(--table-blue);
}

.pro-table__status {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--table-text-secondary);
}

.pro-table__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.pro-table__status.is-success {
  color: #287d5a;
}

.pro-table__status.is-warning {
  color: #b7791f;
}

.pro-table__status.is-danger {
  color: #c2413b;
}

.pro-table__status.is-primary {
  color: #356ae6;
}

.pro-table__status.is-info {
  color: #667085;
}

.pro-table__actions {
  justify-content: center;
  gap: 4px;
}

.pro-table__image {
  display: block;
  border-radius: 6px;
}

.pro-table__empty {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--table-text-tertiary);
}

.pro-table__empty .el-icon {
  margin-bottom: 4px;
  font-size: 28px;
  color: #cbd5e1;
}

.pro-table__empty strong {
  color: var(--table-text-secondary);
  font-size: 13px;
  font-weight: 500;
}

.pro-table__empty span {
  font-size: 12px;
}

.pro-table__empty--error strong {
  color: #c2413b;
}

.pro-table__empty--error .el-icon {
  color: #c2413b;
}

.pro-table__footer {
  justify-content: flex-end;
  gap: 16px;
  padding-top: 16px;
}

.pro-table__pagination-total {
  margin-right: auto;
  color: var(--table-text-tertiary);
  font-size: 12px;
}

.pro-table__footer :deep(.el-pagination) {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--table-blue);
}

.column-setting__header {
  justify-content: space-between;
  margin-bottom: 8px;
}

.column-setting__header strong {
  color: var(--table-text-primary);
  font-size: 13px;
}

.column-setting__list {
  display: grid;
  gap: 2px;
}

.column-setting__item {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  cursor: default;
  color: var(--table-text-secondary);
  font-size: 13px;
}

.column-setting__drag {
  margin-left: auto;
  color: #cbd5e1;
  cursor: move;
}

@media (max-width: 768px) {
  .pro-table__filter {
    padding: 12px;
  }

  .pro-table__toolbar {
    align-items: flex-start;
    padding: 0 12px;
  }

  .pro-table__footer {
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .pro-table__pagination-total {
    width: 100%;
  }
}
</style>
