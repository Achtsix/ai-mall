package com.aimall.service;

import com.aimall.common.BusinessException;
import com.aimall.common.UserContext;
import com.aimall.entity.Review;
import com.aimall.mapper.OrderItemMapper;
import com.aimall.mapper.ReviewMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderItemMapper orderItemMapper;

    public ReviewService(ReviewMapper reviewMapper, OrderItemMapper orderItemMapper) {
        this.reviewMapper = reviewMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public List<Review> listByProduct(Long productId) {
        return reviewMapper.findByProductId(productId);
    }

    public List<Review> adminList() {
        return reviewMapper.findAll();
    }

    public Review add(Review review) {
        Long userId = UserContext.getUserId();
        if (review.getOrderId() != null) {
            boolean purchased = orderItemMapper.findUserPurchased(userId, review.getProductId()).stream()
                    .anyMatch(i -> i.getOrderId().equals(review.getOrderId()));
            if (!purchased) {
                throw new BusinessException(400, "只能评价已购商品");
            }
        }
        review.setUserId(userId);
        reviewMapper.insert(review);
        return review;
    }

    public void reply(Long id, String reply) {
        Review review = reviewMapper.findById(id);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }
        review.setReply(reply);
        reviewMapper.updateReply(review);
    }

    public void delete(Long id) {
        reviewMapper.deleteById(id);
    }
}
