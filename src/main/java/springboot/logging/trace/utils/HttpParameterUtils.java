package springboot.logging.trace.utils;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Enumeration;

/**
 * Http 参数处理工具类.
 * @author leo
 */
public final class HttpParameterUtils {

    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String FORM_MULTIPART_CONTENT_TYPE = "multipart/form-data";

    private HttpParameterUtils() {

    }
    public static String dumpQueryString(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> pnEnu = request.getParameterNames();

        while(pnEnu.hasMoreElements()) {
            String pn = pnEnu.nextElement();
            String pv = request.getParameter(pn);
            params.append(pn).append("=").append(pv == null ? "":pv).append("&");
        }

        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }

        return params.toString();
    }

    public static boolean isForm(HttpServletRequest request){
        String contentType = request.getContentType();
        return contentType != null &&
                (contentType.contains(FORM_CONTENT_TYPE) ||
                        contentType.contains(FORM_MULTIPART_CONTENT_TYPE));
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
            params.append(header).append("=").append(value == null ? "":value).append( "&");
        });
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }

}