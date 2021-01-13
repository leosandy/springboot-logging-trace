package springboot.logging.trace.utils;

/**
 * 追踪Runnable 在线程池内获取主线程的追踪ID.
 * 线程、线程池执行追踪Runnable
 * <pre>
 *     {@code
 *       private ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(10));
          executor.execute(new TraceRunnable(()-> System.out.println(TraceUtils.getTraceId())));
 *     }
 * </pre>
 * @author leo
 * @see java.util.concurrent.Executor#execute(Runnable)
 * @see java.util.concurrent.ThreadPoolExecutor
 */
public class TraceRunnable implements Runnable {

    private final Runnable runnable;

    private final String traceId;
    public TraceRunnable(Runnable runnable){
       this(runnable,true);
    }

    public TraceRunnable(Runnable runnable, boolean enableTrace){
        this.runnable = runnable;
        if (enableTrace){
            this.traceId = TraceUtils.getTraceId();
        } else{
            this.traceId = "";
        }

    }

    @Override
    public void run() {
        if ("".equals(traceId)){
            //initialize
            TraceUtils.initialize();
        } else {
            //passing the log context
            TraceUtils.setTraceId(traceId);
        }

        try {
            this.runnable.run();
        }finally {
            TraceUtils.destroy();
        }
    }
}
