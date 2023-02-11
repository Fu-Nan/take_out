package com.fn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fn.reggie.domain.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
