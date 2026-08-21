package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.Order;
import com.aimall.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<List<Order>> myOrders() {
        return Result.ok(orderService.myOrders());
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    @PostMapping("/create")
    public Result<Order> create(@RequestBody Map<String, Long> req) {
        return Result.ok(orderService.createFromCart(req.get("addressId")));
    }

    @PostMapping("/{id}/pay")
    public Result<Order> pay(@PathVariable Long id) {
        return Result.ok(orderService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.ok();
    }
}
