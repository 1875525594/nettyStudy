package org.Future_Promise4;

import java.util.concurrent.*;

public class JdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "JdkFuture");
            }
        };
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10,10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), factory);

        // 获得Future对象
        Future<Integer> future = executor.submit(//submit()非阻塞
                new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                System.out.printf("====1"+ Thread.currentThread().getName()+"\n");
                TimeUnit.SECONDS.sleep(1);
                System.out.printf("====2"+ Thread.currentThread().getName()+"\n");
                return 50;//把50赋给future
            }
        });

        System.out.printf("====3"+ Thread.currentThread().getName()+"\n");
        // 通过阻塞的方式，获得运行结果
        System.out.println(future.get());
    }
}
