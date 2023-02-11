package com.fn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fn.reggie.common.BaseContext;
import com.fn.reggie.common.BusinessException;
import com.fn.reggie.domain.*;
import com.fn.reggie.mapper.OrdersMapper;
import com.fn.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        //获取当前用户id(可以session也可以BaseContext)
        Long userId = BaseContext.getCurrentId();

        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(shoppingCartWrapper);

        //如果购物车为空，抛异常
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new BusinessException("购物车为空，下单失败");
        }

        //查询用户表数据
        User user = userService.getById(userId);
        //查询地址薄信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        //如果地址信息为空，抛业务异常（因为没有地址就无法派送)
        if (addressBook == null) {
            throw new BusinessException("用户地址错误，下单失败");
        }

        //计算购物车总金额，并设置获取订单详情表
//        BigDecimal amount = null;(BigDecimal的处理方式如下)
        long orderId = IdWorker.getId(); //使用MP提供的工具生成一个订单号
        AtomicInteger amount = new AtomicInteger(0); //用于累加金额，线程安全的方式
        List<OrderDetail> orderDetails = shoppingCarts.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            //addAndGet表示+=;multiply表示单价*份数;.intValue将结果转为Int
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());

            return orderDetail;
        }).collect(Collectors.toList());


        //向订单表插入数据,插入前完善数据
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setUserId(userId);
        orders.setPhone(user.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());

        this.save(orders);

        //向订单明细表插入数据（查到的购物车数据）
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据(前面已经构造好Wrapper)
        shoppingCartService.remove(shoppingCartWrapper);
    }
}
