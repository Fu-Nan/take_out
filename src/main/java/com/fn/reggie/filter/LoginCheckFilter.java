package com.fn.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.fn.reggie.common.BaseContext;
import com.fn.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已登录
 */
@WebFilter("/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符匹配
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //1.获取本次请求URI(URL：包括域名的全路径，URI：不含域名）
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        log.info("拦截到请求： " + requestURI + "准备对该请求进行判断...");

        //uris定义不需要处理的请求
        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/login",
                "/user/sendMsg"
//                "/common/**"
        };

        //2. 判断请求是否需要处理
        boolean check = checkURI(uris, requestURI);
        if (check) {
            //放行
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //3.1 判断后台端登录状态，如果已登录，直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，id为{}", request.getSession().getAttribute("employee"));

            //存储当前用户id到ThreadLocal
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        //3.2 判断移动端登录状态，如果已登录，直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，id为{}", request.getSession().getAttribute("user"));

            //存储当前用户id到ThreadLocal
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        //4. 如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        log.info("用户未登录，准备跳转到登录页面...");
        return;
    }

    /**
     * 判断uri是否需要处理
     *
     * @param uris
     * @param requestURI
     * @return
     */
    private boolean checkURI(String[] uris, String requestURI) {
        for (String uri : uris) {
            //判断不需要处理时，返回true
            if (PATH_MATCHER.match(uri, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
