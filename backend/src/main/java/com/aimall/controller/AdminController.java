package com.aimall.controller;

import com.aimall.common.PageResult;
import com.aimall.common.Result;
import com.aimall.entity.*;
import com.aimall.service.AdminService;
import com.aimall.service.ProductService;
import com.aimall.service.ReviewService;
import com.aimall.service.UserService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final ProductService productService;
    private final ReviewService reviewService;

    public AdminController(AdminService adminService,
                           UserService userService,
                           ProductService productService,
                           ReviewService reviewService) {
        this.adminService = adminService;
        this.userService = userService;
        this.productService = productService;
        this.reviewService = reviewService;
    }

    // 用户管理
    @GetMapping("/users")
    public Result<PageResult<User>> users(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) String keyword) {
        PageInfo<User> pageInfo = userService.page(pageNum, pageSize, keyword);
        return Result.ok(PageResult.of(pageInfo));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> req) {
        userService.updateStatus(id, req.get("status"));
        return Result.ok();
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> req) {
        userService.resetPassword(id, req.get("password"));
        return Result.ok();
    }

    // 分类 / 品牌
    @PostMapping("/category")
    public Result<Category> saveCategory(@RequestBody Category category) {
        return Result.ok(adminService.saveCategory(category));
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.ok();
    }

    @PostMapping("/brand")
    public Result<Brand> saveBrand(@RequestBody Brand brand) {
        return Result.ok(adminService.saveBrand(brand));
    }

    @DeleteMapping("/brand/{id}")
    public Result<Void> deleteBrand(@PathVariable Long id) {
        adminService.deleteBrand(id);
        return Result.ok();
    }

    // 商品管理
    @PostMapping("/product")
    public Result<Product> createProduct(@RequestBody Map<String, Object> req) {
        Product product = new Product();
        product.setCategoryId(Long.valueOf(req.get("categoryId").toString()));
        product.setBrandId(req.get("brandId") == null ? null : Long.valueOf(req.get("brandId").toString()));
        product.setName((String) req.get("name"));
        product.setSubtitle((String) req.get("subtitle"));
        product.setMainImage((String) req.get("mainImage"));
        product.setPrice(new java.math.BigDecimal(req.get("price").toString()));
        product.setOriginalPrice(req.get("originalPrice") == null ? null : new java.math.BigDecimal(req.get("originalPrice").toString()));
        product.setStock(req.get("stock") == null ? 0 : Integer.valueOf(req.get("stock").toString()));
        product.setStatus(req.get("status") == null ? 1 : Integer.valueOf(req.get("status").toString()));
        product.setDetailHtml((String) req.get("detailHtml"));
        product.setParamsJson(req.get("paramsJson") == null ? null : req.get("paramsJson").toString());
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) req.get("images");
        return Result.ok(productService.create(product, images));
    }

    @PutMapping("/product")
    public Result<Product> updateProduct(@RequestBody Map<String, Object> req) {
        Product product = new Product();
        product.setId(Long.valueOf(req.get("id").toString()));
        product.setCategoryId(Long.valueOf(req.get("categoryId").toString()));
        product.setBrandId(req.get("brandId") == null ? null : Long.valueOf(req.get("brandId").toString()));
        product.setName((String) req.get("name"));
        product.setSubtitle((String) req.get("subtitle"));
        product.setMainImage((String) req.get("mainImage"));
        product.setPrice(new java.math.BigDecimal(req.get("price").toString()));
        product.setOriginalPrice(req.get("originalPrice") == null ? null : new java.math.BigDecimal(req.get("originalPrice").toString()));
        product.setStock(req.get("stock") == null ? 0 : Integer.valueOf(req.get("stock").toString()));
        product.setStatus(req.get("status") == null ? 1 : Integer.valueOf(req.get("status").toString()));
        product.setDetailHtml((String) req.get("detailHtml"));
        product.setParamsJson(req.get("paramsJson") == null ? null : req.get("paramsJson").toString());
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) req.get("images");
        return Result.ok(productService.update(product, images));
    }

    @DeleteMapping("/product/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }

    // 订单
    @GetMapping("/orders")
    public Result<List<Order>> orders() {
        return Result.ok(adminService.listOrders());
    }

    // 评价
    @GetMapping("/reviews")
    public Result<List<Review>> reviews() {
        return Result.ok(adminService.listReviews());
    }

    @PostMapping("/review/{id}/reply")
    public Result<Void> replyReview(@PathVariable Long id, @RequestBody Map<String, String> req) {
        reviewService.reply(id, req.get("reply"));
        return Result.ok();
    }

    @DeleteMapping("/review/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.ok();
    }

    // 售后规则
    @GetMapping("/after-sale-rules")
    public Result<List<AfterSaleRule>> afterSaleRules() {
        return Result.ok(adminService.listAfterSaleRules());
    }

    @PostMapping("/after-sale-rule")
    public Result<AfterSaleRule> saveAfterSaleRule(@RequestBody AfterSaleRule rule) {
        return Result.ok(adminService.saveAfterSaleRule(rule));
    }

    @DeleteMapping("/after-sale-rule/{id}")
    public Result<Void> deleteAfterSaleRule(@PathVariable Long id) {
        adminService.deleteAfterSaleRule(id);
        return Result.ok();
    }

    // 知识库
    @GetMapping("/knowledge")
    public Result<List<KnowledgeDoc>> knowledgeList() {
        return Result.ok(adminService.listKnowledgeDocs());
    }

    @PostMapping("/knowledge")
    public Result<KnowledgeDoc> saveKnowledge(@RequestBody KnowledgeDoc doc) {
        return Result.ok(adminService.saveKnowledgeDoc(doc));
    }

    @DeleteMapping("/knowledge/{id}")
    public Result<Void> deleteKnowledge(@PathVariable Long id) {
        adminService.deleteKnowledgeDoc(id);
        return Result.ok();
    }

    // Function Tool
    @GetMapping("/function-tools")
    public Result<List<FunctionTool>> functionTools() {
        return Result.ok(adminService.listFunctionTools());
    }

    @PostMapping("/function-tool")
    public Result<FunctionTool> saveFunctionTool(@RequestBody FunctionTool tool) {
        return Result.ok(adminService.saveFunctionTool(tool));
    }

    @DeleteMapping("/function-tool/{id}")
    public Result<Void> deleteFunctionTool(@PathVariable Long id) {
        adminService.deleteFunctionTool(id);
        return Result.ok();
    }

    @GetMapping("/function-call-logs")
    public Result<List<FunctionCallLog>> functionCallLogs() {
        return Result.ok(adminService.listFunctionCallLogs());
    }

    // Agent 记录
    @GetMapping("/agent-runs")
    public Result<List<AgentRun>> agentRuns() {
        return Result.ok(adminService.listAgentRuns());
    }

    @GetMapping("/agent-runs/{id}")
    public Result<AgentRun> agentRunDetail(@PathVariable Long id) {
        return Result.ok(adminService.agentRunDetail(id));
    }

    // Prompt / 模型
    @GetMapping("/prompt-templates")
    public Result<List<PromptTemplate>> promptTemplates() {
        return Result.ok(adminService.listPromptTemplates());
    }

    @PostMapping("/prompt-template")
    public Result<PromptTemplate> savePromptTemplate(@RequestBody PromptTemplate template) {
        return Result.ok(adminService.savePromptTemplate(template));
    }

    @GetMapping("/model-configs")
    public Result<List<ModelConfig>> modelConfigs() {
        return Result.ok(adminService.listModelConfigs());
    }

    @PostMapping("/model-config")
    public Result<ModelConfig> saveModelConfig(@RequestBody ModelConfig config) {
        return Result.ok(adminService.saveModelConfig(config));
    }

    // 导购任务 / 推荐
    @GetMapping("/guide-tasks")
    public Result<List<GuideTask>> guideTasks() {
        return Result.ok(adminService.listGuideTasks());
    }

    @GetMapping("/recommend-results")
    public Result<List<RecommendResult>> recommendResults() {
        return Result.ok(adminService.listRecommendResults());
    }

    // 评价分析 / 运营报告
    @GetMapping("/evaluation-analysis")
    public Result<List<EvaluationAnalysis>> evaluationAnalysis() {
        return Result.ok(adminService.listEvaluationAnalysis());
    }

    @GetMapping("/operation-reports")
    public Result<List<OperationReport>> operationReports() {
        return Result.ok(adminService.listOperationReports());
    }
}
