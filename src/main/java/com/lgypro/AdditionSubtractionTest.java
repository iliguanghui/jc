package com.lgypro;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdditionSubtractionTest {

    public static void main(String[] args) {
        Room room = new Room();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000 * 10000; i++) {
                room.increment();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000 * 10000; i++) {
                room.decrement();
            }
        }, "t2");
        log.debug("start");
        t1.start();
        t2.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("count: {}", room.getCount());
    }

    private static class Room {
        private int count = 0;

        public void increment() {
            synchronized (this) {
                count++;
            }
        }

        public void decrement() {
            synchronized (this) {
                count--;
            }
        }

        public int getCount() {
            synchronized (this) {
                return count;
            }
        }
    }
}

