package uk.me.jrg.dumpvalve;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.buf.ByteChunk;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class DumpValve extends ValveBase implements DumpValveMBean {

	private String urlPattern = ".*";
	private String logRequestPattern = "";
	private boolean enabled = false;
	private boolean timeRequests = true;

	@Override
	public String getUrlPattern() {
		return urlPattern;
	}

	@Override
	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	@Override
	public String getLogRequestPattern() {
		return logRequestPattern;
	}

	@Override
	public void setLogRequestPattern(String logRequestPattern) {
		this.logRequestPattern = logRequestPattern;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isTimeRequests() {
		return timeRequests;
	}

	@Override
	public void setTimeRequests(boolean timeRequests) {
		this.timeRequests = timeRequests;
	}

	public DumpValve() {
		super();
		registerMBean();
	}

	public DumpValve(boolean asyncSupported) {
		super(asyncSupported);
		registerMBean();
	}

	private void registerMBean() {
		MBeanServer server = getMBeanServer();

		ObjectName name = null;
		try {
			name = new ObjectName("Application:Name=DumpValve,Type=Server");
			server.registerMBean(this, name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MBeanServer getMBeanServer() {
		MBeanServer mbserver = null;

		ArrayList<MBeanServer> mbservers = MBeanServerFactory
				.findMBeanServer(null);

		if (mbservers.size() > 0) {
			mbserver = (MBeanServer) mbservers.get(0);
		}

		if (mbserver == null) {
			mbserver = MBeanServerFactory.createMBeanServer();
		}

		return mbserver;
	}

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {

		try {
			String postBody = "";
			final org.apache.coyote.Request coyoteRequest = request
					.getCoyoteRequest();

			if ("POST".equals(request.getMethod())) {
				ByteChunk body = new ByteChunk();
				body.setLimit(coyoteRequest.getContentLength());

				byte buffer[] = new byte[4096];
				InputStream is = request.getInputStream();
				int bytesRead;

				while ((bytesRead = is.read(buffer)) >= 0) {
					body.append(buffer, 0, bytesRead);
				}

				final byte[] bytes = body.getBytes();

				String encoding = request.getCharacterEncoding();
				if (encoding == null) {
					encoding = "UTF-8";
				}

				if (bytes != null) {
					postBody = new String(bytes, encoding);
				}

				String method = coyoteRequest.method().getString();
				String queryString = coyoteRequest.queryString().getString();
				String requestURI = coyoteRequest.requestURI().getString();

				coyoteRequest.getParameters().recycle();
				coyoteRequest.getParameters().setQueryStringEncoding(
						request.getConnector().getURIEncoding());
				try {
					coyoteRequest.action((ActionCode) ActionCodeFactory
							.getRequestSetBodyReplayActionCode(), body);
				} catch (Exception e) {
					throw new ServletException(e);
				}

				coyoteRequest.method().setString(method);
				coyoteRequest.queryString().setString(queryString);
				coyoteRequest.requestURI().setString(requestURI);

				resetField(request, "usingInputStream");
				resetField(request, "usingReader");
			}

			final Valve valve = getNext();
			valve.invoke(request, response);

			System.out.println("=============================================");
			System.out.println("URL: " + request.getRequestURI());
			System.out.println(postBody);
			System.out.println("=============================================");
		} catch (Throwable t) {
			t.printStackTrace();
			throw new ServletException(t);
		}
	}

	private void resetField(Request request, String fieldName) {
		try {
			final Field declaredField = Request.class
					.getDeclaredField(fieldName);
			declaredField.setAccessible(true);
			declaredField.setBoolean(request, false);
		} catch (Exception e) {
		}
	}
}
