package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class YieldTest {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            int count = 0;
            while (true) {
                log.info("----1----" + count++);
            }
        });
        Thread t2 = new Thread(() -> {
            int count = 0;
            while (true) {
                log.info("               ----2----" + count++);
            }
        });
        t1.setPriority(Thread.MIN_PRIORITY);
        t2.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        t2.start();
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }
}
