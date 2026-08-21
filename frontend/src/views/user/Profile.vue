<template>
  <el-card style="max-width:600px;margin:auto">
    <template #header>个人信息</template>
    <el-form :model="form" label-width="80px">
      <el-form-item label="用户名"><el-input :model-value="userStore.userInfo?.username" disabled /></el-form-item>
      <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      <el-form-item label="头像"><el-input v-model="form.avatar" /></el-form-item>
      <el-button type="primary" @click="save">保存</el-button>
    </el-form>
    <el-divider />
    <h4>修改密码</h4>
    <el-form label-width="80px" style="margin-top:10px">
      <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" /></el-form-item>
      <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" /></el-form-item>
      <el-button type="danger" @click="changePwd">修改密码</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = ref({})
const pwd = ref({})

onMounted(async () => {
  const data = await request.get('/auth/profile')
  form.value = { nickname: data.nickname, phone: data.phone, email: data.email, avatar: data.avatar }
})

async function save() {
  await request.put('/auth/profile', form.value)
  ElMessage.success('保存成功')
}

async function changePwd() {
  await request.put('/auth/password', pwd.value)
  ElMessage.success('密码已修改')
}
</script>
