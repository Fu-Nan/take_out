package com.fn.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fn.reggie.domain.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
