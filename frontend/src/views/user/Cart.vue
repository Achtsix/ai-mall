<template>
  <el-card>
    <template #header>购物车</template>
    <el-table :data="items">
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:10px">
            <el-image :src="row.productImage" style="width:60px;height:60px" />
            <span>{{ row.productName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="单价" width="100" />
      <el-table-column label="数量" width="160">
        <template #default="{ row }">
          <el-input-number :model-value="row.quantity" :min="1" @change="v => updateQuantity(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="{ row }">¥{{ row.price * row.quantity }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button text type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top:16px;display:flex;justify-content:flex-end;gap:10px">
      <el-button type="primary" @click="$router.push('/checkout')">去结算</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const items = ref([])

async function load() {
  items.value = await request.get('/cart')
}

async function updateQuantity(row, v) {
  await request.put(`/cart/${row.id}/quantity`, { quantity: v })
  row.quantity = v
}

async function remove(row) {
  await request.delete(`/cart/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
