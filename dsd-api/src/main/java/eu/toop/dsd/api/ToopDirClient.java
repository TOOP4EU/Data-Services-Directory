/**
 * Copyright 2021 - TOOP Project
 *
 * This file and its contents are licensed under the EUPL, Version 1.2
 * or – as soon they will be approved by the European Commission – subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *       https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.toop.dsd.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.SimpleURL;

/**
 * This class is the bridge between DSD and TOOP directory. It queries the TOOP
 * directory and returns the responses as a list of <code>MatchType</code>
 * objects
 *
 * @author yerlibilgin
 */
public class ToopDirClient {

  public static void disableSSLVerification() {

    TrustManager[] trustAllCerts = new TrustManager[]{new X509ExtendedTrustManager() {
      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {

      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {

      }

      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {

      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {

      }

      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      @Override
      public void checkClientTrusted(X509Certificate[] certs, String authType) {
      }

      @Override
      public void checkServerTrusted(X509Certificate[] certs, String authType) {
      }

    }};

    SSLContext sc = null;
    try {
      sc = SSLContext.getInstance("TLS");
      sc.init(null, trustAllCerts, null);
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
  }


  static {
    disableSSLVerification();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ToopDirClient.class);

  /**
   * Query TOOP-DIR with country code and doctype. Return a String that contains the result
   *
   * @param toopDirBaseURL the base URL of Toop Directory, <code>mandatory</code>
   * @param sCountryCode   two letter Country Code, <code>mandatory</code>
   * @return a String that contains the result
   * @throws IOException if a communication problem occurs
   */
  public static String callSearchApiWithCountryCode(final String toopDirBaseURL, @Nullable final String sCountryCode) throws IOException {
    ValueEnforcer.notEmpty(toopDirBaseURL, "toopDirBaseURL");
    ValueEnforcer.notEmpty(sCountryCode, "sCountryCode");

    // Build base URL and fetch all records per HTTP request
    final SimpleURL aBaseURL = new SimpleURL(toopDirBaseURL + "/search/1.0/xml");
    // More than 1000 is not allowed
    aBaseURL.add("rpc", 100);

    // Parameters to this servlet
    if (sCountryCode != null && !sCountryCode.isEmpty()) {
      aBaseURL.add("country", sCountryCode);
    }

    return callSearchApi(aBaseURL);
  }

  /**
   * Query TOOP-DIR with dpType. Return a String that contains the result
   *
   * @param toopDirBaseURL the base URL of Toop Directory, <code>mandatory</code>
   * @param dpType         data provider type, <code>mandatory</code>
   * @return a String that contains the result
   * @throws IOException if a communication problem occurs
   */
  public static String callSearchApiForDpType(@Nonnull final String toopDirBaseURL,
                                              @Nonnull final String dpType) throws IOException {
    ValueEnforcer.notEmpty(toopDirBaseURL, toopDirBaseURL);
    ValueEnforcer.notEmpty(dpType, dpType);

    if (StringHelper.hasNoText(toopDirBaseURL))
      throw new IllegalStateException("The Directory base URL configuration is missing");

    // Build base URL and fetch all records per HTTP request
    final SimpleURL aBaseURL = new SimpleURL(toopDirBaseURL + "/search/1.0/xml");
    // More than 1000 is not allowed
    aBaseURL.add("rpc", 100);
    aBaseURL.add("identifierScheme", "DataProviderType");

    if (dpType != null && !dpType.isEmpty()) {
      aBaseURL.add("identifierValue", dpType);
    }


    return callSearchApi(aBaseURL);
  }


  private static String callSearchApi(SimpleURL aBaseURL) throws IOException {
    if (LOGGER.isInfoEnabled())
      LOGGER.info("Querying " + aBaseURL.getAsStringWithEncodedParameters());

    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      final HttpGet aGet = new HttpGet(aBaseURL.getAsURI());

      final HttpResponse response = httpClient.execute(aGet);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new IllegalStateException("Request failed " + response.getStatusLine().getStatusCode());
      }

      try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
        response.getEntity().writeTo(stream);
        final byte[] s_bytes = stream.toByteArray();

        final String s_result = new String(s_bytes, StandardCharsets.UTF_8);
        return s_result;
      }
    }
  }

}
