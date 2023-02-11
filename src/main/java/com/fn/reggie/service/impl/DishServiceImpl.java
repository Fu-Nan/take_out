package com.fn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fn.reggie.domain.Dish;
import com.fn.reggie.domain.DishFlavor;
import com.fn.reggie.dto.DishDto;
import com.fn.reggie.mapper.DishMapper;
import com.fn.reggie.service.DishFlavorService;
import com.fn.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 将DishDto中的数据保存到Dish和DishFlavor表中，这里操作了多张表，所以需要事务管理
     *
     * @param dishDto
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //Dish的数据直接保存
        //说明：dishDto继承了dish，所以直接保存，这里调用DishService自己的方法来直接保存
        this.save(dishDto);

        //DishFlavor的数据，因为原始数据没有封装DishId这条数据，所以需要进行处理
        //1. 先获取DishId(DishDto继承了Dish)
        Long dishId = dishDto.getId();

        //2. 将dishId插入到Flavors集合中
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(item -> {
            //item表示某个集合元素
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //3. 将处理好的flavors加入到DishFlavor表中,saveBatch用于存储集合数据
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 将Dish和DishFlavor的数据保存到DishDto中
     *
     * @param id
     */
    @Override
    public DishDto getWithFlavor(Long id) {
        //查询dish表
        Dish dish = this.getById(id);

        //查询dishFlavor表
        //1. 根据DishId查询dishFlavor
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
        dishFlavorWrapper.eq(DishFlavor::getDishId, id);
//        DishFlavor dishFlavor = dishFlavorService.getOne(dishFlavorWrapper);
        List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorWrapper);

        //数据对拷
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
//        BeanUtils.copyProperties(dishFlavor, dishDto);
        //dishDto的Flavor不能使用数据对拷，因为dishFlavor的数据会覆盖dish的数据，直接将查到的数据给到dishDto就可以。
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /**
     * 修改菜品信息
     *
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //dish的数据直接更新
        this.updateById(dishDto);

        //清理当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
        dishFlavorWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(dishFlavorWrapper);

        //添加口味数据
//        dishFlavorService.saveBatch(dishDto.getFlavors());
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(item -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
