package com.yufei.test;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class Tester implements Runnable {
    private BaseTest baseTest;
    private CountDownLatch countDownLatch;
    private volatile boolean keepRunning;

    public Tester(BaseTest baseTest, CountDownLatch countDownLatch) {
        this.baseTest = baseTest;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        if (baseTest.lastingTime > 0) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    keepRunning = false;
                    timer.cancel();
                }
            }, baseTest.lastingTime);
        }

        while (keepRunning) {
            long startTime = System.currentTimeMillis();
            try {
                baseTest.test();
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                //handleExe
                baseTest.onFailure();
            }
            baseTest.onSuccess(System.currentTimeMillis() - startTime);
        }
        System.out.println(Thread.currentThread().getName() + " stop running");
        countDownLatch.countDown();
    }
}
