package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TwoPhaseTerminationVolatile {

    private Thread monitor;
    private volatile boolean running = false;

    private void start() {
        synchronized (this) {
            if (running) {
                log.debug("already started");
                return;
            }
            running = true;
        }
        monitor = new Thread(() -> {
            while (true) {
                if (!running) {
                    log.debug("cleanup");
                    break;
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                    log.debug("monitor");
                } catch (InterruptedException e) {
                    // do nothing
                    // log.debug("interrupted", e);
                }
            }

        });
        monitor.start();
    }

    public void stop() {
        running = false;
        monitor.interrupt();
    }

    public static void main(String[] args) {
        TwoPhaseTerminationVolatile tpt = new TwoPhaseTerminationVolatile();
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
