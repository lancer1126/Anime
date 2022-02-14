package org.lance.network.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HttpServer extends Thread {

    private final int PORT;

    private final List<Object> controllerList;

    private static Channel serverChannel;

    public HttpServer(int port) {
        this.PORT = port;
        this.controllerList = new ArrayList<>();
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(3);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.TCP_NODELAY, true);
            b.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline()
                            .addLast("httpCodec", new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(4194304))
                            .addLast("serverHandle", new HttpServerHandler(controllerList));
                }
            });
            serverChannel = b.bind(PORT).sync().channel();
            serverChannel.closeFuture().sync();
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void addController(Object obj) {
        this.controllerList.add(obj);
    }

    public void stopServer() {
        if (serverChannel != null) {
            log.info("close httpServer");
            serverChannel.close();
            serverChannel = null;
        }
    }

}
