package com.google.common.cache;

import com.google.common.base.Stopwatch;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: hzwangzhichao1@corp.netease.com
 * @Date: 2018/1/4
 */
public class LoadingCacheRefreshTest {

    // 模拟一个需要耗时2s的数据库查询任务
    private static Callable<String> callable = new Callable<String>() {
        @Override
        public String call() throws Exception {
            System.out.println("begin to mock query db...");
            Thread.sleep(2000);
            System.out.println("success to mock query db...");
            return UUID.randomUUID().toString();
        }
    };


    // 1s后刷新缓存
    private static LoadingCache<String, String> cache = CacheBuilder.newBuilder().refreshAfterWrite(1, TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
//                @Override
//                public String load(String key) throws Exception {
//                    return callable.call();
//                }

                @Override
                public String load(String key) throws Exception {
                    System.out.println("begin to query db...");
                    Thread.sleep(2000);
                    System.out.println("success to query db...");
                    return UUID.randomUUID().toString();
                }
            });

    private static CountDownLatch latch = new CountDownLatch(1);


    public static void main(String[] args) throws Exception {

        // 手动添加一条缓存数据,睡眠1.5s让其过期
        cache.put("name", "timma");
        Thread.sleep(1500);

        for (int i = 0; i < 8; i++) {
            startThread(i);
        }

        // 让线程运行
        latch.countDown();

    }

    private static void startThread(int id) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(Thread.currentThread().getName() + "...begin");
                    latch.await();
                    Stopwatch watch = Stopwatch.createStarted();
                    System.out.println(Thread.currentThread().getName() + "...value..." + cache.get("name"));
                    watch.stop();
                    System.out.println(Thread.currentThread().getName() + "...finish,cost time=" + watch.elapsed(TimeUnit.SECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.setName("Thread-" + id);
        t.start();
    }
}
