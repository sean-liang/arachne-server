package arachne.server.job;

import arachne.server.domain.Job;
import arachne.server.domain.JobStatus;
import arachne.server.domain.Target;
import arachne.server.service.TargetService;
import arachne.server.util.IntervalTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Slf4j
@Component
public class JobTrackerRoutine {

    @Value("${arachne.job-tracker.expire}")
    private int expire;

    @Value("${arachne.job-tracker.ttl}")
    private int ttl;

    @Autowired
    private TargetService targetService;

    public Future<?> run(final Supplier<Iterator<Map.Entry<Long, Job>>> supplier) {
        final long expireMs = this.expire * 1000L;
        final long ttlMs = this.ttl * 1000L;
        log.info("Job Tracker Thread Started.");
        return IntervalTaskRunner.run(1000, () -> {
            final long now = System.currentTimeMillis();
            for (Iterator<Map.Entry<Long, Job>> it = supplier.get(); it.hasNext(); ) {
                final Job job = it.next().getValue();
                final long ts = now - job.getUpdatedAt();
                if (ts >= ttlMs) {
                    it.remove();
                    this.getTarget(job).ifPresent(target -> target.onActionExpire(job.getAction()));
                    log.warn("Job exceed ttl.", job.getAction());
                } else if (job.getStatus() == JobStatus.WORKING && ts >= expireMs) {
                    job.updateStatus(JobStatus.PENDING);
                }
            }
        });
    }

    private Optional<Target> getTarget(final Job job) {
        final Optional<Target> target = this.targetService.getById(job.getTargetId());
        if (target.isEmpty()) {
            log.warn("Target absent: {}", job.getTargetId());
        }
        return target;
    }
}
