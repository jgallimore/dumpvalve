package uk.me.jrg.dumpvalve.mbean;

import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private static Configuration instance = new Configuration();

    private boolean enabled;
    private final List<String> contexts = new ArrayList<String>();

    public static Configuration getInstance() {
        return instance;
    }

    private Configuration() {
        registerMBean();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public void addContext(String context) {
        contexts.add(context);
    }

    public boolean isContextEnabled(String contextPath) {
        for (String context : contexts) {
            if (context.equals(contextPath)) {
                return true;
            }
        }

        return false;
    }

    private void registerMBean() {
        javax.management.MBeanServer server = getMBeanServer();

        ObjectName name;
        try {
            name = new ObjectName("Application:Name=DumpValveFilter,Type=Server");
            server.registerMBean(new DumpValve(), name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private javax.management.MBeanServer getMBeanServer() {
        javax.management.MBeanServer mbserver = null;

        ArrayList<javax.management.MBeanServer> mbservers = MBeanServerFactory
                .findMBeanServer(null);

        if (mbservers.size() > 0) {
            mbserver = (javax.management.MBeanServer) mbservers.get(0);
        }

        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }

        return mbserver;
    }
}
