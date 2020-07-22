package arachne.server.job;

import arachne.server.domain.*;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrackedJobAllocator implements JobAllocator {

    @Autowired
    private JobTracker tracker;

    @Override
    public List<Job> allocate(@NonNull final Worker worker, @NonNull final Target target, final int size) {
        final List<Job> jobs = new ArrayList<>();
        target.runOnStatus(TargetStatus.RUNNING, t -> {
            this.tracker
                    .retryJobStream(worker, target.getId(), size)
                    .map(this.tracker::track)
                    .collect(Collectors.toCollection(() -> jobs));
            final int remaining = size - jobs.size();
            if (remaining > 0) {
                target
                        .getProvider()
                        .provide(remaining)
                        .stream()
                        .map(action -> this.trackAction(worker, target, action))
                        .collect(Collectors.toCollection(() -> jobs));
            }
        });
        return jobs;
    }

    private Job trackAction(final Worker worker, final Target target, final JobAction action) {
        return this.tracker.track(Job
                .builder()
                .workerId(worker.getId())
                .targetId(target.getId())
                .action(action)
                .retries(target.getRetryStrategy() == RetryStrategy.LOCAL ? target.getRetries() : 0)
                .build());
    }

}
