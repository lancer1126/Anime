package org.lance.core.downloader;

public class DownloadSpeedMonitor {

    private int speed;

    private long startDownloadSize;
    private long lastDownloadSize;
    private long lastRefreshTime;
    private long startTime;

    private long minIntervalUpdateSpeed = 1000;     // kb/s

    public int getSpeed() {
        return speed;
    }

    public void start(long downloadSize) {
        startDownloadSize = downloadSize;
        startTime = System.currentTimeMillis();
    }

    public void update(long downloadSize) {
        if (minIntervalUpdateSpeed <= 0) {
            return;
        }

        boolean isUpdateTime = false;
        if (lastRefreshTime == 0L) {
            isUpdateTime = true;
        } else {
            long timeInterval = System.currentTimeMillis() - lastRefreshTime;
            if (timeInterval >= minIntervalUpdateSpeed || (speed == 0 && timeInterval > 0)) {
                isUpdateTime = true;
                speed = (int) ((downloadSize - lastDownloadSize) / timeInterval * 1000);
                speed = Math.max(0, speed);
            }
        }

        if (isUpdateTime) {
            lastDownloadSize = downloadSize;
            lastRefreshTime = System.currentTimeMillis();
        }
    }

    public void reset() {
        speed = 0;
        lastRefreshTime = 0L;
    }

    public void end(long downloadSize) {
        if (startTime <= 0) {
            return;
        }

        downloadSize -= startDownloadSize;
        lastRefreshTime = 0L;
        long timeInterval = System.currentTimeMillis() - startTime;
        if (timeInterval <= 0) {
            speed = (int) downloadSize;
        } else {
            speed = (int) (downloadSize / timeInterval);
        }
    }
}
