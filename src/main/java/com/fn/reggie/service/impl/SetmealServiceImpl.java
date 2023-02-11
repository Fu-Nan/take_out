package com.fn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fn.reggie.common.BusinessException;
import com.fn.reggie.domain.Setmeal;
import com.fn.reggie.domain.SetmealDish;
import com.fn.reggie.dto.DishDto;
import com.fn.reggie.dto.SetmealDto;
import com.fn.reggie.mapper.SetmealMapper;
import com.fn.reggie.service.SetmealDishService;
import com.fn.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，用到Setmeal和SetmealDish多表操作
     *
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //Setmeal的数据直接保存
        this.save(setmealDto);

        //获取setmealId
        Long setmealId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        //将添加id后的setmealDishes保存
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据id查询套餐信息
     *
     * @param id
     */
    @Override
    public SetmealDto getWithDish(Long id) {
        SetmealDto setmealDto = new SetmealDto();

        Setmeal setmeal = this.getById(id);
        //拷贝setmeal到Dto
        BeanUtils.copyProperties(setmeal, setmealDto, "setmealDishes");

        LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
        setmealDishWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishWrapper);
        //保存到Dto
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    /**
     * 更新套餐信息
     *
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        //清空setmealDish的数据
        LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
        setmealDishWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(setmealDishWrapper);

        //保存数据到setmealDish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除和批量删除套餐，删除前要将套餐状态设置为0
     *
     * @param ids
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (ids) and status = 1
        //查询套餐status，确认是否可以删除
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.in(Setmeal::getId, ids);
        setmealWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(setmealWrapper);
        //如果查询到某个套餐正在启售(count>0)，就抛一个业务异常
        //1 不能删除：抛一个业务异常
        if (count > 0) {
            throw new BusinessException("套餐正在销售中，不能删除！");
        }

        //2.1 能删除，先删除setmeal表中数据
        this.removeByIds(ids);

        //2.2 再根据setmealId删除setmealDish表中数据
        //delete fromm setmeal_dish where setmeal_id in (....)
        LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
        setmealDishWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishWrapper);
    }
}
