package com.fn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fn.reggie.common.BusinessException;
import com.fn.reggie.common.Result;
import com.fn.reggie.domain.Employee;
import com.fn.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    //HttpServletRequest用于获取当前session
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1.进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes()); //加密完成的密码
        //2.根据用户名查询用户
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lqw);
        //3.是否查询到结果
        if (emp == null) {
            return Result.error("用户名未找到");
        } else if (!emp.getPassword().equals(password)) {
            //密码不一致时返回登录失败
            return Result.error("密码错误，请重试!");
        } else if (emp.getStatus() == 0) {
            //检查员工状态，如果为0被禁用，则返回员工已被禁用的结果
            return Result.error("该员工已被禁用，请联系管理员");
        } else {
            //登录成功，将员工id存入session并返回登录成功结果
            request.getSession().setAttribute("employee", emp.getId());
            return Result.success(emp);
        }
    }

    /**
     * 员工退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        //1.清理Session中的用户id
        request.getSession().removeAttribute("employee");
        log.info("员工退出登录...");
        return Result.success("退出成功");
    }

    /**
     * 添加员工
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public Result<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        //1. 设置员工初始密码，并进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //2. 设置该账户创建和更新时间（改用MybatisPlus提供的自动字段填充实现）
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //3. 设置该账户被谁创建（改用MybatisPlus提供的自动字段填充实现）
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        //4. 存储到数据库
        try {
            boolean save = employeeService.save(employee);
            return Result.success("添加成功");
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 员工管理中的分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> getPage(int page, int pageSize, String name) {
//        log.info("page={},pageSize={},name={}", page, pageSize, name);
        //构造分页对象
        Page pageInfo = new Page(page, pageSize);

        //构造条件对象
        QueryWrapper<Employee> eqw = new QueryWrapper<>();
        eqw.lambda().eq(null != name, Employee::getName, name);
        //添加排序条件，按照更新时间降序排序
        eqw.lambda().orderByAsc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, eqw);

        return Result.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     *
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("账户启用信息：{}", employee);
        //1. 获取当前账户id（改用MybatisPlus提供的自动字段填充实现）
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //2. 设置更新信息
        //employee.setUpdateUser(empId);
        //employee.setUpdateTime(LocalDateTime.now());

        //执行语句
        employeeService.updateById(employee);
        return Result.success("员工信息更新成功");
    }

    /**
     * 根据id查询单个员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getId, id);
        Employee employee = employeeService.getOne(wrapper);
        if (null != employee) {
            return Result.success(employee);
        }
        return Result.error("未找到该员工信息");
    }
}
