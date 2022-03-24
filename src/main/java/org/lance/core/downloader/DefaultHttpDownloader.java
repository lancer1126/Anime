package org.lance.core.downloader;

import lombok.extern.slf4j.Slf4j;
import org.lance.common.enums.HttpDownStatus;
import org.lance.common.enums.MessageType;
import org.lance.core.MessageCore;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.ChunkInfo;
import org.lance.domain.entity.TaskInfo;

import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 下载器母类
 */
@Slf4j
public abstract class DefaultHttpDownloader implements IHttpDownloader {

    protected RequestHeader reqHeader;
    protected TaskInfo taskInfo;
    protected String msg;                                           // 给客户端发送提示消息
    protected int retryTimes;                                       // 重试次数
    protected volatile boolean isResume;                            // 是否是恢复下载
    protected boolean isSupportRange;                               // 是否支持分段下载
    protected boolean isSingleConnection;                           // 是否是单线程下载
    protected ExecutorService executorService;                      // 线程池
    protected ArrayList<DownloadTask> downloadTasks;                // 任务列表
    protected DownloadSpeedMonitor speedMonitor;                    // 速度监控器

    private volatile long lastCallbackTimestamp = 0;
    private volatile boolean isFirstCallbackProgressToUser = true;
    private final AtomicLong callbackIncreaseBuffer = new AtomicLong();
    private final AtomicBoolean needCallbackProgressToUser = new AtomicBoolean(false);

    protected static final int NO_ANY_PROGRESS_CALLBACK = -1;
    protected static final int CALLBACK_MIN_INTERVAL_BYTES = 1024;
    protected static final int CALLBACK_MIN_INTERVAL_MILLIS = 1000; // ms
    protected static final int CALLBACK_DEFAULT_PROGRESS_MAX_COUNT = 100;

    protected long callbackMinIntervalBytes;

    public DefaultHttpDownloader(RequestHeader reqHeader, TaskInfo taskInfo) {
        this.reqHeader = reqHeader;
        this.taskInfo = taskInfo;

        retryTimes = 3;
        isResume = false;
        isSupportRange = false;
        downloadTasks = new ArrayList<>();
        speedMonitor = new DownloadSpeedMonitor();
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
        taskInfo.setStatus(status.getStatus());
        if (status != HttpDownStatus.DOWNLOADING) {
            speedMonitor.reset();
            taskInfo.setSpeed(speedMonitor.getSpeed());
        }
        if (status == HttpDownStatus.COMPLETE) {
            DownloaderManager.getInstance().complete(taskInfo.getId());
        }
        MessageCore.send(status, msg, taskInfo);
        DownloaderManager.getInstance().refresh();
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
        return taskInfo.getStatus() == HttpDownStatus.RETRY.getStatus();
    }

    @Override
    public boolean waiting() {
        return taskInfo.getStatus() == HttpDownStatus.WAITING.getStatus();
    }

    @Override
    public boolean complete() {
        return taskInfo.getStatus() == HttpDownStatus.COMPLETE.getStatus();
    }

    public void onError(String msg) {
        this.msg = msg;
        updateStatus(HttpDownStatus.FAIL);
        if (downloadTasks.isEmpty()) return;

        @SuppressWarnings("unchecked")
        List<DownloadTask> pauseList = (ArrayList<DownloadTask>) downloadTasks.clone();
        pauseList.stream()
                .filter(Objects::nonNull)
                .forEach(DownloadTask::pause);
    }

    public void onRetry(String msg) {
        log.info("request retry, msg = {}", msg);
        this.msg = msg;
        updateStatus(HttpDownStatus.RETRY);
    }

    public void onProcess(long increaseBytes) {
        taskInfo.increaseSize(increaseBytes);
        speedMonitor.update(taskInfo.getCurrentOffset());
        taskInfo.setSpeed(speedMonitor.getSpeed());

        callbackIncreaseBuffer.addAndGet(increaseBytes);
        final long now = System.currentTimeMillis();
        checkIsCallBackToUser(now);
        handleProcess();
    }

    public boolean isRetry(Exception e) {
        // 若为下列异常以及retryTimes大于0，执行重连
        if (retryTimes > 0
                && (e instanceof SocketTimeoutException || e instanceof SocketException)) {
            retryTimes--;
            return true;
        }
        return false;
    }

    protected long calcCallbackMinIntervalBytes(long totalSize) {
        if (totalSize <= 0) {
            // 没有totalSize生成，不显示下载进度
            return NO_ANY_PROGRESS_CALLBACK;
        }
        long callbackMinIntervalBytes = totalSize / CALLBACK_DEFAULT_PROGRESS_MAX_COUNT;
        return callbackMinIntervalBytes <= CALLBACK_MIN_INTERVAL_BYTES ? CALLBACK_MIN_INTERVAL_BYTES : callbackMinIntervalBytes;
    }

    protected void buildChunkInfoList(long totalLength, int connectionCount) {

    }

    protected void handleWithMultipleConnection(final List<ChunkInfo> chunkInfoList) {

    }

    public void onComplete(String msg) {
        this.msg = msg;
        updateStatus(HttpDownStatus.COMPLETE);
    }

    public void onComplete(DownloadTask task) {
        if (isSingleConnection) {
            if (taskInfo.getCurrentOffset() < taskInfo.getTotalSize()) {
                onError("下载出现错误，请单击【resume】按钮恢复下载");
            } else {
                updateStatus(HttpDownStatus.COMPLETE);
            }
        } else {
            long currentOffset = task.getChunkInfo().getCurrentOffset();
            long endOffset = task.getChunkInfo().getEndOffset();
            if (currentOffset < endOffset) {
                onError("下载出现错误，请单击【resume】按钮恢复下载");
            } else {
                downloadTasks.remove(task);
                if (downloadTasks.isEmpty()) {
                    updateStatus(HttpDownStatus.COMPLETE);
                }
            }
        }
    }

    private void checkIsCallBackToUser(long now) {
        boolean needCallBack = false;
        if (isFirstCallbackProgressToUser) {
            isFirstCallbackProgressToUser = false;
            needCallBack = true;
        } else {
            if ((now - lastCallbackTimestamp) >= CALLBACK_MIN_INTERVAL_MILLIS
                    || ((callbackMinIntervalBytes != NO_ANY_PROGRESS_CALLBACK)
                    && callbackIncreaseBuffer.get() >= callbackMinIntervalBytes)) {
                needCallBack = true;
            }
        }

        if (needCallBack && needCallbackProgressToUser.compareAndSet(false, true)) {
            lastCallbackTimestamp = now;
            callbackIncreaseBuffer.set(0L);
        }
    }

    private void handleProcess() {
        if (needCallbackProgressToUser.compareAndSet(true, false)) {
            log.info("大小：" + taskInfo.getSpeed());
            MessageCore.send(MessageType.NORMAL, HttpDownStatus.DOWNLOADING, "", taskInfo);
        }
    }
}
