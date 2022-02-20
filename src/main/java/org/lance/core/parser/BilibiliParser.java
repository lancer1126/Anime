package org.lance.core.parser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lance.common.AnimeException;
import org.lance.common.annotation.Parser;
import org.lance.common.constrants.Global;
import org.lance.common.enums.BilibiliType;
import org.lance.common.enums.FileFormatType;
import org.lance.common.enums.Type;
import org.lance.common.utils.CommonUtil;
import org.lance.common.utils.ConvertUtil;
import org.lance.core.BilibiliClientCore;
import org.lance.core.downloader.DefaultHttpDownloader;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;
import org.lance.domain.entity.VideoInfo;
import org.lance.network.http.bilibili.BiliHttpHeaders;
import org.lance.network.http.model.*;
import org.lance.network.http.response.BilibiliVideoResp;
import org.lance.network.http.response.PlayUrlM4SDataResp;
import org.lance.network.http.view.SubVideoView;
import org.lance.network.http.view.VideoView;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Parser(name = "bilibili")
public final class BilibiliParser extends AbstractParser {

    private static final String AV_PATTERN = "av([0-9]+)";
    private static final String BV_PATTERN = "BV([0-9A-Za-z]+)";
    private static final String SS_PATTERN = "ss([0-9]+)";
    private static final String MD_PATTERN = "md([0-9]+)";
    private static final String EP_PATTERN = "ep([0-9]+)";

    public BilibiliParser() {
        super(Type.BILIBILI);
    }

    @SneakyThrows
    @Override
    public VideoView parse(String url, RequestHeader reqtHeader) {
        log.info("parsing Bilibili Video...");
        String videoId;
        VideoView videoView = null;
        try {
            for (String ptnStr : Arrays.asList(AV_PATTERN, BV_PATTERN, SS_PATTERN, MD_PATTERN, EP_PATTERN)) {
                Matcher matcher = Pattern.compile(ptnStr).matcher(url);
                if (matcher.find()) {
                    if (ptnStr.equals(BV_PATTERN)) {
                        videoId = matcher.group();
                    } else {
                        videoId = matcher.group(1);
                    }
                    BilibiliType videoType = checkBiliType(ptnStr);
                    if (videoType == null) continue;
                    videoView = checkVideoInfo(videoId, videoType, reqtHeader);
                    break;
                }
            }
        } catch (Exception e) {
            throw new AnimeException(-1, "Parsing Url Error!");
        }
        return videoView;
    }

    @Override
    public TaskInfo buildTaskInfo(RequestHeader requestHeader, VideoInfo videoInfo) throws AnimeException {
        TaskInfo taskInfo = new TaskInfo();
        if (StringUtils.isNotBlank(videoInfo.getUrl())) {
            taskInfo.setUrl(videoInfo.getUrl());
        } else {
            taskInfo.setUrl(getM4SVideoUrl(videoInfo, requestHeader));
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

    private String getM4SVideoUrl(VideoInfo videoInfo, RequestHeader requestHeader) throws AnimeException {
        PlayUrlM4SDataResp m4sData = BilibiliClientCore.getBilibiliClient().getM4SFormatVideoPlayUrl(videoInfo, requestHeader);
        List<Video> videoList = m4sData.getData().getDash().getVideo();
        List<Audio> audioList = m4sData.getData().getDash().getAudio();

        String videoUrl = videoList.stream()
                .filter(video -> video.getId().equals(videoInfo.getQuality()))
                .findAny()
                .map(Video::getBaseUrl)
                .orElse("");
        return String.join(Global.URL_SEPARATOR + videoUrl + audioList.get(0).getBaseUrl());
    }

    private VideoView getBVVideoInfoWithM4S(String bvId, RequestHeader reqHeader) throws AnimeException {
        Map<String, String> headers = reqHeader.getHeaders();
        if (headers == null) {
            headers = BiliHttpHeaders.getBilibiliM4sHeaders(bvId);
        } else {
            Map<String, String> biliM4SHeaders = BiliHttpHeaders.getBilibiliM4sHeaders(bvId);
            for (Map.Entry<String, String> entry : biliM4SHeaders.entrySet()) {
                headers.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        reqHeader.setHeaders(headers);

        BilibiliVideoResp videoResp = BilibiliClientCore.getBilibiliClient().getVideoInfo(bvId, reqHeader);
        VideoView videoView = parseToBaseVideoInfo(videoResp.getData());
        videoView.type = this.type();
        videoView.fileType = FileFormatType.M4S;
        videoView.headers = reqHeader.getHeaders();

        PlayUrlM4SDataResp m4SDataResp = BilibiliClientCore.getBilibiliClient()
                .getM4SFormatVideoPlayUrl(new VideoInfo(bvId, videoView.cId), reqHeader);
        parseM4sDetailVideoInfo(videoView, m4SDataResp.getData());
        return videoView;
    }

    private VideoView parseToBaseVideoInfo(VideoData data) {
        VideoView videoInfo = new VideoView();
        videoInfo.id = data.getBvid();
        videoInfo.cId = data.getCid();
        videoInfo.title = data.getTitle();
        videoInfo.description = data.getDesc();
        videoInfo.preViewUrl = data.getPic();
        videoInfo.author = data.getOwner().getName();

        List<SubVideoView> list = new ArrayList<>();
        for (Page page : data.getPages()) {
            SubVideoView subVideoInfo = new SubVideoView();
            subVideoInfo.bvId = data.getBvid();
            subVideoInfo.cid = page.getCid();
            subVideoInfo.name = page.getPart();
            list.add(subVideoInfo);
        }
        videoInfo.subVideoInfos = list;
        return videoInfo;
    }

    private void parseM4sDetailVideoInfo(VideoView videoView, PlayUrlM4SData playUrlM4SData) {
        List<String> acceptDescriptions = playUrlM4SData.getAcceptDescription();
        List<Integer> acceptQualities = playUrlM4SData.getAcceptQuality();
        Collections.reverse(acceptDescriptions);
        Collections.reverse(acceptQualities);

        for (int i = 0; i < acceptDescriptions.size(); i++) {
            videoView.acceptDescription.put(acceptDescriptions.get(i), acceptQualities.get(i));
        }

        videoView.audioUrl = playUrlM4SData.getDash().getAudio().get(0).getBaseUrl();
        videoView.dashVideoMap = playUrlM4SData.getDash().getVideo().stream()
                .filter(e -> e.getId() != null)
                .collect(Collectors.toMap(Video::getId, Video::getBaseUrl, (oldVal, newVal) -> newVal));
    }

    private BilibiliType checkBiliType(String ptnStr) {
        switch (ptnStr) {
            case AV_PATTERN:
                return BilibiliType.AV;
            case BV_PATTERN:
                return BilibiliType.BV;
            case SS_PATTERN:
                return BilibiliType.SS;
            case MD_PATTERN:
                return BilibiliType.MD;
            case EP_PATTERN:
                return BilibiliType.EP;
            default:
                break;
        }
        return null;
    }

    private VideoView checkVideoInfo(String videoId, BilibiliType type, RequestHeader reqHeader) throws AnimeException {
        VideoView videoView = null;
        switch (type) {
            case AV:
                String bvId = ConvertUtil.Av2Bv(videoId);
                videoView = getBVVideoInfoWithM4S(bvId, reqHeader);
                break;
            case BV:
                videoView = getBVVideoInfoWithM4S(videoId, reqHeader);
                break;
            case SS:
                break;
            case MD:
                break;
            case EP:
                break;
        }
        return videoView;
    }
}
