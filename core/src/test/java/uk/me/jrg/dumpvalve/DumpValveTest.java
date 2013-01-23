package uk.me.jrg.dumpvalve;

import junit.framework.Assert;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;

public class DumpValveTest {

    private File catalinaBase;

    private static class EchoServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final ServletInputStream inputStream = req.getInputStream();
            final ServletOutputStream outputStream = resp.getOutputStream();

            byte[] block = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(block)) > 0) {
                outputStream.write(block, 0, bytesRead);
            }
        }
    }


    private Tomcat tomcat;

    @Before
    public void setUp() throws Exception {

        // Trigger loading of catalina.properties
        CatalinaProperties.getProperty("props");
        catalinaBase = new File(System.getProperty("java.io.tmpdir"), "dumpvalvetest");
        Assert.assertTrue("Unable to create temporary directory", catalinaBase.mkdirs());

        File appBase = new File(catalinaBase, "webapps");
        Assert.assertTrue("Unable to create appBase directory", appBase.mkdirs());

        tomcat = new Tomcat();

        String protocol = "org.apache.coyote.http11.Http11Protocol";
        Connector connector = new Connector(protocol);
        connector.setAttribute("address", InetAddress.getByName("localhost").getHostAddress());
        connector.setPort(0);

        connector.setAttribute("connectionTimeout", "3000");
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);

        tomcat.setBaseDir(catalinaBase.getAbsolutePath());
        tomcat.getHost().setAppBase(appBase.getAbsolutePath());
        tomcat.getHost().getPipeline().addValve(new DumpValve());
        Context ctx = tomcat.addContext("/echo", System.getProperty("java.io.tmpdir"));
        Tomcat.addServlet(ctx, "EchoServlet", new EchoServlet());
        ctx.addServletMapping("/*", "EchoServlet");
        tomcat.start();
    }

    @After
    public void tearDown() throws Exception {
        tomcat.stop();
        FileUtils.deleteDirectory(catalinaBase);
    }

    @Test
    public void testShouldSuccessfullyPostALargeRequest() throws Exception {
        final int port = tomcat.getConnector().getLocalPort();

        final PostMethod postMethod = new PostMethod("http://localhost:" + port + "/echo/");


        final InputStream inputStream = getClass().getResourceAsStream("/lipsum.txt");
        String fileContent = convertToString(inputStream);

        StringBuilder payloadBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            payloadBuilder.append(fileContent);
        }

        final String payload = payloadBuilder.toString();
        postMethod.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(payload.getBytes("UTF-8"))));

        final HttpClient httpClient = new HttpClient();
        final int status = httpClient.executeMethod(postMethod);

        Assert.assertEquals(200, status);
        final InputStream responseBodyAsStream = postMethod.getResponseBodyAsStream();
        String response = convertToString(responseBodyAsStream);

        Assert.assertEquals(payload, response);
    }

    private String convertToString(InputStream inputStream) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            builder.append(line);
        }

        inputStream.close();

        return builder.toString();
    }
}
