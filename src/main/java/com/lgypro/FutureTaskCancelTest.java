package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FutureTaskCancelTest {
    public static void main(String[] args) throws Exception {
        Callable<String> callable = () -> {
            log.debug("sleeping");
            TimeUnit.MILLISECONDS.sleep(5000); // Long-running task
            log.debug("awake");
            return "Task completed";
        };

        FutureTask<String> futureTask = new FutureTask<>(callable);
        Thread thread = new Thread(futureTask);
        thread.start();

        // Cancel after 1 second
        TimeUnit.SECONDS.sleep(1);
        futureTask.cancel(true); // Interrupt the running task

        try {
            System.out.println("Result: " + futureTask.get());
        } catch (CancellationException e) {
            log.debug("Task was cancelled");
        }
    }
}
