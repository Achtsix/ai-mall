<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>收货地址</span>
        <el-button type="primary" @click="dialog = true">新增地址</el-button>
      </div>
    </template>
    <el-table :data="list">
      <el-table-column prop="receiverName" label="姓名" />
      <el-table-column prop="receiverPhone" label="电话" />
      <el-table-column prop="detail" label="地址" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="新增地址">
      <el-form :model="form" label-width="70px">
        <el-form-item label="姓名"><el-input v-model="form.receiverName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.receiverPhone" /></el-form-item>
        <el-form-item label="省市区"><el-input v-model="form.province" placeholder="省 市 区" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detail" /></el-form-item>
        <el-form-item label="默认"><el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const dialog = ref(false)
const form = ref({})

async function load() {
  list.value = await request.get('/address')
}

async function save() {
  await request.post('/address', form.value)
  ElMessage.success('保存成功')
  dialog.value = false
  form.value = {}
  load()
}

async function remove(row) {
  await request.delete(`/address/${row.id}`)
  load()
}

onMounted(load)
</script>
