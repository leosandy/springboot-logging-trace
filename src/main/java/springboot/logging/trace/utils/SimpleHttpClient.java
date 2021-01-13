package springboot.logging.trace.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Http Client
 */
public class SimpleHttpClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);
	
	private int defaultConnectTimeout = 20000;
	private int defaultReadTimeout = 30000;

	public int httpGet(String url, Map<String, String> reqHeaders, OutputStream rspOut, Map<String, String> rspHeaders) throws Exception {
		return executeHttpRequest(url, "GET", reqHeaders, null, rspOut, rspHeaders);
	}
	
	public int httpPost(String url, Map<String, String> reqHeaders, byte[] data, OutputStream rspOut, Map<String, String> rspHeaders) throws Exception {
		return executeHttpRequest(url, "POST", reqHeaders, data, rspOut, rspHeaders);
	}
	
	int executeHttpRequest(String url, String method, Map<String, String> headers, byte[] data, OutputStream out, Map<String, String> rspHeaders) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(method.toUpperCase()+" "+url+" "+"HTTP/1.1");
		}
		
		int status = -1;
		
		HttpURLConnection urlc = null;
		OutputStream netout = null;
		InputStream netin = null;
		
		try {
			urlc = getConnection(url);

			urlc.setRequestMethod(method);
			
			if (headers != null) {
				Iterator<String> ite = headers.keySet().iterator();
				while (ite.hasNext()) {
					String name = ite.next();
					String value = headers.get(name);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("[REQ HDR]"+name+": "+value);
					}
					urlc.addRequestProperty(name, value);
				}
			}

			boolean isGet = method.equalsIgnoreCase("GET");
			
			if (!isGet) {
				urlc.setDoOutput(!isGet);
				
				final boolean hasBody = (data != null && data.length > 0);
				if (hasBody) {
					urlc.addRequestProperty("Content-Length", String.valueOf(data.length));
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("[REQ HDR]Content-Length: "+String.valueOf(data.length));
					}
				}
				
				netout = urlc.getOutputStream();
				
				if (hasBody) {
					netout.write(data);
					netout.flush();
				}
			}
			
			status = urlc.getResponseCode();
			if (LOGGER.isDebugEnabled()){
				LOGGER.debug("HTTP/1.1 "+status);
			}
			
			/*
			 * java.io.IOException: Server returned HTTP response code: 502 for URL: http://100.98.24.108/lottery//adminOrder/list
			 * 	at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1627)
			 */
			if (status < 400) {
				netin = urlc.getInputStream();
			} else {
				netin = urlc.getErrorStream();
			}
			
			if (status >= 400) {
				sendErrorLog(url, status, null);
			}
			
			if (rspHeaders != null) {
				for (String hdr:urlc.getHeaderFields().keySet())
				{
					/*
					 * On Coolpad 8190, response status line 'HTTP/1.1 206 Partial Content' has been treated as header, 
					 *  this makes hdr is null, which cause a NullPointerException
					 */
					if (hdr == null || hdr.length()==0) {
						continue;
					}
					
					String hdrVal = urlc.getHeaderField(hdr);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("[RSP HDR]"+hdr+": "+hdrVal);
					}
					
					rspHeaders.put(hdr.toLowerCase(), hdrVal);
				}
			}
			
			String contentEnc = urlc.getContentEncoding();
			
			if (contentEnc != null && netin != null) {
				if (contentEnc.equalsIgnoreCase("gzip")) {
					netin = new GZIPInputStream(netin);
				} else if (contentEnc.equalsIgnoreCase("deflate")) {
					netin = new DeflaterInputStream(netin);
				}
			}
			
			dumpResponseEntity(netin, out, contentEnc);
		} catch (Exception e) {
			LOGGER.error("[REQ FAILED]URL="+url, e);
			
			InputStream errIn = urlc.getErrorStream();
			if (errIn != null) {
				out.write("------ERROR------\r\n".getBytes());
				dumpResponseEntity(errIn, out, null);
				out.write("------ERROR------\r\n".getBytes());
			}
			
			if (status == -1) {
				status = parseHttpStatusFromException(e);
			}
			sendErrorLog(url, status, e);

			if (status == 0){
				throw e;
			}
		} finally {
			closeQuietly(netout);
			closeQuietly(netin);
			
			if (urlc != null){
				urlc.disconnect();
			}
		}
		
		return status;
	}
	
	protected int parseHttpStatusFromException(Exception e) {
		String eclass = e.getClass().getName();
		
		/*
		 * java.io.IOException(Server returned HTTP response code: 502 for URL: http://100.98.24.108/lottery//adminUserInfo/accountDetailList)
java.io.FileNotFoundException(http://10.157.163.41/chargecenter/card/list)
java.net.SocketTimeoutException(Read timed out)
java.net.SocketTimeoutException(connect timed out)
		 */
		if (eclass.contains("FileNotFoundException")) {
			return 404;
		}
		
		String emessage = e.getMessage();
		try {
			if (eclass.contains("IOException")) {
				int pos1 = emessage.indexOf("response code:");
				if (pos1 > 0) {
					int pos2 = emessage.indexOf("for", pos1);
					if (pos2 > pos1) {
						return Integer.valueOf(emessage.substring(pos1+"response code:".length(), pos2).trim());
					}
				}
			}
		} catch (Exception e1) {
		}
		
		return 0;
	}
	
	protected HttpURLConnection getConnection(String url) throws IOException, GeneralSecurityException {
		HttpURLConnection urlc = (HttpURLConnection)new URL(url).openConnection();
		
		urlc.setConnectTimeout(defaultConnectTimeout);
		urlc.setReadTimeout(defaultReadTimeout);
		
		urlc.setInstanceFollowRedirects(true);
		
		if (urlc instanceof HttpsURLConnection) {
			HttpsURLConnection httpsUrlc = (HttpsURLConnection)urlc;
			
			SSLContext sslContext = SSLContext.getInstance("TLS");
			
			sslContext.init(null, new TrustManager[]{ new AllAcceptedTrustManager() }, new java.security.SecureRandom());
			
			httpsUrlc.setSSLSocketFactory(sslContext.getSocketFactory());
			
			httpsUrlc.setHostnameVerifier((home,session) -> true );
		}
		
		return urlc;
	}
	
	static class AllAcceptedTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
		
	}
	
	protected int dumpResponseEntity(InputStream nis, OutputStream out, String contentEnc) throws IOException {
		if (out == null || nis == null) {
			return 0;
		}
		
		int len = 0;
		int total = 0;
		byte[] buffer = new byte[512];
		
		while ((len=nis.read(buffer)) != -1)
		{
			total += len;
			out.write(buffer, 0, len);
		}
		
		return total;
	}

	private void sendErrorLog(String url, int sc, Exception e) {

	}

	private void closeQuietly(Closeable clos) {
		try {
			if (clos != null){
				clos.close();
			}
		} catch (IOException e) {
			//ignore
		}
	}
	
	public void setConnectTimeout(int connectTimeout) {
		this.defaultConnectTimeout = connectTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.defaultReadTimeout = readTimeout;
	}
}
