package springboot.logging.trace.async;

import java.util.concurrent.Callable;
import springboot.logging.trace.utils.TraceUtils;

/**
 * Async callback 增强类，增加日志追踪traceId的上下文.
 * @author leo
 */
public class ContextAwareCallable<T> implements Callable<T> {
    private Callable<T> task;
    private final String traceId;

    public ContextAwareCallable(Callable<T> task, String traceId) {
        this.task = task;
        this.traceId = traceId;
    }

    @Override
    public T call() throws Exception {
        if (traceId != null) {
            TraceUtils.setTraceId(traceId);
        }
        return task.call();
    }
}
