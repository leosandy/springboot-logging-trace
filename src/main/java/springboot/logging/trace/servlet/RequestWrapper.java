package springboot.logging.trace.servlet;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.util.StreamUtils;

/**
 * request wrapper .
 * <br> @author leo
 * <br> @date 2020/5/20
 * <br> @version 1.0
 * <br> Remark 认为有必要的其他信息
 */
public class RequestWrapper extends HttpServletRequestWrapper{

    private ServletInputStream servletInputStream;

    private HttpServletRequest request;

    private byte [] readBody;

    private boolean useReader = false;

    private boolean useInputStream = false;

    public RequestWrapper(HttpServletRequest request) throws IOException{
        super(request);
        this.request = request;
        this.servletInputStream = request.getInputStream();
        readBody = StreamUtils.copyToByteArray(servletInputStream);
    }

    /**
     * {@link ServletRequest#getReader()}
     */
    @Override
    public BufferedReader getReader(){
        if (useInputStream) {
            throw new IllegalStateException("this.getInputStream() has already been called for this request");
        }
        useReader = true;
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * {@link ServletRequest#getInputStream}
     */
    @Override
    public ServletInputStream getInputStream() {
        if (useReader) {
            throw new IllegalStateException("this.getReader() has already been called for this request");
        }
        useInputStream = true;
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(readBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return servletInputStream.isFinished();
            }

            @Override
            public boolean isReady() {
                return servletInputStream.isReady();
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return inputStream.read();
            }
        };
    }

    public String getBody(){
        return new String(readBody);
    }
}
