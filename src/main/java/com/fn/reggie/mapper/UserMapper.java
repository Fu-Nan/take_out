package com.fn.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fn.reggie.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
