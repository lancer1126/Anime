package org.lance.core.downloader;

import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;

public class M4SHttpDownloader extends DefaultHttpDownloader {

    private M4SHttpDownloader(RequestHeader reqHeader, TaskInfo taskInfo) {
        super(reqHeader, taskInfo);
    }

    public static M4SHttpDownloader newInstance(RequestHeader reqHeader, TaskInfo taskInfo) {
        return new M4SHttpDownloader(reqHeader, taskInfo);
    }
}
