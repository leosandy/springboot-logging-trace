package springboot.logging.trace.async;

import java.util.Map;
import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * Async enhance.
 * @author leo
 * @see org.springframework.scheduling.annotation.EnableAsync
 */
public class AsyncTraceConfigurationSelector implements ImportBeanDefinitionRegistrar, AsyncConfigurer {

    private int corePoolSize;

    private int maxPoolSize;

    private int queueCapacity;

    private String threadNamePrefix = "";

    private AsyncUncaughtExceptionHandler exceptionHandler;

    @Override
    public Executor getAsyncExecutor() {
        ContextAwarePoolExecutor executor = new ContextAwarePoolExecutor();
        executor.setCorePoolSize(getCorePoolSize());
        executor.setMaxPoolSize(getMaxPoolSize());
        executor.setQueueCapacity(getQueueCapacity());
        executor.setThreadNamePrefix(getThreadNamePrefix());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return getExceptionHandler();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
       //setter properties
       Map<String,Object> traceMap = importingClassMetadata.getAnnotationAttributes(EnableTraceAsyncConfiguration.class.getName());
       if (traceMap == null){
           return;
       }
       Object object = traceMap.get("asyncExceptionHandler");
       AsyncUncaughtExceptionHandler exceptionHandler = new SimpleAsyncUncaughtExceptionHandler();
       if (object instanceof Class){
           Class<? extends AsyncUncaughtExceptionHandler> exceptionHandlerClass =
                   (Class<? extends AsyncUncaughtExceptionHandler>)traceMap.get("asyncExceptionHandler");
           try {
               exceptionHandler = exceptionHandlerClass.newInstance();
           } catch (InstantiationException |IllegalAccessException e) {
               exceptionHandler = new SimpleAsyncUncaughtExceptionHandler();
           }
       }
       AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(getClass())
               .addPropertyValue("corePoolSize",traceMap.get("corePoolSize"))
               .addPropertyValue("maxPoolSize",traceMap.get("maxPoolSize"))
               .addPropertyValue("queueCapacity",traceMap.get("maxPoolSize"))
               .addPropertyValue("threadNamePrefix",traceMap.get("threadNamePrefix"))
               .addPropertyValue("exceptionHandler",exceptionHandler)
               .getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition,registry);

    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public AsyncUncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(AsyncUncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
