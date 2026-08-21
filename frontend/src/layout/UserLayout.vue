<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo" @click="router.push('/')"><span class="logo-mark">AI</span><span><b>智选商城</b><small>AI SHOPPING ASSISTANT</small></span></div>
      <el-menu mode="horizontal" :router="true" :default-active="$route.path" class="nav">
        <el-menu-item index="/">商城首页</el-menu-item><el-menu-item index="/ai-guide">AI 智能导购</el-menu-item><el-menu-item index="/products">商品</el-menu-item><el-menu-item index="/cart">购物车</el-menu-item><el-menu-item index="/orders">我的订单</el-menu-item><el-menu-item index="/wallet">我的钱包</el-menu-item>
      </el-menu>
      <div class="user-actions"><el-dropdown><span class="user-name"><span class="avatar">{{ (userStore.userInfo?.nickname || '用').slice(0, 1) }}</span>{{ userStore.userInfo?.nickname || '商城用户' }}⌄</span><template #dropdown><el-dropdown-menu><el-dropdown-item @click="$router.push('/profile')">个人信息</el-dropdown-item><el-dropdown-item @click="$router.push('/address')">收货地址</el-dropdown-item><el-dropdown-item divided @click="logout">退出登录</el-dropdown-item></el-dropdown-menu></template></el-dropdown></div>
    </el-header><el-main class="main"><router-view /></el-main>
  </el-container>
</template>
<script setup>
import { useRouter } from 'vue-router'; import { useUserStore } from '../stores/user'
const router = useRouter(); const userStore = useUserStore(); function logout () { userStore.logout(); router.push('/login') }
</script>
<style scoped>
.layout{height:100%}.header{position:relative;z-index:10;display:flex;align-items:center;gap:30px;min-height:64px;background:#fff;border-bottom:1px solid var(--line);box-shadow:0 3px 14px rgba(28,31,36,.04);padding:0 34px}.header:after{content:'';position:absolute;left:0;bottom:-1px;width:180px;height:2px;background:var(--brand)}.logo{display:flex;align-items:center;gap:10px;font-size:18px;white-space:nowrap;cursor:pointer;color:var(--ink)}.logo b{display:block;font-size:20px;line-height:20px}.logo small{display:block;color:var(--muted);font-size:9px;letter-spacing:1.5px;margin-top:3px}.logo-mark{width:34px;height:34px;display:grid;place-items:center;border-radius:7px;background:var(--brand);color:#fff;font-weight:800;font-size:12px;box-shadow:0 0 0 4px var(--brand-soft)}.nav{flex:1;min-width:0;border-bottom:none}.nav :deep(.el-menu-item){border-bottom:2px solid transparent;color:#505761}.nav :deep(.el-menu-item:hover){color:var(--brand);background:var(--brand-soft)}.nav :deep(.el-menu-item.is-active){color:var(--brand);border-bottom-color:var(--brand)}.user-actions{cursor:pointer;white-space:nowrap}.avatar{display:inline-grid;place-items:center;width:34px;height:34px;margin-right:8px;border-radius:7px;background:var(--brand-soft);color:var(--brand);font-weight:700}.user-name{font-weight:600;color:#3d434b}.main{background:var(--page);padding:28px max(24px,calc((100vw - 1420px)/2))}
@media(max-width:1050px){.header{height:auto;min-height:112px;flex-wrap:wrap;align-content:center;gap:6px 20px;padding:10px 20px}.logo{order:1}.user-actions{order:2;margin-left:auto}.nav{order:3;flex-basis:100%;overflow-x:auto}.nav :deep(.el-menu-item){padding:0 14px}.main{padding:20px}}
@media(max-width:560px){.logo small{display:none}.header{padding:10px 14px}.nav :deep(.el-menu-item){font-size:13px;padding:0 11px}.main{padding:14px}.user-name{font-size:0}.avatar{margin-right:0;font-size:13px}}
</style>
