<template>
  <main class="forbidden-page">
    <el-result icon="warning" title="无权访问" sub-title="当前账号没有访问此页面的权限">
      <template #extra>
        <el-button type="primary" @click="router.replace('/dashboard')">返回工作台</el-button>
      </template>
    </el-result>
  </main>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<style scoped>
.forbidden-page {
  min-height: 60vh;
  display: grid;
  place-items: center;
}
</style>
