<template>
  <div class="page-heading">
    <div>
      <h1>云存储配置</h1>
      <p>管理文件存储厂商与当前默认配置</p>
    </div>
    <el-button v-permission="'sys:storage:save'" type="primary" :icon="Plus" @click="openEditor()">新增配置</el-button>
  </div>

  <el-card v-loading="loading">
    <el-table :data="configs" stripe>
      <el-table-column prop="providerName" label="厂商" min-width="130" />
      <el-table-column prop="providerCode" label="编码" min-width="110" />
      <el-table-column prop="serviceName" label="服务名/空间" min-width="160" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          <el-tag v-if="row.isDefault" type="primary" class="default-tag">当前使用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="认证" width="110">
        <template #default="{ row }">
          <el-tag :type="row.credentialConfigured ? 'success' : 'danger'">
            {{ row.credentialConfigured ? '已配置' : '未配置' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column label="操作" width="330" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'sys:storage:update'" link type="primary" :icon="Edit" @click="openEditor(row)">
            编辑
          </el-button>
          <el-button
            v-permission="'sys:storage:test'"
            link
            type="warning"
            :icon="Connection"
            :loading="testingId === row.id"
            @click="testConfig(row)"
          >
            测试
          </el-button>
          <el-button
            v-permission="'sys:storage:activate'"
            link
            type="success"
            :icon="CircleCheck"
            :disabled="row.isDefault || !row.enabled"
            :loading="activatingId === row.id"
            @click="activateConfig(row)"
          >
            启用
          </el-button>
          <el-button
            v-permission="'sys:storage:delete'"
            link
            type="danger"
            :icon="Delete"
            :disabled="row.isDefault"
            @click="deleteConfig(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty v-if="!loading" description="暂无云存储配置" />
      </template>
    </el-table>
  </el-card>

  <el-dialog
    v-model="editorVisible"
    :title="editingId ? '编辑云存储配置' : '新增云存储配置'"
    width="min(680px, 94vw)"
    top="5vh"
    class="storage-editor"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="formModel"
      :rules="rules"
      label-width="140px"
      :disabled="saving"
      @submit.prevent="saveConfig"
    >
      <el-form-item label="厂商编码" prop="providerCode">
        <el-select v-model="formModel.providerCode" :disabled="Boolean(editingId)" style="width: 100%">
          <el-option label="又拍云" value="upyun" />
          <el-option label="S3 兼容存储（又拍云 S3）" value="s3" />
        </el-select>
      </el-form-item>
      <el-form-item label="厂商名称" prop="providerName">
        <el-input v-model="formModel.providerName" placeholder="例如：又拍云" />
      </el-form-item>
      <el-form-item label="服务名/空间" prop="serviceName">
        <el-input v-model="formModel.serviceName" :placeholder="isS3 ? 'Bucket / 空间名' : '又拍云服务名'" />
      </el-form-item>
      <el-form-item label="访问域名" prop="accessDomain">
        <el-input v-model="formModel.accessDomain" placeholder="https://cdn.example.com" />
      </el-form-item>
      <el-form-item label="API 端点" prop="endpoint">
        <el-input
          v-model="formModel.endpoint"
          :placeholder="isS3 ? 'https://S3 服务端点' : '可选，使用默认端点时留空'"
        />
      </el-form-item>

      <el-form-item v-if="isS3" label="Region" prop="region">
        <el-input v-model="formModel.region" placeholder="签名区域" />
      </el-form-item>
      <el-form-item label="对象根路径">
        <el-input v-model="formModel.basePath" placeholder="例如：/foundation" />
      </el-form-item>
      <el-form-item v-if="!isS3" label="操作员" prop="operator">
        <el-input v-model="formModel.operator" autocomplete="off" />
      </el-form-item>
      <el-form-item v-if="!isS3" label="操作员密码" prop="password">
        <el-input
          v-model="formModel.password"
          type="password"
          show-password
          autocomplete="new-password"
          :placeholder="editingId ? '留空保持原密码' : '请输入密码'"
        />
      </el-form-item>

      <el-form-item v-if="isS3" label="AccessKey" prop="accessKey">
        <el-input
          v-model="formModel.accessKey"
          autocomplete="off"
          :placeholder="editingId ? '留空保持原值' : '请输入 AccessKey'"
        />
      </el-form-item>

      <el-form-item v-if="isS3" label="SecretAccessKey" prop="secretAccessKey">
        <el-input
          v-model="formModel.secretAccessKey"
          type="password"
          show-password
          autocomplete="new-password"
          :placeholder="editingId ? '留空保持原值' : '请输入 SecretAccessKey'"
        />
      </el-form-item>
      <el-form-item label="启用状态">
        <el-switch v-model="formModel.enabled" />
      </el-form-item>
      <el-form-item label="私有空间">
        <el-switch v-model="formModel.privateBucket" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formModel.remark" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editorVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveConfig">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { CircleCheck, Connection, Delete, Edit, Plus } from '@element-plus/icons-vue';
import {
  activateStorageProviderConfig,
  createStorageProviderConfig,
  deleteStorageProviderConfig,
  getStorageProviderConfigs,
  testStorageProviderConfig,
  updateStorageProviderConfig,
} from '@/api/storage';
import type { StorageProviderConfig, StorageProviderConfigForm } from '@/api/storage/types';

const loading = ref(false);
const saving = ref(false);
const testingId = ref<number>();
const activatingId = ref<number>();
const configs = ref<StorageProviderConfig[]>([]);
const editorVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const formModel = reactive<StorageProviderConfigForm>(createForm());
const isS3 = computed(() => formModel.providerCode === 's3');
const rules = computed<FormRules>(() => ({
  providerCode: [{ required: true, message: '请选择厂商', trigger: 'change' }],
  providerName: [{ required: true, message: '请输入厂商名称', trigger: 'blur' }],
  serviceName: [{ required: true, message: '请输入服务名或空间名', trigger: 'blur' }],
  accessDomain: [{ required: !isS3.value || !formModel.privateBucket, message: '请输入访问域名', trigger: 'blur' }],
  endpoint: [{ required: isS3.value, message: '请输入 S3 服务端点', trigger: 'blur' }],
  region: [{ required: isS3.value, message: '请输入签名区域', trigger: 'blur' }],
  accessKey: [{ required: isS3.value && !editingId.value, message: '请输入 AccessKey', trigger: 'blur' }],
  secretAccessKey: [{ required: isS3.value && !editingId.value, message: '请输入 SecretAccessKey', trigger: 'blur' }],
  operator: [
    {
      required: !isS3.value && (!editingId.value || Boolean(formModel.password)),
      message: '请输入操作员',
      trigger: 'blur',
    },
  ],
  password: [
    {
      required: !isS3.value && (!editingId.value || Boolean(formModel.operator)),
      message: '请输入操作员密码',
      trigger: 'blur',
    },
  ],
}));

watch(
  () => formModel.providerCode,
  () => {
    if (editingId.value) return;
    formModel.providerName = isS3.value ? '又拍云 S3' : '又拍云';
    formModel.operator = '';
    formModel.password = '';
    formModel.accessKey = '';
    formModel.secretAccessKey = '';
    formRef.value?.clearValidate();
  },
);

function createForm(): StorageProviderConfigForm {
  return {
    providerCode: 'upyun',
    providerName: '又拍云',
    enabled: true,
    endpoint: '',
    region: '',
    accessKey: '',
    secretAccessKey: '',
    serviceName: '',
    accessDomain: '',
    basePath: '/foundation',
    privateBucket: true,
    operator: '',
    password: '',
    remark: '',
  };
}

const load = async () => {
  loading.value = true;
  try {
    configs.value = (await getStorageProviderConfigs()).data.data || [];
  } finally {
    loading.value = false;
  }
};

const openEditor = (row?: StorageProviderConfig) => {
  editingId.value = row?.id;
  Object.assign(formModel, createForm());
  if (row)
    Object.assign(formModel, row, {
      region: row.region || '',
      operator: '',
      password: '',
      accessKey: '',
      secretAccessKey: '',
    });
  editorVisible.value = true;
};

const saveConfig = async () => {
  if (!formRef.value || saving.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    if (editingId.value) {
      await updateStorageProviderConfig({ ...formModel, id: editingId.value });
    } else {
      await createStorageProviderConfig(formModel);
    }
    editorVisible.value = false;
    ElMessage.success('保存成功');
    await load();
  } finally {
    saving.value = false;
  }
};

const testConfig = async (row: StorageProviderConfig) => {
  testingId.value = row.id;
  try {
    await testStorageProviderConfig(row.id);
    ElMessage.success('连接测试成功');
  } finally {
    testingId.value = undefined;
  }
};

const activateConfig = async (row: StorageProviderConfig) => {
  try {
    await ElMessageBox.confirm(`确定将“${row.providerName}”设为当前使用的存储吗？`, '切换确认', { type: 'warning' });
  } catch {
    return;
  }
  activatingId.value = row.id;
  try {
    await activateStorageProviderConfig(row.id);
    ElMessage.success('切换成功');
    await load();
  } finally {
    activatingId.value = undefined;
  }
};

const deleteConfig = async (row: StorageProviderConfig) => {
  try {
    await ElMessageBox.confirm(`确定删除“${row.providerName}”吗？`, '删除确认', { type: 'warning' });
    await deleteStorageProviderConfig(row.id);
    ElMessage.success('删除成功');
    await load();
  } catch {
    // 取消确认或接口失败时保留当前列表
  }
};

onMounted(load);
</script>

<style scoped>
.default-tag {
  margin-left: 6px;
}

:deep(.storage-editor .el-dialog__body) {
  max-height: calc(100dvh - 180px);
  overflow-y: auto;
}
</style>
