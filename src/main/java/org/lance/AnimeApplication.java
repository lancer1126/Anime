package org.lance;

import org.lance.http.AbstractApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnimeApplication extends AbstractApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeApplication.class);

    private static AnimeApplication animeApplication;

    public static void main(String[] args) {
        animeApplication = new AnimeApplication();
        try {
            animeApplication.start();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void setupServer() {

    }

    @Override
    public void startServer() {

    }

    public static AnimeApplication animeApplication() {
        return animeApplication;
    }
}
