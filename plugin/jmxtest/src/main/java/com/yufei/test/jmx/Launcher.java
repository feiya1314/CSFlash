package com.yufei.test.jmx;

import com.yufei.test.jmx.mbean.Config;
import com.yufei.test.jmx.mbean.ConfigMBean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class Launcher {
    public static void main(String[] args) throws Exception {
        System.out.println("param" + System.getProperty("com.feiya.test"));
        System.out.println("test");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName configObj = new ObjectName("com.yufei:type=StorageService");
        ConfigMBean config = new Config();
        mbs.registerMBean(config, configObj);

        for (int i = 0; i < 100000; i++) {
            System.out.println("address : " + config.getAddress());
            System.out.println("name : " + config.getName());
            System.out.println("period : " + config.getPeriod());
            System.out.println("enableThrift : " + config.isEnableThrift());
            Thread.sleep(5000);
        }
    }
}
