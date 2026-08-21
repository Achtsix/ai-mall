package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.Brand;
import com.aimall.entity.Category;
import com.aimall.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryBrandController {

    private final AdminService adminService;

    public CategoryBrandController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/category")
    public Result<List<Category>> categories() {
        return Result.ok(adminService.listCategories());
    }

    @GetMapping("/brand")
    public Result<List<Brand>> brands() {
        return Result.ok(adminService.listBrands());
    }
}
