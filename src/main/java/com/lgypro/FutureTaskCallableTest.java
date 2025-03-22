package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FutureTaskCallableTest {
    public static void main(String[] args) throws Exception {
        // Create a Callable task
        Callable<Integer> callable = () -> {
            log.debug("running");
            TimeUnit.MILLISECONDS.sleep(2000); // Simulate work
            return 42;
        };

        // Wrap it in a FutureTask
        FutureTask<Integer> futureTask = new FutureTask<>(callable);

        // Run it in a separate thread
        Thread thread = new Thread(futureTask);
        thread.start();

        // Do other work while the task runs
        log.debug("Task is running...");

        // Get the result (blocks until complete)
        Integer result = futureTask.get();
        log.debug("Result: " + result);
    }
}
