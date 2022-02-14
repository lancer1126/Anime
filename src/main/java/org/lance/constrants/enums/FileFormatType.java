package org.lance.constrants.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 文件类型枚举类
 */
public enum FileFormatType {
    @JsonProperty("flv")
    FLV("flv"),
    @JsonProperty("m4s")
    M4S("m4s"),
    @JsonProperty("mp4")
    MP4("mp4"),
    @JsonProperty("m3u8")
    M3U8("m3u8");

    private final String formatType;

    FileFormatType(String formatType) {
        this.formatType = formatType;
    }

    public String value() {
        return formatType;
    }
}
