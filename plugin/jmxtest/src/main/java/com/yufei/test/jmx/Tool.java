package com.yufei.test.jmx;

import com.yufei.test.jmx.mbean.ConfigMBean;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.lang.management.ManagementFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.HashMap;
import java.util.Map;

public class Tool {
    private static final String fmtUrl = "service:jmx:rmi:///jndi/rmi://[%s]:%d/jmxrmi";
    private static final int defaultPort = 8999;
    private static final String defaultHost = "127.0.0.1";

    public static void main(String[] args) throws Exception{
        System.out.println("start tool");
        MBeanServerConnection mbeanServerConn;
        JMXConnector jmxc;
        ConfigMBean configMBean;
        String host = null;
        int port = 0;
        String username = null;
        String password = null;

        for (String arg : args) {
            String[] tmp = arg.split("=");
            if ("host".equals(tmp[0])){
                host = tmp[1];
                continue;
            }

            if ("port".equals(tmp[0])){
                port = Integer.parseInt(tmp[1]);
                continue;
            }

            if ("user".equals(tmp[0])){
                username = tmp[1];
                continue;
            }

            if ("pwd".equals(tmp[0])){
                password = tmp[1];
            }
        }

        System.out.println(host + " " +username);
        JMXServiceURL jmxUrl = new JMXServiceURL(String.format(fmtUrl, (host == null || host.isEmpty()) ? defaultHost : host, port <= 0 ? defaultPort : port));
        Map<String,Object> env = new HashMap<String,Object>();

        if (username != null)
        {
            String[] creds = { username, password };
            env.put(JMXConnector.CREDENTIALS, creds);
        }

        env.put("com.sun.jndi.rmi.factory.socket", getRMIClientSocketFactory());

        jmxc = JMXConnectorFactory.connect(jmxUrl, env);
        mbeanServerConn = jmxc.getMBeanServerConnection();
        ObjectName configObjectName = new ObjectName("com.feiya:type=config");
        configMBean = JMX.newMBeanProxy(mbeanServerConn, configObjectName, ConfigMBean.class);

        for (String arg : args) {
            String[] tmp = arg.split("=");
            if ("addr".equals(tmp[0])){
                configMBean.setAddress(tmp[1]);
                continue;
            }

            if ("name".equals(tmp[0])){
                configMBean.setName(tmp[1]);
                continue;
            }

            if ("per".equals(tmp[0])){
                configMBean.setPeriod(Integer.parseInt(tmp[1]));
                continue;
            }

            if ("enable".equals(tmp[0])){
                configMBean.setEnableThrift(Boolean.parseBoolean(tmp[1]));
            }
        }

    }

    private static RMIClientSocketFactory getRMIClientSocketFactory()
    {
        if (Boolean.parseBoolean(System.getProperty("ssl.enable")))
            return new SslRMIClientSocketFactory();
        else
            return RMISocketFactory.getDefaultSocketFactory();
    }
}
