package org.lance.domain;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class MessageData {
    private String id;
    private String title;
    private AtomicLong currentSize = new AtomicLong(0);
    private Long totalSize;
    private Long speed;
    private String filePath;
    private String cover;
}
