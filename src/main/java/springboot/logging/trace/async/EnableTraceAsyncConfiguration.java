package springboot.logging.trace.async;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Import;

/**
 * enhance {@link org.springframework.scheduling.annotation.EnableAsync}.
 * @author leo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AsyncTraceConfigurationSelector.class)
public @interface EnableTraceAsyncConfiguration {

    int corePoolSize() default 4;

    int maxPoolSize() default 8;

    int queueCapacity() default 16;

    String threadNamePrefix() default "Async-";

    Class<? extends AsyncUncaughtExceptionHandler> asyncExceptionHandler() default SimpleAsyncUncaughtExceptionHandler.class;

}
