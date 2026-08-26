package com.aimall.service;

import com.aimall.common.BusinessException;
import com.aimall.entity.Product;
import com.aimall.entity.ProductImage;
import com.aimall.mapper.ProductImageMapper;
import com.aimall.mapper.ProductMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;

    public ProductService(ProductMapper productMapper, ProductImageMapper productImageMapper) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
    }

    public PageInfo<Product> page(int pageNum, int pageSize, Long categoryId, Long brandId, String keyword,
                                  BigDecimal minPrice, BigDecimal maxPrice) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
            normalizedKeyword = null;
        }
        PageHelper.startPage(safePageNum, safePageSize);
        List<Product> list = productMapper.search(categoryId, brandId, normalizedKeyword, minPrice, maxPrice);
        return new PageInfo<>(list);
    }

    public Map<String, Object> detail(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        List<ProductImage> images = productImageMapper.findByProductId(id);
        Map<String, Object> map = new HashMap<>();
        map.put("product", product);
        map.put("images", images);
        return map;
    }

    public List<Product> hot(int limit) {
        return productMapper.findHot(limit);
    }

    @Transactional
    public Product create(Product product, List<String> images) {
        if (product.getStatus() == null) product.setStatus(1);
        productMapper.insert(product);
        saveImages(product.getId(), images);
        return product;
    }

    @Transactional
    public Product update(Product product, List<String> images) {
        // P1-10 修复：使用数据库行锁避免并发竞态
        Product existing = productMapper.findById(product.getId());
        if (existing == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 更新商品信息
        int updated = productMapper.update(product);
        if (updated == 0) {
            // 如果更新失败，可能商品在此期间被删除
            throw new BusinessException(404, "商品已被删除或修改，请刷新后重试");
        }

        if (images != null) {
            productImageMapper.deleteByProductId(product.getId());
            saveImages(product.getId(), images);
        }
        return product;
    }

    private void saveImages(Long productId, List<String> images) {
        if (images == null || images.isEmpty()) return;
        int sort = 1;
        for (String url : images) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setUrl(url);
            image.setSort(sort++);
            productImageMapper.insert(image);
        }
    }

    @Transactional
    public void delete(Long id) {
        // P1-10 修复：先检查商品是否存在，避免静默失败
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        productMapper.deleteById(id);
        productImageMapper.deleteByProductId(id);
    }
}
