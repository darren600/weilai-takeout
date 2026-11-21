package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.channels.MulticastChannel;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/*
* 通用接口
* */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Tag(name = "通用接口")
public class CommonController {
    /*
    * 文件上传
    * @param file 文件
    * @return
    * */

//    注入阿里云
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<String> upload(MultipartFile file)  {
        log.info("文件上传:{}",file);

        try {
            //        原始文件名
            String originalFilename = file.getOriginalFilename();
//            获取原始文件的后缀，dssd.png
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构建新的文件名
            String objectName = UUID.randomUUID().toString() + extension;

//        文件的请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败:{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);

    }
}
