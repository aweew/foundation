<template>
  <el-dialog
    v-model="visible"
    :title="file?.name || '图片预览'"
    width="min(960px, 94vw)"
    destroy-on-close
    @closed="reset"
  >
    <div class="image-tools">
      <el-button :icon="ZoomOut" circle title="缩小" aria-label="缩小" @click="zoom = Math.max(0.25, zoom - 0.25)" />
      <el-button :icon="ZoomIn" circle title="放大" aria-label="放大" @click="zoom = Math.min(4, zoom + 0.25)" />
      <el-button :icon="RefreshRight" circle title="旋转" aria-label="旋转" @click="rotation += 90" />
      <el-button
        :icon="Refresh"
        circle
        title="重置"
        aria-label="重置"
        @click="
          zoom = 1;
          rotation = 0;
        "
      />
    </div>
    <div v-loading="loading" class="image-stage">
      <el-empty v-if="failed" description="图片加载失败，请检查访问地址">
        <el-button @click="retry">重试</el-button>
      </el-empty>
      <img
        v-else-if="file"
        :key="attempt"
        :src="file.url"
        :alt="file.name"
        :style="{ transform: `scale(${zoom}) rotate(${rotation}deg)` }"
        @load="loading = false"
        @error="
          failed = true;
          loading = false;
        "
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { Refresh, RefreshRight, ZoomIn, ZoomOut } from '@element-plus/icons-vue';
import type { PreviewFile } from './types';

const visible = defineModel<boolean>({ default: false });
const props = defineProps<{ file?: PreviewFile }>();
const zoom = ref(1);
const rotation = ref(0);
const loading = ref(true);
const failed = ref(false);
const attempt = ref(0);
const reset = () => {
  zoom.value = 1;
  rotation.value = 0;
  loading.value = true;
  failed.value = false;
};
const retry = () => {
  reset();
  attempt.value++;
};
watch(
  () => [visible.value, props.file?.url],
  () => {
    if (visible.value) reset();
  },
);
</script>

<style scoped>
.image-tools {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}
.image-tools .el-button {
  margin-left: 0;
}
.image-stage {
  height: min(65vh, 640px);
  overflow: auto;
  display: grid;
  place-items: center;
  background: #f5f7fa;
}
.image-stage img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.15s;
}
</style>
