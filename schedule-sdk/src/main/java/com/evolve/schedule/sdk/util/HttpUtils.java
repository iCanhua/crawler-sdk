package com.evolve.schedule.sdk.util;


import com.alibaba.fastjson.JSON;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Http Utils
 *
 * @author Shell
 * @since 1.0 2016-07-28
 */
public class HttpUtils {

  private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

  private static final int BUFFER_SIZE = 4096;

  private static final int SOCKET_TIMEOUT = 1200000;

  private static final int CONNECTION_TIMEOUT = 1200000;

  private static PoolingHttpClientConnectionManager cm = null;

  private static RequestConfig requestConfig = null;

  private static SSLContext sslContext = null;

  private static final CloseableHttpClient HTTP_CLIENT;

  private static ResponseHandler<String> responseHandler;

  private static HttpRequestRetryHandler retryHandler = null;

  private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";


  static {
    try {
      sslContext = SSLContexts.custom().useProtocol("SSL").build();
      sslContext.init(null, new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }}, new SecureRandom());
    } catch (Exception e) {
      logger.error("Unexpected error", e);
    }
    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", new SSLConnectionSocketFactory(sslContext)).build();
    cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    // 灏嗘渶澶ц繛鎺ユ暟澧炲姞鍒�200
    cm.setMaxTotal(200);
    // 灏嗘瘡涓矾鐢卞熀纭�鐨勮繛鎺ュ鍔犲埌20
    cm.setDefaultMaxPerRoute(20);

    requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT)
        .setConnectTimeout(CONNECTION_TIMEOUT)
        .setAuthenticationEnabled(false).build();

    responseHandler = new ResponseHandler<String>() {
      @Override
      public String handleResponse(final HttpResponse response)
          throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 400) {
          HttpEntity entity = response.getEntity();
          return entity != null ? EntityUtils.toString(entity) : null;
        } else {
          throw new ClientProtocolException("Unexpected response status: " + status);
        }
      }
    };

    retryHandler = new DelayHttpRequestRetryHandler(8, true);

    HTTP_CLIENT = buildHttpClient();
  }

  /**
   * do get
   */
  public static String get(String url, Map<String, String> params, Map<String, String> headers)
      throws Exception {
    // 璁剧疆鍙傛暟
    if (params != null) {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      for (Entry<String, String> e : params.entrySet()) {
        NameValuePair namePair = new BasicNameValuePair(e.getKey(), e.getValue());
        nameValuePairs.add(namePair);
      }
      String str = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs));
      if (url.indexOf("?") != -1) {
        url = url + "&" + str;
      } else {
        url = url + "?" + str;
      }
    }
    HttpGet httpGet = new HttpGet(url);
    httpGet.addHeader("user-agent", USER_AGENT);
    httpGet.setConfig(requestConfig);
    // set headers
    if (headers != null) {
      for (Entry<String, String> e : headers.entrySet()) {
        httpGet.setHeader(e.getKey(), e.getValue());
      }
    }
    String response = HTTP_CLIENT.execute(httpGet, responseHandler);
    return response;
  }

  /**
   * do post
   */
  public static String post(String url, Map<String, String> params, Map<String, String> headers)
      throws Exception {
    // 璁剧疆鍙傛暟
    if (params != null) {
      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      for (Entry<String, String> e : params.entrySet()) {
        NameValuePair namePair = new BasicNameValuePair(e.getKey(), e.getValue());
        nameValuePairs.add(namePair);
      }
      String str = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"), "UTF-8");
      if (url.indexOf("?") != -1) {
        url = url + "&" + str;
      } else {
        url = url + "?" + str;
      }
    }
    HttpPost httpPost = new HttpPost(url);
    httpPost.setConfig(requestConfig);
    // set headers
    if (headers != null) {
      for (Entry<String, String> e : headers.entrySet()) {
        httpPost.setHeader(e.getKey(), e.getValue());
      }
    }
    String response = HTTP_CLIENT.execute(httpPost, responseHandler);
    return response;
  }

  /**
   * do post
   */
  public static String post(String url, String body, Map<String, String> headers)
      throws Exception {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setConfig(requestConfig);
    // set headers
    if (headers != null) {
      for (Entry<String, String> e : headers.entrySet()) {
        httpPost.setHeader(e.getKey(), e.getValue());
      }
    }
    HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
    httpPost.setEntity(entity);
    String response = HTTP_CLIENT.execute(httpPost, responseHandler);
    return response;
  }

  public static HttpResponse sendRequest(String url) throws IOException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(url);
    return client.execute(request);
  }

  private static CloseableHttpClient buildHttpClient() {
    return HttpClients.custom().setConnectionManager(cm).setRetryHandler(retryHandler)
        .setUserAgent(USER_AGENT)
        .build();
  }

  /**
   * <b><code>DelayHttpRequestRetryHandler</code></b>
   * <p>
   * retery鍔犲叆寤惰繜澶勭悊
   * </p>
   */
  static class DelayHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {

    private static final Logger logger = LoggerFactory
        .getLogger(DelayHttpRequestRetryHandler.class);
    private final Integer DEFAULT_DELAY_SECONDS = 3;

    /**
     * Default constructor
     */
    protected DelayHttpRequestRetryHandler(final int retryCount,
        final boolean requestSentRetryEnabled) {
      super(retryCount, requestSentRetryEnabled);
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
      boolean isRetry = super.retryRequest(exception, executionCount, context);
      if (isRetry) {
        try {
          logger.info("Retry request will be delay,exception={},executionCount={}", exception,
              executionCount);
          TimeUnit.SECONDS.sleep(DEFAULT_DELAY_SECONDS);
        } catch (InterruptedException e) {
          logger.error("DelayHttpRequestRetryHandler error", e);
        }
      }
      return isRetry;
    }
  }

  /**
   * @return httpResponseCode
   */
  public static Integer get(String url) {
    URL _url = null;
    HttpURLConnection conn = null;
    try {
      _url = new URL(url);
      conn = (HttpURLConnection) _url.openConnection();
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      conn.setUseCaches(false);
      conn.setConnectTimeout(2000);

      conn.connect();
      int responseCode = conn.getResponseCode();
      return responseCode;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != conn) {
        conn.disconnect();
      }
    }
    return null;
  }



  public static HttpResult getWithResult(String url) {
    return getWithResult(url, 15000);
  }

  /**
   *
   * @param url
   * @param timeout millisecond
   * @return
   */
  public static HttpResult getWithResult(String url, int timeout) {
    URL _url = null;
    HttpURLConnection conn = null;

    InputStream in = null;
    InputStreamReader isr = null;
    BufferedReader br = null;

    HttpResult hr = new HttpResult();
    try {
      _url = new URL(url);
      conn = (HttpURLConnection) _url.openConnection();
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      conn.setUseCaches(false);
      conn.setConnectTimeout(2000);
      conn.setReadTimeout(timeout);
      conn.connect();
      int responseCode = conn.getResponseCode();

      if (200 == responseCode) {
        in = conn.getInputStream();
      } else {
        in = conn.getErrorStream();
      }

      if (in == null) {
        return null;
      }

      isr = new InputStreamReader(in);
      br = new BufferedReader(isr);

      StringBuilder buf = new StringBuilder();
      String line = null;
      while (null != (line = br.readLine())) {
        buf.append(line);
      }

      String responseText = buf.toString();
      hr.setResponseText(responseText);

      Map<String, String> header = new HashMap<String, String>();
      Map<String, List<String>> map = conn.getHeaderFields();
      for (String key : map.keySet()) {
        header.put(key, JSON.toJSONString(map.get(key)));
      }
      hr.setHeader(header);
      hr.setResponseCode(responseCode);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (null != br) {
        try {
          br.close();
        } catch (IOException e) {
        }
      }
      if (null != isr) {
        try {
          isr.close();
        } catch (IOException e) {
        }
      }
      if (null != in) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
      if (null != conn) {
        conn.disconnect();
      }
    }
    return hr;
  }

  /**
   * 鑾峰彇鏈�缁堢殑璺宠浆url
   * 濡傛灉鏄�302鍒欎竴鐩村線涓嬭烦,鐩村埌闈�302涓烘
   */
  public static String getFinalURL(String url) {
    try {
      HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
      con.setInstanceFollowRedirects(false);
      con.setRequestProperty("User-Agent", USER_AGENT);

      con.connect();
      con.getInputStream();

      if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
          || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
        String redirectUrl = con.getHeaderField("Location");
        return getFinalURL(redirectUrl);
      }
      return url;
    } catch (IOException e) {
      logger.warn("Exception occur! return null", e);
      return null;
    }
  }

  public static String downloadFile(String urlStr, String dir, boolean ignoreIfExists) {
    HttpURLConnection httpConn = null;
    InputStream inputStream = null;
    FileOutputStream outputStream = null;
    try {
      URL url = new URL(urlStr);
      httpConn = (HttpURLConnection) url.openConnection();
      httpConn.setRequestProperty("User-Agent", USER_AGENT);
      int responseCode = httpConn.getResponseCode();
      // always check HTTP response code first
      if (responseCode != HttpURLConnection.HTTP_OK) {
        logger.warn("Download fail, response code {} url:{}",
            responseCode, urlStr);
        return null;
      }

      String fileName = null;
      String disposition = httpConn.getHeaderField("Content-Disposition");
      if (disposition != null) {
        // extracts file name from header field
        String chartString = "filename=";
        int index = disposition.indexOf(chartString);
        if (index > -1) {
          fileName = disposition.substring(index + chartString.length(),
              disposition.length());
        }
      }
      if (fileName==null||"".equals(fileName)) {
        fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1,
            urlStr.length());
        if (!isPhotoUrl(fileName) && !isZipUrl(fileName)) {
          //to handle the case like below
          //https://xxxx/1492510350kfz.jpg?x-oss-process=image/resize,m_lfit,h_800,w_800
          fileName = Long.toString(System.currentTimeMillis()) + getFileFormat(fileName);
        }
      }

      //to remove the Double quotes if exist
      if (fileName.contains("\"")) {
        fileName = fileName.replace("\"", "");
      }
      if (fileName.contains(",")) {
        fileName = fileName.replace(",", "");
      }
      final String filePath = dir + File.separator + fileName;
      if ((!new File(filePath).exists()) || !ignoreIfExists) {
        inputStream = httpConn.getInputStream();
        outputStream = new FileOutputStream(filePath);

        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }

      return filePath;
    } catch (Exception e) {
      logger.warn("downloadFile {} failed,the reason is :{}",
          urlStr, e.getMessage());
      return null;
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (Exception e) {
        logger.warn("Close stream exception!", e);
      }
      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (Exception e) {
        logger.warn("Close stream exception!", e);
      }
      try {
        if (httpConn != null) {
          httpConn.disconnect();
        }
      } catch (Exception e) {
        logger.warn("Close stream exception!", e);
      }
    }
  }

  /**
   * download file from urlStr to filePath
   *
   * @return filePath downloaded
   */
  public static String downloadFile(String urlStr, String dir) {
    return downloadFile(urlStr, dir, false);
  }

  /**
   * 鏂囦欢涓嬭浇
   */
  public static File downloadFile(String url) {
    URL _url = null;
    HttpURLConnection conn = null;
    InputStream in = null;

    java.io.ByteArrayOutputStream out = null;
    try {
      _url = new URL(url);
      conn = (HttpURLConnection) _url.openConnection();
      in = conn.getInputStream();

      out = new java.io.ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int len = 0;
      while ((len = in.read(buf, 0, 1024)) != -1) {
        out.write(buf, 0, len);
      }

      byte[] bytes = out.toByteArray();
      File target = File.createTempFile("pg-" + url, ".png");
      org.apache.commons.io.FileUtils.writeByteArrayToFile(target, bytes);

      return target;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (null != out) {
        try {
          out.close();
        } catch (IOException e) {
        }
      }
      if (null != in) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
      if (null != conn) {
        conn.disconnect();
      }
    }

  }

  /**
   * 鍚岃繃body 浼犲弬
   */
  public static String postBody(String urlPath, String json) throws Exception {
    // Configure and open a connection to the site you will send the
    // request
    URL url = new URL(urlPath);
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    // 璁剧疆doOutput灞炴�т负true琛ㄧず灏嗕娇鐢ㄦurlConnection鍐欏叆鏁版嵁
    urlConnection.setDoOutput(true);
    // 瀹氫箟寰呭啓鍏ユ暟鎹殑鍐呭绫诲瀷锛屾垜浠缃负application/x-www-form-urlencoded绫诲瀷
    urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
    // 寰楀埌璇锋眰鐨勮緭鍑烘祦瀵硅薄
    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
    // 鎶婃暟鎹啓鍏ヨ姹傜殑Body
    out.write(json);
    out.flush();
    out.close();

    // 浠庢湇鍔″櫒璇诲彇鍝嶅簲
    InputStream inputStream = urlConnection.getInputStream();
    String encoding = urlConnection.getContentEncoding();
    String body = IOUtils.toString(inputStream, encoding);
    if (urlConnection.getResponseCode() == 200) {
      return body;
    }
    return "";
  }


  /**
   * Http response淇℃伅
   *
   * @author Shell
   * @since 1.0 2016-08-08
   */
  public static class HttpResult {

    private Integer responseCode;
    private String responseText;
    private Map<String, String> header;

    public Map<String, String> getHeader() {
      return header;
    }

    public void setHeader(Map<String, String> header) {
      this.header = header;
    }

    public String getResponseText() {
      return responseText;
    }

    public void setResponseText(String responseText) {
      this.responseText = responseText;
    }

    public Integer getResponseCode() {
      return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
      this.responseCode = responseCode;
    }
  }

  private static boolean isPhotoUrl(String url) {
    return url.endsWith(".png")
        || url.endsWith(".jpg")
        || url.endsWith(".jpe")
        || url.endsWith(".jpeg")
        || url.endsWith(".gif")
        || url.endsWith(".PNG")
        || url.endsWith(".JPG")
        || url.endsWith(".JPE")
        || url.endsWith(".JPEG")
        || url.endsWith(".GIF");
  }

  private static boolean isZipUrl(String url) {
    return url.endsWith(".zip");
  }

  private static String getFileFormat(String s) {
    if (s.contains(".png")) {
      return ".png";
    } else if (s.contains(".jpg") || s.contains(".JPG")) {
      return ".jpg";
    } else if (s.contains(".jpe") || s.contains(".JPE")) {
      return ".jpe";
    } else if (s.contains(".jpeg") || s.contains("JPEG")) {
      return ".jpeg";
    } else if (s.contains(".gif") || s.contains("GIF")) {
      return ".gif";
    } else if (s.contains(".zip")) {
      return ".zip";
    }
    return ".jpg";
  }


}
