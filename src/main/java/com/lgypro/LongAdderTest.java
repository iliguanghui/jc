package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class LongAdderTest {
    private static final int taskNumber = 100;
    private static final long iterations = 1L << 20;

    public static void main(String[] args) {
        test1();
        log.debug("====================================");
        test2(); // faster
        log.debug("====================================");
        test3();
    }

    public static void test1() {
        AtomicLong counter = new AtomicLong();
        ArrayList<FutureTask<Long>> taskList = new ArrayList<>();
        for (int i = 0; i < taskNumber; i++) {
            taskList.add(new FutureTask<>(() -> {
                long start = System.currentTimeMillis();
                for (long j = 0; j < iterations; j++) {
                    counter.incrementAndGet();
                }
                log.info("take {} ms", (System.currentTimeMillis() - start));
            }, null));
        }
        execute(taskList);
        log.debug("finally, result is {}", counter.get());
    }

    public static void test2() {
        LongAdder adder = new LongAdder();
        ArrayList<FutureTask<Long>> taskList = new ArrayList<>();
        for (int i = 0; i < taskNumber; i++) {
            taskList.add(new FutureTask<>(() -> {
                long start = System.currentTimeMillis();
                for (long j = 0; j < iterations; j++) {
                    adder.increment();
                }
                log.info("take {} ms", (System.currentTimeMillis() - start));
            }, null));
        }
        execute(taskList);
        log.debug("finally, result is {}", adder.sum());
    }

    public static void test3() {
        CustomAdder customAdder = new CustomAdder();
        ArrayList<FutureTask<Long>> taskList = new ArrayList<>();
        for (int i = 0; i < taskNumber; i++) {
            taskList.add(new FutureTask<>(() -> {
                long start = System.currentTimeMillis();
                for (long j = 0; j < iterations; j++) {
                    customAdder.increment();
                }
                log.info("take {} ms", (System.currentTimeMillis() - start));
            }, null));
        }
        execute(taskList);
        log.debug("finally, result is {}", customAdder.get());
    }


    private static void execute(ArrayList<FutureTask<Long>> taskList) {
        ArrayList<Thread> threadList = new ArrayList<>();
        for (FutureTask<Long> longFutureTask : taskList) {
            threadList.add(new Thread(longFutureTask));
        }
        log.debug("start");
        long start = System.currentTimeMillis();
        threadList.forEach(Thread::start);
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        log.debug("end, total cost is {} ms", (System.currentTimeMillis() - start));
    }

    private static class CustomAdder {

        private long counter = 0;

        public synchronized void increment() {
            counter++;
        }

        public synchronized long get() {
            return counter;
        }
    }
}
