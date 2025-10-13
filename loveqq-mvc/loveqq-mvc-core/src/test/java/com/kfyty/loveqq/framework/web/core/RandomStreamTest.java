package com.kfyty.loveqq.framework.web.core;

import com.kfyty.loveqq.framework.web.core.request.support.InputStreamRandomAccessStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/9/22 20:12
 * @email kfyty725@hotmail.com
 */
public class RandomStreamTest {

    @Test
    public void test() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes("123456".getBytes(StandardCharsets.UTF_8));
        InputStreamRandomAccessStream stream = new InputStreamRandomAccessStream(new ByteArrayInputStream(out.toByteArray()));

        byte[] bytes = new byte[2];
        stream.read(bytes, 0, 2);
        Assertions.assertEquals(Character.toChars(bytes[0])[0], '1');
        Assertions.assertEquals(Character.toChars(bytes[1])[0], '2');
    }

    @Test
    public void test2() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes("123456".getBytes(StandardCharsets.UTF_8));
        InputStreamRandomAccessStream stream = new InputStreamRandomAccessStream(new ByteArrayInputStream(out.toByteArray()));

        byte[] bytes = new byte[2];

        stream.seek(1);
        stream.read(bytes, 0, 2);
        Assertions.assertEquals(Character.toChars(bytes[0])[0], '2');
        Assertions.assertEquals(Character.toChars(bytes[1])[0], '3');

        stream.seek(5);
        stream.read(bytes, 0, 1);
        Assertions.assertEquals(Character.toChars(bytes[0])[0], '6');
    }
}
