package org.lance.network.http.bilibili;

import java.util.HashMap;
import java.util.Map;

/**
 * 关于b站请求header的一些处理
 */
public class BiliHttpHeaders {

    final static String UA_PC_Chrome = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    /**
     * 该Header配置用于M4s视频下载
     */
    public static HashMap<String, String> getBilibiliM4sHeaders(String bvId) {
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Origin", "https://www.bilibili.com");
        headerMap.put("Referer", "https://www.bilibili.com/video/" + bvId);// need addbvId
        headerMap.put("User-Agent", UA_PC_Chrome);
        return headerMap;
    }
}
