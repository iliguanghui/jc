package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadJoinTest {
    static volatile int r = 0;

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        log.debug("start");
        Thread t = new Thread(() -> {
            log.debug("start");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("end");
            r = 10;
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("result = {}", r);
        log.debug("end");
    }
}
