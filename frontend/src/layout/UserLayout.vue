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
.layout{height:100%}.header{display:flex;align-items:center;gap:42px;background:#fff;border-bottom:1px solid #e8eef3;padding:0 34px}.logo{display:flex;align-items:center;gap:10px;font-size:18px;white-space:nowrap;cursor:pointer;color:#17283b}.logo b{display:block;font-size:20px;line-height:20px}.logo small{display:block;color:#6c899d;font-size:9px;letter-spacing:1.5px;margin-top:3px}.logo-mark{width:34px;height:34px;display:grid;place-items:center;border-radius:50%;background:#59c9ed;color:#fff;font-weight:800;font-size:12px;border:3px solid #d9f3fb}.nav{flex:1;border-bottom:none}.user-actions{cursor:pointer;white-space:nowrap}.avatar{display:inline-grid;place-items:center;width:34px;height:34px;margin-right:8px;border-radius:50%;background:#d7edc8;color:#4d7f47;font-weight:700}.user-name{font-weight:600}.main{background:#f3f7fb;padding:28px max(24px,calc((100vw - 1420px)/2))}
</style>
