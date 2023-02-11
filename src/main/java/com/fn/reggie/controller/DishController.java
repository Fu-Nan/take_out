package com.fn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fn.reggie.domain.Category;
import com.fn.reggie.domain.Dish;
import com.fn.reggie.domain.DishFlavor;
import com.fn.reggie.dto.DishDto;
import com.fn.reggie.common.Result;
import com.fn.reggie.service.CategoryService;
import com.fn.reggie.service.DishFlavorService;
import com.fn.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品管理中的分页查询，需要查询到多张表
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        Page<Dish> dishPage = new Page<Dish>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //设置条件查询，可以通过输入菜品名称进行分页查询，支持模糊查询
        QueryWrapper<Dish> wrapper = new QueryWrapper<>();
        //1. 姓名模糊查询
        wrapper.like(null != name, "name", name);
        //2. 按sort升序排序
        wrapper.lambda().orderByAsc(Dish::getSort);
        //3. 执行查询
        dishService.page(dishPage, wrapper);

        //对象拷贝（因为Dish中没有封装categoryName，所以需要查询多个表）
        //通过spring提供的工具,复制属性方法，进行对象拷贝，参数：(原始对象,目标对象,"忽略的属性名")
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        //对records进行处理，将DishPage中的categoryId取出，去查对应的categoryName，再给到DishDtoPage里的categoryName
        List<Dish> dishRecords = dishPage.getRecords();
        List<DishDto> dishDtoRecords = dishRecords.stream().map(item -> {
            //1. 用于封装查询到的categoryName
            DishDto dishDto = new DishDto();
            //2. 拷贝Dish除categoryName以外的所有属性到DishDto中
            BeanUtils.copyProperties(item, dishDto);

            //3. 获取到Dish中的CategoryId
            Long categoryId = item.getCategoryId();
            //4. 通过categoryId查询categoryName
            Category category = categoryService.getById(categoryId);
            dishDto.setCategoryName(category.getName());

            return dishDto;
        }).collect(Collectors.toList());

        //将处理完后的dishDtoRecords加入到dishDtoPage里
        dishDtoPage.setRecords(dishDtoRecords);

        return Result.success(dishDtoPage);
    }

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return Result.success("新增菜品成功");
    }

    /**
     * 修改菜品时，根据id查询某个菜品信息和对应的口味信息（回显信息）
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getWithFlavor(id);
        return Result.success(dishDto);
    }

    /**
     * 修改菜品信息
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return Result.success("修改菜品成功");
    }

    /**
     * 停售菜品
     * 此方法用于停售单个菜品、批量停售菜品
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> statusSet0(Long[] ids) {
        List<Dish> statusList = new ArrayList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(0);
            statusList.add(dish);
        }
        boolean b = dishService.updateBatchById(statusList);
        log.info(b ? "菜品状态修改成功" : "菜品状态修改失败");
        return Result.success("菜品状态修改成功");
    }

    /**
     * 启售菜品
     * 此方法用于起售单个菜品、批量起售菜品
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> statusSet1(Long[] ids) {
        List<Dish> statusList = new ArrayList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(1);
            statusList.add(dish);
        }
        boolean b = dishService.updateBatchById(statusList);
        log.info(b ? "菜品状态修改成功" : "菜品状态修改失败");
        return Result.success("菜品状态修改成功");
    }

    /**
     * 根据Id删除菜品
     * 此方法用于删除单个菜品，或者批量删除多个菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long[] ids) {
        //Long数组转为集合，再一起删除
        List<Long> delList = new ArrayList<>(Arrays.asList(ids));
        dishService.removeByIds(delList);
        return Result.success("删除成功");
    }

    /**
     * 套餐管理setmeal->查询菜品分类对应的所有菜品信息
     *
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public Result<List<Dish>> list(Long categoryId) {
//        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Dish::getCategoryId, categoryId);
//        //并且该菜品status状态为1，起售才展示
//        wrapper.eq(Dish::getStatus, 1);
//        wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList = dishService.list(wrapper);
//        return Result.success(dishList);
//    }
    //代码改造，新增返回口味数据
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        //并且该菜品status状态为1，起售才展示
        wrapper.eq(Dish::getStatus, 1);
        wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(wrapper);

        List<DishDto> dishDtoList = dishList.stream().map(item -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            //dishFlavors
            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
            dishFlavorWrapper.eq(DishFlavor::getDishId, id);
            List<DishFlavor> flavors = dishFlavorService.list(dishFlavorWrapper);
            dishDto.setFlavors(flavors);

            //categoryName
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null)
                dishDto.setCategoryName(category.getName());

            return dishDto;
        }).collect(Collectors.toList());

        return Result.success(dishDtoList);
    }
}
