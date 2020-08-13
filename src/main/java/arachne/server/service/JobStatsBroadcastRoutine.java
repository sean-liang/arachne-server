package arachne.server.service;

import arachne.server.controller.admin.AdminWebsocketDashboardHandler;
import arachne.server.controller.admin.dto.JobStatsUpdate;
import arachne.server.util.IntervalTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.function.Supplier;

@Slf4j
@Component
public class JobStatsBroadcastRoutine {

    @Value("${arachne.intervals.job-stats-broadcast}")
    private int interval;

    @Autowired
    private AdminWebsocketDashboardHandler adminDashboard;

    public Future<?> run(final Supplier<JobStatsUpdate> supplier) {
        log.info("Job Stats Broadcast Routine Thread Started.");
        return IntervalTaskRunner.run(interval, () -> this.broadcast(supplier));
    }

    public void broadcast(final Supplier<JobStatsUpdate> supplier) {
        this.adminDashboard.broadcast(supplier::get);
    }

}
