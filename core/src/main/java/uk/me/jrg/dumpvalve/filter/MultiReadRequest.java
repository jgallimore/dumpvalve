package uk.me.jrg.dumpvalve.filter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class MultiReadRequest extends HttpServletRequestWrapper {

    private byte[] body;

    public MultiReadRequest(HttpServletRequest request) {
        super(request);

        try {
            InputStream inputStream = super.getInputStream();
            body = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStreamImpl(new ByteArrayInputStream(body));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String enc = getCharacterEncoding();
        if(enc == null) enc = "UTF-8";
        return new BufferedReader(new InputStreamReader(getInputStream(), enc));
    }

    public String getBodyAsString() throws UnsupportedEncodingException {
        return new String(body, "UTF-8");
    }

    private class ServletInputStreamImpl extends ServletInputStream {
        private InputStream inputStream;

        public ServletInputStreamImpl(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public synchronized void mark(int readlimit) {
            throw new RuntimeException(new IOException("mark/reset not supported"));
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public synchronized void reset() throws IOException {
            new IOException("mark/reset not supported");
        }
    }
}
