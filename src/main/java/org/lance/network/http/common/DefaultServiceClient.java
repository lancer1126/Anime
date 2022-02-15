package org.lance.network.http.common;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * 发送请求类
 */
public class DefaultServiceClient {

    /**
     * 使用HttpClient发送请求
     */
    public ResponseMessage sendRequestCore(RequestMessage request) {
        int timeout = 3000;
        HttpRequestBase httpRequest = createHttpRequest(request);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpRequest);
        } catch (Exception e) {
            httpRequest.abort();
            throw new RuntimeException(e.getMessage(), e);
        }
        return parseResponse(httpResponse);
    }

    private HttpRequestBase createHttpRequest(RequestMessage request) {
        HttpGet getMethod = new HttpGet(request.getUri());
        if (request.getHeaders() != null && request.getHeaders().size() > 0) {
            for (String key : request.getHeaders().keySet()) {
                getMethod.addHeader(key, request.getHeaders().get(key));
            }
        }
        return getMethod;
    }

    private ResponseMessage parseResponse(HttpResponse httpResponse) {
        if (httpResponse == null) return null;

        ResponseMessage response = new ResponseMessage();
        if (httpResponse.getStatusLine() != null) {
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        }
        if (httpResponse.getEntity() != null && response.getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = httpResponse.getEntity();
            try {
                String content = EntityUtils.toString(entity, Consts.UTF_8);
                EntityUtils.consumeQuietly(entity);
                response.setBody(content);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return response;
    }
}
