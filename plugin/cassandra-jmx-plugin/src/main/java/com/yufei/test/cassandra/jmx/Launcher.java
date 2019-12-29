package com.yufei.test.cassandra.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    public static void premain(String args) throws Exception {
        logger.info("start remote jmx by premain");
        Integer jmxPort = Integer.getInteger("cassandra.jmx.remote.port");
        if (jmxPort == null) {
            logger.warn("cassandra.jmx.remote.port is null");
            return;
        }

        Map<String, String> env = new HashMap<>();
        JMXServiceURL url = new JMXServiceURL("rmi", null, jmxPort, "/jndi/rmi://localhost:" + jmxPort + "/jmxrmi");

        if (Boolean.parseBoolean(System.getProperty("com.sun.management.jmxremote.authenticate"))) {
            String accessFile = System.getProperty("com.sun.management.jmxremote.access.file");
            String passwordFile = System.getProperty("com.sun.management.jmxremote.password.file");
            env.put("jmx.remote.x.access.file", accessFile);
            env.put("jmx.remote.x.password.file", passwordFile);
        }

        if (args != null) {
            String[] argArr = args.split(",");
            for (String arg : argArr) {
                String[] tmp = arg.split(":");
                if (tmp.length < 2) {
                    throw new IllegalArgumentException("invalid premain args " + args);
                }
                env.put(tmp[0], tmp[1]);
            }
        }

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        LocateRegistry.createRegistry(jmxPort);

        JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mBeanServer);
        connectorServer.start();
    }
}
