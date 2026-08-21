package com.aimall.controller;

import com.aimall.common.Result;
import com.aimall.entity.Review;
import com.aimall.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public Result<List<Review>> listByProduct(@PathVariable Long productId) {
        return Result.ok(reviewService.listByProduct(productId));
    }

    @PostMapping
    public Result<Review> add(@RequestBody Review review) {
        return Result.ok(reviewService.add(review));
    }
}
