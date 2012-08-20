package uk.me.jrg.dumpvalve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.RequestFilterValve;
import org.apache.coyote.ActionCode;
import org.apache.coyote.InputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;

import javax.servlet.ServletException;
import java.io.IOException;

public class DumpValve extends RequestFilterValve {
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {


        final org.apache.coyote.Request coyoteRequest = request.getCoyoteRequest();
        final InputBuffer inputBuffer = coyoteRequest.getInputBuffer();

        ByteChunk body = new ByteChunk();
        inputBuffer.doRead(body, coyoteRequest);

        final byte[] bytes = body.getBytes();
        String postBody = "";

        if (bytes != null) {
            postBody = new String(bytes, request.getCharacterEncoding());
        }

        System.out.println("=============================================");
        System.out.println("URL: " + request.getRequestURI());
        System.out.println(postBody);
        System.out.println("=============================================");


        coyoteRequest.action(ActionCode.ACTION_REQ_SET_BODY_REPLAY, body);
        getNext().invoke(request, response);
    }
}
