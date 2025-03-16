package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadInterruptionTest {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            log.debug("enter sleep");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.debug("wake up", e);
            }
        });
        t1.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("interrupt");
        t1.interrupt();
    }
}
