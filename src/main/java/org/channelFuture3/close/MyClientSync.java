package org.channelFuture3.close;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyClientSync {
    public static void main(String[] args) throws IOException, InterruptedException {
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                // connect方法为异步非阻塞方法，main线程调用后不会被阻塞，真正去执行连接操作的是NIO线程，即：new NioEventLoopGroup()
                // NIO线程：NioEventLoop 中的线程
                .connect(new InetSocketAddress("localhost", 8088));//connect连接需要一定时间。。。

        // 该方法用于等待连接真正建立,sync()方法用于阻塞main线程，等待nio线程连接完毕后会解锁，让所有线程往下执行。
        ChannelFuture sync = channelFuture.sync();

        // 获取客户端-服务器之间的Channel对象
        Channel channel = channelFuture.channel();
        channel.writeAndFlush("hello world");
        System.in.read();
    }
}
