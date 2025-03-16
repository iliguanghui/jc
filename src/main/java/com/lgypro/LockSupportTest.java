package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class LockSupportTest {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            log.debug("park");
            LockSupport.park();
            log.debug("unpark");
            log.debug("interrupt status: {}", Thread.currentThread().isInterrupted());
        });
        t.start();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("thread state: {}", t.getState());
        t.interrupt();
    }
}
