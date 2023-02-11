package com.fn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fn.reggie.domain.Dish;
import com.fn.reggie.dto.DishDto;
import org.springframework.transaction.annotation.Transactional;

public interface DishService extends IService<Dish> {
    @Transactional
    void saveWithFlavor(DishDto dishDto);

    @Transactional
    DishDto getWithFlavor(Long id);

    @Transactional
    void updateWithFlavor(DishDto dishDto);
}
