package org.lance.core.parser;

import org.apache.commons.lang3.StringUtils;
import org.lance.common.AnimeException;
import org.lance.common.enums.Type;
import org.lance.core.downloader.DefaultHttpDownloader;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;
import org.lance.domain.entity.VideoInfo;
import org.lance.network.http.view.VideoView;

/**
 * 解析器母类
 */
public abstract class AbstractParser {

    private final Type type;

    public AbstractParser(Type type) {
        this.type = type;
    }

    protected Type type() {
        return type;
    }

    public abstract VideoView parse(String url, RequestHeader requestHeader);

    public abstract TaskInfo buildTaskInfo(RequestHeader requestHeader, VideoInfo videoInfo) throws AnimeException;

    public abstract DefaultHttpDownloader buildDownloader(RequestHeader requestHeader, TaskInfo taskInfo);

    public boolean matchParser(Type type) {
        return this.type == type;
    }

    public boolean matchParser(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return this.type.verify(url);
    }
}
