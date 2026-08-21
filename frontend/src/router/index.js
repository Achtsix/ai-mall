import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../layout/UserLayout.vue'),
    children: [
      { path: '', component: () => import('../views/user/Home.vue') },
      { path: 'products', component: () => import('../views/user/ProductList.vue') },
      { path: 'product/:id', component: () => import('../views/user/ProductDetail.vue') },
      { path: 'cart', component: () => import('../views/user/Cart.vue') },
      { path: 'checkout', component: () => import('../views/user/Checkout.vue') },
      { path: 'orders', component: () => import('../views/user/Orders.vue') },
      { path: 'wallet', component: () => import('../views/user/Wallet.vue') },
      { path: 'address', component: () => import('../views/user/Address.vue') },
      { path: 'ai-guide', component: () => import('../views/user/AiGuide.vue') },
      { path: 'profile', component: () => import('../views/user/Profile.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    children: [
      { path: '', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', component: () => import('../views/admin/UserManage.vue') },
      { path: 'products', component: () => import('../views/admin/ProductManage.vue') },
      { path: 'center', component: () => import('../views/admin/AdminCenter.vue') },
      { path: 'profile', component: () => import('../views/admin/AdminProfile.vue') },
      { path: 'knowledge', component: () => import('../views/admin/KnowledgeManage.vue') },
      { path: 'agents', component: () => import('../views/admin/AgentRecords.vue') },
      { path: 'operation', component: () => import('../views/admin/OperationAnalysis.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (token && (to.path === '/login' || to.path === '/register')) {
    return role === 'ADMIN' ? '/admin' : '/'
  }
  if (to.path.startsWith('/admin') && role !== 'ADMIN') {
    return '/login'
  }
  if (!token && to.path !== '/login' && to.path !== '/register') {
    return '/login'
  }
  return true
})

export default router
