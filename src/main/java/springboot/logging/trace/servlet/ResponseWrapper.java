package springboot.logging.trace.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import org.apache.commons.io.output.TeeOutputStream;

/**
 * Response wrapper.
 * @author leo
 */
public class ResponseWrapper extends HttpServletResponseWrapper{

    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    /**
     * Constructs a response adaptor wrapping the given response.
     * @param response The response to be wrapped
     * @throws IllegalArgumentException if the response is null
     */
    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletResponse getResponse() {
        return this;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener listener) {

            }

            @Override
            public void write(int b) throws IOException {
                new TeeOutputStream(ResponseWrapper.super.getOutputStream(),bos).write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new ShadowPrintWriter(super.getWriter(),new PrintWriter(new OutputStreamWriter(bos,Charset.defaultCharset())));
    }

    public byte[] toByteArray(){
        return bos.toByteArray();
    }

    private static class ShadowPrintWriter extends PrintWriter {

        private final PrintWriter shadow;

        ShadowPrintWriter(PrintWriter writer,PrintWriter shadow) {
            super(writer,true);
            this.shadow = shadow;
        }

        @Override
        public void write(char [] buf, int off, int len) {
            super.write(buf, off, len);
            super.flush();
            shadow.write(buf, off, len);
            shadow.flush();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
            shadow.write(s, off, len);
            shadow.flush();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
            shadow.write(c);
            shadow.flush();
        }

        @Override
        public void flush() {
            super.flush();
            shadow.flush();
        }
    }
}

