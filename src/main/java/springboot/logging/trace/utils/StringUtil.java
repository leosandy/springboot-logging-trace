package springboot.logging.trace.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.springframework.util.StringUtils;

/**
 * 字符串处理工具类 .
 * @author leo
 */
public class StringUtil {

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * 判断是否为空 null | "" | "null" | "NULL"
     * @param value value
     * @return boolean
     */
    public static boolean isBlank(String value) {
        return StringUtils.isEmpty(value)|| "null".equalsIgnoreCase(value);
    }

    public static String decode(String encode){
        try {
            return URLDecoder.decode(encode,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return encode;
        }
    }
}
