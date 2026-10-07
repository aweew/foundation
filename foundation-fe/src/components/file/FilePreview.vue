<template>
  <ImagePreview v-if="kind === 'image'" v-model="visible" :file="file" />
  <el-dialog v-else v-model="visible" :title="file?.name || '文件预览'" width="min(960px, 94vw)" destroy-on-close>
    <div v-loading="loading" class="file-stage">
      <el-empty v-if="failed" description="文件加载失败，请检查访问地址">
        <el-button @click="loadText">重试</el-button>
      </el-empty>
      <video v-else-if="kind === 'video'" :src="file?.url" controls preload="metadata" @error="failed = true" />
      <audio v-else-if="kind === 'audio'" :src="file?.url" controls preload="metadata" @error="failed = true" />
      <iframe
        v-else-if="kind === 'pdf'"
        :src="file?.url"
        :title="file?.name"
        sandbox="allow-scripts allow-same-origin"
      />
      <pre v-else-if="kind === 'text'">{{ text }}</pre>
      <el-empty v-else description="该文件暂不支持在线预览" />
    </div>
    <template #footer>
      <el-button :icon="Download" :disabled="!file?.url" @click="download">下载文件</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { Download } from '@element-plus/icons-vue';
import ImagePreview from './ImagePreview.vue';
import type { PreviewFile } from './types';

const visible = defineModel<boolean>({ default: false });
const props = defineProps<{ file?: PreviewFile }>();
const loading = ref(false);
const failed = ref(false);
const text = ref('');
let request: AbortController | undefined;
const kind = computed(() => {
  const mime = props.file?.contentType?.split(';')[0]?.toLowerCase() || '';
  const extension = (props.file?.extension || props.file?.name.split('.').pop() || '').toLowerCase();
  if (mime.startsWith('image/') || ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg', 'avif'].includes(extension))
    return 'image';
  if (mime.startsWith('video/') || ['mp4', 'webm', 'ogv'].includes(extension)) return 'video';
  if (mime.startsWith('audio/') || ['mp3', 'wav', 'ogg', 'm4a', 'flac'].includes(extension)) return 'audio';
  if (mime === 'application/pdf' || extension === 'pdf') return 'pdf';
  if (mime.startsWith('text/') || ['txt', 'csv', 'json', 'log', 'md', 'xml'].includes(extension)) return 'text';
  return 'other';
});
const loadText = async () => {
  request?.abort();
  failed.value = false;
  loading.value = false;
  text.value = '';
  if (!visible.value || kind.value !== 'text' || !props.file?.url) return;
  const controller = new AbortController();
  request = controller;
  loading.value = true;
  try {
    const response = await fetch(props.file.url, { signal: controller.signal });
    if (!response.ok) throw new Error('文件读取失败');
    const reader = response.body?.getReader();
    if (!reader) throw new Error('文件读取失败');
    const decoder = new TextDecoder();
    let received = 0;
    let content = '';
    const limit = 1024 * 1024;
    while (received < limit) {
      const chunk = await reader.read();
      if (chunk.done) break;
      const part = chunk.value.subarray(0, limit - received);
      content += decoder.decode(part, { stream: true });
      received += part.length;
    }
    content += decoder.decode();
    await reader.cancel();
    if (!controller.signal.aborted) text.value = content + (received >= limit ? '\n\n[仅预览前 1 MB]' : '');
  } catch {
    if (!controller.signal.aborted) failed.value = true;
  } finally {
    if (!controller.signal.aborted) loading.value = false;
  }
};
const download = () => {
  if (!props.file?.url) return;
  const link = document.createElement('a');
  link.href = props.file.url;
  link.download = props.file.name;
  link.target = '_blank';
  link.rel = 'noopener noreferrer';
  link.click();
};
watch(() => [visible.value, props.file], loadText);
onBeforeUnmount(() => request?.abort());
</script>

<style scoped>
.file-stage {
  min-height: 240px;
}
.file-stage video,
.file-stage audio {
  display: block;
  width: 100%;
  max-height: 65vh;
}
.file-stage iframe {
  display: block;
  width: 100%;
  height: 65vh;
  border: 0;
}
.file-stage pre {
  max-height: 65vh;
  overflow: auto;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  margin: 0;
}
</style>
