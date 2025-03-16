package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Join2Threads {
    private static volatile int r1 = 0;
    private static volatile int r2 = 0;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("start");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            r1 = 10;
            log.debug("end");
        });
        Thread t2 = new Thread(() -> {
            log.debug("start");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            r2 = 20;
            log.debug("end");
        });
        long start = System.currentTimeMillis();
        t1.start();
        t2.start();
        log.debug("join t1");
        try {
            t1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("join t2");
        try {
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("r1: {}, r2: {}, cost: {}", r1, r2, (System.currentTimeMillis() - start));
    }
}
