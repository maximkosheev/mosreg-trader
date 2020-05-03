package ru.monsterdev.mosregtrader.services.impl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.monsterdev.mosregtrader.http.TraderRequest;
import ru.monsterdev.mosregtrader.http.TraderResponse;
import ru.monsterdev.mosregtrader.services.HttpService;

@Slf4j
@Service
@Qualifier("httpService")
@Scope("prototype")
public class HttpServiceImpl implements HttpService {

  @Autowired
  private CookieStore cookieStore;

  @Override
  public TraderResponse sendRequest(TraderRequest request) {
    try {
      TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }
          }
      };

      SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

      SSLConnectionSocketFactory sslConnectionSocketFactory =
          new SSLConnectionSocketFactory(sslContext);

      CloseableHttpClient httpClient = HttpClientBuilder.create()
          .setDefaultCookieStore(cookieStore)
          .setSSLSocketFactory(sslConnectionSocketFactory)
          .build();

      log.trace("Http request << " + request.toString());
      HttpResponse httpResponse = httpClient.execute(request.getType() == TraderRequest.RequestType.POST
          ? request.getPOSTRequest()
          : request.getGETRequest());
      TraderResponse response = new TraderResponse();
      response.setCode(httpResponse.getStatusLine().getStatusCode());
      response.setEntity(httpResponse.getEntity() != null ? EntityUtils.toString(httpResponse.getEntity()) : null);
      Header contentHeader = httpResponse.getFirstHeader("Content-Type");
      if (contentHeader != null && contentHeader.getValue().contains("text/html")) {
        log.trace("Http response >> BIG HTML PAGE");
      } else {
        log.trace("Http response >> " + response.getEntity());
      }
      return response;
    } catch (Exception ex) {
      log.error("", ex);
      return null;
    }
  }
}
