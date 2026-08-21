<template>
  <el-card>
    <template #header>用户管理</template>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const list = ref([])
async function load() {
  const data = await request.get('/admin/users', { params: { pageNum: 1, pageSize: 100 } })
  list.value = data.list
}
async function toggle(row) {
  await request.put(`/admin/users/${row.id}/status`, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success('操作成功')
  load()
}
onMounted(load)
</script>
