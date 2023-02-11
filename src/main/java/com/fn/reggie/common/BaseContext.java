package com.fn.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用于设置和获取当前登录用户的ID
 */
public class BaseContext {
    //new一个ThreadLocal，作用于Long id，所以T->Long
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
