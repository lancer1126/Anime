package org.lance.http;

import org.lance.utils.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApplication.class);

    private static final int REST_PORT = 2333;

    private static final int WEBSOCKET_PORT = 9090;

    protected abstract void setupServer();

    public abstract void startServer();

    public void start() {
        checkPort();
        setupServer();
        startServer();
    }

    private static void checkPort() {
        if (OSUtil.checkIsPortsUsing(REST_PORT, WEBSOCKET_PORT)) {
            LOGGER.error("Port is already in use!");
            System.exit(0);
        }
    }
}
