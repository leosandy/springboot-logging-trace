package springboot.logging.trace.schedule;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
import springboot.logging.trace.async.ContextAwareCallable;
import springboot.logging.trace.utils.TraceRunnable;
import springboot.logging.trace.utils.TraceUtils;

/**
 * Schedule ThreadPool enhance logging trace {@link org.springframework.scheduling.annotation.Scheduled}.
 * @author leo
 */
public class TraceLoggingSchedulingThreadPoolExecutor extends ThreadPoolTaskScheduler {
    @Override
    public void execute(Runnable task) {
        super.execute(new TraceRunnable(task,false));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        super.execute(new TraceRunnable(task,false), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(new TraceRunnable(task,false));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(new ContextAwareCallable(task, TraceUtils.getTraceId()));
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        return super.submitListenable(new TraceRunnable(task,false));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return super.submitListenable(new ContextAwareCallable(task, TraceUtils.getTraceId()));
    }

    @Override
    protected void cancelRemainingTask(Runnable task) {
        super.cancelRemainingTask(new TraceRunnable(task,false));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return super.schedule(new TraceRunnable(task,false), trigger);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
        return super.schedule(new TraceRunnable(task,false), startTime);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        return super.scheduleAtFixedRate(new TraceRunnable(task,false), startTime, period);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
        return super.scheduleAtFixedRate(new TraceRunnable(task,false), period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        return super.scheduleWithFixedDelay(new TraceRunnable(task,false), startTime, delay);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
        return super.scheduleWithFixedDelay(new TraceRunnable(task,false), delay);
    }
}
