package com.kfyty.loveqq.framework.core.thread;

/**
 * 描述: 上下文刷新线程
 *
 * @author kfyty725
 * @date 2021/7/3 11:05
 * @email kfyty725@hotmail.com
 */
public class ContextRefreshThread extends Thread {

    public ContextRefreshThread() {
    }

    public ContextRefreshThread(Runnable task) {
        super(task);
        setName("ContextRefreshThread");
    }

    public ContextRefreshThread(ThreadGroup group, Runnable task) {
        super(group, task);
        setName("ContextRefreshThread");
    }

    public ContextRefreshThread(String name) {
        super(name);
    }

    public ContextRefreshThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public ContextRefreshThread(Runnable task, String name) {
        super(task, name);
    }

    public ContextRefreshThread(ThreadGroup group, Runnable task, String name) {
        super(group, task, name);
    }

    public ContextRefreshThread(ThreadGroup group, Runnable task, String name, long stackSize) {
        super(group, task, name, stackSize);
    }

    public ContextRefreshThread(ThreadGroup group, Runnable task, String name, long stackSize, boolean inheritInheritableThreadLocals) {
        super(group, task, name, stackSize, inheritInheritableThreadLocals);
    }
}
