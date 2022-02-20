package org.lance.domain.entity;

import lombok.Data;
import org.lance.common.enums.FileFormatType;
import org.lance.common.enums.Type;

@Data
public class VideoInfo {
    private String id;
    private String bId; // bilibili id
    public  Integer cId;
    private String title;
    private Type type;
    private FileFormatType fileType;
    private Integer quality;
    private String url;
    private String savePath;
    private String coverImg;
    private Long totalSize;

    public VideoInfo() {}
    public VideoInfo(String bId, Integer cId) {
        this.bId = bId;
        this.cId = cId;
    }
}
