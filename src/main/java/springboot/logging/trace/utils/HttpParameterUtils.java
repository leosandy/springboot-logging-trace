package springboot.logging.trace.utils;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Enumeration;

/**
 * Http 参数处理工具类.
 * @author leo
 */
public final class HttpParameterUtils {
    private HttpParameterUtils() {

    }
    public static String dumpQueryString(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> pnEnu = request.getParameterNames();

        while(pnEnu.hasMoreElements()) {
            String pn = pnEnu.nextElement();
            String pv = request.getParameter(pn);
            params.append(pn).append("=").append( pv).append("&");
        }

        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }

        return params.toString();
    }

    public static String getRemoteAddress(HttpServletRequest request) {
        String httpXff = request.getHeader("X-Forwarded-For");
        if (httpXff != null) {
            String[] ips = httpXff.split(",");
            if (ips.length > 0) {
                return ips[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    public static String dumpHeaders(HttpServletRequest request,String [] headers) {
        if (headers == null || headers.length ==0){
            return "";
        }
        StringBuffer params = new StringBuffer();
        Arrays.stream(headers).forEach(header->
        {
            String value = request.getHeader(header);
            if (StringUtil.isNotBlank(value)){
                params.append(header).append("=").append(value).append( "&");
            }
        });
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }

}