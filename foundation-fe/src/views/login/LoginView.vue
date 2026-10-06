<script setup lang="ts">
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, type FormInstance, type FormRules} from 'element-plus'
import {useAuthStore} from '@/stores/auth'
import {Lock, User} from '@element-plus/icons-vue'

const router = useRouter();
const auth = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false)
const form = reactive({clientId: 'foundation-web', grantType: 'PASSWORD', phone: '', password: ''})
const rules: FormRules = {
  phone: [{required: true, message: '请输入手机号', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
}

async function submit() {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      await auth.signIn(form);
      ElMessage.success('登录成功');
      router.replace('/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>
<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-brand"><span class="brand-mark">F</span>
        <div><strong>Foundation</strong><small>基础管理平台</small></div>
      </div>
      <h1>欢迎回来</h1>
      <p class="login-subtitle">登录管理台，开始今天的工作</p>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="submit">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" size="large" placeholder="手机号" :prefix-icon="User"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" size="large" placeholder="密码" show-password
                    :prefix-icon="Lock"/>
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="login-button" @click="submit">登录</el-button>
      </el-form>
    </section>
  </main>
</template>
