package org.lance.network.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.lance.domain.Message;

@Slf4j
public final class ClientCallback {

    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void push(Message message) {
        if (group.isEmpty()) {
            log.warn("no websocket client connect...");
            return;
        }
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(message)));
    }
}
