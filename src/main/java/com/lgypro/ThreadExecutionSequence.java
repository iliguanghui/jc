package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ThreadExecutionSequence {
    private static volatile boolean t2Run = false;
    private static volatile int flag = 1;
    private static final Random RANDOM = new Random();
    private static final int loopNumber = 10;

    public static void main(String[] args) {
        // testWaitNotify();
        // testAwaitSignal();
        // testParkUnpark();
        // testAlternateOutputWaitNotify();
        testAlternateOutputAwaitSignal();
        // testAlternateOutputParkUnpark();
    }

    public static void testWaitNotify() {
        final Object lock = new Object();
        new Thread(() -> {
            synchronized (lock) {
                while (!t2Run) {
                    try {
                        log.debug("waiting");
                        lock.wait();
                        log.debug("I'm ok");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                log.debug("running");
            }

        }, "t1").start();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            synchronized (lock) {
                log.debug("running");
                t2Run = true;
                lock.notify();
            }
        }, "t2").start();
    }

    public static void testAwaitSignal() {
        final ReentrantLock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        new Thread(() -> {
            lock.lock();
            try {
                while (!t2Run) {
                    log.debug("waiting");
                    condition.await();
                    log.debug("I'm ok");
                }
                log.debug("running");
            } catch (InterruptedException e) {
                log.debug(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }, "t1").start();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            lock.lock();
            try {
                log.debug("running");
                t2Run = true;
                condition.signal();
            } finally {
                lock.unlock();
            }
        }, "t2").start();
    }

    public static void testParkUnpark() {
        Thread t1 = new Thread(() -> {
            while (!t2Run) {
                log.debug("waiting");
                LockSupport.park();
                log.debug("I'm ok");
            }
            log.debug("running");
        }, "t1");
        t1.start();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            log.debug("running");
            t2Run = true;
            LockSupport.unpark(t1);
        }, "t2").start();
    }

    public static void testAlternateOutputWaitNotify() {
        final Object lock12 = new Object();
        final Object lock23 = new Object();
        final Object lock31 = new Object();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                synchronized (lock31) {
                    while (flag != 1) {
                        try {
                            lock31.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    log.info("current value is {}", i);
                    try {
                        TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    flag = 2;
                    synchronized (lock12) {
                        lock12.notify();
                    }
                }
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                synchronized (lock12) {
                    while (flag != 2) {
                        try {
                            lock12.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    log.info("current value is {}", i);
                    try {
                        TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    flag = 3;
                    synchronized (lock23) {
                        lock23.notify();
                    }
                }
            }
        }, "t2");
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                synchronized (lock23) {
                    while (flag != 3) {
                        try {
                            lock23.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    log.info("current value is {}", i);
                    try {
                        TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    flag = 1;
                    synchronized (lock31) {
                        lock31.notify();
                    }
                }
            }
        }, "t3");
        t1.start();
        t2.start();
        t3.start();
    }

    public static void testAlternateOutputAwaitSignal() {
        final ReentrantLock lock = new ReentrantLock();
        Condition condition12 = lock.newCondition();
        Condition condition23 = lock.newCondition();
        Condition condition31 = lock.newCondition();
        new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                lock.lock();
                try {
                    while (flag != 1) {
                        condition31.await();
                    }
                    log.info("current value is {}", i);
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                    flag = 2;
                    condition12.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }, "t1").start();

        new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                lock.lock();
                try {
                    while (flag != 2) {
                        condition12.await();
                    }
                    log.info("current value is {}", i);
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                    flag = 3;
                    condition23.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }, "t2").start();

        new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                lock.lock();
                try {
                    while (flag != 3) {
                        condition23.await();
                    }
                    log.info("current value is {}", i);
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                    flag = 1;
                    condition31.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }, "t3").start();
    }

    public static void testAlternateOutputParkUnpark() {
        List<Thread> threadList = new ArrayList<>(4);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                while (flag != 1) {
                    LockSupport.park();
                }
                log.info("current value is {}", i);
                try {
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                flag = 2;
                LockSupport.unpark(threadList.get(1));
            }
        }, "t1");
        threadList.add(t1);
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                while (flag != 2) {
                    LockSupport.park();
                }
                log.info("current value is {}", i);
                try {
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                flag = 3;
                LockSupport.unpark(threadList.get(2));
            }
        }, "t2");
        threadList.add(t2);
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                while (flag != 3) {
                    LockSupport.park();
                }
                log.info("current value is {}", i);
                try {
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(500));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                flag = 1;
                LockSupport.unpark(threadList.get(0));
            }
        }, "t3");
        threadList.add(t3);
        t1.start();
        t2.start();
        t3.start();
    }
}
