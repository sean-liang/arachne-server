package arachne.server.job;

import arachne.server.domain.Job;
import arachne.server.domain.JobStatus;
import arachne.server.domain.Worker;
import arachne.server.service.SnowflakeIDGenerator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
@Component
public class InMemoryJobTracker implements JobTracker {

    @Autowired
    private SnowflakeIDGenerator idGenerator;

    @Autowired
    private JobTrackerRoutine routine;

    private ConcurrentHashMap<Long, Job> jobs = new ConcurrentHashMap<>();

    @PostConstruct
    public void startup() {
        this.routine.run(() -> this.jobs.entrySet().iterator());
    }

    @Override
    public Optional<Job> get(final long id) {
        return Optional.ofNullable(this.jobs.get(id));
    }

    @Override
    public Job track(@NonNull final Job job) {
        synchronized (job) {
            if (job.getId() == 0) {
                job.setId(this.idGenerator.nextId());
            }
            if (StringUtils.isEmpty(job.getTargetId())) {
                throw new RuntimeException("Empty Target Id.");
            }
            if (!job.isTracked()) {
                job.setTracked(true);
                this.jobs.put(job.getId(), job);
            }
            job.updateStatus(JobStatus.WORKING);
            return job;
        }
    }

    @Override
    public Job remove(final long id) {
        return this.jobs.remove(id);
    }

    @Override
    public Stream<Job> retryJobStream(final Worker worker, final String targetId, final int size) {
        return this.jobs
                .values()
                .stream()
                .filter(job -> job.getTargetId().equals(targetId) &&
                        (job.getStatus() == JobStatus.PENDING ||
                                (job.getStatus() == JobStatus.FAIL && !job.getFailedWorkers().contains(worker.getId()))))
                .limit(size);
    }

}
