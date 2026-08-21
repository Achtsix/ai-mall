<template>
  <el-card>
    <template #header>Agent 运行记录</template>
    <el-table :data="list">
      <el-table-column prop="id" label="Run ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="question" label="问题" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="startedAt" label="开始时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" @click="detail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawer" title="Agent 执行步骤" size="600px">
      <el-timeline v-if="steps.length">
        <el-timeline-item v-for="s in steps" :key="s.id" :timestamp="'Step ' + s.seq">
          <b>{{ s.toolName }}</b>
          <p style="font-size:12px;color:#888">入参：{{ s.inputJson }}</p>
          <p style="font-size:12px;color:#888">出参：{{ s.outputJson }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无步骤" />
    </el-drawer>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const list = ref([])
const drawer = ref(false)
const steps = ref([])

async function load() {
  list.value = await request.get('/admin/agent-runs')
}
async function detail(row) {
  const data = await request.get(`/admin/agent-runs/${row.id}`)
  steps.value = data.steps || []
  drawer.value = true
}
onMounted(load)
</script>
