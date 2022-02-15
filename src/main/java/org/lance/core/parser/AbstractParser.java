package org.lance.core.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.lance.common.AnimeException;
import org.lance.core.downloader.DefaultHttpDownloader;
import org.lance.network.http.view.VideoView;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;
import org.lance.domain.entity.VideoInfo;

import java.util.regex.Pattern;

/**
 * 解析器母类
 */
public abstract class AbstractParser {

    public abstract VideoView parse(String url, RequestHeader requestHeader);

    public abstract TaskInfo buildTaskInfo(RequestHeader requestHeader, VideoInfo videoInfo) throws AnimeException;

    public abstract DefaultHttpDownloader buildDownloader(RequestHeader requestHeader, TaskInfo taskInfo);

    public boolean matchParser(Type type) {
        return this.type == type;
    }

    public boolean matchParser(String url) {
        if (StringUtils.isNotBlank(url)) {
            return this.type.verify(url);
        }
        return false;
    }

    public enum Type {
        @JsonProperty("BILIBILI")
        BILIBILI(new Pattern[]{
                Pattern.compile("https?://(www\\.)?bilibili\\.com"),
                Pattern.compile("av(\\d+)"),
                Pattern.compile("BV(\\S+)"),
                Pattern.compile("ss(\\d+)"),
                Pattern.compile("md(\\d+)"),
                Pattern.compile("ep(\\d+)")
        });

        public boolean verify(String url) {
            for (Pattern type : types) {
                if (type.matcher(url).find()) {
                    return true;
                }
            }
            return false;
        }

        private final Pattern[] types;

        Type(Pattern[] types) {
            this.types = types;
        }
    }

    private final Type type;

    public AbstractParser(Type type) {
        this.type = type;
    }

    protected Type type() {
        return type;
    }
}
