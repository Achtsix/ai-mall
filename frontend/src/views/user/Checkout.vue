<template>
  <el-card>
    <template #header>确认订单</template>
    <el-form label-width="80px">
      <el-form-item label="收货地址">
        <el-select v-model="addressId" placeholder="选择地址" style="width:100%">
          <el-option v-for="a in addresses" :key="a.id" :label="a.receiverName + ' ' + a.receiverPhone + ' ' + a.detail" :value="a.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">提交订单</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const addresses = ref([])
const addressId = ref(null)

onMounted(async () => {
  addresses.value = await request.get('/address')
})

async function submit() {
  if (!addressId.value) return ElMessage.warning('请选择地址')
  const order = await request.post('/order/create', { addressId: addressId.value })
  ElMessage.success('订单已创建')
  router.push('/orders')
}
</script>
