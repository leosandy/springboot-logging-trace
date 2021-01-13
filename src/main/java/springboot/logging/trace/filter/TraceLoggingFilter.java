package springboot.logging.trace.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import springboot.logging.trace.servlet.RequestWrapper;
import springboot.logging.trace.servlet.ResponseWrapper;
import springboot.logging.trace.utils.AdvanceHttpUtils;
import springboot.logging.trace.utils.HttpParameterUtils;
import springboot.logging.trace.utils.StringUtil;
import springboot.logging.trace.utils.TraceUtils;

/**
 * 追踪过滤.
 * @author leo
 */
public class TraceLoggingFilter extends OncePerRequestFilter implements Ordered {

    /**
     * LOG
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String [] headers = new String[0];

    /**
     * 相应字符限制.
     */
    private int respLimit = 1024;

    /**
     * Filter 排序
     */
    private int order = Ordered.LOWEST_PRECEDENCE - 99;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(AdvanceHttpUtils.HDR_FIELD_X_TRACE_ID);
        TraceUtils.setTraceId(StringUtil.isBlank(traceId) ? TraceUtils.getDefaultTraceId():traceId);
        long s = System.currentTimeMillis();
        try {
            RequestWrapper requestWrapper = new RequestWrapper(request);
            requestBuilder(requestWrapper);
            response = new ResponseWrapper(response);
            filterChain.doFilter(requestWrapper,response);
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
    private void requestBuilder(RequestWrapper request){
        final String remoteAddr = HttpParameterUtils.getRemoteAddress(request);
        String httpXff = request.getHeader("X-Forwarded-For");
        final String reqUri = request.getRequestURI();
        final String method = request.getMethod();
        String reqBody = request.getBody();
        String queryString = HttpParameterUtils.dumpQueryString (request);
        String headers = HttpParameterUtils.dumpHeaders(request,getHeaders());
        String paramsInfo = String.format("REQ|%s|%s|uri=%s|queryString=[%s]" +
                        "|HEADERS=[%s]|BODY=[%s],XFF:[%s]",remoteAddr, method,
                reqUri,queryString ,headers, reqBody ,httpXff);
        logger.info(paramsInfo);
    }

    private void responseBuilder(HttpServletRequest request,ResponseWrapper response,long cost){
        byte [] resultByte = response.toByteArray();
        int length = Math.min(resultByte.length, getRespLimit());
        String result;
        if (resultByte.length <= getRespLimit()){
            result = new String(resultByte,StandardCharsets.UTF_8);
        } else {
            result = new String(resultByte, 0, length,StandardCharsets.UTF_8).concat("...");
        }
        logger.info("RSP|{}|method={}|uri={}|SC={}|COST={}|BODY=[{}]",HttpParameterUtils.getRemoteAddress(request),
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

    public int getRespLimit() {
        return respLimit;
    }

    public void setRespLimit(int respLimit) {
        this.respLimit = respLimit;
    }

    @Override
    public int getOrder () {
        return order;
    }

    public void setOrder (int order) {
        this.order = order;
    }
}
