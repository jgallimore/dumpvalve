package uk.me.jrg.dumpvalve;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;
import org.apache.coyote.ActionCode;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;

public class DumpValve extends RequestFilterValve {
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        String postBody = "";
        final org.apache.coyote.Request coyoteRequest = request.getCoyoteRequest();

        if ("POST".equals(request.getMethod())) {
            final InputBuffer inputBuffer = coyoteRequest.getInputBuffer();

            ByteChunk body = new ByteChunk();
            body.setLimit(coyoteRequest.getContentLength());

            byte buffer[] = new byte[4096];
            InputStream is = request.getInputStream();
            int bytesRead;

            while((bytesRead = is.read(buffer)) >= 0) {
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

            coyoteRequest.getParameters().recycle();
            coyoteRequest.getParameters().setQueryStringEncoding(
                    request.getConnector().getURIEncoding());
            coyoteRequest.action(ActionCode.REQ_SET_BODY_REPLAY, body);
        }

        final Valve valve = getNext();
        valve.invoke(request, response);

        System.out.println("=============================================");
        System.out.println("URL: " + request.getRequestURI());
        System.out.println(postBody);
        System.out.println("=============================================");
    }
}
