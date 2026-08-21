<template>
  <div class="auth-page"><div class="auth-shell">
    <section class="auth-brand"><div class="brand"><span>AI</span><div><b>智选商城</b><small>AI SHOPPING ASSISTANT</small></div></div><div class="brand-copy"><p>CREATE YOUR ACCOUNT</p><h1>加入智能购物体验</h1><span>收藏心仪商品、管理订单，并让 AI 根据你的真实需求提供购买建议。</span></div><div class="features"><i>01</i> 真实商品与库存　<i>02</i> AI 智能导购　<i>03</i> 售前售后问答</div></section>
    <section class="auth-card"><p class="eyebrow">NEW MEMBER</p>
      <h2>注册账号</h2><p class="sub">填写基本信息，创建你的商城账号</p>
      <el-form :model="form" @submit.prevent="register">
        <label>用户名</label><el-form-item><el-input v-model="form.username" placeholder="请输入用户名" /></el-form-item>
        <label>昵称</label><el-form-item><el-input v-model="form.nickname" placeholder="请输入昵称" /></el-form-item>
        <label>密码</label><el-form-item><el-input v-model="form.password" type="password" placeholder="请输入密码" show-password /></el-form-item>
        <el-button class="submit" type="primary" :loading="loading" @click="register">创建账号</el-button>
        <div class="auth-links">已有账号？<router-link to="/login">返回登录</router-link></div>
      </el-form>
    </section>
  </div></div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = ref({ username: '', nickname: '', password: '' })
const loading = ref(false)

async function register() {
  if (!form.value.username.trim() || !form.value.nickname.trim() || !form.value.password) {
    return ElMessage.warning('请完整填写注册信息')
  }
  loading.value = true
  try {
    await request.post('/auth/register', form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page{min-height:100%;display:grid;place-items:center;padding:36px;background:#eef0f3}.auth-shell{display:grid;grid-template-columns:1fr 1fr;width:min(980px,100%);min-height:600px;overflow:hidden;border-radius:8px;background:#fff;box-shadow:0 24px 70px rgba(25,28,33,.16)}.auth-brand{display:flex;flex-direction:column;padding:42px;background:var(--tech);color:#fff}.brand{display:flex;align-items:center;gap:12px}.brand>span{display:grid;place-items:center;width:38px;height:38px;border-radius:7px;background:var(--brand);font-size:12px;font-weight:800;box-shadow:0 0 0 4px rgba(242,85,61,.16)}.brand b,.brand small{display:block}.brand small{margin-top:3px;color:#969da6;font-size:10px;letter-spacing:1.5px}.brand-copy{margin:auto 0}.brand-copy p{color:#ff806d;font-size:11px;font-weight:800;letter-spacing:2px}.brand-copy h1{margin:18px 0 14px;font-size:36px}.brand-copy span{color:#b9bec5;line-height:1.8}.features{color:#9299a2;font-size:12px;line-height:2}.features i{color:var(--ai-cyan);font-style:normal}.auth-card{padding:72px 58px}.eyebrow{color:var(--brand);font-size:11px;font-weight:800;letter-spacing:2px}.auth-card h2{margin:16px 0 8px;font-size:31px}.sub{margin-bottom:30px;color:#9299a2}.auth-card label{display:block;margin:14px 0 8px;font-size:13px;font-weight:700}.auth-card :deep(.el-input__wrapper){min-height:50px;border-radius:7px;background:#f7f8f9;box-shadow:0 0 0 1px var(--line) inset}.submit{width:100%;height:52px;margin-top:14px;font-weight:700}.auth-links{margin-top:24px;text-align:center;color:#8b929b}.auth-links a{margin-left:5px;color:var(--brand);font-weight:700;text-decoration:none}@media(max-width:760px){.auth-page{padding:0}.auth-shell{grid-template-columns:1fr;min-height:100%;border-radius:0}.auth-brand{min-height:240px;padding:28px}.brand-copy h1{font-size:28px}.features{display:none}.auth-card{padding:40px 28px}}
</style>
