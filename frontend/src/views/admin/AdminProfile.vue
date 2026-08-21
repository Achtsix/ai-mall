<template>
  <el-row :gutter="16" class="profile-page">
    <el-col :xs="24" :lg="12"><el-card shadow="never"><template #header>管理员信息</template><el-form :model="form" label-width="90px"><el-form-item label="用户名"><el-input :model-value="userStore.userInfo?.username" disabled /></el-form-item><el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item><el-form-item label="头像地址"><el-input v-model="form.avatar" /></el-form-item><el-button type="primary" @click="saveProfile">保存资料</el-button></el-form></el-card></el-col>
    <el-col :xs="24" :lg="12"><el-card shadow="never"><template #header>修改密码</template><el-form :model="password" label-width="90px"><el-form-item label="原密码"><el-input v-model="password.oldPassword" type="password" show-password /></el-form-item><el-form-item label="新密码"><el-input v-model="password.newPassword" type="password" show-password /></el-form-item><el-button type="danger" @click="changePassword">更新密码</el-button></el-form></el-card></el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'
import { useUserStore } from '../../stores/user'
const userStore = useUserStore(); const form = ref({}); const password = ref({})
onMounted(async () => { const data = await request.get('/auth/profile'); form.value = { nickname: data.nickname, phone: data.phone, email: data.email, avatar: data.avatar } })
async function saveProfile() { await request.put('/auth/profile', form.value); ElMessage.success('资料已保存') }
async function changePassword() { await request.put('/auth/password', password.value); password.value = {}; ElMessage.success('密码已修改') }
</script>

<style scoped>.profile-page { max-width: 1100px; margin: 0 auto; }</style>
