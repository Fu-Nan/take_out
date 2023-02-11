package com.fn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fn.reggie.common.BaseContext;
import com.fn.reggie.common.Result;
import com.fn.reggie.domain.ShoppingCart;
import com.fn.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 根据UserId，查询购物车列表
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(HttpServletRequest httpServletRequest) {
        //根据userId查询对应商品
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(shoppingCartWrapper);
        return Result.success(shoppingCarts);
    }

    /**
     * 添加菜品到购物车
     * 如果某个菜品在购物车已存在，则number字段+1
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        //封装userId
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        //1. 当前菜品是否在购物车
        //select * from shopping_cart where userId = XXX and dishId/setmealId == XXX
        //下面统一设置上条件：where userId = XXX and dishId/setmealId == XXX
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(wrapper);
        //1.1 在购物车
        //update shopping_cart set number = XXX (where部分上面已经设置上)where userId = XXX and dishId/setmealId == XXX
        if (cartServiceOne != null) {
            cartServiceOne.setNumber(cartServiceOne.getNumber() + 1);
            shoppingCartService.update(cartServiceOne, wrapper);
        } else {
            //1.2 不在购物车
            //insert into shopping_cart VALUES(XXX)
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return Result.success(cartServiceOne);
    }

    /**
     * 根据UserId，清空购物车
     *
     * @param httpServletRequest
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean(HttpServletRequest httpServletRequest) {
        Long userId = (Long) httpServletRequest.getSession().getAttribute("user");

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(null != userId, ShoppingCart::getUserId, userId);
        shoppingCartService.remove(wrapper);
        return Result.success("购物车已清空");
    }
}
