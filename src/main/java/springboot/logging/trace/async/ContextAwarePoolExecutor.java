package springboot.logging.trace.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import springboot.logging.trace.utils.TraceUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * Async Executors Thread Pool Task Executor using xml configuration.
 * @author leo
 * @see org.springframework.scheduling.annotation.Async
 */
public class ContextAwarePoolExecutor extends ThreadPoolTaskExecutor {

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(new ContextAwareCallable(task, TraceUtils.getTraceId()));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return super.submitListenable(new ContextAwareCallable(task, TraceUtils.getTraceId()));
    }


}
