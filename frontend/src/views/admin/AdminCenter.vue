<template>
  <div class="admin-center">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div>
            <h2>业务与 AI 管理中心</h2>
            <span class="muted">集中维护商城基础数据、订单售后和 Agent 配置</span>
          </div>
          <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="分类与品牌" name="catalog">
          <el-row :gutter="16">
            <el-col :xs="24" :lg="12">
              <section class="section-panel">
                <div class="section-title"><span>商品分类</span><el-button size="small" type="primary" @click="openCategory()">新增</el-button></div>
                <el-table :data="categories" size="small" stripe>
                  <el-table-column prop="id" label="ID" width="70" />
                  <el-table-column prop="name" label="名称" />
                  <el-table-column prop="sort" label="排序" width="80" />
                  <el-table-column label="操作" width="140">
                    <template #default="{ row }"><el-button link type="primary" @click="openCategory(row)">编辑</el-button><el-button link type="danger" @click="remove('/admin/category/' + row.id, loadCatalog)">删除</el-button></template>
                  </el-table-column>
                </el-table>
              </section>
            </el-col>
            <el-col :xs="24" :lg="12">
              <section class="section-panel">
                <div class="section-title"><span>品牌</span><el-button size="small" type="primary" @click="openBrand()">新增</el-button></div>
                <el-table :data="brands" size="small" stripe>
                  <el-table-column prop="id" label="ID" width="70" /><el-table-column prop="name" label="名称" /><el-table-column prop="description" label="简介" show-overflow-tooltip />
                  <el-table-column label="操作" width="140"><template #default="{ row }"><el-button link type="primary" @click="openBrand(row)">编辑</el-button><el-button link type="danger" @click="remove('/admin/brand/' + row.id, loadCatalog)">删除</el-button></template></el-table-column>
                </el-table>
              </section>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="订单与评价" name="trade">
          <el-table :data="orders" stripe size="small"><el-table-column prop="orderNo" label="订单号" min-width="180" /><el-table-column prop="userId" label="用户" width="80" /><el-table-column prop="payAmount" label="实付" width="100" /><el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="orderStatusType(row.status)">{{ orderStatus(row.status) }}</el-tag></template></el-table-column><el-table-column prop="createTime" label="创建时间" min-width="170" /></el-table>
          <div class="sub-heading">评价管理</div>
          <el-table :data="reviews" stripe size="small"><el-table-column prop="productId" label="商品" width="80" /><el-table-column prop="nickname" label="用户" width="110" /><el-table-column prop="rating" label="评分" width="80" /><el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip /><el-table-column prop="reply" label="回复" min-width="180" show-overflow-tooltip /><el-table-column label="操作" width="150"><template #default="{ row }"><el-button link type="primary" @click="replyReview(row)">回复</el-button><el-button link type="danger" @click="remove('/admin/review/' + row.id, loadTrade)">删除</el-button></template></el-table-column></el-table>
        </el-tab-pane>

        <el-tab-pane label="售后规则" name="afterSale">
          <div class="toolbar"><el-button type="primary" @click="openRule()">新增规则</el-button></div>
          <el-table :data="rules" stripe><el-table-column prop="title" label="标题" min-width="180" /><el-table-column prop="category" label="分类" width="120" /><el-table-column prop="keywords" label="关键词" min-width="180" /><el-table-column prop="priority" label="优先级" width="90" /><el-table-column prop="content" label="规则内容" min-width="260" show-overflow-tooltip /><el-table-column label="操作" width="140"><template #default="{ row }"><el-button link type="primary" @click="openRule(row)">编辑</el-button><el-button link type="danger" @click="remove('/admin/after-sale-rule/' + row.id, loadAfterSale)">删除</el-button></template></el-table-column></el-table>
        </el-tab-pane>

        <el-tab-pane label="模型与 Prompt" name="model">
          <el-alert class="secret-notice" title="API Key 仅从后端环境变量读取，页面输入不会被保存到数据库。" type="info" show-icon :closable="false" />
          <el-row :gutter="16"><el-col :xs="24" :lg="12"><section class="section-panel"><div class="section-title"><span>模型配置</span><el-button size="small" type="primary" @click="openModel()">新增</el-button></div><el-table :data="models" size="small"><el-table-column prop="name" label="名称" /><el-table-column prop="provider" label="提供商" /><el-table-column prop="model" label="模型" /><el-table-column prop="enabled" label="启用" width="80"><template #default="{ row }"><el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="saveModel(row)" /></template></el-table-column><el-table-column label="操作" width="70"><template #default="{ row }"><el-button link type="primary" @click="openModel(row)">编辑</el-button></template></el-table-column></el-table></section></el-col><el-col :xs="24" :lg="12"><section class="section-panel"><div class="section-title"><span>Prompt 模板</span><el-button size="small" type="primary" @click="openPrompt()">新增</el-button></div><el-table :data="prompts" size="small"><el-table-column prop="name" label="名称" /><el-table-column prop="type" label="类型" width="110" /><el-table-column prop="enabled" label="启用" width="80"><template #default="{ row }"><el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="savePrompt(row)" /></template></el-table-column><el-table-column label="操作" width="70"><template #default="{ row }"><el-button link type="primary" @click="openPrompt(row)">编辑</el-button></template></el-table-column></el-table></section></el-col></el-row>
        </el-tab-pane>

        <el-tab-pane label="Function Tool" name="tools">
          <div class="toolbar"><el-button type="primary" @click="openTool()">注册工具</el-button></div>
          <el-table :data="tools" stripe><el-table-column prop="name" label="名称" /><el-table-column prop="method" label="方法" width="90" /><el-table-column prop="url" label="URL" min-width="220" /><el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip /><el-table-column prop="enabled" label="启用" width="80"><template #default="{ row }"><el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="saveTool(row)" /></template></el-table-column><el-table-column label="操作" width="70"><template #default="{ row }"><el-button link type="primary" @click="openTool(row)">编辑</el-button></template></el-table-column></el-table>
          <div class="sub-heading">调用日志</div><el-table :data="toolLogs" size="small"><el-table-column prop="toolName" label="工具" /><el-table-column prop="status" label="状态" width="90" /><el-table-column prop="costMs" label="耗时(ms)" width="100" /><el-table-column prop="createTime" label="时间" min-width="170" /></el-table>
        </el-tab-pane>

        <el-tab-pane label="导购任务与推荐" name="guide">
          <el-table :data="guideTasks" stripe size="small"><el-table-column prop="id" label="任务" width="80" /><el-table-column prop="userId" label="用户" width="80" /><el-table-column prop="question" label="用户需求" min-width="300" show-overflow-tooltip /><el-table-column prop="status" label="状态" width="110" /><el-table-column prop="createTime" label="时间" min-width="170" /></el-table>
          <div class="sub-heading">推荐结果</div><el-table :data="recommendations" size="small"><el-table-column prop="guideTaskId" label="任务" width="80" /><el-table-column prop="productId" label="商品" width="80" /><el-table-column prop="productName" label="商品名称" min-width="180" /><el-table-column prop="priceSnapshot" label="价格快照" width="110" /><el-table-column prop="stockSnapshot" label="库存快照" width="100" /><el-table-column prop="reason" label="推荐理由" min-width="260" show-overflow-tooltip /></el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="560px"><el-form :model="dialog.form" label-width="90px"><template v-if="dialog.type === 'category'"><el-form-item label="名称"><el-input v-model="dialog.form.name" /></el-form-item><el-form-item label="父分类"><el-input-number v-model="dialog.form.parentId" :min="0" /></el-form-item><el-form-item label="排序"><el-input-number v-model="dialog.form.sort" :min="0" /></el-form-item></template><template v-else-if="dialog.type === 'brand'"><el-form-item label="名称"><el-input v-model="dialog.form.name" /></el-form-item><el-form-item label="Logo"><el-input v-model="dialog.form.logo" /></el-form-item><el-form-item label="简介"><el-input v-model="dialog.form.description" type="textarea" /></el-form-item></template><template v-else-if="dialog.type === 'rule'"><el-form-item label="标题"><el-input v-model="dialog.form.title" /></el-form-item><el-form-item label="分类"><el-input v-model="dialog.form.category" /></el-form-item><el-form-item label="关键词"><el-input v-model="dialog.form.keywords" /></el-form-item><el-form-item label="优先级"><el-input-number v-model="dialog.form.priority" :min="0" /></el-form-item><el-form-item label="内容"><el-input v-model="dialog.form.content" type="textarea" rows="6" /></el-form-item></template><template v-else-if="dialog.type === 'model'"><el-form-item label="名称"><el-input v-model="dialog.form.name" /></el-form-item><el-form-item label="提供商"><el-input v-model="dialog.form.provider" /></el-form-item><el-form-item label="Base URL"><el-input v-model="dialog.form.baseUrl" /></el-form-item><el-form-item label="模型"><el-input v-model="dialog.form.model" /></el-form-item><el-form-item label="API Key"><el-input v-model="dialog.form.apiKey" show-password /></el-form-item><el-form-item label="Temperature"><el-input-number v-model="dialog.form.temperature" :precision="2" :step="0.1" /></el-form-item><el-form-item label="Max Tokens"><el-input-number v-model="dialog.form.maxTokens" :min="1" /></el-form-item></template><template v-else-if="dialog.type === 'prompt'"><el-form-item label="名称"><el-input v-model="dialog.form.name" /></el-form-item><el-form-item label="类型"><el-input v-model="dialog.form.type" /></el-form-item><el-form-item label="内容"><el-input v-model="dialog.form.content" type="textarea" rows="8" /></el-form-item></template><template v-else-if="dialog.type === 'tool'"><el-form-item label="名称"><el-input v-model="dialog.form.name" /></el-form-item><el-form-item label="描述"><el-input v-model="dialog.form.description" /></el-form-item><el-form-item label="URL"><el-input v-model="dialog.form.url" /></el-form-item><el-form-item label="方法"><el-select v-model="dialog.form.method"><el-option label="GET" value="GET" /><el-option label="POST" value="POST" /><el-option label="PUT" value="PUT" /></el-select></el-form-item><el-form-item label="请求 Schema"><el-input v-model="dialog.form.requestSchema" type="textarea" /></el-form-item><el-form-item label="响应 Schema"><el-input v-model="dialog.form.responseSchema" type="textarea" /></el-form-item></template></el-form><template #footer><el-button @click="dialog.visible = false">取消</el-button><el-button type="primary" @click="submitDialog">保存</el-button></template></el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '../../api/request'

const route = useRoute()
const availableTabs = ['catalog', 'trade', 'afterSale', 'model', 'tools', 'guide']
const routeTab = () => availableTabs.includes(route.query.tab) ? route.query.tab : 'catalog'
const activeTab = ref(routeTab())
const categories = ref([]); const brands = ref([]); const orders = ref([]); const reviews = ref([]); const rules = ref([])
const models = ref([]); const prompts = ref([]); const tools = ref([]); const toolLogs = ref([]); const guideTasks = ref([]); const recommendations = ref([])
const dialog = reactive({ visible: false, title: '', type: '', form: {} })
const endpoints = { category: '/admin/category', brand: '/admin/brand', rule: '/admin/after-sale-rule', model: '/admin/model-config', prompt: '/admin/prompt-template', tool: '/admin/function-tool' }
const clone = row => row ? { ...row } : {}
function open(type, title, row) { dialog.type = type; dialog.title = title; dialog.form = clone(row); dialog.visible = true }
const openCategory = row => open('category', row ? '编辑分类' : '新增分类', row)
const openBrand = row => open('brand', row ? '编辑品牌' : '新增品牌', row)
const openRule = row => open('rule', row ? '编辑售后规则' : '新增售后规则', row)
const openModel = row => open('model', row ? '编辑模型配置' : '新增模型配置', row)
const openPrompt = row => open('prompt', row ? '编辑 Prompt' : '新增 Prompt', row)
const openTool = row => open('tool', row ? '编辑工具' : '注册工具', row)
async function submitDialog() { await request.post(endpoints[dialog.type], dialog.form); dialog.visible = false; ElMessage.success('保存成功'); await loadAll() }
async function remove(url, reload) { await ElMessageBox.confirm('确定删除这条记录吗？', '提示', { type: 'warning' }); await request.delete(url); ElMessage.success('删除成功'); await reload() }
async function replyReview(row) { const { value } = await ElMessageBox.prompt('请输入商家回复', '回复评价', { inputValue: row.reply || '' }); await request.post('/admin/review/' + row.id + '/reply', { reply: value }); await loadTrade() }
async function saveModel(row) { await request.post('/admin/model-config', row); ElMessage.success('已更新') }
async function savePrompt(row) { await request.post('/admin/prompt-template', row); ElMessage.success('已更新') }
async function saveTool(row) { await request.post('/admin/function-tool', row); ElMessage.success('已更新') }
async function loadCatalog() { [categories.value, brands.value] = await Promise.all([request.get('/category'), request.get('/brand')]) }
async function loadTrade() { [orders.value, reviews.value] = await Promise.all([request.get('/admin/orders'), request.get('/admin/reviews')]) }
async function loadAfterSale() { rules.value = await request.get('/admin/after-sale-rules') }
async function loadAi() { [models.value, prompts.value, tools.value, toolLogs.value, guideTasks.value, recommendations.value] = await Promise.all([request.get('/admin/model-configs'), request.get('/admin/prompt-templates'), request.get('/admin/function-tools'), request.get('/admin/function-call-logs'), request.get('/admin/guide-tasks'), request.get('/admin/recommend-results')]) }
async function loadAll() { await Promise.all([loadCatalog(), loadTrade(), loadAfterSale(), loadAi()]) }
function orderStatus(status) { return ({ 0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消' })[status] || '未知' }
function orderStatusType(status) { return ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' })[status] || 'info' }
watch(() => route.query.tab, () => { activeTab.value = routeTab() })
onMounted(loadAll)
</script>

<style scoped>
.admin-center { max-width: 1600px; margin: 0 auto; }
.page-header, .section-title, .toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.page-header h2 { font-size: 20px; margin-bottom: 4px; }
.muted { color: #909399; font-size: 13px; }
.section-panel { border: 1px solid #ebeef5; padding: 14px; margin-bottom: 16px; }
.section-title { font-weight: 600; margin-bottom: 12px; }
.sub-heading { font-weight: 600; margin: 24px 0 12px; padding-left: 10px; border-left: 3px solid var(--brand); }
.toolbar { margin-bottom: 12px; }
.secret-notice { margin-bottom: 16px; }
</style>
