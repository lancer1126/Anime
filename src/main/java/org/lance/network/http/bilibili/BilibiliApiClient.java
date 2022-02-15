package org.lance.network.http.bilibili;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lance.common.AnimeException;
import org.lance.common.constrants.ApiConfig;
import org.lance.common.constrants.enums.VideoTypeFormat;
import org.lance.network.http.common.DefaultServiceClient;
import org.lance.network.http.common.RequestMessage;
import org.lance.network.http.common.ResponseMessage;
import org.lance.network.http.response.GenericResponse;
import org.lance.network.http.response.PlayUrlM4SDataResp;
import org.lance.network.http.response.ResponseCode;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.VideoInfo;

import java.util.Map;

@Slf4j
public class BilibiliApiClient {

    private final DefaultServiceClient serviceClient;

    public BilibiliApiClient() {
        this.serviceClient = new DefaultServiceClient();
    }

    public PlayUrlM4SDataResp getM4SFormatVideoPlayUrl(VideoInfo videoInfo, RequestHeader requestHeader) throws AnimeException {
        String url = String.format(ApiConfig.BILIBILI_PLAYURL, videoInfo.getBId(), videoInfo.getCId(), VideoTypeFormat.M4S.getValue(), "");
        return doRequest(url, requestHeader, PlayUrlM4SDataResp.class);
    }

    protected <T extends GenericResponse> T doRequest(String url, RequestHeader requestHeader, Class<T> clazz) throws AnimeException {
        RequestMessage request = buildRequest(url, requestHeader.getHeaders());
        ResponseMessage responseMessage = sendRequest(request);
        T response = parseResponse(responseMessage, clazz);
        checkResponse(response);
        return response;
    }

    protected RequestMessage buildRequest(String url, Map<String, String> headers) {
        return new RequestMessage(url, headers);
    }

    protected ResponseMessage sendRequest(RequestMessage request) {
        return serviceClient.sendRequestCore(request);
    }

    protected <T> T parseResponse(ResponseMessage responseMessage, Class<T> clazz) {
        if (responseMessage == null || StringUtils.isBlank(responseMessage.getBody())) {
            return null;
        }
        log.info("Response Data: " + responseMessage.getBody());
        return JSON.parseObject(responseMessage.getBody(), clazz);
    }

    protected void checkResponse(GenericResponse genericResponse) throws AnimeException {
        if (!genericResponse.getCode().equals(ResponseCode.SUCCESS)) {
            throw new AnimeException(genericResponse.getCode(), genericResponse.getMessage());
        }
    }
}
