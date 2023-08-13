package org.Future_Promise4;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        // 获得 EventLoop 对象.
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.printf("====1"+ Thread.currentThread().getName()+"\n");
                TimeUnit.SECONDS.sleep(1);
                System.out.printf("====2"+ Thread.currentThread().getName()+"\n");
                return 50;
            }
        });

        // 主线程中获取结果
        System.out.println(Thread.currentThread().getName() + " 获取结果1");
        System.out.println("getNow1 " + future.getNow());//getNow()非阻塞地获取结果，若还没有结果，则返回null
        System.out.println("get " + future.get());//get方法，阻塞地获取返回结果

        // NIO线程中异步获取结果，由执行call()方法的那个线程调用operationComplete()
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                System.out.println(Thread.currentThread().getName() + " 获取结果2");
                System.out.println("getNow2 " + future.getNow());
            }
        });
    }
}
