package com.fn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fn.reggie.common.Result;
import com.fn.reggie.domain.Category;
import com.fn.reggie.domain.Setmeal;
import com.fn.reggie.domain.SetmealDish;
import com.fn.reggie.dto.SetmealDto;
import com.fn.reggie.service.CategoryService;
import com.fn.reggie.service.SetmealDishService;
import com.fn.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 套餐管理中的分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> setmealPage = new Page<Setmeal>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //定义查询条件，name模糊查询，并按照更新时间降序排序
        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        wrapper.like(null != name, "name", name);
        wrapper.lambda().orderByAsc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, wrapper);

        //拷贝除套餐信息外的其他所有内容到setmealDtoPage中
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        //处理records内容
        List<Setmeal> setmealRecords = setmealPage.getRecords();
        List<SetmealDto> setmealDtoRecords = setmealRecords.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            //查询设置categoryId对应的分类名称
            Long categoryId = setmealDto.getCategoryId();
            Category category = categoryService.getById(categoryId);
            setmealDto.setCategoryName(category.getName());

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoRecords);

        return Result.success(setmealDtoPage);
    }

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    //将该value下所有key全删除
    @CacheEvict(value = "setmealCache", allEntries = true)
//    @CachePut(value = "setmealCache", key = "#setmealDto.id") //Put不适用
    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功");
    }

    /**
     * 根据id查询单个套餐信息
     *
     * @param id
     * @return
     */
    //unless，满足条件就不缓存
    @Cacheable(value = "setmealCache", key = "#id", unless = "#result == null ")
    @GetMapping("/{id}")
    public Result<Setmeal> getById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getWithDish(id);
        return Result.success(setmealDto);
    }

    /**
     * 更新套餐信息
     *
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache", key = "#setmealDto.id")
    @PutMapping
    public Result<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDish(setmealDto);
        return Result.success("更新成功");
    }

    /**
     * 删除和批量删除，删除前套餐必须为停售状态，否则抛业务异常
     *
     * @param ids
     * @return
     */
    //将该value下所有key全删除
    @CacheEvict(value = "setmealCache", allEntries = true)
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDish(ids);
        return Result.success("删除成功");
    }

    /**
     * 停售和批量停售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> statusSet0(Long[] ids) {
        List<Setmeal> setmeals = new ArrayList<>();
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(0);
            setmeals.add(setmeal);
        }
        setmealService.updateBatchById(setmeals);
        return Result.success("批量停售修改成功");
    }

    /**
     * 启售和批量启售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> statusSet1(Long[] ids) {
        List<Setmeal> setmeals = new ArrayList<>();
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(1);
            setmeals.add(setmeal);
        }
        setmealService.updateBatchById(setmeals);
        return Result.success("批量启售修改成功");
    }

    /**
     * 获取套餐信息
     *
     * @param setmeal
     * @return
     */
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId+'_'+#setmeal.status")
    @GetMapping("/list")
    public Result<List<SetmealDto>> list(Setmeal setmeal) {
        //setmealList
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        setmealWrapper.eq(Setmeal::getStatus, 1);
        setmealWrapper.orderByAsc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(setmealWrapper);

        List<SetmealDto> setmealDtoList = setmealList.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            //setmeal内容
            BeanUtils.copyProperties(item, setmealDto);

            //setmealDishes
            Long id = item.getId();
            LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
            setmealDishWrapper.eq(SetmealDish::getSetmealId, id);
            List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishWrapper);
            setmealDto.setSetmealDishes(setmealDishes);

            //categoryName
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            setmealDto.setCategoryName(category.getName());

            return setmealDto;
        }).collect(Collectors.toList());

        return Result.success(setmealDtoList);
    }
}
