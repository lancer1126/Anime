package org.lance.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.lance.common.ResultEntity;
import org.lance.common.annotation.RequestMapping;
import org.lance.common.constrants.enums.HttpDownStatus;
import org.lance.common.constrants.enums.MessageType;
import org.lance.common.utils.HttpHandlerUtil;
import org.lance.core.MessageCore;
import org.lance.core.downloader.DownloaderManager;
import org.lance.core.parser.ParserManager;
import org.lance.domain.RequestHeader;
import org.lance.domain.entity.TaskInfo;
import org.lance.domain.entity.VideoInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/anime")
public class AnimeController {

    @RequestMapping("/download")
    public FullHttpResponse download(Channel channel, FullHttpRequest request) throws IOException {
        Map<String, String> paramMap = parseRequestToMap(request);
        HashMap<String, String> headers = getJSONParams(paramMap.get("headers"), new TypeReference<>() {});
        List<VideoInfo> videoInfoList = getJSONParams(paramMap.get("videoInfoList"), new TypeReference<>() {});

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

    private Map<String, String> parseRequestToMap(FullHttpRequest request) throws IOException {
        return getJSONParams(request.content().toString(StandardCharsets.UTF_8), new TypeReference<>() {});
    }

    private <T> T getJSONParams(Object obj, TypeReference<T> clazzList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(objectMapper.writeValueAsString(obj), clazzList);
    }
}
