# Logger Trace using the tutorial
   项目为日志追踪项目，通过`x-trace-id` request head 透传的方式，传递到相关联的项目，通过`x-trace-id`可追踪到整个调用链的日志，包括在项目内部(Method)
上记录的日志.项目依赖于logback、基于slf4j MDC的机制.

### Feature

- support camel quartz logging trace.
- support Spring `@Scheduled` logging trace.

## Usage

### Maven
```xml
<dependency>
			<groupId>spring.boot.logging.trace</groupId>
			<artifactId>logging-trace</artifactId>
			<version>0.0.1</version>
</dependency>
```
### SpringBoot Enable Configuration
```java
@SpringBootApplication
@EnableAsync
@EnableTraceAsyncConfiguration
@EnableLoggingTrace
public class SpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(LxlActivityApplication.class, args);
	}

}
```
- EnableLoggingTrace 代表启用Logging 日志追踪。
- EnableTraceAsyncConfiguration 增强EnableAsync日志，在异步调用中使用.
    * EnableTraceAsyncConfiguration 已经帮助我们自动注入了Async线程池，无需再次配置。
    可通过注解原信息配置调整相关参数。
- 自定义线程池的追踪ID传递方式.
```java
class ThreadPoolSimple{
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(10));
    
    public void trace(){
        //通过TraceRunnable透传父线程的追踪ID
        executor.execute(new TraceRunnable(()-> log.info("再此执行异步业务")));
    }
    
}
```

### Support Scheduled Configuration

```java
/**
 * {@link org.springframework.scheduling.annotation.Scheduled} 增强日志追踪 .
 * @author leo
 */
@Configuration
public class SchedulingConfiguration implements SchedulingConfigurer{
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean
    public TaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new TraceLoggingSchedulingThreadPoolExecutor();
        taskScheduler.setPoolSize(4);
        taskScheduler.setThreadNamePrefix("Schedule-");
        return taskScheduler;
    }
}
```



### logback.xml Configuration

```xml
<encoder>
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS}|%.-1level|%logger{0}|%X{tid}|%X{traceId}|%msg%n</pattern>
</encoder>
```
- %X{tid} 代表的是当前线程ID
- %X{traceId} 代表的是当前追踪ID标识从请求到结束唯一的追踪标识符.

### Http Remote Invoker Tools
  集成了追踪Http的工具类`AdvanceHttpUtils`.

#### HttpUtils 说明
- AdvanceHttpUtils 追踪增强类，针对调用项目需要传递追踪日志可以使用.
- SimpleHttpClient 普通Http请求，调用外部第三方可以使用.
- MultipartUploadHttpUtils 主要用用于Form表单提交附件上传,项目透传附件使用.

### Reference 
- [Slf4j MDC 机制](https://www.jianshu.com/p/1dea7479eb07)
- [Logback MDC Simple](https://logback.qos.ch/xref/chapters/mdc/SimpleMDC.html)