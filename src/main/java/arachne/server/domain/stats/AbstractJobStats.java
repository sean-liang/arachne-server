package arachne.server.domain.stats;

import arachne.server.domain.Job;
import arachne.server.service.SystemClockService;
import lombok.Getter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractJobStats implements JobStats {

    private static final int MAX_TRACKING_MINUTES = 60;

    protected final SystemClockService clock;

    protected final LinkedList<JobStatsMinuteWindow> windows = new LinkedList<>();

    protected boolean dirty = false;

    protected long lastPersistAt = 0;

    @Getter
    private long lastRequestAt = 0;

    @Getter
    private String lastRequestIp = null;


    public AbstractJobStats(final SystemClockService clock) {
        this.clock = clock;
    }

    @Override
    public synchronized void feed(final Job job, final String ip, final boolean isSuccess) {
        final long now = this.clock.currentTimeMillis();
        final long m = this.clock.currentTimeMinutes(now);
        final JobStatsMinuteWindow currentWindow = Optional
                .ofNullable(this.windows.isEmpty() || m > this.windows.getLast().getMinute() ? null : this.windows.getLast())
                .orElseGet(() -> {
                    final JobStatsMinuteWindow newWindow = new JobStatsMinuteWindow(m);
                    this.windows.add(newWindow);
                    return newWindow;
                });
        if (m != currentWindow.getMinute()) {
            throw new RuntimeException("ILLEGAL_WINDOW_IN_QUEUE");
        }
        currentWindow.increaseCount(isSuccess);
        this.lastRequestAt = now;
        this.lastRequestIp = ip;
        this.onFeed(job, ip, isSuccess);
        this.dirty = true;
    }

    @Override
    public RequestCount requestCount(final int minutes) {
        final RequestCount result = new RequestCount();
        if (this.windows.isEmpty()) {
            return result;
        }

        final long now = this.clock.currentTimeMinutes();
        if (minutes <= 0) {
            final JobStatsMinuteWindow lastWindow = this.windows.getLast();
            return lastWindow.getMinute() == now ?
                    result.add(this.windows.getLast().getSuccessCount(), this.windows.getLast().getFailCount()) :
                    result;
        }

        final long to = now - minutes;
        for (Iterator<JobStatsMinuteWindow> it = this.windows.descendingIterator(); it.hasNext(); ) {
            final JobStatsMinuteWindow window = it.next();
            if (window.getMinute() == now) {
                continue;
            }
            if (window.getMinute() < to) {
                break;
            }
            result.add(window.getSuccessCount(), window.getFailCount());
        }
        return result;
    }

    @Override
    public synchronized void persistIfDirty(final Consumer<JobStatsSlice> serializer) {
        if (this.dirty && serializer != null) {
            final long minute = this.clock.currentTimeMinutes();
            final JobStatsSlice slice = new JobStatsSlice(this.windows
                    .stream()
                    .filter(w -> w.getMinute() >= this.lastPersistAt && w.getMinute() < minute));
            slice.getMeta().put("lastRequestAt", this.lastRequestAt);
            slice.getMeta().put("lastRequestIp", this.lastRequestIp);
            this.dumpMeta(slice.getMeta());
            serializer.accept(slice);
            this.pack();
            this.dirty = false;
            this.lastPersistAt = minute;
        }
    }

    protected abstract void onFeed(Job job, String ip, boolean isSuccess);

    protected abstract void dumpMeta(Map<String, Serializable> meta);

    private void pack() {
        final long expireMinute = this.clock.currentTimeMinutes() - MAX_TRACKING_MINUTES;
        final Iterator<JobStatsMinuteWindow> it = this.windows.descendingIterator();
        while (it.hasNext()) {
            final JobStatsMinuteWindow current = it.next();
            if (expireMinute > current.getMinute()) {
                it.remove();
            }
        }
    }

}
