package com.fn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fn.reggie.domain.Orders;
import org.springframework.transaction.annotation.Transactional;

public interface OrdersService extends IService<Orders> {
    @Transactional
    void submit(Orders orders);
}
