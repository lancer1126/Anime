package org.lance.core.downloader;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lance.common.AnimeException;
import org.lance.common.enums.HttpDownStatus;
import org.lance.common.utils.HttpDownUtil;
import org.lance.domain.entity.ChunkInfo;
import org.lance.domain.entity.ConnectInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

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
        if (paused) return;
        log.info("Download Task 开始处理，线程：" + Thread.currentThread().getName());

        RandomAccessFile raf = null;
        FileDescriptor fileDesc = null;
        BufferedOutputStream outStream = null;
        while (true) {
            try {
                raf = new RandomAccessFile(path, "rw");
                raf.seek(chunkInfo.getCurrentOffset());
                fileDesc = raf.getFD();
                outStream = new BufferedOutputStream(new FileOutputStream(fileDesc));

                connect();
                if (paused) return;

                byte[] buffer = new byte[BUFFER_SIZE];
                int readLen = 0;
                while (downloader.downloading()) {
                    readLen = inputStream.read(buffer);
                    if (readLen <= -1) {
                        downloader.onComplete(this);
                        break;
                    }
                    outStream.write(buffer, 0, readLen);
                }
                break;
            } catch (IOException ioe) {
                if (isRetry(ioe)) {
                    try {
                        log.info("request timeout. chunkInfo index's {}", chunkInfo.getIndex());
                        downloader.onRetry("请求超时尝试重新连接...");
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException ie) {
                        log.error(ie.getMessage());
                        downloader.onError("下载失败");
                        break;
                    }
                } else {
                    log.error(ioe.getMessage());
                    downloader.onError("下载失败");
                    break;
                }
            } finally {
                closeStream(outStream, fileDesc, raf);
            }
        }
    }

    public void pause() {
        paused = true;
    }

    public ChunkInfo getChunkInfo() {
        return chunkInfo;
    }

    private void connect() throws IOException {
        try {
            HttpURLConnection connect = HttpDownUtil.connect(connectInfo.getUrl(), connectInfo.getHeaders());
            connect.connect();

            int responseCode = connect.getResponseCode();
            log.info("the url[{}], is connected with code[{}] ", connectInfo.getUrl(), responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK
                    && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                throw new AnimeException(String.format("request failed. status code %s", responseCode));
            }
            inputStream = connect.getInputStream();
            if (downloader.retrying()) {
                downloader.updateStatus(HttpDownStatus.DOWNLOADING);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private void closeStream(OutputStream out, FileDescriptor fileDesc, RandomAccessFile raf) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        if (out != null) {
            try {
                out.flush();
                fileDesc.sync();
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                try {
                    raf.close();
                    out.close();
                } catch (IOException ioe) {
                    log.error(ioe.getMessage());
                }
            }
        }
    }

    private boolean isRetry(Exception e) {
        if ((e instanceof SocketTimeoutException
                || e instanceof SocketException)
                && retryTimes > 0) {
            retryTimes--;
            return true;
        }
        return false;
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
