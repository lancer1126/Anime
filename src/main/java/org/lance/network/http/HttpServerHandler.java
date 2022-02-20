package org.lance.network.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.lance.common.annotation.RequestMapping;
import org.lance.common.ResultEntity;
import org.lance.common.utils.HttpHandlerUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final List<Object> controllerList;

    public HttpServerHandler(List<Object> controllerList) {
        this.controllerList = controllerList;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        URI uri = new URI(msg.uri());
        FullHttpResponse httpResponse = invoke(uri.getPath(), ctx.channel(), msg);
        if (httpResponse != null) {
            httpResponse.headers()
                    .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                    .set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            ctx.channel().writeAndFlush(httpResponse);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause = cause.getCause() == null ? cause : cause.getCause();
        log.error("request error " + cause);
        FullHttpResponse httpResponse = HttpHandlerUtil.buildJson(ResultEntity.error("未知错误"));
        httpResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        ctx.channel().writeAndFlush(httpResponse);
    }

    public FullHttpResponse invoke(String uri, Channel channel, FullHttpRequest request) throws InvocationTargetException, IllegalAccessException {
        if (controllerList == null) {
            return HttpHandlerUtil.buildJson(ResultEntity.error(404, "not found"));
        }
        for (Object obj : controllerList) {
            Class<?> clazz = obj.getClass();
            RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
            if (mapping == null) continue;

            String mappingUri = fixUri(mapping.value()[0]);
            for (Method actionMethod : clazz.getMethods()) {
                RequestMapping subMapping = actionMethod.getAnnotation(RequestMapping.class);
                if (subMapping == null) continue;
                String subMappingUri = fixUri(subMapping.value()[0]);
                if (uri.equalsIgnoreCase(mappingUri + subMappingUri)) {
                    return (FullHttpResponse) actionMethod.invoke(obj, channel, request);
                }
            }
        }
        return HttpHandlerUtil.buildJson(ResultEntity.error(404, "not found"));
    }

    private String fixUri(String uri) {
        StringBuilder builder = new StringBuilder(uri);
        if (builder.indexOf("/") != 0) {
            builder.insert(0, "/");
        }
        if (builder.lastIndexOf("/") == builder.length() - 1) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder.toString();
    }
}
