<template>
  <el-card>
    <template #header>我的订单</template>
    <el-table :data="orders">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="payAmount" label="金额" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-if="row.status===0" type="primary" size="small" @click="pay(row)">支付</el-button>
          <el-button v-if="row.status===0" type="danger" size="small" @click="cancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const orders = ref([])

function statusText(s) {
  return ['待支付', '已支付', '已发货', '已完成', '已取消'][s] || s
}

async function load() {
  orders.value = await request.get('/order')
}

async function pay(row) {
  await request.post(`/order/${row.id}/pay`)
  ElMessage.success('支付成功')
  load()
}

async function cancel(row) {
  await request.post(`/order/${row.id}/cancel`)
  ElMessage.success('已取消')
  load()
}

onMounted(load)
</script>
