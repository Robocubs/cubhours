package com.robocubs.cubhours.util;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TimedTrigger {
    private final Runnable target;
    private final int time;
    private final TimeUnit unit;

    private final ScheduledExecutorService executor = CubUtil.newSingleThreadScheduledExecutor("trigger");

    ScheduledFuture<?> future;

    public void execute() {
        if (isActive()) {
            cancel();
        }
        future = CubUtil.newSingleThreadScheduledExecutor("trigger").schedule(target, time, unit);
    }

    public void cancel() {
        if (isActive()) {
            future.cancel(true);
        }
    }

    public boolean isActive() {
        return future != null && !future.isDone();
    }
}
