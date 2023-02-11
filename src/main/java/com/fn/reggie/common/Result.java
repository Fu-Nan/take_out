package com.fn.reggie.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果，将数据封装好后返回给网页
 * @param <T>
 */
@Data
public class Result<T> {
    private Integer code;   //编码，1成功，0和其他数字失败
    private T data;    //数据
    private String msg; //错误信息
    private Map map = new HashMap();    //动态数据

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.code = 1;
        result.data = object;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.msg = msg;
        return result;
    }

    public Result<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}
