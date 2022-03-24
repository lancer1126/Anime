package org.lance.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.lance.domain.RequestHeader;

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    private static final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .setSocketTimeout(10000)
            .build();

    private static final ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setMalformedInputAction(CodingErrorAction.IGNORE)
            .setUnmappableInputAction(CodingErrorAction.IGNORE)
            .setCharset(Consts.UTF_8)
            .build();

    public static RequestHeader doGetForHeaders(String url, Map<String, String> headers) throws IOException {
        RequestHeader reqHeader = new RequestHeader();
        HttpGet httpGet = new HttpGet(url);

        List<Header> headerList = new ArrayList<>();
        if (MapUtil.isNotEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                Header header = new BasicHeader(entry.getKey(), entry.getValue());
                headerList.add(header);
            }
        }

        try (
                CloseableHttpClient httpclient = getBuilder(headerList).build();
                CloseableHttpResponse response = httpclient.execute(httpGet)) {
            Header[] allHeaders = response.getAllHeaders();
            int statusCode = response.getStatusLine().getStatusCode();
            Map<String, String> headerMap = new HashMap<>();
            for (Header header : allHeaders) {
                headerMap.put(header.getName(), header.getValue());
            }
            reqHeader.setCode(statusCode);
            reqHeader.setHeaders(headerMap);
        } catch (IOException e) {
            httpGet.abort();
            throw e;
        }
        return reqHeader;
    }

    private static HttpClientBuilder getBuilder(List<Header> headers) {
        return HttpClients.custom().setDefaultConnectionConfig(connectionConfig)
                .setDefaultHeaders(headers).setDefaultRequestConfig(requestConfig);
    }

}
