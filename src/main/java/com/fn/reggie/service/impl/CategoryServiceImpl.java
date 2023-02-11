package com.fn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fn.reggie.common.BusinessException;
import com.fn.reggie.domain.Category;
import com.fn.reggie.domain.Dish;
import com.fn.reggie.domain.Setmeal;
import com.fn.reggie.mapper.CategoryMapper;
import com.fn.reggie.service.CategoryService;
import com.fn.reggie.service.DishService;
import com.fn.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除前判断是否和菜品与套餐进行了关联
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了菜品，如果关联，抛出业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);
        if (dishCount > 0) {
            //抛出业务异常
            throw new BusinessException("删除失败，当前分类下有关联的菜品！");
        }

        //查询当前分类是否关联了套餐，如果关联，抛出业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setmealCount > 0) {
            throw new BusinessException("删除失败，当前分类下有关联的套餐！");
        }

        //无任何关联，正常删除分类
//        categoryMapper.deleteById(id);
        super.removeById(id);
    }
}
