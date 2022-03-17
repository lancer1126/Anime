package org.lance.core.downloader;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.lance.common.AnimeException;
import org.lance.common.constrants.Global;
import org.lance.common.enums.HttpDownStatus;
import org.lance.common.utils.HttpDownUtil;
import org.lance.common.utils.HttpUtil;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class M4SHttpDownloader extends DefaultHttpDownloader {

    private String videoUrl;
    private String audioUrl;
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
                    // 请求失败
                    log.error("trialConnect fail with status code {}", reqHeader.getCode());
                    String errMsg = reqHeader.getCode() == HttpStatus.SC_FORBIDDEN
                            ? "链接失效，请删除该下载任务并重新下载"
                            : "网络连接失败，状态码：" + reqHeader.getCode();
                    throw new AnimeException(errMsg);
                }

                // 请求成功
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
        } while (true);
    }

    private boolean checkIfRequestFail(RequestHeader reqHeader) {
        return reqHeader.getCode() != HttpStatus.SC_OK && reqHeader.getCode() != HttpStatus.SC_PARTIAL_CONTENT;
    }
}
