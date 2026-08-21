<template>
  <div class="dashboard">
    <section class="welcome"><div><p>ADMIN CONTROL CENTER</p><h1>管理后台工作台</h1><span>集中维护商城业务、AI 能力和运营数据。</span></div><div class="status"><i></i> 系统服务正常</div></section>
    <h2>常用管理模块</h2>
    <div class="module-grid">
      <button v-for="module in modules" :key="module.path" @click="$router.push(module.path)">
        <span class="module-icon"><el-icon><component :is="module.icon" /></el-icon></span>
        <div><b>{{ module.title }}</b><p>{{ module.description }}</p></div><el-icon class="arrow"><ArrowRight /></el-icon>
      </button>
    </div>
    <section class="notice"><el-icon><InfoFilled /></el-icon><div><b>数据展示原则</b><p>工作台只提供真实管理入口，不展示未经接口统计的虚构经营数字。</p></div></section>
  </div>
</template>

<script setup>
import { User, Goods, Cpu, DataAnalysis, ArrowRight, InfoFilled } from '@element-plus/icons-vue'
const modules = [
  { title: '用户管理', description: '查看并维护商城用户信息', path: '/admin/users', icon: User },
  { title: '商品管理', description: '维护商品、库存、图片与参数', path: '/admin/products', icon: Goods },
  { title: 'AI 基础配置', description: '管理模型、Prompt 与工具', path: '/admin/center?tab=model', icon: Cpu },
  { title: '运营分析', description: '查看评价分析与运营报告', path: '/admin/operation', icon: DataAnalysis }
]
</script>

<style scoped>
.dashboard{max-width:1320px;margin:auto}.welcome{display:flex;align-items:center;justify-content:space-between;padding:30px 34px;border:1px solid #303640;border-radius:8px;background:var(--tech);color:#fff}.welcome p{margin:0 0 9px;color:#ff806d;font-size:11px;font-weight:800;letter-spacing:2px}.welcome h1{margin:0 0 8px;font-size:28px}.welcome span{color:#aeb4bc}.status{padding:10px 14px;border:1px solid #3b424c;border-radius:6px;color:#cbd0d5;font-size:13px}.status i{display:inline-block;width:8px;height:8px;margin-right:7px;border-radius:50%;background:var(--ai-cyan);box-shadow:0 0 0 4px rgba(25,167,184,.13)}.dashboard>h2{margin:28px 0 16px;font-size:20px}.module-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:14px}.module-grid button{display:flex;align-items:center;gap:16px;width:100%;padding:20px;border:1px solid var(--line);border-radius:8px;background:#fff;text-align:left;cursor:pointer}.module-grid button:hover{border-color:var(--brand-border);box-shadow:0 8px 22px rgba(44,35,32,.07)}.module-icon{display:grid;place-items:center;flex:0 0 44px;height:44px;border-radius:7px;background:var(--brand-soft);color:var(--brand);font-size:21px}.module-grid button div{flex:1}.module-grid b{color:var(--ink);font-size:16px}.module-grid p{margin:6px 0 0;color:var(--muted);font-size:13px}.arrow{color:#a7acb3}.notice{display:flex;align-items:flex-start;gap:12px;margin-top:18px;padding:16px 18px;border-left:3px solid var(--ai-cyan);border-radius:5px;background:#fff;color:#52606a}.notice>.el-icon{margin-top:2px;color:var(--ai-cyan)}.notice p{margin:5px 0 0;color:#858c95;font-size:13px}@media(max-width:720px){.welcome{align-items:flex-start;flex-direction:column;gap:20px;padding:24px}.module-grid{grid-template-columns:1fr}}
</style>
