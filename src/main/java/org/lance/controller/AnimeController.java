package org.lance.controller;

import com.alibaba.fastjson.TypeReference;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.lance.common.ResultEntity;
import org.lance.common.annotation.RequestMapping;
import org.lance.common.enums.HttpDownStatus;
import org.lance.common.enums.MessageType;
import org.lance.common.utils.CommonUtil;
import org.lance.common.utils.HttpHandlerUtil;
import org.lance.core.MessageCore;
import org.lance.core.downloader.DownloaderManager;
import org.lance.core.parser.ParserManager;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;
import org.lance.domain.entity.VideoInfo;
import org.lance.network.http.view.VideoView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/anime")
public class AnimeController {

    @RequestMapping("/parse")
    public FullHttpResponse parse(Channel channel, FullHttpRequest request) {
        String requestContent = request.content().toString(StandardCharsets.UTF_8);
        Map<String, String> paramMap = CommonUtil.parseJSONToEntity(requestContent, new TypeReference<>() {});

        try {
            HashMap<String, String> headers = CommonUtil.parseJSONToEntity(paramMap.get("headers"), new TypeReference<>() {});
            RequestHeader reqHeader = new RequestHeader(headers);

            VideoView videoView = ParserManager.getInstance().parse(paramMap.get("search"), reqHeader);
            if (videoView == null) {
                return HttpHandlerUtil.buildJson(ResultEntity.error("解析失败"));
            } else {
                return HttpHandlerUtil.buildJson(ResultEntity.success(videoView));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return HttpHandlerUtil.buildJson(ResultEntity.error("解析失败"));
        }
    }

    @RequestMapping("/download")
    public FullHttpResponse download(Channel channel, FullHttpRequest request) throws IOException {
        String requestContent = request.content().toString(StandardCharsets.UTF_8);
        Map<String, String> paramMap = CommonUtil.parseJSONToEntity(requestContent, new TypeReference<>() {});
        HashMap<String, String> headers = CommonUtil.parseJSONToEntity(paramMap.get("headers"), new TypeReference<>() {});
        List<VideoInfo> videoInfoList = CommonUtil.parseJSONToEntity(paramMap.get("videoInfoList"), new TypeReference<>() {});

        RequestHeader requestHeader = new RequestHeader(headers);
        List<TaskInfo> taskInfoList = new ArrayList<>();
        // 获取任务列表
        for (VideoInfo videoInfo : videoInfoList) {
            TaskInfo taskInfo = null;
            try {
                taskInfo = ParserManager.getInstance().buildTaskInfo(requestHeader, videoInfo);
                taskInfoList.add(taskInfo);
            } catch (Exception e) {
                if (taskInfo != null) {
                    taskInfo = new TaskInfo();
                    taskInfo.setId(videoInfo.getId());
                }
                MessageCore.send(MessageType.CALL_BACK, HttpDownStatus.FAIL, e.getMessage(), taskInfo);
            }
        }

        // 对任务列表执行下载
        for (TaskInfo taskInfo : taskInfoList) {
            try {
                DownloaderManager.getInstance().start(taskInfo, requestHeader);
            } catch (Exception e) {
                MessageCore.send(MessageType.CALL_BACK, HttpDownStatus.FAIL, e.getMessage(), taskInfo);
            }
        }
        return HttpHandlerUtil.buildJson(ResultEntity.success());
    }
}
