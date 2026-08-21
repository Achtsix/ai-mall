<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between">
        <span>商品知识库管理</span>
        <div>
          <el-button @click="reindex">重建向量索引</el-button>
          <el-button type="primary" @click="dialog=true; form={}">新增资料</el-button>
        </div>
      </div>
    </template>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="知识资料" width="600px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="商品ID"><el-input-number v-model="form.productId" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type"><el-option label="商品" value="PRODUCT" /><el-option label="FAQ" value="FAQ" /><el-option label="售后" value="AFTER_SALE" /></el-select></el-form-item>
        <el-form-item label="内容"><el-input type="textarea" v-model="form.content" rows="6" /></el-form-item>
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
  list.value = await request.get('/admin/knowledge')
}
async function save() {
  await request.post('/admin/knowledge', form.value)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove(row) {
  await request.delete(`/admin/knowledge/${row.id}`)
  load()
}
async function reindex() {
  await request.post('/ai/knowledge/reindex')
  ElMessage.success('索引重建完成')
}
onMounted(load)
</script>
