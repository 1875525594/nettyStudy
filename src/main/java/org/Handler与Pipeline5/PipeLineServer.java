package org.Handler与Pipeline5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
/**
 * 通过channel.pipeline().addLast(name, handler)添加handler时，
 * 记得给handler取名字。这样可以调用pipeline的addAfter、addBefore等方法更灵活地向pipeline中添加handler
 *
 * handler需要放入通道的pipeline中，才能根据放入顺序来使用handler
 *
 * pipeline是结构是一个带有head与tail指针的双向链表，其中的节点为handler
 * 要通过ctx.fireChannelRead(msg)等方法，将当前handler的处理结果传递给下一个handler
 *
 * 当有入站（Inbound）操作时，会从head开始向后调用handler，直到handler不是处理Inbound操作为止
 * 当有出站（Outbound）操作时，会从tail开始向前调用handler，直到handler不是处理Outbound操作为止
 * */
public class PipeLineServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 在socketChannel的pipeline中添加handler
                        // pipeline中handler是带有head与tail节点的双向链表，的实际结构为
                        // head <-> handler1 <-> ... <-> handler6 <->tail
                        // Inbound 主要处理入站操作，一般为读操作，发生入站操作时会触发Inbound方法
                        // 入站时，handler是从head向后调用的
                        socketChannel.pipeline().addLast("handler1" ,new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(Thread.currentThread().getName() + " Inbound handler 1"+"    :"+msg);
                                // 父类该方法内部会调用fireChannelRead
                                // 将数据传递给下一个handler

                                //处理数据成字符串
                                ByteBuf buf =(ByteBuf)msg;
                                String name = buf.toString(Charset.defaultCharset());


                                super.channelRead(ctx, name);
                            }
                        });
                        socketChannel.pipeline().addLast("handler2" ,new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(Thread.currentThread().getName() + " Inbound handler 2"+"    :"+msg);
                                // 父类该方法内部会调用fireChannelRead
                                // 将数据传递给下一个handler
                                Student student = new Student(msg.toString());
                                super.channelRead(ctx, student);
                            }
                        });
                        socketChannel.pipeline().addLast("handler3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(Thread.currentThread().getName() + " Inbound handler 3"+"    :"+msg);
                                // 执行write操作，使得Outbound的方法能够得到调用,否则不执行出栈
                                socketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("Server...".getBytes(StandardCharsets.UTF_8)));
                                //super.channelRead(ctx, msg);没必要加上，因为是入栈末尾，没有要唤醒的handler
                            }
                        });

                        // Outbound主要处理出站操作，一般为写操作，发生出站操作时会触发Outbound方法
                        // 出站时，handler的调用是从tail向前调用的
                        socketChannel.pipeline().addLast("handler4" ,new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                System.out.println(Thread.currentThread().getName() + " Outbound handler 4"+"   :"+msg);
                                super.write(ctx, msg, promise);
                            }
                        });
                        socketChannel.pipeline().addLast("handler5" ,new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                System.out.println(Thread.currentThread().getName() + " Outbound handler 5"+"   :"+msg);
                                super.write(ctx, msg, promise);
                            }
                        });
                        socketChannel.pipeline().addLast("handler6" ,new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                System.out.println(Thread.currentThread().getName() + " Outbound handler 6"+"   :"+msg);
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .bind(8088);
    }
}

@Data
@AllArgsConstructor
class Student {
    private String name;
}