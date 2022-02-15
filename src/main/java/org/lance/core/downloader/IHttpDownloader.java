package org.lance.core.downloader;

import org.lance.common.constrants.enums.HttpDownStatus;
import org.lance.domain.entity.TaskInfo;

/**
 * 下载器母接口
 */
public interface IHttpDownloader extends Runnable{

    String getId();

    TaskInfo getTaskInfo();

    void updateStatus(HttpDownStatus status);

    void start();

    void pause();

    void resume();

    void cancel();

    boolean downloading();

    boolean retrying();

    boolean waiting();

    boolean complete();
}
