package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ReentrantConditionTest {
    private static final ReentrantLock room = new ReentrantLock();
    private static final Condition waitCigaretteSet = room.newCondition();
    private static final Condition waitTakeoutSet = room.newCondition();
    private static boolean hasCigarette = false;
    private static boolean hasTakeout = false;

    public static void main(String[] args) {

        new Thread(() -> {
            room.lock();
            try {
                while (!hasCigarette) {
                    log.debug("没有烟，等待");
                    waitCigaretteSet.await();
                }
                log.info("干活了");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                room.unlock();
            }
        }, "小南").start();

        new Thread(() -> {
            room.lock();
            try {
                while (!hasTakeout) {
                    log.debug("没有外卖，等待");
                    waitTakeoutSet.await();
                }
                log.info("干活了");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                room.unlock();
            }
        }, "小女").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            room.lock();
            try {
                log.info("送烟来了");
                hasCigarette = true;
                waitCigaretteSet.signal();
            } finally {
                room.unlock();
            }
        }, "送烟的").start();

        new Thread(() -> {
            room.lock();
            try {
                log.info("送外卖来了");
                hasTakeout = true;
                waitTakeoutSet.signal();
            } finally {
                room.unlock();
            }
        }, "送外卖的").start();
    }
}
