package com.kfyty.support.utils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 描述: io 工具
 *
 * @author kfyty725
 * @date 2022/7/2 11:13
 * @email kfyty725@hotmail.com
 */
public abstract class IOUtil {

    public static void copy(InputStream in, OutputStream out) {
        copy(in, out, 1024);
    }

    public static void copy(InputStream in, OutputStream out, int limit) {
        try {
            int n = -1;
            byte[] bytes = new byte[limit];
            while ((n = in.read(bytes)) != -1) {
                out.write(bytes, 0, n);
            }
            out.flush();
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
