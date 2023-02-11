package com.fn.reggie.controller;

import com.fn.reggie.common.BusinessException;
import com.fn.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upLoad(MultipartFile file) {
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除


        //1. 获取原始文件名，以及文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //2. 使用UUID生成文件名，防止文件名重复导致的文件被覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        //3. 创建目录对象，判断该目录是否存在
        File dir = new File(basePath);
        if (!dir.exists()) {
            //目录不存在就创建
            dir.mkdirs();
        }

        //4. 将临时文件转存到指定位置
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //返回文件名，方便图片通过文件名存入数据库
        return Result.success(fileName);
    }

    /**
     * 文件下载，通过response输出流传给前端展示
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void downLocal(String name, HttpServletResponse response) {
        try {
            //1. 通过name读取本地文件
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(basePath + name));

            //2. 获取输出流
            ServletOutputStream outputStream = response.getOutputStream();
            //设置返回类型为图片
            response.setContentType("image/*");

            //3. 通过IOUtils工具实现输入输出流对拷
            IOUtils.copy(bis, outputStream);

            outputStream.close();
            bis.close();
        } catch (IOException e) {
//            e.printStackTrace();
            throw new BusinessException("文件下载失败，请稍后重试");
        }
    }
}
