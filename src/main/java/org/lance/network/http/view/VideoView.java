package org.lance.network.http.view;

import org.lance.constrants.enums.FileFormatType;
import org.lance.core.parser.AbstractParser;

import java.util.*;

public class VideoView {
    public String id;
    // in bilibili: (bvId + cId) get a video detail
    public Integer cId;
    public AbstractParser.Type type;
    public FileFormatType fileType;
    public Map<String, String> headers;
    public String title;
    public String author;
    public String preViewUrl;
    public String description;
    public Map<String, Integer> acceptDescription = new LinkedHashMap<>();
    public List<SubVideoView> subVideoInfos = new ArrayList<>();
    // key = quality  value = videoUrl
    public Map<Integer, String> dashVideoMap = new HashMap<>();
    // key = quality  value = totalSize
    public Map<Integer, Long> totalSizeMap = new LinkedHashMap<>();
    public String audioUrl;

    @Override
    public String toString() {
        return "VideoView{" +
                "id='" + id + '\'' +
                ", cId=" + cId +
                ", type=" + type +
                ", fileType=" + fileType +
                ", headers=" + headers +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", preViewUrl='" + preViewUrl + '\'' +
                ", description='" + description + '\'' +
                ", acceptDescription=" + acceptDescription +
                ", subVideoInfos=" + subVideoInfos +
                ", dashVideoMap=" + dashVideoMap +
                ", audioUrl='" + audioUrl + '\'' +
                '}';
    }
}
