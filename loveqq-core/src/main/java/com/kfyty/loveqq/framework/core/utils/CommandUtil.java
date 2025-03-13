package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 描述: 命令工具
 *
 * @author kfyty725
 * @date 2021/6/17 10:30
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class CommandUtil {

    public static CommandResult exec(Predicate<String> lineTest, List<String> commands) {
        return exec(lineTest, commands.toArray(CommonUtil.EMPTY_STRING_ARRAY));
    }

    public static CommandResult exec(Predicate<String> lineTest, String... commands) {
        CommandResult result = new CommandResult();
        execute(line -> {
            result.addLine(line);
            if (lineTest.test(line)) {
                result.set(true);
            }
        }, commands);
        return result;
    }

    public static <T> List<T> execute(Function<String, T> mapping, List<String> commands) {
        return execute(mapping, commands.toArray(CommonUtil.EMPTY_STRING_ARRAY));
    }

    public static <T> List<T> execute(Function<String, T> mapping, String... commands) {
        List<T> list = new LinkedList<>();
        execute(e -> {
            T applied = mapping.apply(e);
            if (applied != null) {
                list.add(applied);
            }
        }, commands);
        return list;
    }

    public static void execute(Consumer<String> lineConsumer, List<String> commands) {
        execute(lineConsumer, null, commands);
    }

    public static void execute(Consumer<String> lineConsumer, String env, List<String> commands) {
        execute(lineConsumer, env, Charset.defaultCharset().name(), commands);
    }

    public static void execute(Consumer<String> lineConsumer, String env, String charset, List<String> commands) {
        execute(lineConsumer, env, charset, commands.toArray(CommonUtil.EMPTY_STRING_ARRAY));
    }

    public static void execute(Consumer<String> lineConsumer, String... commands) {
        execute(lineConsumer, null, commands);
    }

    public static void execute(Consumer<String> lineConsumer, String env, String... commands) {
        execute(lineConsumer, env, Charset.defaultCharset().name(), commands);
    }

    public static void execute(Consumer<String> lineConsumer, String env, String charset, String... commands) {
        ProcessBuilder commandBuilder = createProcessBuilder(env, commands);
        try {
            String line;
            Process process = commandBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    lineConsumer.accept(line);
                    log.info(line);
                }
            }
        } catch (IOException e) {
            throw new ResolvableException("command execute failed: " + String.join(" ", commandBuilder.command()) + ", error: " + e.getMessage(), e);
        }
    }

    public static ProcessBuilder createProcessBuilder(String env, List<String> commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        if (env != null) {
            builder.directory(new File(env));
        }
        return builder;
    }

    public static ProcessBuilder createProcessBuilder(String env, String... commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        if (env != null) {
            builder.directory(new File(env));
        }
        return builder;
    }

    @Getter
    @AllArgsConstructor
    public static final class CommandResult {
        private boolean success;
        private List<String> lines;

        public CommandResult() {
            this(false);
        }

        public CommandResult(boolean success) {
            this.success = success;
            this.lines = new LinkedList<>();
        }

        public void set(boolean success) {
            this.success = success;
        }

        public void addLine(String line) {
            this.lines.add(line);
        }

        @Override
        public String toString() {
            return (this.success ? "succeed" : "failed") + String.join("\n", this.lines);
        }
    }
}
