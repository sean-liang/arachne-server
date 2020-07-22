package arachne.server.service;

import arachne.server.domain.JobStatsLog;
import arachne.server.domain.stats.JobStats;
import arachne.server.domain.stats.TargetJobStats;
import arachne.server.domain.stats.WorkerJobStats;
import arachne.server.repository.JobStatsLogRepository;
import arachne.server.util.IntervalTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
public class JobStatsPersistRoutine {

    @Autowired
    private JobStatsLogRepository repo;

    public Future<?> run(final ConcurrentMap<String, TargetJobStats> targetStats, final ConcurrentMap<String, WorkerJobStats> workerStats) {
        log.info("Job Stats Persist Routine Thread Started.");
        return IntervalTaskRunner.run(10000, () -> {
            targetStats.forEach((key, stat) -> this.repo.persistOnDirty("targetId", key, stat));
            workerStats.forEach((key, stat) -> this.repo.persistOnDirty("workerId", key, stat));
        });
    }

}
