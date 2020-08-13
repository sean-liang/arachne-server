package arachne.server.service;

import arachne.server.domain.stats.TargetJobStats;
import arachne.server.domain.stats.WorkerJobStats;
import arachne.server.repository.JobStatsLogRepository;
import arachne.server.util.IntervalTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


@Slf4j
@Component
public class JobStatsPersistRoutine {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${arachne.intervals.job-stats-persist}")
    private int interval;

    @Value("${arachne.intervals.job-stats-persist-gap}")
    private int gap;

    @Autowired
    private JobStatsLogRepository repo;

    public Future<?> run(final ConcurrentMap<String, TargetJobStats> targetStats, final ConcurrentMap<String, WorkerJobStats> workerStats) {
        log.info("Job Stats Persist Routine Thread Started.");
        return IntervalTaskRunner.run(interval, () -> {
            if (this.running.compareAndExchange(false, true)) {
                targetStats.forEach((key, stat) -> this.runAndSleep(() -> this.repo.persistOnDirty("targetId", key, stat)));
                workerStats.forEach((key, stat) -> this.runAndSleep(() -> this.repo.persistOnDirty("workerId", key, stat)));
                this.running.set(false);
            }
        });
    }

    private void runAndSleep(Supplier<Boolean> func) {
        if (func.get()) {
            try {
                Thread.sleep(gap);
            } catch (InterruptedException e) {
                log.debug("Interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }
    }

}
