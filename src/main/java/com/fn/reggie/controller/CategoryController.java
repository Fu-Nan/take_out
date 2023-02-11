package com.fn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fn.reggie.common.Result;
import com.fn.reggie.domain.Category;
import com.fn.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类管理
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品分类和套餐分类（公用同一方法）
     *
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody Category category) {
        try {
            categoryService.save(category);
            return Result.success("添加菜品成功!");
        } catch (Exception e) {
//            throw new BusinessException("添加失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 分类管理中的分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize) {
        Page pageInfo = new Page(page, pageSize);
        //添加查询条件，按照sort升序排序
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo, wrapper);
        return Result.success(pageInfo);
    }

    /**
     * 根据id删除分类信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(Long ids) {
        log.info("删除id为：{}", ids);
        //删除前判断该分类下是否关联了菜品或套餐（在service层功能完善)
        categoryService.remove(ids);


        //categoryService.removeById(id);
        return Result.success("删除分类成功!");
    }

    /**
     * 修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getId, category.getId());
        categoryService.update(category, wrapper);
        return Result.success("分类修改成功!");
    }

    /**
     * 菜品管理和套餐管理新建套餐时的套餐分类
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        //查询目标type
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(null != category.getType(), Category::getType, category.getType());
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //开始查询
        List<Category> list = categoryService.list(wrapper);
        return Result.success(list);
    }
}
