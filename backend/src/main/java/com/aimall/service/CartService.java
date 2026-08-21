package com.aimall.service;

import com.aimall.common.BusinessException;
import com.aimall.common.UserContext;
import com.aimall.entity.CartItem;
import com.aimall.entity.Product;
import com.aimall.mapper.CartItemMapper;
import com.aimall.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartService(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    public List<CartItem> list() {
        return cartItemMapper.findByUserId(UserContext.getUserId());
    }

    @Transactional
    public CartItem add(Long productId, Integer quantity) {
        Long userId = UserContext.getUserId();
        Product product = productMapper.findById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(400, "商品不存在或已下架");
        }
        CartItem exist = cartItemMapper.find(userId, productId);
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + (quantity == null ? 1 : quantity));
            cartItemMapper.update(exist);
            return exist;
        }
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity == null ? 1 : quantity);
        item.setChecked(1);
        cartItemMapper.insert(item);
        return item;
    }

    public void updateQuantity(Long id, Integer quantity) {
        Long userId = UserContext.getUserId();
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "数量必须大于0");
        }
        CartItem item = new CartItem();
        item.setId(id);
        item.setUserId(userId);
        item.setQuantity(quantity);
        cartItemMapper.update(item);
    }

    public void updateChecked(Long id, Integer checked) {
        Long userId = UserContext.getUserId();
        CartItem item = new CartItem();
        item.setId(id);
        item.setUserId(userId);
        item.setChecked(checked);
        cartItemMapper.update(item);
    }

    public void updateAllChecked(Integer checked) {
        cartItemMapper.updateAllChecked(UserContext.getUserId(), checked == null ? 1 : checked);
    }

    public void delete(Long id) {
        cartItemMapper.delete(id, UserContext.getUserId());
    }
}
