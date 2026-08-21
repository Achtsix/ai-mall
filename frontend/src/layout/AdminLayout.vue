<template>
  <el-container class="admin-layout">
    <el-aside width="250px" class="aside">
      <div class="admin-logo"><span class="mark">AI</span><span><b>AI 智能商城</b><small>导购系统管理后台</small></span></div>
      <el-menu :default-active="$route.path" router background-color="#071b2b" text-color="#b9cbd4" active-text-color="#fff">
        <el-menu-item index="/admin">⌂　商城首页</el-menu-item>
        <el-sub-menu index="mall"><template #title>▣　商城管理</template>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/products">商品管理</el-menu-item>
          <el-menu-item index="/admin/center">订单、评价与售后</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="ai-config"><template #title>⚙　AI 基础配置</template>
          <el-menu-item index="/admin/center">模型、Prompt 与工具</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="rag"><template #title>▤　AI 知识库（RAG）</template>
          <el-menu-item index="/admin/knowledge">知识库与向量索引</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="agent"><template #title>✣　AI 导购与运营</template>
          <el-menu-item index="/admin/agents">Agent 运行记录</el-menu-item>
          <el-menu-item index="/admin/operation">评价分析与运营报告</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/profile">♙　个人设置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="crumb"><b>AI 智能商城导购系统</b><span>首页</span><i>/</i><span>{{ $route.meta?.title || '管理后台' }}</span></div>
        <div class="admin-user"><span class="admin-avatar">⌁</span>商城管理员⌄</div>
        <el-button text @click="logout">退出</el-button>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { height: 100%; }
.admin-layout { user-select: none; }
.admin-layout :deep(input), .admin-layout :deep(textarea), .admin-layout :deep(.el-input__inner), .admin-layout :deep(.el-textarea__inner) { user-select: text; }
.admin-layout :deep(.el-menu), .admin-layout :deep(.el-menu-item), .admin-layout :deep(.el-sub-menu__title) { user-select: none; }
.admin-layout :deep(.el-menu-item), .admin-layout :deep(.el-sub-menu__title) { transition: none; }
.admin-layout :deep(.el-menu-item::selection), .admin-layout :deep(.el-sub-menu__title::selection) { background: transparent; }
.aside { background: #071b2b; }.admin-logo { color: #fff; font-weight: 800; padding: 22px 18px; font-size: 16px; display:flex; align-items:center; gap:10px; user-select:none; }.admin-logo b{display:block}.admin-logo small{display:block;color:#86a8b7;font-size:11px;font-weight:400;margin-top:4px}.mark{width:36px;height:36px;display:grid;place-items:center;border-radius:50%;background:#58c8e9;color:#fff;font-size:12px;border:3px solid #bceaf5}
.admin-header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #e6edf1; padding:0 28px; }.crumb{display:flex;gap:10px;align-items:center;color:#8295a0;font-size:13px}.crumb b{color:#15283a;font-size:17px}.crumb i{color:#bcc8ce}.admin-user{color:#526a78}.admin-avatar{display:inline-grid;place-items:center;width:34px;height:34px;border-radius:50%;background:#eac9ae;color:#6b4c32;margin-right:8px;font-weight:700}
.admin-main { background: #f5f6f8; }
</style>
