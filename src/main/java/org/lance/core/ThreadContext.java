package org.lance.core;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class ThreadContext {

    private static final ScheduledExecutorService EXECUTOR_TIMER;

    public static final String ANIME_TIMER = "Anime_Timer";

    public static final String ANIME_THREAD_DOWNLOADER = "Anime-Downloader";

    static {
        EXECUTOR_TIMER = ThreadContext.newTimerExecutor(1, ANIME_TIMER);
    }

    public static ScheduledExecutorService newTimerExecutor(int poolSize, String name) {
        return new ScheduledThreadPoolExecutor(poolSize, newThreadFactory(name));
    }

    public static ScheduledFuture<?> timer(Runnable runnable, long delay, long period, TimeUnit unit) {
        return EXECUTOR_TIMER.scheduleAtFixedRate(runnable, delay, period, unit);
    }

    public static ExecutorService newExecutor(int corePoolSize, int maxPoolSize, int queueSize,
                                              long keepAliveTime, String name) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(queueSize),
                newThreadFactory(name),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    public static void shutdown(ScheduledFuture<?> scheduledFuture) {
        if (scheduledFuture == null || scheduledFuture.isCancelled()) {
            return;
        }
        try {
            scheduledFuture.cancel(true);
        } catch (Exception e) {
            log.error("close scheduledFuture error", e);
        }
    }

    private static ThreadFactory newThreadFactory(String name) {
        return runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setName(name);
            thread.setDaemon(true);
            return thread;
        };
    }
}
