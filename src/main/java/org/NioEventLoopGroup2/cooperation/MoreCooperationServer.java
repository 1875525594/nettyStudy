package org.NioEventLoopGroup2.cooperation;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.StandardCharsets;
/**此时：
 * NioEventLoopGroup有2个
 * 自定义DefaultEventLoopGroup有无数个
 *
 * 开启5个客户端。
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
 *
 * */
public class MoreCooperationServer {
    public static void main(String[] args) {
        // 增加自定义的非NioEventLoopGroup
        EventLoopGroup group = new DefaultEventLoopGroup();
        System.out.printf("=====+++=====");
        new ServerBootstrap()
                .group(new NioEventLoopGroup(1), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 增加两个handler，第一个使用NioEventLoopGroup处理，第二个使用自定义EventLoopGroup处理
                        socketChannel.pipeline()
                                .addLast("nioHandler",new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        System.out.println(Thread.currentThread().getName() + " " + buf.toString(StandardCharsets.UTF_8));
                                        // 调用下一个handler
                                        ctx.fireChannelRead(msg);
                                    }
                                })
                                // 由于ctx.fireChannelRead(msg)会调用下一个handler;
                                // 上面的nioHandler指派任务给该myHandler处理。
                                .addLast(group, "myHandler", new ChannelInboundHandlerAdapter() {
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
