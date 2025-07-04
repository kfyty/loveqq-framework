package com.kfyty.loveqq.framework.web.core.request.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: 输出流范围，默认单位：字节
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
@Getter
@ToString
@EqualsAndHashCode
public class AcceptRange {
    /**
     * 请求起始位置
     */
    private final long pos;

    /**
     * 请求结束位置
     */
    private final long last;

    /**
     * 请求长度
     */
    private final long length;

    public AcceptRange(long pos, long last) {
        this.pos = pos;
        this.last = last;
        this.length = last - pos + 1;
    }

    /**
     * 解析范围
     *
     * @param range  请求头范围，eg: 0-1000,0-,-100 (-100 表示倒数 100 个字节)
     * @param length 请求资源的总大小
     * @return 解析后的范围
     */
    public static List<AcceptRange> resolve(String range, long length) {
        if (range == null || range.isEmpty()) {
            return new LinkedList<>();
        }
        if (!range.startsWith("bytes=")) {
            throw new IllegalArgumentException("Range unit must be 'bytes'");
        }
        List<AcceptRange> result = new LinkedList<>();
        String[] ranges = Arrays.stream(range.substring(6).split(",")).map(String::trim).toArray(String[]::new);
        for (String s : ranges) {
            if (s.charAt(0) == '-') {
                long num = Long.parseLong(s.substring(1));
                result.add(new AcceptRange(length - num, length - 1));
                continue;
            }
            String[] split = s.split("-");
            if (split.length == 1 || split[1].isEmpty()) {
                result.add(new AcceptRange(Long.parseLong(split[0]), length - 1));
            } else {
                result.add(new AcceptRange(Long.parseLong(split[0]), Long.parseLong(split[1])));
            }
        }
        return result.stream().sorted(Comparator.comparing(AcceptRange::getPos)).collect(Collectors.toList());
    }
}
