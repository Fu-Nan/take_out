package com.fn.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fn.reggie.common.Result;
import com.fn.reggie.domain.User;
import com.fn.reggie.service.UserService;
import com.fn.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpServletRequest httpServletRequest) {
        //获取手机号
        String phone = user.getPhone();

        //等价：phone==null || phone.length()==0
        if (!StringUtils.isEmpty(phone)) {
            //生成随机4位验证码,转为String方便对比
            String validateCode = ValidateCodeUtils.generateValidateCode(4).toString();

            //模拟调用API发送短信给用户，此处就打印随机的验证码，后期来后台看
            log.info("用户登录验证码：{}", validateCode);

            //将验证码保存到session，用于和用户输入的验证码进行对比
//            HttpSession session = httpServletRequest.getSession();
//            session.setAttribute(phone, validateCode);
            //使用Redis缓存优化验证码，保存到Redis中，有效期5分钟
            redisTemplate.opsForValue().set(phone, validateCode, 5, TimeUnit.MINUTES);
            log.info("Redis缓存验证码成功");

            return Result.success("短信验证码发送成功");
        }
        return Result.error("验证码发送失败");
    }

    @PostMapping("/login")
    //前端发了phone和code两个内容，但User中没有封装code，这时可以使用UserDto的方式，也可以使用Map集合直接存放两个字段
    public Result<User> login(@RequestBody Map<String, String> map, HttpServletRequest httpServletRequest) {
        //获取手机号
        String phone = map.get("phone");

        //获取用户输入的验证码
        String userCode = map.get("code");

        //从session中获取保存的验证码
//        String code = (String) httpServletRequest.getSession().getAttribute(phone);
        //代码优化，改从Redis获取
        String code = (String) redisTemplate.opsForValue().get(phone);

        //验证码比对
        if (userCode.equals(code)) {
            //1.1 比对成功，进行登录成功逻辑
            //判断当前手机号是否为新用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, phone);
            User user = userService.getOne(wrapper);
            if (user == null) {
                //如果是，保存号码到数据库
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //如果不是，直接登录
            //登录前设置session
            httpServletRequest.getSession().setAttribute("user", user.getId());

            //如果登录成功，立即删除Redis缓存的验证码
            redisTemplate.delete(phone);
            return Result.success(user);
        }

        //1.2 比对失败
        return Result.error("登录失败");
    }

}
