package com.lgypro;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FalseSharingTest {
    /*
    https://www.baeldung.com/java-false-sharing-contended
     */
    public static class Counter {
        public long count1 = 0;
        // public long p1, p2, p3, p4, p5, p6, p7; // faster
        public long count2 = 0;
    }

    public static void main(String[] args) {
        Counter counter = new Counter();
        long iterations = 1L << 35;
        Thread thread1 = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            for (long i = 0; i < iterations; i++) {
                counter.count1++;
            }
            long endTime = System.currentTimeMillis();
            log.debug("total time: {} ms", endTime - startTime);
        });
        Thread thread2 = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            for (long i = 0; i < iterations; i++) {
                counter.count2++;
            }
            long endTime = System.currentTimeMillis();
            log.debug("total time: {} ms", endTime - startTime);
        });
        thread1.start();
        thread2.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("finally, count1 = {}, count2 = {}", counter.count1, counter.count2);
    }
}
