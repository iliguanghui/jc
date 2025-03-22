package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SpinLockTest {
    private static class SpinLock {
        private final AtomicBoolean lock = new AtomicBoolean(false);

        public void lock() {
            int spins = 1000;
            while (spins > 0) {
                if (lock.compareAndSet(false, true)) {
                    log.info("自旋成功");
                    return;
                }
                spins--;
                log.debug("自旋...");
                Thread.yield();
            }
            if (lock.get()) {
                log.debug("自旋失败");
                synchronized (this) {
                    while (!lock.compareAndSet(false, true)) {
                        log.debug("wait");
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }

        public void unlock() {
            lock.set(false);
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    private static class Counter {
        private int number = 0;
        private final SpinLock lock = new SpinLock();

        public void increment() {
            lock.lock();
            log.debug("acquire lock");
            try {
                number++;
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                log.debug("release lock");
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        Thread t1 = new Thread(counter::increment);
        Thread t2 = new Thread(counter::increment);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
