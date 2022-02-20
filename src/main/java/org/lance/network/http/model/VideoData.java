package org.lance.network.http.model;

import lombok.Data;

import java.util.List;

@Data
public class VideoData {
    private String bvid;
    private Integer aid;
    private Integer videos; // 视频数量
    private String pic;
    private String title;
    private String desc;
    private Owner owner;
    private Integer cid;
    private List<Page> pages;
}
