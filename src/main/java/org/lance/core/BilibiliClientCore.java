package org.lance.core;

import org.lance.network.http.bilibili.BilibiliApiClient;

public class BilibiliClientCore {

    private static BilibiliApiClient bilibiliApiClient = null;

    public static void init() {
        bilibiliApiClient = new BilibiliApiClient();
    }

    public static BilibiliApiClient getBilibiliClient() {
        return bilibiliApiClient;
    }
}
