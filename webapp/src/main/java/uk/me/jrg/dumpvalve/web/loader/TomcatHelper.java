package uk.me.jrg.dumpvalve.web.loader;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.core.StandardServer;

public class TomcatHelper {

	public static StandardServer getServer() {
        StandardServer server = null;

        // first try to use Tomcat's ServerFactory class to give us a reference to the server
		
		try {
			Class<?> tomcatServerFactory = Class.forName("org.apache.catalina.ServerFactory");
			Method getServerMethod = tomcatServerFactory.getMethod("getServer");
			server = (StandardServer) getServerMethod.invoke(null);
		} catch (Exception e) {
            // ignored
		}
		
		// if this fails, we'll try and get a reference from the platform mbean server
		try {
			MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
			server = (StandardServer) mbeanServer.getAttribute(new ObjectName("Catalina:type=Server"), "managedResource");
		} catch (Exception e) {
            // ignored
		}

		return server;
	}
	
	public static void installValve() {
		StandardServer server = getServer();
	}
	
	public static void uninstallValve() {
		
	}
	
}
