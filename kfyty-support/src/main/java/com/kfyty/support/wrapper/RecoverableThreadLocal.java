package com.kfyty.support.wrapper;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/6/18 11:24
 * @email kfyty725@hotmail.com
 */
public class RecoverableThreadLocal<T> extends ThreadLocal<T> {
    /**
     * 保存本地线程变量的入栈顺序
     */
    private static final ThreadLocal<Deque<Object>> LOCAL_STACK = ThreadLocal.withInitial(LinkedList::new);

    /**
     * 使用给定的初始值创建一个 RecoverableThreadLocal
     *
     * @param supplier 初始值提供者
     * @return 初始值
     */
    public static <S> RecoverableThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }

    /**
     * 获取本地栈，没有则新建
     *
     * @return 本地栈
     */
    public Deque<Object> getStack() {
        return LOCAL_STACK.get();
    }

    /**
     * 设置新的值，旧的值将入栈保存
     * 如果是空值，则包装为 {@link EmptyPrev} 避免占用过多的栈空间
     *
     * @param value the value to be stored in the current thread's copy of this thread-local.
     */
    @Override
    public void set(T value) {
        T prev = super.get();
        Deque<Object> stack = this.getStack();
        super.set(value);
        if (prev != null) {
            stack.push(prev);
            return;
        }
        Object last = stack.peek();
        if (last instanceof EmptyPrev) {
            ((EmptyPrev) last).increment();
            return;
        }
        stack.push(new EmptyPrev());
    }

    /**
     * 移除当前值，并将之前的值恢复到本地线程变量中
     */
    @Override
    @SuppressWarnings("unchecked")
    public void remove() {
        Deque<Object> stack = this.getStack();
        Object prev = stack.peek();
        if (!(prev instanceof EmptyPrev)) {
            stack.pop();
            super.set((T) prev);
            return;
        }
        int count = ((EmptyPrev) prev).decrement();
        if (count <= 0) {
            stack.pop();
        }
        super.remove();
    }

    /**
     * 空值包装
     */
    private static class EmptyPrev {
        /**
         * 空值数量
         */
        private int count;

        public EmptyPrev() {
            this.count = 1;
        }

        public int increment() {
            return ++this.count;
        }

        public int decrement() {
            return --this.count;
        }
    }

    /**
     * 具有初始值的 RecoverableThreadLocal
     */
    private static final class SuppliedThreadLocal<T> extends RecoverableThreadLocal<T> {
        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return this.supplier.get();
        }
    }
}
