/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.lib;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author José Fernando
 */
public class Client {

    private OkHttpClient httpClient;
    private OkHttpClient http2Client;
    
    public Client() throws NoSuchAlgorithmException, KeyManagementException
    {
         TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                    return myTrustedAnchors;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        http2Client = new OkHttpClient();
        http2Client.setSslSocketFactory(sslSocketFactory);
        http2Client.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        
        httpClient = http2Client.clone();
        
        httpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
        http2Client.setProtocols(Arrays.asList(Protocol.HTTP_2));
    }
    
    
    public int test(String url) throws IOException, MalformedURLException
    {
        int successful = 0;
        URL entry = new URL(url);

        Request request = new Request.Builder().url(entry.toString()).build();

        Response response = httpClient.newCall(request).execute();
        System.out.println(response.protocol());

        try {
            JSONObject json = new JSONObject(response.body().string());
            
            JSONArray urls = json.getJSONArray("urls");
            for (int i = 0; i < urls.length(); ++i) {
                response = request(http2Client, new URL(urls.getString(i)));
                if (response != null) {
                    successful++;
                }
            }

            URL finish = new URL(
                    entry.getProtocol(),
                    entry.getHost(),
                    entry.getPort(),
                    json.getString("finish")
            );
            request(httpClient, finish);

        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        
        return successful;
    }
    
    
    private Response request(OkHttpClient client, URL url) {
        Response response = null;

        try {
            System.out.println(client.getProtocols().get(0) + " => " + url.toString());

            Request request = new Request.Builder().url(url.toString()).build();
            response = client.newCall(request).execute();

            System.out.println("> " + response.code() + " " + response.protocol());

        } catch (ConnectException e) {
            System.out.println("ConnectException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

        return response;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try {
            Client client = new Client();
            client.test("https://ametrics.it.uc3m.es/start/win");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
}
