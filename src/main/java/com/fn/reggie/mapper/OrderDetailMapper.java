package com.fn.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fn.reggie.domain.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
