package org.lance;

import lombok.extern.slf4j.Slf4j;
import org.lance.core.BilibiliClientCore;
import org.lance.core.MessageCore;
import org.lance.core.downloader.DownloaderManager;
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
        DownloaderManager.getInstance().init();
        BilibiliClientCore.init();
        MessageCore.getInstance().init();
    }

    @Override
    public void stopServer() {
        super.stopServer();
        DownloaderManager.getInstance().stop();
        MessageCore.getInstance().stop();
    }

    public static AnimeApplication animeApplication() {
        return animeApplication;
    }
}
