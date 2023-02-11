package com.fn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fn.reggie.domain.Setmeal;
import com.fn.reggie.domain.SetmealDish;
import com.fn.reggie.dto.SetmealDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    @Transactional
    void saveWithDish(SetmealDto setmealDto);

    @Transactional
    SetmealDto getWithDish(Long id);

    @Transactional
    void updateWithDish(SetmealDto setmealDto);

    @Transactional
    void removeWithDish(List<Long> ids);
}
