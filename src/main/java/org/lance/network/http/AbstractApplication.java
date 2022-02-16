package org.lance.network.http;

import lombok.extern.slf4j.Slf4j;
import org.lance.controller.AnimeController;
import org.lance.common.utils.OSUtil;
import org.lance.network.websocket.WebSocketServer;

@Slf4j
public abstract class AbstractApplication {

    private static final int REST_PORT = 2233;

    private static final int WEBSOCKET_PORT = 3322;

    protected abstract void setupServer();

    private HttpServer httpServer;

    private WebSocketServer webSocketServer;

    public void start() {
        try {
            checkPort();
            setupServer();
            startServer();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void checkPort() {
        if (OSUtil.checkIsPortsUsing(REST_PORT, WEBSOCKET_PORT)) {
            log.error("Port is already in use!");
            System.exit(0);
        }
    }

    protected void setupHttpServer() {
        httpServer = new HttpServer(REST_PORT);
        httpServer.addController(new AnimeController());
    }

    protected void setupWebSocketServer() {
        webSocketServer = new WebSocketServer("127.0.0.1", WEBSOCKET_PORT);
    }

    public void startServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                stopServer();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }));

        if (httpServer != null) {
            httpServer.start();
            log.info("http server 启动");
        }
        if (webSocketServer != null) {
            webSocketServer.start();
            log.info("websocket server 启动");
        }
    }

    public void stopServer() {
        if (httpServer != null) {
            httpServer.stopServer();
        }
        if (webSocketServer != null) {
            webSocketServer.closeServer();
        }
    }
}
