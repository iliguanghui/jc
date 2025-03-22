package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class RunnableAndCallableTest {
    private static class MyRunnable implements Runnable {
        public void run() {
            log.info("Runnable running");
        }
    }

    private static class MyCallable implements Callable<String> {
        public String call() throws Exception {
            return "Callable completed";
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Runnable
        Future<?> runnableFuture = executor.submit(new MyRunnable());
        log.info("Runnable result: " + runnableFuture.get()); // null

        // Callable
        Future<String> callableFuture = executor.submit(new MyCallable());
        log.info("Callable result: " + callableFuture.get()); // "Callable completed"

        executor.shutdown();
    }
}