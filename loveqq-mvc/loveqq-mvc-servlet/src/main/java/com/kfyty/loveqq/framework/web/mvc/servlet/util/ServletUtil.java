package com.kfyty.loveqq.framework.web.mvc.servlet.util;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.multipart.DefaultMultipartFile;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述: servlet 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/11 16:07
 * @since JDK 1.8
 */
public class ServletUtil {
    /**
     * 解析文件上传
     *
     * @param request servet request
     * @return 上传文件列表
     */
    public static List<MultipartFile> from(HttpServletRequest request) {
        try {
            List<MultipartFile> multipartFiles = new ArrayList<>();
            if (CommonUtil.notEmpty(request.getParts())) {
                for (Part part : request.getParts()) {
                    Lazy<InputStream> inputStream = new Lazy<>(() -> {
                        try {
                            return part.getInputStream();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    multipartFiles.add(new DefaultMultipartFile(part.getName(), part.getSubmittedFileName(), part.getContentType(), part.getSubmittedFileName() != null, part.getSize(), inputStream));
                }
            }
            return multipartFiles;
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
