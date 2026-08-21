<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>注册账号</h2>
      <el-form :model="form" @submit.prevent="register">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.nickname" placeholder="昵称" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" show-password /></el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="register">注册</el-button>
        <div class="auth-links"><router-link to="/login">已有账号，去登录</router-link></div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = ref({ username: '', nickname: '', password: '' })
const loading = ref(false)

async function register() {
  loading.value = true
  try {
    await request.post('/auth/register', form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { height: 100%; display: flex; align-items: center; justify-content: center; background: #f0f2f5; }
.auth-card { width: 400px; padding: 20px; }
.auth-card h2 { text-align: center; margin-bottom: 20px; }
.auth-links { margin-top: 12px; text-align: center; }
</style>
