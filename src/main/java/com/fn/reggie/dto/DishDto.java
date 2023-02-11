package com.fn.reggie.dto;

import com.fn.reggie.domain.Dish;
import com.fn.reggie.domain.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据传输对象，用于传输Dish和DishFlavor
 */
@Data
public class DishDto extends Dish {
    //注：Dish已经实现序列化，其子类也自动实现了序列化

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
