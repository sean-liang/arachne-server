package arachne.server.job;

import arachne.server.domain.*;
import arachne.server.domain.feedback.JobFeedbackMessageList;
import arachne.server.service.JobStatsService;
import arachne.server.service.TargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrackedJobScheduler implements JobScheduler {

    @Autowired
    private JobTracker tracker;

    @Autowired
    private JobScheduleStrategy strategy;

    @Autowired
    private TargetService targetService;

    @Autowired
    private JobStatsService jobStats;

    @Override
    public List<Job> take(final Worker worker) {
        final List<Target> targets = this.targetService
                .getTargetStream()
                .filter(target -> target.getStatus() == TargetStatus.RUNNING &&
                        null != target.getWorkers() &&
                        target.getWorkers().contains(worker.getId()))
                .collect(Collectors.toList());
        if (targets.isEmpty()) {
            return Collections.emptyList();
        }
        return this.strategy.takeJobs(worker, targets);
    }

    @Override
    public void feed(final Worker worker, final JobFeedbackMessageList list) {
        list.getFeedback().forEach(fb -> {
            this.tracker.get(fb.getId()).ifPresent(job -> {
                this.targetService.getById(job.getTargetId()).ifPresent(target -> {

                    this.jobStats.feed(job, list.getIp(), fb.isSuccess());

                    final int retries = target.getRetries();
                    if (fb.isSuccess()
                            || TargetStatus.RUNNING != target.getStatus()
                            || !(RetryStrategy.CLUSTER == target.getRetryStrategy() && job.failAndNeedRetry(target.getRetries()))) {
                        this.tracker.remove(job.getId());
                        target.feed(fb);
                    }
                });
            });
        });
    }
}
