package org.lance.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class WebSocketServer extends Thread {

    private static Channel serverChannel;

    private final String IP;
    private final Integer PORT;

    public WebSocketServer(String ip, Integer port) {
        this.IP = ip;
        this.PORT = port;
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new WebSocketChannelInitializer(IP, PORT));
            serverChannel = serverBootstrap.bind(new InetSocketAddress(IP, PORT)).sync().channel();
            serverChannel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void closeServer() {
        if (serverChannel != null) {
            log.info("close webSocketServer");
            serverChannel.close();
            serverChannel = null;
        }
    }
}
