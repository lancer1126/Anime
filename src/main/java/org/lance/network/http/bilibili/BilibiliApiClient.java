package org.lance.network.http.bilibili;

import org.lance.network.http.common.DefaultServiceClient;
import org.lance.network.http.model.PlayUrlM4SData;
import org.lance.pojo.RequestHeader;
import org.lance.pojo.entity.VideoInfo;

public class BilibiliApiClient {

    private DefaultServiceClient serviceClient;

    public BilibiliApiClient() {
        this.serviceClient = new DefaultServiceClient();
    }

    public PlayUrlM4SData getM4SFormatVideoPlayUrl(VideoInfo videoInfo, RequestHeader requestHeader) {
        //todo 获取m4类型视频的url
        return null;
    }
}
