package org.lance.websocket;

public class WebSocketServer extends Thread {

    private String ip;
    private Integer port;

    public WebSocketServer(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public void closeServer() {

    }
}
