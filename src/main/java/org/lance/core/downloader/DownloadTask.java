package org.lance.core.downloader;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lance.common.AnimeException;
import org.lance.domain.entity.ChunkInfo;
import org.lance.domain.entity.ConnectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

@Slf4j
public class DownloadTask implements Runnable {

    private static final int BUFFER_SIZE = 1024 * 4;

    private final String path;
    private final ChunkInfo chunkInfo;
    private final ConnectInfo connectInfo;
    private final DefaultHttpDownloader downloader;
    private InputStream inputStream;
    private int retryTimes;
    private volatile boolean paused;


    public DownloadTask(String path, ChunkInfo chunkInfo, ConnectInfo connectInfo,
                            DefaultHttpDownloader downloader) {
        this.path = path;
        this.chunkInfo = chunkInfo;
        this.connectInfo = connectInfo;
        this.paused = false;
        this.downloader = downloader;
        retryTimes = 3;
    }

    @Override
    public void run() {
        // todo 下载任务
    }

    public void pause() {
        paused = true;
    }

    public static class Builder {
        private String path;
        private ChunkInfo chunkInfo;
        private ConnectInfo connectInfo;
        private DefaultHttpDownloader hostRunner;

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setChunkInfo(ChunkInfo chunkInfo) {
            this.chunkInfo = chunkInfo;
            return this;
        }

        public Builder setConnectInfo(ConnectInfo connectInfo) {
            this.connectInfo = connectInfo;
            return this;
        }

        public Builder setHttpDownloader(DefaultHttpDownloader hostRunner) {
            this.hostRunner = hostRunner;
            return this;
        }

        public DownloadTask build() {
            if (StringUtils.isBlank(path) || hostRunner == null)
                throw new AnimeException("missing parameters");
            return new DownloadTask(path, chunkInfo, connectInfo, hostRunner);
        }

    }
}
