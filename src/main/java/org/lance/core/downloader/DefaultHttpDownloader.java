package org.lance.core.downloader;

import lombok.extern.slf4j.Slf4j;
import org.lance.common.enums.HttpDownStatus;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * 下载器母类
 */
@Slf4j
public abstract class DefaultHttpDownloader implements IHttpDownloader {

    protected RequestHeader reqHeader;
    protected TaskInfo taskInfo;
    protected int retryTimes;                           // 重试次数
    protected volatile boolean isResume;                // 是否恢复下载
    protected boolean isSupportRange;                   // 是否支持分段下载
    protected ExecutorService executorService;
    protected ArrayList<DownloadTask> downloadTasks;

    public DefaultHttpDownloader(RequestHeader reqHeader, TaskInfo taskInfo) {
        this.reqHeader = reqHeader;
        this.taskInfo = taskInfo;

        retryTimes = 3;
        isResume = false;
        isSupportRange = false;
        downloadTasks = new ArrayList<>();
        executorService = DownloaderManager.getInstance().getExecutorService();
    }

    @Override
    public void run() {

    }

    @Override
    public void start() {
        if (downloading() || complete()) return;
        updateStatus(HttpDownStatus.WAITING);
    }

    @Override
    public String getId() {
        return taskInfo.getId();
    }

    @Override
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    public void updateStatus(HttpDownStatus status) {
        // todo 更新状态
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean downloading() {
        return taskInfo.getStatus() == HttpDownStatus.DOWNLOADING.getStatus();
    }

    @Override
    public boolean retrying() {
        return false;
    }

    @Override
    public boolean waiting() {
        return false;
    }

    @Override
    public boolean complete() {
        return taskInfo.getStatus() == HttpDownStatus.COMPLETE.getStatus();
    }
}
