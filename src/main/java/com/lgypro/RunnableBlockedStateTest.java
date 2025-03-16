package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RunnableBlockedStateTest {
    /**
     * In your current code, the volatile modifier on num is not required because:
     * t.join() provides a happens-before relationship that guarantees the main
     * thread sees the updated value of num.
     * There’s no concurrent access to num that requires additional visibility guarantees.
     * You could safely remove volatile here without changing the program’s behavior:
     */
    static volatile int num = 0;

    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            /*
             * The thread is in RUNNABLE during Scanner.nextInt() because
             * it’s executing native I/O code, even though it’s "blocked"
             * from a user perspective waiting for input. This reflects how
             * Java defines thread states: RUNNABLE encompasses both CPU
             * execution and native blocking calls, while WAITING is specific
             * to JVM-level waiting mechanisms. This distinction can be
             * confusing, but it’s consistent with Java’s threading model.
             */
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            num = new Scanner(System.in).nextInt();
            log.info("read number {}", num);
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("num: {}", num);
    }
}
