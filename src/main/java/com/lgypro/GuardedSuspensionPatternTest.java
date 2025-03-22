package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class GuardedSuspensionPatternTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        GuardedObject<String> guardedObject = new GuardedObject<>(() -> {
            log.debug("start");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // throw new IllegalArgumentException();
            log.debug("end");
        }, "Hello World!");
        new Thread(guardedObject).start();
        log.debug("start");
        log.debug(guardedObject.get());
    }

    private interface SimpleFuture<V> {
        V get() throws ExecutionException, InterruptedException;

        V get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException, InterruptedException;
    }

    private static class GuardedObject<V> implements Runnable, SimpleFuture<V> {
        // Result stored by this
        private Object outcome;
        private Callable<V> callable;
        private Thread runner;

        private Boolean isSuccess;

        public GuardedObject(Callable<V> callable) {
            if (callable == null) throw new NullPointerException();
            this.callable = callable;
        }

        public GuardedObject(Runnable runnable) {
            this(runnable, null);
        }

        public GuardedObject(Runnable runnable, V outcome) {
            if (runnable == null) throw new NullPointerException();
            this.callable = Executors.callable(runnable, outcome);
        }

        // Getter
        @Override
        public V get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException, InterruptedException {
            synchronized (this) {
                // Start time
                long begin = System.currentTimeMillis();
                // Time passed
                long passedTime = 0;
                // Wait until timeout or notify
                while (isSuccess == null) {
                    if (timeout == 0) {
                        wait();
                    } else {
                        // Time spent waiting
                        long waitTime = unit.toMillis(timeout) - passedTime;
                        if (waitTime <= 0) {
                            throw new TimeoutException("Timeout after " + timeout + " " + unit);
                        }
                        wait(waitTime);
                        // Update time spent waiting
                        passedTime = System.currentTimeMillis() - begin;
                    }
                }
                return report();
            }
        }

        // Setter
        private void complete() {
            synchronized (this) {
                // Notify all waiting threads
                notifyAll();
            }
        }

        @Override
        public void run() {
            if (isSuccess == null && runner == null) {
                try {
                    runner = Thread.currentThread();
                    outcome = callable.call();
                    isSuccess = true;
                } catch (Exception e) {
                    outcome = new ExecutionException(e);
                    isSuccess = false;
                } finally {
                    runner = null;
                    complete();
                }
            }
        }

        @Override
        public V get() throws ExecutionException, InterruptedException {
            try {
                return get(0, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // cannot happen
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private V report() throws ExecutionException {
            if (isSuccess && !(outcome instanceof ExecutionException)) {
                return (V) outcome;
            } else {
                throw (ExecutionException) outcome;
            }
        }
    }
}
