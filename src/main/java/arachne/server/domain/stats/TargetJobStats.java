package arachne.server.domain.stats;

import arachne.server.domain.Job;
import arachne.server.service.SystemClockService;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

public class TargetJobStats extends AbstractJobStats {

    private static final long serialVersionUID = 1L;

    @Getter
    private String lastRequestWorkerId;

    public TargetJobStats(final SystemClockService clock) {
        super(clock);
    }

    @Override
    protected void onFeed(final Job job, final String ip, final boolean isSuccess) {
        this.lastRequestWorkerId = job.getWorkerId();
    }

    @Override
    protected void dumpMeta(final Map<String, Serializable> meta) {
        meta.put("lastRequestWorkerId", this.lastRequestWorkerId);
    }

}
