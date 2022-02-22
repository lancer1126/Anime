package org.lance.core.downloader;

import org.lance.common.enums.HttpDownStatus;
import org.lance.domain.entity.TaskInfo;

/**
 * 下载器母接口
 */
public interface IHttpDownloader extends Runnable{

    void start();

    String getId();

    TaskInfo getTaskInfo();

    void updateStatus(HttpDownStatus status);

    void pause();

    void resume();

    void cancel();

    boolean downloading();

    boolean retrying();

    boolean waiting();

    boolean complete();
}
