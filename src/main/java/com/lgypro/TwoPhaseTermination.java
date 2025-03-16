package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TwoPhaseTermination {
    private Thread monitor;

    private void start() {
        monitor = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    log.debug("cleanup");
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                    log.debug("monitor");
                } catch (InterruptedException e) {
                    log.debug("interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }

        });
        monitor.start();
    }

    public void stop() {
        monitor.interrupt();
    }

    public static void main(String[] args) {
        TwoPhaseTermination tpt = new TwoPhaseTermination();
        log.debug("start");
        tpt.start();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("stop");
        tpt.stop();
    }
}
