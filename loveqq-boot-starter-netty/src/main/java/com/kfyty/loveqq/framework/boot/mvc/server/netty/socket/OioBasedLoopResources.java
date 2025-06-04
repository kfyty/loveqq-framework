package com.kfyty.loveqq.framework.boot.mvc.server.netty.socket;

import com.kfyty.loveqq.framework.core.thread.NamedThreadFactory;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ThreadPerChannelEventLoop;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioDatagramChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.util.concurrent.Future;
import reactor.core.publisher.Mono;
import reactor.netty.FutureMono;
import reactor.netty.ReactorNetty;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 描述: 传统 io + 虚拟线程
 *
 * @author kfyty725
 * @date 2024/9/13 20:20
 * @email kfyty725@hotmail.com
 */
public class OioBasedLoopResources implements LoopResources {
    /**
     * 接受连接线程数
     */
    private static final int IO_SELECT_COUNT = Integer.parseInt(System.getProperty(ReactorNetty.IO_SELECT_COUNT, "1"));

    /**
     * 是否运行中
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * 连接连接线程组
     */
    private volatile EventLoopGroup bossGroup;

    /**
     * 工作连接线程组
     */
    private volatile EventLoopGroup workGroup;

    @Override
    public EventLoopGroup onServer(boolean useNative) {
        return this.obtainWorkGroup();
    }

    @Override
    public EventLoopGroup onServerSelect(boolean useNative) {
        return this.obtainBossGroup();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <CHANNEL extends Channel> CHANNEL onChannel(Class<CHANNEL> channelType, EventLoopGroup group) {
        if (channelType.equals(DatagramChannel.class)) {
            return (CHANNEL) new OioDatagramChannel();
        }
        if (channelType.equals(SocketChannel.class)) {
            return (CHANNEL) new OioSocketChannel();
        }
        if (channelType.equals(ServerSocketChannel.class)) {
            return (CHANNEL) new OioServerSocketChannel();
        }
        throw new IllegalArgumentException("Unsupported channel type: " + channelType.getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <CHANNEL extends Channel> Class<? extends CHANNEL> onChannelClass(Class<CHANNEL> channelType, EventLoopGroup group) {
        if (channelType.equals(DatagramChannel.class)) {
            return (Class<? extends CHANNEL>) OioDatagramChannel.class;
        }
        if (channelType.equals(SocketChannel.class)) {
            return (Class<? extends CHANNEL>) OioSocketChannel.class;
        }
        if (channelType.equals(ServerSocketChannel.class)) {
            return (Class<? extends CHANNEL>) OioServerSocketChannel.class;
        }
        throw new IllegalArgumentException("Unsupported channel type: " + channelType.getSimpleName());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Mono<Void> disposeLater(Duration quietPeriod, Duration timeout) {
        if (this.running.compareAndSet(true, false)) {
            Mono<?> boss = Mono.empty();
            Mono<?> work = Mono.empty();
            if (this.bossGroup != null) {
                boss = FutureMono.from((Future) this.bossGroup.shutdownGracefully(quietPeriod.toMillis(), timeout.toMillis(), TimeUnit.MILLISECONDS));
            }
            if (this.workGroup != null) {
                work = FutureMono.from((Future) this.workGroup.shutdownGracefully(quietPeriod.toMillis(), timeout.toMillis(), TimeUnit.MILLISECONDS));
            }
            return Mono.when(boss, work);
        }
        return LoopResources.super.disposeLater(quietPeriod, timeout);
    }

    protected EventLoopGroup obtainBossGroup() {
        if (this.bossGroup == null) {
            synchronized (this) {
                if (this.bossGroup == null) {
                    this.bossGroup = new OioEventLoopGroup(0, Executors.newFixedThreadPool(IO_SELECT_COUNT, new NamedThreadFactory("reactor-select"))) {

                        @Override
                        public EventLoop next() {
                            return new ThreadPerChannelEventLoop(this);
                        }
                    };
                }
            }
        }
        return this.bossGroup;
    }

    protected EventLoopGroup obtainWorkGroup() {
        if (this.workGroup == null) {
            synchronized (this) {
                if (this.workGroup == null) {
                    this.workGroup = new OioEventLoopGroup(0, Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("reactor-handler-", 0).factory())) {

                        @Override
                        public EventLoop next() {
                            return new ThreadPerChannelEventLoop(this);
                        }
                    };
                }
            }
        }
        return this.workGroup;
    }
}
