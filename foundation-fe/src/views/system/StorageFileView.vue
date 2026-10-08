<template>
  <div class="page-heading">
    <div>
      <h1>文件管理</h1>
      <p>统一查看、上传和维护云存储文件</p>
    </div>
    <FileUpload v-permission="'sys:storage:file:save'" @success="() => load(false)" />
  </div>

  <el-card class="filter-panel">
    <el-form :model="filters" inline @submit.prevent="search">
      <el-form-item label="文件名">
        <el-input v-model="filters.originalName" clearable placeholder="输入文件名" class="filter-control" />
      </el-form-item>
      <el-form-item label="业务类型">
        <el-input v-model="filters.businessType" clearable placeholder="输入业务类型" class="filter-control" />
      </el-form-item>
      <el-form-item label="业务ID">
        <el-input v-model="filters.businessId" clearable placeholder="输入业务ID" class="filter-control" />
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
        <el-button type="primary" :icon="Search" :loading="loading" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <el-card>
    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column label="预览" width="88" fixed="left">
        <template #default="{ row }">
          <button
            v-if="isImage(row)"
            class="file-thumbnail"
            type="button"
            :aria-label="`预览 ${row.originalName}`"
            :title="row.originalName"
            @click="preview(row)"
          >
            <el-image :src="row.accessUrl" :alt="row.originalName" fit="cover" loading="lazy">
              <template #placeholder>
                <el-icon>
                  <Picture />
                </el-icon>
              </template>
              <template #error>
                <el-icon>
                  <Picture />
                </el-icon>
              </template>
            </el-image>
          </button>
          <el-icon v-else class="file-icon">
            <Document />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="originalName" label="文件名" min-width="220" show-overflow-tooltip />
      <el-table-column prop="contentType" label="类型" width="150" show-overflow-tooltip />
      <el-table-column prop="fileSize" label="大小" width="110">
        <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="businessType" label="业务类型" width="130" />
      <el-table-column prop="businessId" label="业务ID" width="130" />
      <el-table-column prop="providerCode" label="存储厂商" width="120" />
      <el-table-column prop="createTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button
            v-if="!isImage(row)"
            link
            type="primary"
            :icon="View"
            :disabled="!row.accessUrl"
            @click="preview(row)"
          >
            查看
          </el-button>
          <el-button link type="info" :icon="CopyDocument" :disabled="!row.accessUrl" @click="copyUrl(row)">
            复制链接
          </el-button>
          <el-button v-permission="'sys:storage:file:delete'" link type="danger" :icon="Delete" @click="remove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty v-if="!loading" description="暂无文件" />
      </template>
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
  <FilePreview v-model="previewVisible" :file="previewFile" />
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CopyDocument, Delete, Document, Picture, Refresh, Search, View } from '@element-plus/icons-vue';
import { deleteStorageFile, getStorageFileAccessUrl, getStorageFilePage } from '@/api/storage';
import FileUpload from '@/components/file/FileUpload.vue';
import FilePreview from '@/components/file/FilePreview.vue';
import type { PreviewFile } from '@/components/file/types';
import type { StorageFile, StorageFileQuery } from '@/api/storage/types';

const loading = ref(false);
const previewVisible = ref(false);
const previewFile = ref<PreviewFile>();
const rows = ref<StorageFile[]>([]);
const total = ref(0);
const page = reactive({ current: 1, size: 10 });
const dateRange = ref<[string, string]>();
const filters = reactive<StorageFileQuery>({ current: 1, size: 10 });

const isImage = (file: StorageFile) => {
  const extension = (file.extension || file.originalName.split('.').pop() || '').toLowerCase();
  return (
    file.contentType?.startsWith('image/') ||
    ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg', 'avif'].includes(extension)
  );
};

const load = async (showLoading = true) => {
  if (showLoading) loading.value = true;
  try {
    filters.current = page.current;
    filters.size = page.size;
    filters.startTime = dateRange.value?.[0];
    filters.endTime = dateRange.value?.[1];
    const response = await getStorageFilePage({ ...filters });
    rows.value = response.data.data.records || [];
    total.value = response.data.data.total || 0;
  } finally {
    loading.value = false;
  }
};

const search = async () => {
  page.current = 1;
  await load();
};

const reset = async () => {
  filters.originalName = undefined;
  filters.businessType = undefined;
  filters.businessId = undefined;
  dateRange.value = undefined;
  await search();
};

const preview = async (row: StorageFile) => {
  try {
    const response = await getStorageFileAccessUrl(row.id);
    const file = response.data.data;
    if (!file.accessUrl) throw new Error('文件访问地址不存在');
    previewFile.value = {
      name: file.originalName,
      url: file.accessUrl,
      contentType: file.contentType,
      extension: file.extension,
    };
    previewVisible.value = true;
  } catch {
    ElMessage.error('无法获取文件预览地址');
  }
};

const copyUrl = async (row: StorageFile) => {
  try {
    const response = await getStorageFileAccessUrl(row.id);
    const url = response.data.data.accessUrl;
    if (!url) throw new Error('文件访问地址不存在');
    await navigator.clipboard.writeText(url);
    ElMessage.success('链接已复制');
  } catch {
    ElMessage.error('复制失败，请检查访问地址或剪贴板权限');
  }
};

const remove = async (row: StorageFile) => {
  try {
    await ElMessageBox.confirm(`确定删除“${row.originalName}”吗？删除后文件将无法访问。`, '删除确认', {
      type: 'warning',
    });
    await deleteStorageFile(row.id);
    ElMessage.success('删除成功');
    await load();
  } catch {
    // 用户取消删除时保持当前列表
  }
};

const formatFileSize = (size: number) => {
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`;
  return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`;
};

onMounted(load);
</script>

<style scoped>
.file-thumbnail {
  display: grid;
  place-items: center;
  width: 56px;
  height: 56px;
  padding: 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  cursor: pointer;
  overflow: hidden;
}

.file-thumbnail:hover,
.file-thumbnail:focus-visible {
  border-color: var(--el-color-primary);
  outline: 2px solid var(--el-color-primary-light-7);
  outline-offset: 2px;
}

.file-thumbnail .el-image {
  width: 100%;
  height: 100%;
}

.file-thumbnail :deep(.el-image__placeholder),
.file-thumbnail :deep(.el-image__error) {
  display: grid;
  place-items: center;
  height: 100%;
}

.file-icon {
  width: 56px;
  height: 56px;
  font-size: 24px;
  color: var(--el-text-color-secondary);
}

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
