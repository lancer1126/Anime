package org.lance.network.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.lance.core.MessageCore;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private final String ip;

    private final Integer port;

    private WebSocketServerHandshaker handShaker;

    public WebSocketServerHandler(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端与服务端连接开启");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientCallback.group.remove(ctx.channel());
        log.info("客户端与服务端服务关闭");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        if (!ClientCallback.group.isEmpty()) {
            ClientCallback.group.forEach(Channel::close);
            ClientCallback.group.clear();
        }
        ClientCallback.group.add(ctx.channel());

        String url = "ws://" + ip + ":" + port + WEBSOCKET_PATH;
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(url, null, true);
        handShaker = wsFactory.newHandshaker(req);
        if (handShaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handShaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (msg instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) msg).text();
            if (Objects.equals(request, "2333")) { // 心跳
                ClientCallback.group.writeAndFlush(new TextWebSocketFrame(request));
                return;
            }
            MessageCore.delete(request);
        } else if (msg instanceof PingWebSocketFrame) {
            ctx.channel().write(new PingWebSocketFrame(msg.content().retain()));
        } else if (msg instanceof CloseWebSocketFrame) {
            handShaker.close(ctx.channel(), ((CloseWebSocketFrame) msg).retain());
        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse resp) {
        if (resp.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(resp.status().toString(), StandardCharsets.UTF_8);
            resp.content().writeBytes(buf);
            buf.release();
        }

        ChannelFuture future = ctx.channel().writeAndFlush(resp);
        if (resp.status().code() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
