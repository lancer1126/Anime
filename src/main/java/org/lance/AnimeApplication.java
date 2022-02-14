package org.lance;

import lombok.extern.slf4j.Slf4j;
import org.lance.network.http.AbstractApplication;
import org.lance.core.parser.ParserManager;

@Slf4j
public class AnimeApplication extends AbstractApplication {

    private static AnimeApplication animeApplication;

    public static void main(String[] args) {
        animeApplication = new AnimeApplication();
        try {
            animeApplication.start();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void setupServer() {
        setupHttpServer();
        setupWebSocketServer();
        ParserManager.getInstance().init();
    }

    @Override
    public void stopServer() {
        super.stopServer();
    }

    public static AnimeApplication animeApplication() {
        return animeApplication;
    }
}
