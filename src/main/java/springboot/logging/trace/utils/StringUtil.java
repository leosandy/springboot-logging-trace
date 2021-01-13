package springboot.logging.trace.utils;

import org.springframework.util.StringUtils;

/**
 * 字符串处理工具类 .
 * @author leo
 */
public class StringUtil {
    /**
     * 判断是否为空 null | "" | "null" | "NULL"
     * @param value value
     * @return boolean
     */
    public static boolean isBlank(String value) {
        return StringUtils.isEmpty(value)|| "null".equalsIgnoreCase(value);
    }
}
