package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DiningPhilosophersTest {
    private static class NotImplementedException extends RuntimeException {
        public NotImplementedException() {
            super("Not implemented");
        }
    }

    private interface Lockable {
        default boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            throw new NotImplementedException();
        }

        default void unlock() {
        }
    }

    private static class Chopstick implements Lockable {
        private final String name;

        public Chopstick(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Chopstick{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }


    private static class Philosopher implements Runnable {
        protected static final Random random = new Random();
        private final String name;
        protected final Chopstick left;
        protected final Chopstick right;
        protected volatile int count;

        public Philosopher(String name, Chopstick left, Chopstick right) {
            this.name = name;
            this.left = left;
            this.right = right;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (left) {
                    synchronized (right) {
                        log.info("eating...");
                        count++;
                        try {
                            TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public int getCount() {
            return count;
        }

        public String getName() {
            return name;
        }
    }

    private static class NewChopstick extends Chopstick {
        private final ReentrantLock lock = new ReentrantLock();

        public NewChopstick(String name) {
            super(name);
        }

        @Override
        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return lock.tryLock(timeout, unit);
        }

        @Override
        public void unlock() {
            lock.unlock();
        }
    }

    private static class NewPhilosopher extends Philosopher {

        public NewPhilosopher(String name, Chopstick left, Chopstick right) {
            super(name, left, right);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (left.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            if (right.tryLock(100, TimeUnit.MILLISECONDS)) {
                                try {
                                    log.info("eating...");
                                    count++;
                                    TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
                                } finally {
                                    right.unlock();
                                }
                            }
                        } finally {
                            left.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        // test1();
        test2();
    }

    private static void test1() {
        Chopstick c1 = new Chopstick("1");
        Chopstick c2 = new Chopstick("2");
        Chopstick c3 = new Chopstick("3");
        Chopstick c4 = new Chopstick("4");
        Chopstick c5 = new Chopstick("5");
        Philosopher philosopher1 = new Philosopher("苏格拉底", c1, c2);
        Philosopher philosopher2 = new Philosopher("柏拉图", c2, c3);
        Philosopher philosopher3 = new Philosopher("亚里士多德", c3, c4);
        Philosopher philosopher4 = new Philosopher("赫拉克利特", c4, c5);
        Philosopher philosopher5 = new Philosopher("阿基米德", c5, c1);
        ArrayList<Philosopher> philosophers = new ArrayList<>();
        philosophers.add(philosopher1);
        philosophers.add(philosopher2);
        philosophers.add(philosopher3);
        philosophers.add(philosopher5);
        philosophers.add(philosopher5);
        new Thread(philosopher1, "苏格拉底").start();
        new Thread(philosopher2, "柏拉图").start();
        new Thread(philosopher3, "亚里士多德").start();
        new Thread(philosopher4, "赫拉克利特").start();
        new Thread(philosopher5, "阿基米德").start();
        new StatsService(philosophers).start();
    }

    private static void test2() {
        Chopstick c1 = new NewChopstick("1");
        Chopstick c2 = new NewChopstick("2");
        Chopstick c3 = new NewChopstick("3");
        Chopstick c4 = new NewChopstick("4");
        Chopstick c5 = new NewChopstick("5");
        Philosopher philosopher1 = new NewPhilosopher("苏格拉底", c1, c2);
        Philosopher philosopher2 = new NewPhilosopher("柏拉图", c2, c3);
        Philosopher philosopher3 = new NewPhilosopher("亚里士多德", c3, c4);
        Philosopher philosopher4 = new NewPhilosopher("赫拉克利特", c4, c5);
        Philosopher philosopher5 = new NewPhilosopher("阿基米德", c5, c1);
        ArrayList<Philosopher> philosophers = new ArrayList<>();
        philosophers.add(philosopher1);
        philosophers.add(philosopher2);
        philosophers.add(philosopher3);
        philosophers.add(philosopher5);
        philosophers.add(philosopher5);
        new Thread(philosopher1, "苏格拉底").start();
        new Thread(philosopher2, "柏拉图").start();
        new Thread(philosopher3, "亚里士多德").start();
        new Thread(philosopher4, "赫拉克利特").start();
        new Thread(philosopher5, "阿基米德").start();
        new StatsService(philosophers).start();
    }

    private static class StatsService implements Runnable {
        private final List<Philosopher> philosophers;

        public StatsService(List<Philosopher> philosophers) {
            this.philosophers = philosophers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                stats();
            }
        }

        public void start() {
            new Thread(this, this.getClass().getSimpleName()).start();
        }

        private void stats() {
            StringBuilder sb = new StringBuilder();
            for (Philosopher philosopher : philosophers) {
                sb.append(philosopher.getName()).append(": ").append(philosopher.getCount()).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
            log.debug(sb.toString());
        }
    }
}
