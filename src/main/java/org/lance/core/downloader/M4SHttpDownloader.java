package org.lance.core.downloader;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.lance.common.AnimeException;
import org.lance.common.constrants.Global;
import org.lance.common.enums.HttpDownStatus;
import org.lance.common.utils.FileUtils;
import org.lance.common.utils.HttpDownUtil;
import org.lance.common.utils.HttpUtil;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.ChunkInfo;
import org.lance.domain.entity.ConnectInfo;
import org.lance.domain.entity.TaskInfo;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class M4SHttpDownloader extends DefaultHttpDownloader {

    private String videoUrl;
    private String audioUrl;
    private String filePath;
    private String videoFilePath;
    private String audioFilePath;
    private long videoTotalSize;
    private long audioTotalSize;

    private M4SHttpDownloader(RequestHeader reqHeader, TaskInfo taskInfo) {
        super(reqHeader, taskInfo);
        init();
        // bilibili支持分段下载
        isSupportRange = true;
    }

    private void init() {
        String[] urls = taskInfo.getUrl().split(Global.URL_SEPARATOR);
        videoUrl = urls[0];
        audioUrl = urls[1];
    }

    public static M4SHttpDownloader newInstance(RequestHeader reqHeader, TaskInfo taskInfo) {
        return new M4SHttpDownloader(reqHeader, taskInfo);
    }

    @Override
    public void run() {
        if (taskInfo.isPause() || taskInfo.isDownloading()) {
            return;
        }

        msg = "";
        updateStatus(HttpDownStatus.DOWNLOADING);
        callbackMinIntervalBytes = calcCallbackMinIntervalBytes(taskInfo.getTotalSize());

        try {
            trialConnect();
        } catch (Exception e) {
            onError(e.getMessage());
            return;
        }

        // 获取filePath
        filePath = checkFileExist();

        // 若当前task状态为pause，暂停任务
        if (taskInfo.isPause()) {
            return;
        }

        final int connectCount = calConnectCount(taskInfo.getTotalSize());
        taskInfo.setConnectionCount(connectCount);
        isSingleConnection = connectCount == 1;

        if (!isResume) {
            videoFilePath = HttpDownUtil.getTaskFilePathWithoutSuffix(this) + Global.M4S_VIDEO_SUFFIX;
            audioFilePath = HttpDownUtil.getTaskFilePathWithoutSuffix(this) + Global.M4S_AUDIO_SUFFIX;
            try {
                // 若当前不是恢复任务，删除原有的文件，新创建
                FileUtils.deleteFile(videoFilePath);
                FileUtils.deleteFile(audioFilePath);
                FileUtils.createFile(videoFilePath);
                FileUtils.createFile(audioFilePath);

                // 为视频和音频文件设置大小
                FileUtils.setLength(videoFilePath, videoTotalSize);
                FileUtils.setLength(audioFilePath, audioTotalSize);

                // 初始化一些下载信息
                buildChunkInfoList(taskInfo.getTotalSize(), connectCount);
            } catch (Exception e) {
                log.error("handle connection fail. {}", e.getMessage());
                onError("download fail");
            }
        }

        try {
            // 进行多线程下载
            handleWithMultipleConnection(taskInfo.getChunkInfoList());
        } catch (Exception e) {
            log.error("process connect fail");
            onError("process connect fail");
        }
    }

    @Override
    protected void buildChunkInfoList(long totalLength, int connectionCount) {
        final List<ChunkInfo> chunkInfoList = new ArrayList<>();
        ChunkInfo videoChunkInfo = new ChunkInfo();
        videoChunkInfo.setIndex(0);
        videoChunkInfo.setStartOffset(0);
        videoChunkInfo.setCurrentOffset(0);
        videoChunkInfo.setEndOffset(videoTotalSize);
        chunkInfoList.add(videoChunkInfo);

        ChunkInfo audioChunkInfo = new ChunkInfo();
        audioChunkInfo.setIndex(1);
        audioChunkInfo.setStartOffset(0);
        audioChunkInfo.setCurrentOffset(0);
        audioChunkInfo.setEndOffset(audioTotalSize);
        chunkInfoList.add(audioChunkInfo);

        taskInfo.setChunkInfoList(chunkInfoList);
    }

    @Override
    protected void handleWithMultipleConnection(List<ChunkInfo> chunkInfoList) {
        long realCurLength = 0;
        final ArrayList<DownloadTask> tempDownloadTasks = new ArrayList<>();

        for (ChunkInfo chunkInfo : chunkInfoList) {
            if (chunkInfo.getStatus() == HttpDownStatus.COMPLETE.getStatus()) {
                continue;
            }

            log.info("chunkInfo - {}", chunkInfo);
            realCurLength += (chunkInfo.getCurrentOffset() - chunkInfo.getStartOffset());
            final long contentLength = chunkInfo.getEndOffset() - chunkInfo.getCurrentOffset();
            if (contentLength <= 0) {
                chunkInfo.setStatus(HttpDownStatus.COMPLETE.getStatus());
            }
        }

        // 构建具体的下载任务
        ChunkInfo videoChunkInfo = chunkInfoList.get(0);
        log.info("videoChunkInfo - {}", videoChunkInfo);
        parseDownloadTask(videoChunkInfo, tempDownloadTasks, 1);

        ChunkInfo audioChunkInfo = chunkInfoList.get(1);
        log.info("audioChunkInfo - {}", audioChunkInfo);
        parseDownloadTask(audioChunkInfo, tempDownloadTasks, 2);

        downloadTasks = tempDownloadTasks;
        taskInfo.setCurrentOffset(realCurLength);
        speedMonitor.start(taskInfo.getCurrentOffset());

        if (!taskInfo.isPause()) {
            if (downloadTasks.isEmpty()) {
                updateStatus(HttpDownStatus.COMPLETE);
            } else {
                downloadTasks.forEach(task -> {
                    log.info(Thread.currentThread().getName() + "在执行下载任务");
                    executorService.submit(task);
                });
            }
        }
    }

    private void trialConnect() {
        if (isResume) return;

        Map<String, String> newHeaders = HttpDownUtil.addRangeForHeader(reqHeader.getHeaders(), 0);
        RequestHeader reqHeader;
        do {
            try {
                reqHeader = HttpUtil.doGetForHeaders(videoUrl, newHeaders);
                if (checkIfRequestFail(reqHeader)) {
                    log.error("trialConnect fail with status code {}", reqHeader.getCode());
                    String errMsg = reqHeader.getCode() == HttpStatus.SC_FORBIDDEN
                            ? "链接失效，请删除该下载任务并重新下载"
                            : "网络连接失败，状态码：" + reqHeader.getCode();
                    throw new AnimeException(errMsg);
                }
                String range = reqHeader.getHeaders().get("Content-Range");
                videoTotalSize = Long.parseLong(range.split("/")[1]) - 1;
                taskInfo.setTotalSize(videoTotalSize);

                reqHeader = HttpUtil.doGetForHeaders(audioUrl, newHeaders);
                if (checkIfRequestFail(reqHeader)) {
                    log.error("trialConnect fail with status code {}", reqHeader.getCode());
                    throw new AnimeException("网络连接失败，状态码：" + reqHeader.getCode());
                }
                range = reqHeader.getHeaders().get("Content-Range");
                audioTotalSize = Long.parseLong(range.split("/")[1]) - 1;
                taskInfo.addTotalSize(audioTotalSize);
                break;
            } catch (IOException e) {
                checkIfRetry(e);
            }
        } while (true);
    }

    private void checkIfRetry(Exception e) {
        if (isRetry(e)) {
            try {
                onRetry("请求超时，进行重试");
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException ie) {
                log.error(ie.getMessage());
                throw new AnimeException(ie.getMessage());
            }
        } else {
            log.error("trialConnect fail. {}", e.getMessage());
            throw new AnimeException("网络连接失败");
        }
    }

    private boolean checkIfRequestFail(RequestHeader reqHeader) {
        return reqHeader.getCode() != HttpStatus.SC_OK && reqHeader.getCode() != HttpStatus.SC_PARTIAL_CONTENT;
    }

    private String checkFileExist() {
        String tempPath = HttpDownUtil.getTaskFilePathWithMp4Suffix(this);
        if (FileUtils.existsFile(tempPath)) {
            if (isResume) {
                if (!isSupportRange) {
                    FileUtils.deleteFile(tempPath);
                }
            } else {
                String newFileName = FileUtils.renameFile(tempPath);
                taskInfo.setName(newFileName);
                tempPath = HttpDownUtil.getTaskFilePathWithMp4Suffix(this);
            }
        }
        return tempPath;
    }

    private int calConnectCount(long totalLength) {
        // 一个线程下载video，一个下载audio
        return 2;
    }

    private void parseDownloadTask(ChunkInfo chunkInfo, List<DownloadTask> downloadTasks, Integer type) {
        if (chunkInfo.getStatus() == HttpDownStatus.COMPLETE.getStatus()) {
            return;
        }

        String url = type.equals(1) ? videoUrl : audioUrl;
        String filePath = type.equals(1) ? videoFilePath : audioFilePath;
        Map<String, String> newHeaders = HttpDownUtil
                .addRangeForHeader(reqHeader.getHeaders(), chunkInfo.getCurrentOffset(), chunkInfo.getEndOffset());
        ConnectInfo connectInfo = new ConnectInfo.Builder()
                .url(url)
                .headers(newHeaders)
                .supportRange(isSupportRange)
                .build();
        DownloadTask task = new DownloadTask.Builder()
                .setPath(filePath)
                .setChunkInfo(chunkInfo)
                .setConnectInfo(connectInfo)
                .setHttpDownloader(this)
                .build();
        downloadTasks.add(task);
    }
}
