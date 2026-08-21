package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.CartItem;
import com.aimall.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<CartItem>> list() {
        return Result.ok(cartService.list());
    }

    @PostMapping
    public Result<CartItem> add(@RequestBody Map<String, Object> req) {
        Long productId = Long.valueOf(req.get("productId").toString());
        Integer quantity = req.get("quantity") == null ? 1 : Integer.valueOf(req.get("quantity").toString());
        return Result.ok(cartService.add(productId, quantity));
    }

    @PutMapping("/{id}/quantity")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> req) {
        cartService.updateQuantity(id, req.get("quantity"));
        return Result.ok();
    }

    @PutMapping("/{id}/checked")
    public Result<Void> updateChecked(@PathVariable Long id, @RequestBody Map<String, Integer> req) {
        cartService.updateChecked(id, req.get("checked"));
        return Result.ok();
    }

    @PutMapping("/checked")
    public Result<Void> updateAllChecked(@RequestBody Map<String, Integer> req) {
        cartService.updateAllChecked(req.get("checked"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return Result.ok();
    }
}
