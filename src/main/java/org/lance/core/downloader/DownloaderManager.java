package org.lance.core.downloader;

import lombok.extern.slf4j.Slf4j;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;

@Slf4j
public class DownloaderManager {

    private static final DownloaderManager INSTANCE = new DownloaderManager();

    public static DownloaderManager getInstance() {
        return INSTANCE;
    }

    public void init() {

    }

    public void start(TaskInfo taskInfo, RequestHeader requestHeader) {
        // todo 执行下载
    }

    public void stop() {

    }
}
