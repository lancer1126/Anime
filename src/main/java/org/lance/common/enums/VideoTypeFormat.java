package org.lance.common.enums;

public enum VideoTypeFormat {
    M4S(16),
    FLV(0),
    MP4(1);

    private int value;

    public int getValue() {
        return value;
    }

    VideoTypeFormat(int value) {
        this.value = value;
    }
}
