<template>
  <div class="page-heading">
    <div>
      <h1>{{ title }}</h1>
      <p>维护平台基础数据</p>
    </div>
    <el-button v-permission="`sys:${resource}:save`" type="primary" :icon="Plus" @click="openEditor()">新增</el-button>
  </div>
  <el-card :class="{ 'plain-surface': plain }">
    <el-table
      v-loading="loading"
      :data="rows"
      :row-key="tree ? 'id' : undefined"
      :tree-props="tree ? { children: 'childList' } : undefined"
      stripe
    >
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        min-width="150"
      >
        <template #default="{ row }">
          <StatusBadge
            v-if="column.enumName || column.prop === 'status'"
            :enum-name="column.enumName || 'StatusEnum'"
            :value="row[column.prop]"
          />
          <template v-else>{{ formatCell(row, column) }}</template>
        </template>
      </el-table-column>

      <el-table-column label="操作" :width="tree ? 156 : relation ? 128 : 96" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-tooltip content="编辑" placement="top">
              <el-button
                v-permission="`sys:${resource}:update`"
                circle
                text
                type="primary"
                :icon="Edit"
                aria-label="编辑"
                @click="openEditor(row)"
              />
            </el-tooltip>
            <el-tooltip v-if="relation" :content="relation.label" placement="top">
              <el-button
                v-permission="relation.permission"
                circle
                text
                type="primary"
                :icon="Setting"
                :aria-label="relation.label"
                @click="emit('manage-relation', row)"
              />
            </el-tooltip>
            <el-tooltip v-if="tree && showChildAction" content="新增子项" placement="top">
              <el-button
                v-permission="`sys:${resource}:save`"
                circle
                text
                type="primary"
                :icon="Plus"
                aria-label="新增子项"
                @click="openChildEditor(row)"
              />
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button
                v-permission="`sys:${resource}:delete`"
                circle
                text
                type="danger"
                :icon="Delete"
                :loading="deletingId === row.id"
                :disabled="deletingId !== undefined"
                aria-label="删除"
                @click="deleteItem(row)"
              />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="!tree" class="pagination">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @change="load"
      />
    </div>
  </el-card>
  <el-dialog
    v-model="editorVisible"
    :title="`${editingId === undefined ? '新增' : '编辑'}${title.replace('管理', '')}`"
    width="min(640px, 94vw)"
    top="8vh"
    :close-on-click-modal="false"
    :before-close="closeEditor"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      v-loading="editorLoading"
      class="editor-form"
      :model="formModel"
      :rules="formRules"
      label-width="110px"
      :disabled="saving || editorLoading"
      @submit.prevent="saveItem"
    >
      <el-form-item v-for="field in visibleFields" :key="field.prop" :label="field.label" :prop="field.prop">
        <el-select
          v-if="field.type === 'select'"
          v-model="formModel[field.prop]"
          :placeholder="`请选择${field.label}`"
          :clearable="!field.required"
          style="width: 100%"
        >
          <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-switch
          v-else-if="field.type === 'switch'"
          :model-value="Boolean(formModel[field.prop])"
          @update:model-value="formModel[field.prop] = Boolean($event)"
        />
        <el-input-number
          v-else-if="field.type === 'number'"
          :model-value="typeof formModel[field.prop] === 'number' ? Number(formModel[field.prop]) : undefined"
          :min="0"
          :precision="0"
          @update:model-value="formModel[field.prop] = $event"
        />
        <el-input
          v-else
          :model-value="String(formModel[field.prop] ?? '')"
          :type="field.type === 'textarea' ? 'textarea' : field.type === 'password' ? 'password' : 'text'"
          :rows="3"
          :show-password="field.type === 'password'"
          :autocomplete="field.type === 'password' ? 'new-password' : 'off'"
          @update:model-value="formModel[field.prop] = $event"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="saving || editorLoading" @click="editorVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" :disabled="editorLoading" @click="saveItem">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Delete, Edit, Plus, Setting } from '@element-plus/icons-vue';
import { createSystemItem, deleteSystemItem, getSystemItem, updateSystemItem } from '@/api/system';
import type { ApiResult, PageResponse } from '@/types/api';
import type { QueryPage, SystemFormField, SystemPageRecord, SystemRecord, SystemResource } from '@/api/system/types';
import StatusBadge from '@/components/StatusBadge.vue';

interface TableColumn {
  prop: string;
  label: string;
  enumName?: string;
  options?: { value: string | number; label: string }[];
}

type TableData = PageResponse<SystemPageRecord> | SystemPageRecord[];

const props = defineProps<{
  title: string;
  resource: SystemResource;
  loader: (params: QueryPage) => Promise<{ data: ApiResult<TableData> }>;
  fields: SystemFormField[];
  preservedFields?: string[];
  columns: TableColumn[];
  relation?: { label: string; permission: string };
  tree?: boolean;
  showChildAction?: boolean;
  plain?: boolean;
}>();
const emit = defineEmits<{ 'manage-relation': [row: SystemPageRecord] }>();
const loading = ref(false);
const rows = ref<SystemPageRecord[]>([]);
const total = ref(0);
const page = ref({ current: 1, size: 10 });
const editorVisible = ref(false);
const editorLoading = ref(false);
const saving = ref(false);
const editingId = ref<string | number>();
const deletingId = ref<string | number>();
const formRef = ref<FormInstance>();
const formModel = ref<SystemRecord>({});
const originalRecord = ref<SystemRecord>({});
const visibleFields = computed(() =>
  props.fields.filter((field) => !field.createOnly || editingId.value === undefined),
);
const formRules = computed<FormRules>(() => {
  const rules: FormRules = {};
  for (const field of visibleFields.value) {
    if (field.required) {
      const numeric =
        field.type === 'number' || (field.type === 'select' && typeof field.options?.[0]?.value === 'number');
      rules[field.prop] = [
        {
          required: true,
          type: numeric ? 'number' : 'string',
          whitespace: !numeric,
          message: `${field.type === 'select' ? '请选择' : '请输入'}${field.label}`,
          trigger: field.type === 'select' ? 'change' : 'blur',
        },
      ];
    }
  }
  return rules;
});

/**
 * 打开新增或编辑弹窗，并准备表单初始数据
 * @param row 待编辑的列表记录，不传时进入新增模式
 */
const openEditor = async (row?: SystemPageRecord) => {
  editingId.value = row?.id as string | number | undefined;
  originalRecord.value = {};
  formModel.value = {};
  editorVisible.value = true;
  editorLoading.value = true;
  try {
    // 编辑模式先读取完整记录，保留列表中未展示的字段
    if (editingId.value !== undefined) {
      const response = await getSystemItem(props.resource, editingId.value);
      if (!response.data.data) {
        ElMessage.error('记录不存在，请刷新列表');
        editorVisible.value = false;
        return;
      }
      originalRecord.value = response.data.data;
    }
    // 按字段配置生成表单值，确保新增和编辑使用一致的数据结构
    for (const field of visibleFields.value) {
      formModel.value[field.prop] =
        originalRecord.value[field.prop] ??
        field.defaultValue ??
        (field.type === 'switch' ? false : field.type === 'number' || field.type === 'select' ? undefined : '');
    }
    await nextTick();
    formRef.value?.clearValidate();
  } catch {
    editorVisible.value = false;
  } finally {
    editorLoading.value = false;
  }
};

/**
 * 打开新增子项表单并自动填充父级节点
 * @param row 当前父级节点
 */
const openChildEditor = async (row: SystemPageRecord) => {
  await openEditor();
  formModel.value.parentId = row.id;
};

/**
 * 校验并保存当前表单，完成新增或编辑后刷新列表
 */
const saveItem = async () => {
  if (saving.value || editorLoading.value || !formRef.value) return;
  saving.value = true;
  try {
    // 先完成前端校验，避免向接口提交不完整数据
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) return;
    // 只提交当前资源允许编辑的字段，并转换空值为接口可识别的 null
    const payload: SystemRecord = {};
    for (const field of visibleFields.value) payload[field.prop] = formModel.value[field.prop] ?? null;
    if (editingId.value !== undefined) {
      // 编辑时回填需要保留的系统字段，防止表单覆盖原有数据
      payload.id = editingId.value;
      for (const field of props.preservedFields ?? []) payload[field] = originalRecord.value[field];
      await updateSystemItem(props.resource, payload);
    } else {
      // 新增成功后回到第一页，确保新记录能立即出现在列表中
      await createSystemItem(props.resource, payload);
      page.value.current = 1;
    }
    editorVisible.value = false;
    ElMessage.success(editingId.value !== undefined ? '修改成功' : '新增成功');
    await load();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
    // 保留表单以便修正后重试
  } finally {
    saving.value = false;
  }
};

/**
 * 二次确认后删除指定系统资源，并同步刷新当前页
 * @param row 待删除的列表记录
 */
const deleteItem = async (row: SystemPageRecord) => {
  if (deletingId.value !== undefined) return;
  const id = row.id as string | number;
  const name = row.nickName || row.name || row.title || id;
  try {
    await ElMessageBox.confirm(`确定删除“${name}”吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }
  deletingId.value = id;
  try {
    // 删除成功后处理当前页为空的情况，避免停留在不存在的页码
    await deleteSystemItem(props.resource, id);
    ElMessage.success('删除成功');
    if (rows.value.length === 1 && page.value.current > 1) page.value.current--;
    await load();
  } catch {
    // 请求拦截器显示接口错误，保留列表记录
  } finally {
    deletingId.value = undefined;
  }
};

/**
 * 处理编辑弹窗关闭请求，保存或加载期间禁止关闭
 * @param done Element Plus 提供的实际关闭回调
 */
const closeEditor = (done: () => void) => {
  if (!saving.value && !editorLoading.value) done();
};

/**
 * 将枚举值转换为表格展示文本
 * @param row 当前行数据
 * @param column 当前列配置
 */
const formatCell = (row: SystemPageRecord, column: TableColumn) => {
  const fieldValue = row[column.prop];
  if (fieldValue === null || fieldValue === undefined) return '';

  const option = column.options?.find((option) => String(option.value) === String(fieldValue));
  return option?.label ?? fieldValue;
};

/**
 * 按当前分页条件加载系统资源列表
 */
const load = async () => {
  loading.value = true;
  try {
    const response = await props.loader(page.value);
    const pageData = response.data.data;
    rows.value = Array.isArray(pageData) ? pageData : pageData.records || [];
    total.value = Array.isArray(pageData) ? rows.value.length : pageData.total || 0;
  } catch {
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.row-actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.row-actions :deep(.el-button) {
  width: 36px;
  height: 36px;
  margin: 0;
}

.plain-surface {
  border: 0;
  box-shadow: none;
  background: transparent;
}

.plain-surface :deep(.el-card__body) {
  padding: 0;
}

.plain-surface :deep(.el-table),
.plain-surface :deep(.el-table__expanded-cell) {
  background: transparent;
}

.plain-surface :deep(.el-table tr) {
  background: rgba(255, 255, 255, 0.45);
}

.plain-surface :deep(.el-table th.el-table__cell) {
  background: rgba(255, 255, 255, 0.6);
  color: #6e6e73;
  font-weight: 500;
}

.editor-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 16px;
}
</style>
