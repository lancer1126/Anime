package org.lance.core.downloader;

import lombok.extern.slf4j.Slf4j;
import org.lance.common.AnimeException;
import org.lance.common.enums.HttpDownStatus;
import org.lance.common.enums.MessageType;
import org.lance.core.MessageCore;
import org.lance.core.ThreadContext;
import org.lance.core.parser.ParserManager;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
public class DownloaderManager {

    private final Map<String, IHttpDownloader> downloadTaskMap;

    private final ExecutorService executorService;

    public DownloaderManager() {
        executorService = ThreadContext.newExecutor(8, 20, 1000, 60,
                ThreadContext.ANIME_THREAD_DOWNLOADER);
        this.downloadTaskMap = new ConcurrentHashMap<>();
    }

    public static DownloaderManager getInstance() {
        return DownloaderManagerHolder.INSTANCE;
    }

    public void init() {

    }

    public void start(TaskInfo taskInfo, RequestHeader reqHeader) {
        if (taskInfo == null) return;
        synchronized (this.downloadTaskMap) {
            IHttpDownloader httpDownloader = buildDownloader(taskInfo, reqHeader);
            if (httpDownloader == null) {
                throw new AnimeException(-1, "创建下载器失败");
            }
            submit(httpDownloader).start();
        }
    }

    public void stop() {

    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    private IHttpDownloader buildDownloader(TaskInfo taskInfo, RequestHeader reqHeader) {
        return ParserManager.getInstance().buildDownloader(taskInfo, reqHeader);
    }

    private IHttpDownloader submit(IHttpDownloader httpDownloader) {
        IHttpDownloader existDownloader = downloadTaskMap.get(httpDownloader.getId());
        if (existDownloader != null) {
            return existDownloader;
        }
        downloadTaskMap.put(httpDownloader.getId(), httpDownloader);
        DefaultHttpDownloader defaultDownloader = (DefaultHttpDownloader)httpDownloader;
        MessageCore.send(MessageType.NORMAL, HttpDownStatus.WAITING, "", defaultDownloader.taskInfo);
        return defaultDownloader;
    }

    private static class DownloaderManagerHolder {
        private static final DownloaderManager INSTANCE = new DownloaderManager();
    }
}
