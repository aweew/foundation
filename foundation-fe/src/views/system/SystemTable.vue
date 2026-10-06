<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import {
  createSystemItem,
  deleteSystemItem,
  getSystemItem,
  updateSystemItem,
} from '@/api/system'
import type { QueryPage, SystemFormField, SystemRecord, SystemResource } from '@/api/system/types'
import StatusBadge from '@/components/StatusBadge.vue'

interface TableColumn {
  prop: string;
  label: string;
  enumName?: string;
  options?: { value: string | number; label: string }[]
}

const props = defineProps<{
  title: string;
  resource: SystemResource;
  loader: (params: QueryPage) => Promise<any>;
  fields: SystemFormField[];
  preservedFields?: string[];
  columns: TableColumn[]
}>()
const loading = ref(false)
const rows = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref({ current: 1, size: 10 })
const editorVisible = ref(false)
const editorLoading = ref(false)
const saving = ref(false)
const editingId = ref<string | number>()
const deletingId = ref<string | number>()
const formRef = ref<FormInstance>()
const formModel = ref<SystemRecord>({})
const originalRecord = ref<SystemRecord>({})
const visibleFields = computed(() => props.fields.filter(field => !field.createOnly || editingId.value === undefined))
const formRules = computed<FormRules>(() => {
  const rules: FormRules = {}
  for (const field of visibleFields.value) {
    if (field.required) {
      const numeric = field.type === 'number' || (field.type === 'select' && typeof field.options?.[0]?.value === 'number')
      rules[field.prop] = [{
        required: true,
        type: numeric ? 'number' : 'string',
        whitespace: !numeric,
        message: `${field.type === 'select' ? '请选择' : '请输入'}${field.label}`,
        trigger: field.type === 'select' ? 'change' : 'blur',
      }]
    }
  }
  return rules
})

const openEditor = async (row?: Record<string, unknown>) => {
  editingId.value = row?.id as string | number | undefined
  originalRecord.value = {}
  formModel.value = {}
  editorVisible.value = true
  editorLoading.value = true
  try {
    if (editingId.value !== undefined) {
      const response = await getSystemItem(props.resource, editingId.value)
      if (!response.data.data) {
        ElMessage.error('记录不存在，请刷新列表')
        editorVisible.value = false
        return
      }
      originalRecord.value = response.data.data
    }
    for (const field of visibleFields.value) {
      formModel.value[field.prop] = originalRecord.value[field.prop] ?? field.defaultValue ??
        (field.type === 'switch' ? false : field.type === 'number' || field.type === 'select' ? undefined : '')
    }
    await nextTick()
    formRef.value?.clearValidate()
  } catch {
    editorVisible.value = false
  } finally {
    editorLoading.value = false
  }
}

const saveItem = async () => {
  if (saving.value || editorLoading.value || !formRef.value) return
  saving.value = true
  try {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    const payload: SystemRecord = {}
    for (const field of visibleFields.value) payload[field.prop] = formModel.value[field.prop] ?? null
    if (editingId.value !== undefined) {
      payload.id = editingId.value
      for (const field of props.preservedFields ?? []) payload[field] = originalRecord.value[field]
      await updateSystemItem(props.resource, payload)
    } else {
      await createSystemItem(props.resource, payload)
      page.value.current = 1
    }
    editorVisible.value = false
    ElMessage.success(editingId.value !== undefined ? '修改成功' : '新增成功')
    await load()
  } catch {
    // 请求拦截器显示接口错误，保留表单以便重试
  } finally {
    saving.value = false
  }
}

const deleteItem = async (row: Record<string, unknown>) => {
  if (deletingId.value !== undefined) return
  const id = row.id as string | number
  const name = row.nickName || row.name || row.title || id
  try {
    await ElMessageBox.confirm(`确定删除“${name}”吗？`, '删除确认', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消',
    })
  } catch {
    return
  }
  deletingId.value = id
  try {
    await deleteSystemItem(props.resource, id)
    ElMessage.success('删除成功')
    if (rows.value.length === 1 && page.value.current > 1) page.value.current--
    await load()
  } catch {
    // 请求拦截器显示接口错误，保留列表记录
  } finally {
    deletingId.value = undefined
  }
}

const closeEditor = (done: () => void) => {
  if (!saving.value && !editorLoading.value) done()
}

const formatCell = (row: Record<string, unknown>, column: TableColumn) => {
  const fieldValue = row[column.prop]
  if (fieldValue === null || fieldValue === undefined) return ''

  const option = column.options?.find(option => String(option.value) === String(fieldValue))
  return option?.label ?? fieldValue
}

const load = async () => {
  loading.value = true
  try {
    const response = await props.loader(page.value)
    rows.value = response.data.data.records || []
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
    <el-button type="primary" :icon="Plus" @click="openEditor()">新增</el-button>
  </div>
  <el-card>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column v-for="column in columns" :key="column.prop" :prop="column.prop" :label="column.label"
                       min-width="150">
        <template #default="{ row }">
          <StatusBadge v-if="column.enumName || column.prop === 'status'"
                       :enum-name="column.enumName || 'StatusEnum'" :value="row[column.prop]" />
          <template v-else>{{ formatCell(row, column) }}</template>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="openEditor(row)">编辑</el-button>
          <el-button link type="danger" :icon="Delete" :loading="deletingId === row.id"
                     :disabled="deletingId !== undefined" @click="deleteItem(row)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size"
                     layout="total, sizes, prev, pager, next" :total="total" @change="load" />
    </div>
  </el-card>
  <el-dialog v-model="editorVisible" :title="`${editingId === undefined ? '新增' : '编辑'}${title.replace('管理', '')}`"
             width="min(640px, 94vw)" top="8vh" :close-on-click-modal="false" :before-close="closeEditor"
             destroy-on-close>
    <el-form ref="formRef" v-loading="editorLoading" class="editor-form" :model="formModel" :rules="formRules"
             label-width="110px" :disabled="saving || editorLoading" @submit.prevent="saveItem">
      <el-form-item v-for="field in visibleFields" :key="field.prop" :label="field.label" :prop="field.prop">
        <el-select v-if="field.type === 'select'" v-model="formModel[field.prop]" :placeholder="`请选择${field.label}`"
                   :clearable="!field.required" style="width: 100%">
          <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-switch v-else-if="field.type === 'switch'" :model-value="Boolean(formModel[field.prop])"
                   @update:model-value="formModel[field.prop] = Boolean($event)" />
        <el-input-number v-else-if="field.type === 'number'"
                         :model-value="typeof formModel[field.prop] === 'number' ? Number(formModel[field.prop]) : undefined"
                         :min="0" :precision="0" @update:model-value="formModel[field.prop] = $event" />
        <el-input v-else :model-value="String(formModel[field.prop] ?? '')"
                  :type="field.type === 'textarea' ? 'textarea' : field.type === 'password' ? 'password' : 'text'"
                  :rows="3" :show-password="field.type === 'password'"
                  :autocomplete="field.type === 'password' ? 'new-password' : 'off'"
                  @update:model-value="formModel[field.prop] = $event" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="saving || editorLoading" @click="editorVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" :disabled="editorLoading" @click="saveItem">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.editor-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 16px;
}
</style>
