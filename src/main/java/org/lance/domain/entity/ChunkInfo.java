package org.lance.domain.entity;

import lombok.Data;

/**
 * 单个任务分成很多小块
 */
@Data
public class ChunkInfo {
    private int index;
    private long startOffset;
    private long endOffset;
    private long currentOffset;
    private int status;
    private long speed;

    public void increaseSize(long increaseBytes) {
        this.currentOffset += increaseBytes;
    }
}
