package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NestedMonitorLockoutTest {
    private interface Factory {
        void manage();

        void work();
    }

    private static class ProblematicFactory implements Factory {

        private final Object outerLock = new Object();
        private final Object innerLock = new Object();
        private volatile boolean condition = false;

        @Override
        public void manage() {
            synchronized (outerLock) {
                log.debug("{}: Holding outerLock", Thread.currentThread().getName());
                synchronized (innerLock) {
                    log.debug("{}: Holding innerLock", Thread.currentThread().getName());
                    while (!condition) {
                        try {
                            log.debug("{}: Waiting for condition", Thread.currentThread().getName());
                            innerLock.wait(); // Releases innerLock, keeps outerLock
                        } catch (InterruptedException e) {
                            log.debug(e.getMessage(), e);
                        }
                    }
                    log.debug("{}: Condition met, proceeding", Thread.currentThread().getName());
                }
            }
        }

        @Override
        public void work() {
            synchronized (outerLock) {
                log.debug("{}: Holding outerLock", Thread.currentThread().getName());
                synchronized (innerLock) {
                    log.debug("{}: Holding innerLock", Thread.currentThread().getName());
                    condition = true;
                    innerLock.notify(); // Wakes Thread 1
                    log.debug("{}: Notified", Thread.currentThread().getName());
                }
            }
        }
    }

    private static class AdvancedFactory implements Factory {

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition lockCondition = lock.newCondition();
        private volatile boolean condition = false;

        @Override
        public void manage() {
            lock.lock();
            log.debug("{}: Holding lock", Thread.currentThread().getName());
            try {
                while (!condition) {
                    try {
                        log.debug("{}: Waiting for condition", Thread.currentThread().getName());
                        lockCondition.await();
                    } catch (InterruptedException e) {
                        log.debug(e.getMessage(), e);
                    }
                }
                log.debug("{}: Condition met, proceeding", Thread.currentThread().getName());
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void work() {
            lock.lock();
            log.debug("{}: Holding lock", Thread.currentThread().getName());
            try {
                condition = true;
                lockCondition.signal(); // Wakes Thread 1
                log.debug("{}: Notified", Thread.currentThread().getName());
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        // testProblematicFactory();
        testAdvancedFactory();
    }

    public static void testProblematicFactory() {
        ProblematicFactory factory = new ProblematicFactory();
        testFactory(factory);
    }

    public static void testAdvancedFactory() {
        AdvancedFactory factory = new AdvancedFactory();
        testFactory(factory);
    }

    public static void testFactory(Factory factory) {
        Thread employer = new Thread(factory::manage, "employer");
        Thread worker = new Thread(factory::work, "worker");

        employer.start();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
        } // Let t1 run first
        worker.start();
    }
}
