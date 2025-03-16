package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FutureTaskTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Long> task = new FutureTask<>(() -> {
            log.info("running");
            long sum = 0L;
            for (int i = 0; i < 10000 * 10000; i++) {
                sum += i;
            }
            TimeUnit.SECONDS.sleep(3);
            return sum;
        });
        new Thread(task).start();
        log.info("waiting");
        log.info("result is " + task.get());
    }
}
