package springboot.logging.trace.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

/**
 * multipart/form-data post data transfer.
 * @author leo
 */
public class MultipartUploadHttpUtils {

    private static final String PREFIX = "--", LINE = "\r\n";

    private static final String MULTIPART_FROM_DATA = "multipart/form-data";

    private static final String CHARSET = "UTF-8";

    private static final int MAX_BUFFER_SIZE = 16 * 1024;

    private static final int DEFAULT_READ_TIMEOUT = 10000;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;

    public static String post(String url,HttpServletRequest request){
        return post(url,request, DEFAULT_READ_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT,true);
    }

    public static String post(String url,HttpServletRequest request,int readTimeout,int connectionTimeout,boolean enableTrace){
        //边界线
        String boundary = java.util.UUID.randomUUID().toString().replaceAll("-","");
        StringBuilder result = new StringBuilder("");
        BufferedReader in = null;
        HttpURLConnection conn = null;
        DataOutputStream outStream = null;
        try {
            conn = getHttpURLConnection(url, boundary,readTimeout,connectionTimeout,enableTrace);
            outStream = new DataOutputStream(conn.getOutputStream());
            parameterBuilder(request,boundary,outStream);

            byte[] end = (PREFIX + boundary + PREFIX + LINE).getBytes();
            outStream.write(end);
            outStream.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), CHARSET));
            String line = in.readLine();
            while (line != null) {
                result.append(line);
                line = in.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeGracefully(outStream,in,conn);
        }
        return result.toString();
    }


    private static void parameterBuilder(HttpServletRequest request,String boundary,DataOutputStream outputStream) throws ServletException,IOException{
        if (!request.getContentType().startsWith(MULTIPART_FROM_DATA)){
            throw new RuntimeException("content-type is not multipart/form-data");
        }
        Collection<Part> parts = request.getParts();
        //文本类型的参数
        StringBuilder paramBuilder = new StringBuilder();
        for (Part part : parts) {

            if (part.getSubmittedFileName() == null){
                paramBuilder.append(PREFIX);
                paramBuilder.append(boundary);
                paramBuilder.append(LINE);
                paramBuilder.append("Content-Disposition: form-data; name=\"")
                        .append(part.getName()).append("\"").append(LINE);
                paramBuilder.append("Content-Type: text/plain; charset=").append(CHARSET).append(LINE);
                paramBuilder.append("Content-Transfer-Encoding: 8bit" + LINE);
                paramBuilder.append(LINE);
                paramBuilder.append(request.getParameter(part.getName()));
                paramBuilder.append(LINE);
            }
        }

        if (paramBuilder.length() > 0){
            outputStream.writeUTF(paramBuilder.toString());
        }

        for (Part part : parts) {
            //附件part
            if (part.getSubmittedFileName() != null){
                StringBuilder attachmentBuilder = new StringBuilder();
                attachmentBuilder.append(PREFIX);
                attachmentBuilder.append(boundary);
                attachmentBuilder.append(LINE);
                attachmentBuilder.append("Content-Disposition: form-data; ")
                        .append("filename=\"")
                        .append(part.getSubmittedFileName()).append("\";")
                        .append(" name=\"").append(part.getName()).append("\"").append(LINE);
                attachmentBuilder.append("Content-Type: ").append(StringUtil.isBlank(part.getContentType())
                        ? "application/octet-stream":part.getContentType())
                        .append("; charset=").append(CHARSET)
                        .append(LINE).append(LINE);
                outputStream.write(attachmentBuilder.toString().getBytes());
                byte[] buffer = new byte[1024];
                int len;
                InputStream inputStream = part.getInputStream();
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                inputStream.close();
                outputStream.write(LINE.getBytes());
            }
        }
    }

    private static HttpURLConnection getHttpURLConnection(String url, String boundary,int readTimeout,int connTimeout,boolean enableTrace) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        URL uri = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(readTimeout);
        conn.setConnectTimeout(connTimeout);
        // 允许输入
        conn.setDoInput(true);
        // 允许输出
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        if (enableTrace){
            conn.setRequestProperty(AdvanceHttpUtils.HDR_FIELD_X_TRACE_ID,TraceUtils.getTraceId());
        }
        conn.setRequestProperty("Charset", CHARSET);
        conn.setRequestProperty("Content-Type", String.format("%s;boundary=%s",MULTIPART_FROM_DATA,boundary));
        // 设置大文件分块上传
        conn.setChunkedStreamingMode(MAX_BUFFER_SIZE);
        // Although setChunkedStreamingMode sets this header, setting it explicitly here works
        // around an OutOfMemoryException when using https.
        conn.setRequestProperty("Transfer-Encoding", "chunked");

        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection httpsUrlc = (HttpsURLConnection)conn;

            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, new TrustManager[]{ new SimpleHttpClient.AllAcceptedTrustManager() }, new java.security.SecureRandom());

            httpsUrlc.setSSLSocketFactory(sslContext.getSocketFactory());

            httpsUrlc.setHostnameVerifier((host,session) -> true);
        }

        return conn;
    }

    private static void closeGracefully(OutputStream outStream, Reader in, HttpURLConnection conn){
        try {
            if (outStream != null) {
                outStream.close();
            }
            if (in != null) {
                in.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        } catch (IOException e) {
            //Ignore
        }
    }
}
