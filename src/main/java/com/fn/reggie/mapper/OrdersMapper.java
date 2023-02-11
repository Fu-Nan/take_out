package com.fn.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fn.reggie.domain.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
