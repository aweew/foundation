<template>
  <el-upload
    :show-file-list="false"
    :multiple="multiple"
    :accept="accept"
    :disabled="disabled || uploading"
    :http-request="uploadFile"
    :before-upload="beforeUpload"
  >
    <slot :uploading="uploading">
      <el-button type="primary" :icon="Upload" :loading="uploading" :disabled="disabled">上传文件</el-button>
    </slot>
  </el-upload>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage, type UploadRequestOptions } from 'element-plus';
import { Upload } from '@element-plus/icons-vue';
import { uploadStorageFile } from '@/api/storage';
import type { StorageFile } from '@/api/storage/types';

const props = withDefaults(
  defineProps<{
    accept?: string;
    multiple?: boolean;
    disabled?: boolean;
    maxSize?: number;
    compress?: boolean;
    businessType?: string;
    businessId?: string;
    upload?: (file: File) => Promise<StorageFile>;
  }>(),
  { maxSize: 5 * 1024 * 1024, compress: true },
);
const emit = defineEmits<{
  success: [file: StorageFile];
  error: [error: Error];
  progress: [percent: number];
}>();
const pendingUploads = ref(0);
const uploading = computed(() => pendingUploads.value > 0);
const beforeUpload = (file: File) => {
  if (!file.size) {
    ElMessage.warning('不能上传空文件');
    return false;
  }
  if (file.size > props.maxSize && (!props.compress || !file.type.startsWith('image/'))) {
    ElMessage.warning('文件不能超过 ' + props.maxSize / 1024 / 1024 + ' MB');
    return false;
  }
  return true;
};
const uploadFile = async (options: UploadRequestOptions) => {
  pendingUploads.value++;
  try {
    const file = options.file.size > props.maxSize ? await compressImage(options.file) : options.file;
    const result = props.upload
      ? await props.upload(file)
      : (
          await uploadStorageFile(file, props.businessType, props.businessId, (percent) => {
            options.onProgress(Object.assign(new ProgressEvent('progress'), { percent }));
            emit('progress', percent);
          })
        ).data.data;
    options.onSuccess(result);
    emit('success', result);
    ElMessage.success('上传成功');
  } catch (cause) {
    const error = cause instanceof Error ? cause : new Error('文件上传失败');
    options.onError(Object.assign(error, { status: 0, method: 'POST', url: options.action }));
    emit('error', error);
    ElMessage.error(error.message);
  } finally {
    pendingUploads.value--;
  }
};

const compressImage = (file: File): Promise<File> => {
  return new Promise((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file);
    const image = new Image();
    image.onload = () => {
      URL.revokeObjectURL(objectUrl);
      const maxDimension = 4096;
      const scale = Math.min(1, maxDimension / Math.max(image.width, image.height));
      const canvas = document.createElement('canvas');
      canvas.width = Math.max(1, Math.round(image.width * scale));
      canvas.height = Math.max(1, Math.round(image.height * scale));
      const context = canvas.getContext('2d');
      if (!context) {
        reject(new Error('图片压缩失败，请重试'));
        return;
      }
      context.drawImage(image, 0, 0, canvas.width, canvas.height);
      compressCanvas(canvas, file, 0.85, resolve, reject);
    };
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error('无法读取图片，请选择有效的图片文件'));
    };
    image.src = objectUrl;
  });
};

const compressCanvas = (
  canvas: HTMLCanvasElement,
  source: File,
  quality: number,
  resolve: (file: File) => void,
  reject: (reason?: Error) => void,
) => {
  canvas.toBlob(
    (blob) => {
      if (!blob) {
        reject(new Error('图片压缩失败，请重试'));
        return;
      }
      if (blob.size <= props.maxSize * 0.9 || quality <= 0.35) {
        if (blob.size > props.maxSize * 0.9 && quality <= 0.35 && Math.min(canvas.width, canvas.height) > 640) {
          const resizedCanvas = document.createElement('canvas');
          resizedCanvas.width = Math.max(640, Math.round(canvas.width * 0.8));
          resizedCanvas.height = Math.max(640, Math.round(canvas.height * 0.8));
          resizedCanvas.getContext('2d')?.drawImage(canvas, 0, 0, resizedCanvas.width, resizedCanvas.height);
          compressCanvas(resizedCanvas, source, 0.85, resolve, reject);
          return;
        }
        if (blob.size > props.maxSize) {
          reject(new Error('图片压缩后仍超过大小限制，请选择更小的图片'));
          return;
        }
        resolve(new File([blob], source.name, { type: source.type, lastModified: Date.now() }));
        return;
      }
      compressCanvas(canvas, source, quality - 0.1, resolve, reject);
    },
    source.type || 'image/jpeg',
    quality,
  );
};
</script>
