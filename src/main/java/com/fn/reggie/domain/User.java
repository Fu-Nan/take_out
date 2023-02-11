package com.fn.reggie.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String phone;
    private String sex;
    //身份证号
    private String idNumber;
    //头像
    private String avatar;
    private Integer status;
}
