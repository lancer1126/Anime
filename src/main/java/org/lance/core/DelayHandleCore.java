package org.lance.core;

import lombok.extern.slf4j.Slf4j;
import org.lance.domain.DelayHandleTask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public class DelayHandleCore {

    private static List<DelayHandleTask> tasks = new CopyOnWriteArrayList<>();

    private ScheduledFuture<?> delayHandleTaskTimer;

    public static DelayHandleCore getInstance() {
        return DelayHandleCoreHolder.INSTANCE;
    }

    public void addTask(DelayHandleTask task) {
        tasks.add(task);
    }

    private static class DelayHandleCoreHolder {
        private static final DelayHandleCore INSTANCE = new DelayHandleCore();
    }

}
