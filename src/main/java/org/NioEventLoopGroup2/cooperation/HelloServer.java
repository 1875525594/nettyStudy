package org.NioEventLoopGroup2.cooperation;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.StandardCharsets;
/**
 * 客户端开启3次debug发送分别发送数据111，222，333（即：启动3个客户端实例）
 * 服务端接受结果：
 *      nioEventLoopGroup-3-1 111
 *      nioEventLoopGroup-3-2 222
 *      nioEventLoopGroup-3-1 333
 * 即，一个线程可以管理多个客户端实例。
 * */
public class HelloServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                // 两个Group，分别为Boss，Worker ，Boss负责Accept事件，Worker 负责读写事件
                //分工合作，1个boss，2个worker，此时，可以为客户端分配2线程,一个线程可以管理多个客户端。
                .group(new NioEventLoopGroup(1), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println(Thread.currentThread().getName() + " " + buf.toString(StandardCharsets.UTF_8));

                            }
                        });
                    }
                })
                .bind(8088);
    }
}
