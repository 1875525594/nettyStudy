package org.channelFuture3.close;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class MyClientAddListener {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 创建EventLoopGroup，使用完毕后关闭
        NioEventLoopGroup group = new NioEventLoopGroup();

        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
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
        Scanner scanner = new Scanner(System.in);

        // 创建一个线程用于输入并向服务器发送
        new Thread(()->{
            while (true) {
                String msg = scanner.next();
                if ("q".equals(msg)) {
                    // 关闭操作是也是异步的，真正的关闭操作并不是在调用该方法的线程中执行的，而是NIO线程中执行真正的关闭操作
                    // 即：是NIO线程中执行close()
                    channel.close();
                    break;
                }
                channel.writeAndFlush(msg);
            }
        }, "inputThread").start();

        // 获得closeFuture对象
        ChannelFuture closeFuture = channel.closeFuture();
        System.out.println("waiting close...");

        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                // 等待channel关闭后才执行的操作，还是NIO线程中执行，因为是NIO线程调用的close()
                System.out.println("关闭之后执行一些额外操作...");
                // 关闭EventLoopGroup
                group.shutdownGracefully();
            }
        });

        // 关闭EventLoopGroup
        group.shutdownGracefully();
    }


}
