package com.kfyty.support.io;

import com.kfyty.support.utils.CommonUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 功能描述: 简单的缓冲输出流
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/4 9:36
 * @since JDK 1.8
 */
public class SimpleBufferedWriter extends BufferedWriter {

    public SimpleBufferedWriter(Writer out) {
        super(out);
    }

    public void write(String str, Object ... params) throws IOException {
        super.write(CommonUtil.format(str, params));
    }

    public void writeLine(String str) throws IOException {
        super.write(str);
        super.newLine();
    }

    public void writeLine(String str, Object ... params) throws IOException {
        write(str, params);
        super.newLine();
    }
}
