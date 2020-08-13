package arachne.server.service;

import arachne.server.controller.admin.dto.JobStatsUpdate;
import arachne.server.controller.admin.dto.TargetJobStatsUpdate;
import arachne.server.controller.admin.dto.WorkerJobStatsUpdate;
import arachne.server.domain.Job;
import arachne.server.domain.stats.TargetJobStats;
import arachne.server.domain.stats.WorkerJobStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class JobStatsService {

    private final ConcurrentHashMap<String, TargetJobStats> targetStats = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, WorkerJobStats> workerStats = new ConcurrentHashMap<>();

    @Autowired
    private TargetService targetService;

    @Autowired
    private WorkerService workerService;

    @Autowired
    private JobStatsBroadcastRoutine broadcastRoutine;

    @Autowired
    private JobStatsPersistRoutine persistRoutine;

    @Autowired
    private SystemClockService clock;

    @PostConstruct
    private void startup() {
        this.broadcastRoutine.run(this::buildUpdate);
        this.persistRoutine.run(this.targetStats, this.workerStats);
    }

    public void feed(final Job job, final String ip, final boolean isSuccess) {
        this.targetStats
                .computeIfAbsent(job.getTargetId(), (key) -> new TargetJobStats(clock))
                .feed(job, ip, isSuccess);
        this.workerStats
                .computeIfAbsent(job.getWorkerId(), (key) -> new WorkerJobStats(clock))
                .feed(job, ip, isSuccess);
    }

    public void broadcast() {
        this.broadcastRoutine.broadcast(this::buildUpdate);
    }

    private JobStatsUpdate buildUpdate() {
        return new JobStatsUpdate(
                this.targetService
                        .getTargetStream()
                        .map(target -> TargetJobStatsUpdate.of(target, this.targetStats.get(target.getId())))
                        .collect(Collectors.toList()),
                this.workerService.
                        getWorkerStream()
                        .map(worker -> WorkerJobStatsUpdate.of(worker, this.workerStats.get(worker.getId())))
                        .collect(Collectors.toList())
        );
    }

}
