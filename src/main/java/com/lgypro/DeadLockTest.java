package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class DeadLockTest {
    public static void main(String[] args) {
        Object lockA = new Object();
        Object lockB = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                log.info("Thread 1 acquired lockA");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (lockB) {
                    log.info("Thread 1 acquired lockB");
                }
            }
        }, "t1");
        t1.start();
        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                log.info("Thread 2 acquired lockB");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (lockA) {
                    log.info("Thread 2 acquired lockA");
                }
            }
        }, "t2");
        t2.start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("t1 state: {}, t2 state: {}", t1.getState(), t2.getState());
    }
}
