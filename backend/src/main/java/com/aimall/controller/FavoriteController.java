package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.ProductFavorite;
import com.aimall.mapper.ProductFavoriteMapper;
import com.aimall.common.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final ProductFavoriteMapper favoriteMapper;

    public FavoriteController(ProductFavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    @GetMapping
    public Result<List<ProductFavorite>> list() {
        return Result.ok(favoriteMapper.findByUserId(UserContext.getUserId()));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Long> req) {
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(UserContext.getUserId());
        favorite.setProductId(req.get("productId"));
        favoriteMapper.insert(favorite);
        return Result.ok();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> delete(@PathVariable Long productId) {
        favoriteMapper.delete(UserContext.getUserId(), productId);
        return Result.ok();
    }
}
