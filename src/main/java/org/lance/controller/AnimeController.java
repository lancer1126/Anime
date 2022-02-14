package org.lance.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.lance.annotation.RequestMapping;
import org.lance.common.ResultEntity;
import org.lance.core.downloader.DownloaderManager;
import org.lance.core.parser.ParserManager;
import org.lance.pojo.RequestHeader;
import org.lance.pojo.entity.TaskInfo;
import org.lance.pojo.entity.VideoInfo;
import org.lance.utils.HttpHandlerUtil;

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
                // todo 传递处理错误的信息
            }
        }

        // 对任务列表执行下载
        for (TaskInfo taskInfo : taskInfoList) {
            try {
                DownloaderManager.getInstance().start(taskInfo, requestHeader);
            } catch (Exception e) {
                // todo 传递错误信息
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
