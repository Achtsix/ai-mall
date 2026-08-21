<template>
  <el-card>
    <template #header>商品管理</template>
    <el-button type="primary" @click="dialog = true; form = {}">新增商品</el-button>
    <el-table :data="list" style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="商品编辑" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类ID"><el-input-number v-model="form.categoryId" /></el-form-item>
        <el-form-item label="品牌ID"><el-input-number v-model="form.brandId" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" /></el-form-item>
        <el-form-item label="主图"><el-input v-model="form.mainImage" /></el-form-item>
        <el-form-item label="详情"><el-input type="textarea" v-model="form.detailHtml" /></el-form-item>
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
  const data = await request.get('/product/page', { params: { pageNum: 1, pageSize: 100 } })
  list.value = data.list
}
function edit(row) {
  form.value = { ...row }
  dialog.value = true
}
async function save() {
  if (form.value.id) {
    await request.put('/admin/product', form.value)
  } else {
    await request.post('/admin/product', form.value)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove(row) {
  await request.delete(`/admin/product/${row.id}`)
  ElMessage.success('删除成功')
  load()
}
onMounted(load)
</script>
