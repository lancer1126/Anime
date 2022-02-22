package org.lance.core.downloader;

import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;

public class FlvHttpDownloader extends DefaultHttpDownloader {
    private FlvHttpDownloader(RequestHeader reqHeader, TaskInfo taskInfo) {
        super(reqHeader, taskInfo);
    }

    public static FlvHttpDownloader newInstance(RequestHeader reqHeader, TaskInfo taskInfo) {
        return new FlvHttpDownloader(reqHeader, taskInfo);
    }
}
