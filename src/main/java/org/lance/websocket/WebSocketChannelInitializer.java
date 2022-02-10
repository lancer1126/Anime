package org.lance.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private String ip;
    private Integer port;

    public WebSocketChannelInitializer(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

    }
}
