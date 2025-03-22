package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class WaitNotifyTest {
    public static void main(String[] args) {
        Object lock = new Object();
        Runnable runnable = () -> {
            synchronized (lock) {
                log.debug("start");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("I'm fine");
                while(true) {
                    // do nothing
                }
            }
        };
        Thread t1 = new Thread(runnable, "t1");
        Thread t2 = new Thread(runnable, "t2");
        t1.start();
        t2.start();
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("State of t1: {}, state of t2: {}", t1.getState(), t2.getState());
        synchronized (lock) {
            lock.notifyAll();
        }
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("After notify, state of t1: {}, state of t2: {}", t1.getState(), t2.getState());
    }
}
