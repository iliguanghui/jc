package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SellExercise {
    private static class TicketWindow {
        private int number;

        public TicketWindow(int number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "Ticket{" +
                    "number=" + number +
                    '}';
        }

        public synchronized int getNumber() {
            return number;
        }

        public synchronized int sell(int amount) {
            if (this.number >= amount) {
                this.number -= amount;
                return amount;
            } else {
                return 0;
            }
        }
    }

    public static void main(String[] args) {
        int total = 5000;
        TicketWindow ticketWindow = new TicketWindow(total);
        Random random = new Random();
        ArrayList<Thread> threadList = new ArrayList<>();
        List<Integer> amountList = new Vector<>();
        log.info("开始卖票");
        for (int i = 0; i < 1000; i++) {
            Thread t = new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                int amount = ticketWindow.sell(random.nextInt(5) + 1);
                amountList.add(amount);
            });
            threadList.add(t);
            t.start();
        }
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        int remaining = ticketWindow.getNumber();
        int sold = amountList.stream().mapToInt(i -> i).sum();
        log.debug("余票: {}", remaining);
        log.debug("卖出的票数: {}", sold);
        log.debug("right? {}", remaining + sold == total);
    }
}
