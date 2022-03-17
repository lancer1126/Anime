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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
public class DownloaderManager {

    private final Map<String, IHttpDownloader> downloaderMap;

    private final ExecutorService executorService;

    private static final Integer DOWNLOADING_TASK_COUNT_LIMIT = 3;

    public DownloaderManager() {
        executorService = ThreadContext.newExecutor(8, 20, 1000, 60,
                ThreadContext.ANIME_THREAD_DOWNLOADER);
        this.downloaderMap = new ConcurrentHashMap<>();
    }

    public static DownloaderManager getInstance() {
        return DownloaderManagerHolder.INSTANCE;
    }

    public void init() {

    }

    public void start(TaskInfo taskInfo, RequestHeader reqHeader) {
        if (taskInfo == null) return;
        synchronized (this.downloaderMap) {
            IHttpDownloader httpDownloader = buildDownloader(taskInfo, reqHeader);
            if (httpDownloader == null) {
                throw new AnimeException(-1, "创建下载器失败");
            }
            submit(httpDownloader).start();
        }
    }

    public void stop() {

    }

    public void complete(String taskId) {
        log.debug("task complete, delete taskId - {}", taskId);
        downloaderMap.remove(taskId);
    }

    public void refresh() {
        synchronized (downloaderMap) {
            Collection<IHttpDownloader> downloads = downloaderMap.values();
            final long downloadingCount = downloads.stream()
                    .filter(e -> e.downloading() || e.retrying())
                    .count();

            if (downloadingCount > DOWNLOADING_TASK_COUNT_LIMIT) {
                // 下载中的数量大于最大数量时,最大数量之后的任务都暂时暂停
                downloads.stream()
                        .filter(IHttpDownloader::downloading)
                        .skip(DOWNLOADING_TASK_COUNT_LIMIT)
                        .forEach(IHttpDownloader::pause);
            } else if (downloadingCount < DOWNLOADING_TASK_COUNT_LIMIT){
                // 若任务数量小于最大任务数，执行下载任务
                downloads.stream()
                        .filter(IHttpDownloader::waiting)
                        .limit(DOWNLOADING_TASK_COUNT_LIMIT - downloadingCount)
                        .forEach(executorService::submit);
            }
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    private IHttpDownloader buildDownloader(TaskInfo taskInfo, RequestHeader reqHeader) {
        return ParserManager.getInstance().buildDownloader(taskInfo, reqHeader);
    }

    private IHttpDownloader submit(IHttpDownloader httpDownloader) {
        IHttpDownloader existDownloader = downloaderMap.get(httpDownloader.getId());
        if (existDownloader != null) {
            return existDownloader;
        }
        downloaderMap.put(httpDownloader.getId(), httpDownloader);
        MessageCore.send(MessageType.NORMAL, HttpDownStatus.WAITING, "", httpDownloader.getTaskInfo());
        return httpDownloader;
    }

    private static class DownloaderManagerHolder {
        private static final DownloaderManager INSTANCE = new DownloaderManager();
    }
}
