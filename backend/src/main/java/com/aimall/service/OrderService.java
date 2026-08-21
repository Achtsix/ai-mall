package com.aimall.service;

import cn.hutool.core.util.IdUtil;
import com.aimall.common.BusinessException;
import com.aimall.common.UserContext;
import com.aimall.entity.*;
import com.aimall.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final WalletService walletService;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        CartItemMapper cartItemMapper,
                        ProductMapper productMapper,
                        AddressMapper addressMapper,
                        WalletService walletService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.addressMapper = addressMapper;
        this.walletService = walletService;
    }

    public List<Order> myOrders() {
        List<Order> orders = orderMapper.findByUserId(UserContext.getUserId());
        orders.forEach(o -> o.setItems(orderItemMapper.findByOrderId(o.getId())));
        return orders;
    }

    public Order detail(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null || !order.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(404, "订单不存在");
        }
        order.setItems(orderItemMapper.findByOrderId(order.getId()));
        return order;
    }

    @Transactional
    public Order createFromCart(Long addressId) {
        Long userId = UserContext.getUserId();
        Address address = addressMapper.findByIdAndUser(addressId, userId);
        if (address == null) {
            throw new BusinessException(400, "请选择收货地址");
        }
        List<CartItem> checkedItems = cartItemMapper.findByUserId(userId).stream()
                .filter(i -> i.getChecked() != null && i.getChecked() == 1)
                .toList();
        if (checkedItems.isEmpty()) {
            throw new BusinessException(400, "购物车没有选中商品");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : checkedItems) {
            if (item.getStock() == null || item.getStock() < item.getQuantity()) {
                throw new BusinessException(400, "商品库存不足：" + item.getProductName());
            }
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Order order = new Order();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setStatus(0);
        order.setAddressSnapshot(address.getReceiverName() + " " + address.getReceiverPhone() + " " +
                address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        orderMapper.insert(order);

        for (CartItem item : checkedItems) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(item.getProductId());
            oi.setProductName(item.getProductName());
            oi.setProductImage(item.getProductImage());
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setTotalAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(oi);
            productMapper.deductStock(item.getProductId(), item.getQuantity());
        }
        cartItemMapper.deleteChecked(userId);
        order.setItems(orderItemMapper.findByOrderId(order.getId()));
        return order;
    }

    @Transactional
    public Order pay(Long id) {
        Long userId = UserContext.getUserId();
        Order order = orderMapper.findById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态不允许支付");
        }
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        order.setItems(items);
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "订单明细不存在，无法支付");
        }
        walletService.pay(userId, order.getPayAmount());
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
        items.forEach(i -> productMapper.increaseSales(i.getProductId(), i.getQuantity()));
        return order;
    }

    public void cancel(Long id) {
        Long userId = UserContext.getUserId();
        Order order = orderMapper.findById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(400, "只有待支付订单可以取消");
        }
        order.setStatus(4);
        orderMapper.updateStatus(order);
    }
}
