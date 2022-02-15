package org.lance.domain.entity;

import lombok.Data;
import org.lance.common.constrants.enums.FileFormatType;
import org.lance.core.parser.AbstractParser;

@Data
public class VideoInfo {
    private String id;
    private String bId; // bilibili id
    public  Integer cId;
    private String title;
    private AbstractParser.Type type;
    private FileFormatType fileType;
    private Integer quality;
    private String url;
    private String savePath;
    private String coverImg;
    private Long totalSize;
}
