package arachne.server.job;

import arachne.server.domain.Job;
import arachne.server.domain.Target;
import arachne.server.domain.Worker;
import arachne.server.service.JobStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class WeightedLoadBalanceJobScheduleStrategy implements JobScheduleStrategy {

    @Autowired
    private JobAllocator allocator;

    @Autowired
    private JobStatsService jobStatsService;

    @Override
    public List<Job> takeJobs(final Worker worker, final List<Target> targets) {
        final int batchSize = worker.getBatchSize() > 0 ? worker.getBatchSize() : 1;
        final List<Job> jobs = new ArrayList<>();
        if (null != targets && !targets.isEmpty()) {
            take(batchSize, worker, jobs, new ArrayList<>(targets));
        }
        return jobs;
    }

    private void take(final int total, final Worker worker, final List<Job> jobs,
                      final List<Target> targets) {
        int remaining = total;
        targets.sort((a, b) -> b.getWeight() - a.getWeight());
        final int totalWeight = targets.stream().mapToInt(Target::getWeight).sum();
        for (final Iterator<Target> it = targets.iterator(); it.hasNext(); ) {
            final Target target = it.next();
            int amount = (total * target.getWeight() - 1) / totalWeight + 1;
            amount = Math.min(amount, remaining);
            final List<Job> targetJobs = this.allocator.allocate(worker, target, amount);
            if(null != targetJobs) {
                if (!targetJobs.isEmpty()) {
                    jobs.addAll(targetJobs);
                    remaining = remaining - targetJobs.size();
                }
                if (targetJobs.size() < amount) {
                    target.stop();
                    it.remove();
                    this.jobStatsService.broadcast();
                }
            }
            if (total <= jobs.size()) {
                break;
            }
        }
        if (total > jobs.size()) {
            this.take(total - jobs.size(), worker, jobs, targets);
        }
    }

}
