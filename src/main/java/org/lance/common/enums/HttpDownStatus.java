package org.lance.common.enums;

/**
 * 下载器状态枚举类
 */
public enum HttpDownStatus {
    WAITING(0),
    DOWNLOADING(1),
    PAUSE(2),
    FAIL(3),
    COMPLETE(4),
    RETRY(5);

    private final int status;

    public int getStatus() {
        return status;
    }

    HttpDownStatus(int status) {
        this.status = status;
    }

    public static HttpDownStatus getStatus(int status) {
        for (HttpDownStatus value : HttpDownStatus.values()) {
            if (value.getStatus() == status) {
                return value;
            }
        }
        return null;
    }
}
