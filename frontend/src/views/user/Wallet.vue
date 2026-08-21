<template>
  <el-row :gutter="20">
    <el-col :span="10">
      <el-card>
        <template #header>我的钱包</template>
        <div class="balance">¥{{ wallet.balance }}</div>
        <el-input-number v-model="amount" :min="1" :precision="2" />
        <el-button type="primary" style="margin-left:10px" @click="recharge">充值</el-button>
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card>
        <template #header>充值记录</template>
        <el-table :data="records" size="small">
          <el-table-column prop="amount" label="金额" />
          <el-table-column prop="createTime" label="时间" />
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const wallet = ref({ balance: 0 })
const records = ref([])
const amount = ref(100)

async function load() {
  wallet.value = await request.get('/wallet')
  records.value = await request.get('/wallet/recharges')
}

async function recharge() {
  await request.post('/wallet/recharge', { amount: amount.value })
  ElMessage.success('充值成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.balance { font-size: 36px; font-weight: 800; color: #f56c6c; margin-bottom: 20px; }
</style>
