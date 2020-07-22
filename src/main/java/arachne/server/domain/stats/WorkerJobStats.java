package arachne.server.domain.stats;

import arachne.server.domain.Job;
import arachne.server.service.SystemClockService;

import java.io.Serializable;
import java.util.Map;

public class WorkerJobStats extends AbstractJobStats {

    private static final long serialVersionUID = 1L;

    public WorkerJobStats(final SystemClockService clock) {
        super(clock);
    }

    @Override
    protected void onFeed(final Job job, final String ip, final boolean isSuccess) {

    }

    @Override
    protected void dumpMeta(Map<String, Serializable> meta) {

    }

}
