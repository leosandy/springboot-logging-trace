package springboot.logging.trace.utils;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.MDC;

/**
 * 追踪服务类.
 * @author leo
 */
public class TraceUtils {

	/**
	 * 追踪id
	 */
	private static final String TRACE_ID = "traceId";

	/**
	 * 线程ID
	 */
	private static final String THREAD_ID = "tid";

    /**
     * 初始化启动
     */
    public static void initialize(){
        getTraceId();
    }

	/**
	 * 获取追踪ID.
	 */
	public static String getTraceId(){
		String traceId = MDC.get(TRACE_ID);
		if (StringUtil.isBlank(traceId)){
			traceId = getDefaultTraceId();
			MDC.put(TRACE_ID,traceId);
		}
		if (StringUtil.isBlank(MDC.get(THREAD_ID))){
			MDC.put(THREAD_ID,String.valueOf(Thread.currentThread().getId()));
		}
		return traceId;
	}

	/**
	 * 清空追踪ID
	 */
	public static void clear() {
		MDC.remove(TRACE_ID);
		MDC.remove (THREAD_ID);
	}

	/**
	 * 设置追踪ID
	 * @param traceId 追踪ID
	 */
	public static void setTraceId(String traceId) {
		MDC.put(TRACE_ID,traceId);
		MDC.put(THREAD_ID,String.valueOf(Thread.currentThread().getId()));
	}

	public static String getDefaultTraceId(){
		StringBuffer sid = new StringBuffer();

		sid.append(System.currentTimeMillis());

		for (int i=0; i< 8; i++)
		{
			sid.append((char)('a'+ThreadLocalRandom.current().nextInt(26)));
		}
		return sid.toString();
	}

    public static void destroy(){
        clear();
    }

}
