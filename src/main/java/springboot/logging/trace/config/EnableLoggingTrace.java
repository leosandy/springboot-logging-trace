package springboot.logging.trace.config;

import javax.servlet.http.HttpServletRequest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * enable trace logging.
 * @author leo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TraceLoggingSelector.class)
public @interface EnableLoggingTrace {
    /**
     * 是否开启.
     */
    boolean enable() default true;

    /**
     * 请求head日志打印
     * {@link HttpServletRequest#getHeaderNames()}
     */
    String [] headers() default {};

    /**
     * 返回数据最大length限制 默认 1024
     */
    int respLimit() default 1024;

    /**
     * filter 排序默认顺序
     * @see org.springframework.boot.web.servlet.FilterRegistrationBean#setOrder(int)
     */
    int order() default Ordered.LOWEST_PRECEDENCE - 99;
}
