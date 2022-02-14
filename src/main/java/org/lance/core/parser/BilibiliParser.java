package org.lance.core.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lance.annotation.Parser;
import org.lance.constrants.enums.Global;
import org.lance.core.BilibiliClientCore;
import org.lance.core.downloader.DefaultHttpDownloader;
import org.lance.network.http.model.Audio;
import org.lance.network.http.model.PlayUrlM4SData;
import org.lance.network.http.model.Video;
import org.lance.network.http.view.VideoView;
import org.lance.pojo.RequestHeader;
import org.lance.pojo.entity.TaskInfo;
import org.lance.pojo.entity.VideoInfo;
import org.lance.utils.CommonUtil;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Parser(name = "bilibili")
public final class BilibiliParser extends AbstractParser {

    private static final Pattern BV_PATTERN = Pattern.compile("BV([0-9A-Za-z]+)");

    public BilibiliParser() {
        super(Type.BILIBILI);
    }

    @Override
    public VideoView parse(String url, RequestHeader requestHeader) {
        return null;
    }

    @Override
    public TaskInfo buildTaskInfo(RequestHeader requestHeader, VideoInfo videoInfo) {
        TaskInfo taskInfo = new TaskInfo();
        if (StringUtils.isNotBlank(videoInfo.getUrl())) {
            taskInfo.setUrl(videoInfo.getUrl());
        } else {
            taskInfo.setUrl(getM4VideoUrl(videoInfo, requestHeader));
        }
        taskInfo.setId(videoInfo.getId());
        taskInfo.setCoverImg(videoInfo.getCoverImg());
        taskInfo.setName(CommonUtil.clearInvalidChars(videoInfo.getTitle()));
        taskInfo.setType(videoInfo.getType());
        taskInfo.setFileType(videoInfo.getFileType());
        taskInfo.setFilePath(videoInfo.getSavePath());
        return taskInfo;
    }

    @Override
    public DefaultHttpDownloader buildDownloader(RequestHeader requestHeader, TaskInfo taskInfo) {
        return null;
    }

    private String getM4VideoUrl(VideoInfo videoInfo, RequestHeader requestHeader) {
        PlayUrlM4SData m4SData = BilibiliClientCore.getBilibiliClient().getM4SFormatVideoPlayUrl(videoInfo, requestHeader);
        List<Video> videoList = m4SData.getDash().getVideo();
        List<Audio> audioList = m4SData.getDash().getAudio();

        String videoUrl = videoList.stream()
                .filter(video -> video.getId().equals(videoInfo.getQuality()))
                .findAny()
                .map(Video::getBaseUrl)
                .orElse("");
        return String.join(Global.URL_SEPARATOR + videoUrl + audioList.get(0).getBaseUrl());
    }
}
