package com.aimall.controller;

import com.aimall.common.PageResult;
import com.aimall.common.Result;
import com.aimall.entity.Product;
import com.aimall.service.ProductService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/page")
    public Result<PageResult<Product>> page(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Long brandId,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) BigDecimal minPrice,
                                            @RequestParam(required = false) BigDecimal maxPrice) {
        PageInfo<Product> pageInfo = productService.page(pageNum, pageSize, categoryId, brandId, keyword, minPrice, maxPrice);
        return Result.ok(PageResult.of(pageInfo));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @GetMapping("/hot")
    public Result<List<Product>> hot(@RequestParam(defaultValue = "8") int limit) {
        return Result.ok(productService.hot(limit));
    }
}
