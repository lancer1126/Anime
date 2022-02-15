package org.lance.domain.entity;

import lombok.Data;
import org.lance.common.constrants.enums.FileFormatType;
import org.lance.core.downloader.DefaultHttpDownloader;
import org.lance.core.parser.AbstractParser;
import org.lance.domain.RequestHeader;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 下载任务
 */
@Data
public class TaskInfo {
    private String id;
    private String coverImg;
    private String name;
    private String url;
    private AbstractParser.Type type;
    private FileFormatType fileType;
    private String filePath;
    private AtomicLong currentOffset = new AtomicLong(0);
    private AtomicLong totalSize = new AtomicLong(0);
    private int status;
    private long speed;
    private int connectionCount;
    private List<ChunkInfo> chunkInfoList;

    public DefaultHttpDownloader buildDownloader(RequestHeader requestHeader) {
        return null;
    }
}
