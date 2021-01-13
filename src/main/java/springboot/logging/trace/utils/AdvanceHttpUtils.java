package springboot.logging.trace.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * advance http utils enable trace id.
 */
public class AdvanceHttpUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvanceHttpUtils.class);

	/**
	 * HTTP request header transfer trace id sign.
	 */
	public static final String HDR_FIELD_X_TRACE_ID = "x-trace-id";
	
	private final SimpleHttpClient httpClient = new SimpleHttpClient();
	
	private int rspLimit = 2048;
	private int defaultConnectTimeout = 20000;
	private int defaultReadTimeout = 30000;
	
	private String contentType = "application/x-www-form-urlencoded";
	private String charset = "UTF-8";
	
	public String postDirectly(String url, String params) {
		return executeDirectly(url, "POST", params, defaultConnectTimeout, defaultReadTimeout, true);
	}
	
	public String postDirectly(String url, String params, boolean enableTrace) {
		return executeDirectly(url, "POST", params, defaultConnectTimeout, defaultReadTimeout, enableTrace);
	}
	
	public String postDirectly(String url, String params, boolean enableTrace, int timeout) {
		return executeDirectly(url, "POST", params, timeout, timeout, enableTrace);
	}
	
	public String postDirectly(String url, String params, int timeout) {
		return executeDirectly(url, "POST", params, timeout, timeout, true);
	}
	
	public String getDirectly(String url) {
		return executeDirectly(url, "GET", null, defaultConnectTimeout, defaultReadTimeout, true);
	}
	
	public String getDirectly(String url, boolean enableTrace) {
		return executeDirectly(url, "GET", null, defaultConnectTimeout, defaultReadTimeout, enableTrace);
	}
	
	public String getDirectly(String url, boolean enableTrace, int timeout) {
		return executeDirectly(url, "GET", null, timeout, timeout, enableTrace);
	}
	
	public String getDirectly(String url, int timeout) {
		return executeDirectly(url, "GET", null, timeout, timeout, true);
	}
	
	String executeDirectly(String url, String method, String params, int connectTimeout, int readTimeout, boolean enableTrace) {
		StringBuilder result = new StringBuilder();
		
		httpClient.setConnectTimeout(connectTimeout);
		httpClient.setReadTimeout(readTimeout);

		int sc;
		
		final String sid = TraceUtils.getTraceId();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			if (LOGGER.isInfoEnabled()){
				LOGGER.info("{}|URL:{}|BODY:{}",method,url,params);
			}
			
			HashMap<String, String> headers = new HashMap<String, String>();
			
			headers.put("Accept", "*/*");
			headers.put("Accept-Encoding", "gzip");
			headers.put("Content-Type", contentType);
			if (enableTrace){
				headers.put(HDR_FIELD_X_TRACE_ID, sid);
			}

			
			byte[] data = null;
			if (params != null) {
				data = params.getBytes(charset);
			}
			
			sc = httpClient.executeHttpRequest(url, method, headers, data, out, null);
			
			if (sc != 200) {
				LOGGER.warn(method+"|"+sid+"|SC="+sc);
			}
		} catch (Exception e) {
			LOGGER.error(method+"|"+sid+"|ERR="+e.getMessage(), e);
		}
		
		// dump response message
		if (out.size() > 0) {
			BufferedReader in = null;
			
			try {
				in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), charset));
				String line;
				while ((line=in.readLine()) != null) {
					result.append(line);  
				}
				
				if (LOGGER.isInfoEnabled()) {
					String rss = result.toString();
					
					if (rss.startsWith("------ERROR------") || rspLimit < 0 || result.length() <= rspLimit){
						LOGGER.info("{}|RSP:{}",method,filterSensitiveData(rss));
					}
					else{
						LOGGER.info("{}|RSP:{}...",method,filterSensitiveData(rss.substring(0, rspLimit)));
					}
				}
			} catch (Exception e) {
				LOGGER.error(method+"|"+sid+"|ERR="+e.getMessage(), e);
			} finally {
				closeQuietly(in);
			}
		}
		
		return result.toString();
	}
	
	
	protected String filterSensitiveData(String msg) {
		return msg;
	}
	
	private void closeQuietly(Closeable clos) {
		try {
			if (clos != null){
				clos.close();
			}
		} catch (IOException e) {
		}
	}

	public void setConnectTimeout(int connectTimeout) {
		this.defaultConnectTimeout = connectTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.defaultReadTimeout = readTimeout;
	}
	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setRspLimit(int rspLimit) {
		this.rspLimit = rspLimit;
	}

}
