package com.fn.reggie.dto;

import com.fn.reggie.domain.Setmeal;
import com.fn.reggie.domain.SetmealDish;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据传输对象，用于传输Setmeal和SetmealDish
 */
@Data
public class SetmealDto extends Setmeal {
    private List<SetmealDish> setmealDishes;
    private String categoryName;
}
