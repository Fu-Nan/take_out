package com.fn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fn.reggie.common.BaseContext;
import com.fn.reggie.common.Result;
import com.fn.reggie.domain.AddressBook;
import com.fn.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody AddressBook addressBook) {
        //新增前封装userId
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);

        //新增前判断当前用户是否第一次添加地址(地址薄有无当前用户)
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, AddressBook::getUserId, userId);
        AddressBook bookServiceOne = addressBookService.getOne(wrapper);
        //如果第一次添加，设置该地址为默认地址is_default=1
        if (bookServiceOne == null) {
            addressBook.setIsDefault(1);
        }

        //如果不是，直接添加
        addressBookService.save(addressBook);
        return Result.success("新增地址成功");
    }

    /**
     * 获取该用户地址列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> list() {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //获取当前用户下的所有地址
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, AddressBook::getUserId, userId);
        List<AddressBook> addressBooks = addressBookService.list(wrapper);
        return Result.success(addressBooks);
    }

    /**
     * 获取默认地址
     *
     * @return
     */
    @GetMapping("/default")
    public Result<AddressBook> defaultAddress() {
        //获取userid
        Long userId = BaseContext.getCurrentId();

        //根据userId查询is_default=1的记录
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, AddressBook::getUserId, userId);
        wrapper.eq(AddressBook::getIsDefault, 1);
        AddressBook addressBookServiceOne = addressBookService.getOne(wrapper);

        //如果有记录，返回该记录
        if (addressBookServiceOne != null) {
            return Result.success(addressBookServiceOne);
        } else {
            //如果没有，返回空
            return Result.error("该用户没有默认地址");
        }
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public Result<AddressBook> setDefaultAddress(@RequestBody AddressBook addressBook) {
        //1. 先根据userId查询is_default=1的记录，将该记录默认值设为0
        //update address_book set is_default = 0 where is_default = 1 and user_id = XX
        LambdaUpdateWrapper<AddressBook> userIdWrapper = new LambdaUpdateWrapper<>();
        userIdWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        userIdWrapper.eq(AddressBook::getIsDefault, 1);
        userIdWrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(userIdWrapper);

        //2. 根据address_book的id查询对应记录,将该条记录设置is_default=1
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return Result.success(addressBook);
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public Result<AddressBook> get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return Result.success(addressBook);
        } else {
            return Result.error("未找到该地址");
        }
    }

    /**
     * 更新地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return Result.success("地址更新成功");
    }
}
