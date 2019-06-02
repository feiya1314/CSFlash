package com.yufeiblog.cassandra.test;

import com.datastax.driver.core.Session;
import com.yufeiblog.cassandra.SessionManager;
import com.yufeiblog.cassandra.SessionRepository;
import com.yufeiblog.cassandra.service.ICassandraManageService;
import com.yufeiblog.cassandra.service.CassandraManageService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

abstract class BaseTest implements Runnable {

    protected int appId = 20190103;
    protected String replication = "{\"class\": \"NetworkTopologyStrategy\",\"dc1\": \"3\",\"dc1\": \"3\"}";
    protected String tableName = "yftest";
    protected String username = "cassandra";
    protected String password = "cassandra";
    protected String contactPoints = "192.168.3.10";
    protected ICassandraManageService service;
    protected int startUid = 1;
    protected AtomicInteger uid = new AtomicInteger(startUid);
    protected int recordSize = 5;
    protected int lastingTime = 60 * 1000;
    protected volatile boolean keepRunning = true;
    protected int threadNums = 2;
    protected CountDownLatch countDownLatch = new CountDownLatch(threadNums);
    protected SessionRepository sessionRepository;
    protected Session session;

    public BaseTest() {
        init();
    }

    @Override
    public void run() {

        if (lastingTime > 0) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    keepRunning = false;
                    timer.cancel();
                }
            }, lastingTime);
        }

        while (keepRunning) {
            test();
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + " stop running");
        countDownLatch.countDown();
    }

    private void init() {
        SessionManager.Builder builder = SessionManager.builder()
                .withCredentials(username, password)
                .withContactPoints(contactPoints)
                .withReplication(replication);
        //.withLoadBalancingPolicy()
        SessionManager sessionManager = builder.build();
        sessionRepository = sessionManager.getSessionRepository();
        service = new CassandraManageService(sessionRepository);
        session = sessionRepository.getSession();
    }

    protected abstract void test();

    public void execute() {
        for (int i = 0; i < threadNums; i++) {
            Thread thread1 = new Thread(this);
            thread1.setName("thread " + (i + 1));
            thread1.start();
        }
        try {
            countDownLatch.await();
            System.out.println("all thread running out");
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
