<template>
  <div class="products-page">
    <section class="page-hero">
      <div>
        <p class="eyebrow">DISCOVER YOUR NEXT FAVORITE</p>
        <h1>找到更适合你的商品</h1>
        <p>按分类、品牌或关键词筛选，实时查看在售商品和库存。</p>
      </div>
      <div class="hero-stat"><strong>{{ total }}</strong><span>件在售商品</span></div>
    </section>

    <section class="filter-panel">
      <div class="filter-row search-row">
        <el-input v-model="filters.keyword" class="keyword-input" clearable size="large" placeholder="搜索商品名称、卖点、品牌或规格" @keyup.enter="submit" @clear="submit">
          <template #prefix><Search /></template>
        </el-input>
        <el-select v-model="filters.categoryId" class="filter-select" clearable size="large" placeholder="全部分类" @change="submit">
          <el-option v-for="category in categories" :key="category.id" :label="categoryLabel(category)" :value="category.id" />
        </el-select>
        <el-select v-model="filters.brandId" class="filter-select" clearable size="large" placeholder="全部品牌" @change="submit">
          <el-option v-for="brand in brands" :key="brand.id" :label="brand.name" :value="brand.id" />
        </el-select>
        <el-button type="primary" size="large" :icon="Search" :loading="loading" @click="submit">搜索</el-button>
        <el-button size="large" :icon="Refresh" @click="reset">重置</el-button>
      </div>

      <div class="category-row">
        <span class="filter-label">分类</span>
        <button class="category-chip" :class="{ active: !filters.categoryId }" @click="selectCategory(null)">全部</button>
        <button v-for="category in topCategories" :key="category.id" class="category-chip" :class="{ active: filters.categoryId === category.id }" @click="selectCategory(category.id)">{{ category.name }}</button>
      </div>

      <div v-if="activeFilters.length" class="active-row">
        <span class="filter-label">当前筛选</span>
        <el-tag v-for="item in activeFilters" :key="item.key" closable effect="plain" @close="removeFilter(item.key)">{{ item.label }}</el-tag>
        <span class="clear-all" @click="reset">清空条件</span>
      </div>
    </section>

    <section class="results-toolbar">
      <div><h2>商品列表</h2><span class="result-count">共 {{ total }} 件商品</span></div>
      <span v-if="loading" class="loading-text">正在更新商品列表...</span>
    </section>

    <el-row v-loading="loading" :gutter="18" class="product-grid">
      <el-col v-for="product in list" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
        <article class="product-card" @click="openProduct(product)">
          <div class="image-wrap"><el-image :src="product.mainImage" fit="cover" class="product-image"><template #error><div class="image-error">暂无图片</div></template></el-image><el-tag v-if="product.stock > 0" class="stock-tag" type="success" size="small">现货</el-tag><el-tag v-else class="stock-tag" type="info" size="small">暂时缺货</el-tag></div>
          <div class="product-body"><div class="product-tags"><el-tag v-if="product.categoryName" size="small" effect="plain">{{ product.categoryName }}</el-tag><el-tag v-if="product.brandName" size="small" type="success" effect="plain">{{ product.brandName }}</el-tag></div><h3 :title="product.name">{{ product.name }}</h3><p>{{ product.subtitle || '精选品质商品，满足你的日常需求' }}</p><div class="product-foot"><strong>¥{{ formatPrice(product.price) }}</strong><span>已售 {{ product.sales || 0 }}</span></div></div>
        </article>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && !list.length" description="没有找到符合条件的商品" class="empty-state"><template #default><el-button type="primary" plain @click="reset">清空筛选条件</el-button></template></el-empty>

    <div v-if="total" class="pagination-wrap"><el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" background layout="total, sizes, prev, pager, next, jumper" :page-sizes="[12, 24, 48]" :total="total" @current-change="handlePageChange" @size-change="handleSizeChange" /></div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '../../api/request'

const route = useRoute()
const router = useRouter()
const filters = reactive({ keyword: '', categoryId: null, brandId: null })
const page = reactive({ pageNum: 1, pageSize: 12 })
const list = ref([])
const total = ref(0)
const categories = ref([])
const brands = ref([])
const loading = ref(false)
const initialized = ref(false)

const topCategories = computed(() => categories.value.filter(item => !item.parentId || item.parentId === 0))
const activeFilters = computed(() => {
  const result = []
  if (filters.keyword) result.push({ key: 'keyword', label: `关键词：${filters.keyword}` })
  if (filters.categoryId) {
    const category = categories.value.find(item => item.id === filters.categoryId)
    if (category) result.push({ key: 'categoryId', label: `分类：${categoryLabel(category)}` })
  }
  if (filters.brandId) {
    const brand = brands.value.find(item => item.id === filters.brandId)
    if (brand) result.push({ key: 'brandId', label: `品牌：${brand.name}` })
  }
  return result
})

function categoryLabel(category) {
  const parent = categories.value.find(item => item.id === category.parentId)
  return parent && parent.id !== category.id ? `${parent.name} / ${category.name}` : category.name
}

function readRouteQuery() {
  const query = route.query
  filters.keyword = typeof query.keyword === 'string' ? query.keyword : ''
  filters.categoryId = query.categoryId ? Number(query.categoryId) || null : null
  filters.brandId = query.brandId ? Number(query.brandId) || null : null
  page.pageNum = Math.max(Number(query.pageNum) || 1, 1)
  page.pageSize = [12, 24, 48].includes(Number(query.pageSize)) ? Number(query.pageSize) : 12
}

function routeQuery() {
  const query = { pageNum: String(page.pageNum), pageSize: String(page.pageSize) }
  if (filters.keyword.trim()) query.keyword = filters.keyword.trim()
  if (filters.categoryId) query.categoryId = String(filters.categoryId)
  if (filters.brandId) query.brandId = String(filters.brandId)
  return query
}

function syncRoute() {
  const next = routeQuery()
  const current = JSON.stringify(route.query)
  if (current === JSON.stringify(next)) {
    load()
    return
  }
  router.replace({ path: '/products', query: next })
}

async function load() {
  loading.value = true
  try {
    const params = { pageNum: page.pageNum, pageSize: page.pageSize }
    if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.brandId) params.brandId = filters.brandId
    const data = await request.get('/product/page', { params })
    list.value = Array.isArray(data?.list) ? data.list : []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function submit() {
  page.pageNum = 1
  syncRoute()
}

function reset() {
  filters.keyword = ''
  filters.categoryId = null
  filters.brandId = null
  page.pageNum = 1
  syncRoute()
}

function selectCategory(categoryId) {
  filters.categoryId = categoryId
  submit()
}

function removeFilter(key) {
  filters[key] = key === 'keyword' ? '' : null
  submit()
}

function handlePageChange(value) {
  page.pageNum = value
  syncRoute()
}

function handleSizeChange(value) {
  page.pageSize = value
  page.pageNum = 1
  syncRoute()
}

function openProduct(product) {
  router.push(`/product/${product.id}`)
}

function formatPrice(value) {
  const price = Number(value)
  return Number.isFinite(price) ? price.toFixed(2) : '0.00'
}

async function loadOptions() {
  const [categoryData, brandData] = await Promise.all([request.get('/category'), request.get('/brand')])
  categories.value = Array.isArray(categoryData) ? categoryData : []
  brands.value = Array.isArray(brandData) ? brandData : []
}

watch(() => route.query, () => {
  if (!initialized.value) return
  readRouteQuery()
  load()
}, { deep: true })

onMounted(async () => {
  readRouteQuery()
  initialized.value = true
  await Promise.all([loadOptions(), load()])
})
</script>

<style scoped>
.products-page { max-width: 1420px; margin: 0 auto; }
.page-hero { display: flex; align-items: center; justify-content: space-between; gap: 24px; padding: 34px 42px; border-radius: 16px; color: #fff; background: linear-gradient(110deg, #071d2e, #185967); }
.eyebrow { margin: 0 0 10px; color: #65d6e3; font-size: 11px; font-weight: 800; letter-spacing: 2px; }
.page-hero h1 { margin: 0 0 10px; font-size: 32px; }
.page-hero p:last-child { margin: 0; color: #b6d4da; }
.hero-stat { min-width: 140px; padding: 16px 22px; border: 1px solid #397480; border-radius: 12px; text-align: center; background: rgba(255, 255, 255, .08); }
.hero-stat strong { display: block; font-size: 28px; }.hero-stat span { color: #b8dce0; font-size: 12px; }
.filter-panel { margin-top: 18px; padding: 20px 22px; border: 1px solid #e1ebf0; border-radius: 12px; background: #fff; }
.filter-row { display: flex; align-items: center; gap: 12px; }.keyword-input { flex: 1; min-width: 220px; }.filter-select { width: 180px; }
.category-row, .active-row { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; margin-top: 18px; padding-top: 18px; border-top: 1px solid #edf2f4; }.filter-label { flex: 0 0 auto; margin-right: 8px; color: #637b89; font-size: 13px; font-weight: 700; }.category-chip { padding: 7px 15px; border: 0; border-radius: 18px; color: #607786; background: transparent; cursor: pointer; font-size: 13px; }.category-chip:hover { color: #1593a9; background: #eef9fb; }.category-chip.active { color: #fff; background: #159fba; }.clear-all { color: #1593a9; cursor: pointer; font-size: 13px; }
.results-toolbar { display: flex; align-items: baseline; justify-content: space-between; margin: 28px 0 16px; }.results-toolbar h2 { display: inline; margin: 0 12px 0 0; color: #1b2e40; font-size: 22px; }.result-count, .loading-text { color: #8296a2; font-size: 13px; }.loading-text { color: #1593a9; }
.product-grid { min-height: 120px; }.product-card { overflow: hidden; margin-bottom: 18px; border: 1px solid #e1ebf0; border-radius: 11px; background: #fff; cursor: pointer; transition: transform .2s, box-shadow .2s; }.product-card:hover { transform: translateY(-3px); box-shadow: 0 12px 28px rgba(29, 76, 104, .12); }.image-wrap { position: relative; height: 220px; background: #f6f8f9; }.product-image { display: block; width: 100%; height: 100%; }.image-error { display: grid; place-items: center; height: 100%; color: #a4b2ba; font-size: 13px; }.stock-tag { position: absolute; top: 12px; right: 12px; }.product-body { padding: 14px 16px 16px; }.product-tags { display: flex; gap: 6px; min-height: 24px; }.product-body h3 { overflow: hidden; margin: 11px 0 7px; color: #1d3142; font-size: 16px; text-overflow: ellipsis; white-space: nowrap; }.product-body p { display: -webkit-box; height: 40px; overflow: hidden; margin: 0; color: #7b8e9b; font-size: 13px; line-height: 1.55; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }.product-foot { display: flex; align-items: baseline; justify-content: space-between; margin-top: 14px; }.product-foot strong { color: #ff604f; font-size: 22px; }.product-foot span { color: #9aaab3; font-size: 12px; }.empty-state { margin: 40px 0; }.pagination-wrap { display: flex; justify-content: center; padding: 12px 0 24px; }
@media (max-width: 900px) { .page-hero { padding: 28px; }.filter-row { flex-wrap: wrap; }.keyword-input { flex-basis: 100%; }.filter-select { flex: 1; width: auto; } }
@media (max-width: 560px) { .page-hero { display: block; }.hero-stat { margin-top: 20px; }.page-hero h1 { font-size: 27px; }.filter-panel { padding: 16px; }.filter-row .el-button { flex: 1; }.results-toolbar { align-items: flex-start; }.pagination-wrap :deep(.el-pagination) { flex-wrap: wrap; justify-content: center; gap: 8px; } }
</style>
