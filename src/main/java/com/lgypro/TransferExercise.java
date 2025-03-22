package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TransferExercise {
    public static void main(String[] args) {
        Account jack = new Account("jack", 1000);
        Account rose = new Account("rose", 1000);
        List<Account> accountList = new ArrayList<>();
        accountList.add(jack);
        accountList.add(rose);
        List<Future<?>> futureList = new ArrayList<>();
        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                Collections.shuffle(accountList);
                Account first = accountList.get(0);
                Account second = accountList.get(1);
                futureList.add(executorService.submit(() -> {
                    first.transfer(second, random.nextInt(200));
                }));
            }
        }
        futureList.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        log.debug("jack: {}", jack.getBalance());
        log.debug("rose: {}", rose.getBalance());
        log.debug("right? {}", jack.getBalance() + rose.getBalance() == 2000);
    }

    @Slf4j
    private static class Account {
        private static final Object lock = new Object();
        private final String name;

        private int balance;

        public Account(String name, int balance) {
            this.name = name;
            this.balance = balance;
        }

        public int getBalance() {
            synchronized (lock) {
                return balance;
            }
        }

        public void setBalance(int balance) {
            synchronized (lock) {
                this.balance = balance;
            }
        }

        public String getName() {
            return name;
        }

        public void transfer(Account target, int amount) {
            synchronized (lock) {
                if (this == target) return;
                if (balance > amount) {
                    int myBalance = balance;
                    int otherBalance = target.getBalance();
                    setBalance(balance - amount);
                    target.setBalance(target.getBalance() + amount);
                    log.debug("{} have {}, {} have {}, after transfer {} {}, {} new balance is {} and {} new balance is {}",
                            name, myBalance, target.getName(), otherBalance,
                            target.getName(), amount,
                            name, balance, target.getName(), target.getBalance());
                } else {
                    log.debug("{} have {}, {} have {}, amount is {}, but {} balance is not enough",
                            name, balance, target.getName(), target.getBalance(), amount, name);
                }
            }
        }
    }
}
