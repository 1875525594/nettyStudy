package org.NioEventLoopGroup2.cooperation;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.net.InetSocketAddress;
/**
 * 测试1：
 *      客户端开启3次debug发送分别发送数据111，222，333（即：启动3个客户端实例）
 *      服务端接受结果：
 *           nioEventLoopGroup-3-1 111
 *           nioEventLoopGroup-3-2 222
 *           nioEventLoopGroup-3-1 333
 *      即，一个线程可以管理多个客户端实例。
 * 测试2：
 *      开启5个客户端。
 *      服务端接受结果：
 *          nioEventLoopGroup-4-1 111
 *          defaultEventLoopGroup-2-1 111
 *          nioEventLoopGroup-4-2 222
 *          defaultEventLoopGroup-2-2 222
 *          nioEventLoopGroup-4-1 333
 *          defaultEventLoopGroup-2-3 333
 *          nioEventLoopGroup-4-2 444
 *          defaultEventLoopGroup-2-4 444
 *          nioEventLoopGroup-4-1 555
 *          defaultEventLoopGroup-2-5 555
 * */
public class HelloClient {
    public static void main(String[] args) throws IOException, InterruptedException {

        Channel channel = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new StringEncoder());
                        System.out.printf("===="+ Thread.currentThread().getName());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8088))
                .sync()
                .channel();
        System.out.println(Thread.currentThread().getName());
        System.out.println("channel");
        // 此处打断点调试，调用 channel.writeAndFlush(...);
        //System.in.read();
    }
}
