package org.lance.core.downloader;

public class DownloadTask implements Runnable {

    private volatile boolean paused;

    @Override
    public void run() {
        // todo 下载任务
    }

    public void pause() {
        paused = true;
    }
}
