package springboot.logging.trace.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springboot.logging.trace.servlet.ResponseWrapper;
import springboot.logging.trace.utils.HttpParameterUtils;
import springboot.logging.trace.utils.StringUtil;
import springboot.logging.trace.utils.TraceUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartRequest;

/**
 * 追踪过滤.
 * @author leo
 */
public class TraceLoggingFilter extends OncePerRequestFilter {

    /**
     * HTTP request header transfer trace id sign.
     */
    private static final String HTTP_HDR_TRACE_ID = "x-rpc-tid";

    /**
     * LOG
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String [] headers = new String[0];

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HTTP_HDR_TRACE_ID);
        TraceUtils.setTraceId(StringUtil.isBlank(traceId) ? TraceUtils.getDefaultTraceId():traceId);
        long s = System.currentTimeMillis();
        try {
            requestBuilder(request);
            response = new ResponseWrapper(response);
            filterChain.doFilter(request,response);
            long diff = System.currentTimeMillis() - s;
            responseBuilder(request,(ResponseWrapper) response,diff);
        } finally {
            release();
        }
    }

    /**
     * LOG 构建.
     * @param request Servlet Request
     */
    private void requestBuilder(HttpServletRequest request){
        final String remoteAddr = HttpParameterUtils.getRemoteAddress(request);
        String httpXff = request.getHeader("X-Forwarded-For");
        final String reqUri = request.getRequestURI();
        final String method = request.getMethod();
        String reqBody = HttpParameterUtils.dumpQueryString(request);
        if (MultipartRequest.class.isAssignableFrom(request.getClass())){
            reqBody = StringUtil.decode(reqBody);
        }
        String headers = HttpParameterUtils.dumpHeaders(request,getHeaders());
        String paramsInfo = String.format("REQ|%s|%s|%s|HEADERS:[%s]|BODY=[%s],XFF:[%s]",remoteAddr, method, reqUri,headers, reqBody ,httpXff);
        logger.info(paramsInfo);
    }

    private void responseBuilder(HttpServletRequest request,ResponseWrapper response,long cost){

        byte [] resultByte = response.toByteArray();
        String result = new String(resultByte, StandardCharsets.UTF_8);
        logger.info("RSP|{}|{}|{}|SC={}|COST={}|BODY={}",HttpParameterUtils.getRemoteAddress(request),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                cost,result
                );
    }

    /**
     * release resource.
     */
    private void release(){
        TraceUtils.clear();
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }
}
