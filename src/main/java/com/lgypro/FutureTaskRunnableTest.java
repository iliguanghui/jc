package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@Slf4j
public class FutureTaskRunnableTest {
    public static void main(String[] args) throws Exception {
        // Create a Runnable task
        Runnable runnable = () -> log.debug("Runnable task executed");

        // Wrap it in a FutureTask with a predefined result
        FutureTask<String> futureTask = new FutureTask<>(runnable, "Done");

        // Submit to an ExecutorService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(futureTask);

        // Get the result
        String result = futureTask.get();
        log.debug("Result: " + result);

        executor.shutdown();
    }
}
