package com.google.common.cache;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 *
 * @Author: hzwangzhichao1@corp.netease.com
 * @Date: 2017/12/22
 */
public class testLocalCache {

    private static final int CONCURRENT_NUM = 10; //并发数

    private volatile static int value = 1;

    private static LoadingCache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .refreshAfterWrite(1, TimeUnit.SECONDS)
            .setKeyStrength(LocalCache.Strength.WEAK)
            .setValueStrength(LocalCache.Strength.WEAK)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) throws Exception {
                    System.out.println("load by" + Thread.currentThread().getName());
                    return createValue(key);
                }

                @Override
                public ListenableFuture<String> reload(String key, String oldValue) throws Exception {
                    System.out.println("reload by" + Thread.currentThread().getName());
                    return Futures.immediateFuture(createValue(key));
                }


            });

    //创建value
    private static String createValue(String key) throws InterruptedException{

        Thread.sleep(1000);
        return String.valueOf(value++);
    }

    public static void main(String []args) throws InterruptedException,ExecutionException {

        CyclicBarrier barrier = new CyclicBarrier(CONCURRENT_NUM);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_NUM);

        for (int i = 0 ; i < CONCURRENT_NUM ; i++) {
            final ClientRunnable clientRunnable = new ClientRunnable(barrier, latch);
            Thread thread = new Thread(clientRunnable, "client-"+i);
            thread.start();
        }

        latch.await();
        Thread.sleep(5100L);

        System. out.println( "\n超过expire时间未读之后...");
        System. out.println(Thread. currentThread().getName() + ",val:"+ cache .get("key"));

    }

    public static class ClientRunnable implements Runnable {
        CyclicBarrier cyclicBarrier;
        CountDownLatch countDownLatch;

        public ClientRunnable(CyclicBarrier cyclicBarrier, CountDownLatch countDownLatch) {
            this.cyclicBarrier = cyclicBarrier;
            this.countDownLatch = countDownLatch;
        }

        public void run() {
            try {
                cyclicBarrier.await();

                Thread.sleep((long)Math.random()*4000);
                System. out.println(Thread.currentThread().getName() + ",val:"+ cache.get("key"));

                countDownLatch.countDown();

            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }
}
