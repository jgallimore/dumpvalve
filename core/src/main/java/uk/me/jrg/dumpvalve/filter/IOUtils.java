package uk.me.jrg.dumpvalve.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {
    public static byte[] toByteArray(InputStream is) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[8192];

        while ((bytesRead = is.read(buffer)) > -1 ) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        return os.toByteArray();
    }
}
