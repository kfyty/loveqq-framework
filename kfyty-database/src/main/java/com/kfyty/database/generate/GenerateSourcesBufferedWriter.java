package com.kfyty.database.generate;

import com.kfyty.support.utils.CommonUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 功能描述: 生成资源的缓冲输出流
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/4 9:36
 * @since JDK 1.8
 */
public class GenerateSourcesBufferedWriter extends BufferedWriter {

    public GenerateSourcesBufferedWriter(Writer out) {
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
