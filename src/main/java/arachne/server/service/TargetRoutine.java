package arachne.server.service;

import arachne.server.domain.Target;
import arachne.server.domain.TargetStatus;
import arachne.server.domain.target.actionprovider.TargetActionProvider;
import arachne.server.util.IntervalTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@Component
public class TargetRoutine {

    @Autowired
    private JobStatsService jobStatsService;

    public Future<?> run(final Supplier<Stream<Target>> supplier) {
        log.info("Target Routine Thread Started.");
        return IntervalTaskRunner.run(1000, () -> {
            final long now = System.currentTimeMillis();
            supplier.get().forEach((target) -> this.handleTarget(target, now));
        });
    }

    public void handleTarget(final Target target, final long ts) {
        if(null == target) {
            return;
        }
        synchronized (target) {
            Optional.ofNullable(target.getProvider()).ifPresent(TargetActionProvider::persistOnDirty);
            if (target.getStatus() == TargetStatus.SCHEDULED && target.getNextRunAt() > 0 && ts >= target.getNextRunAt()) {
                target.start();
                this.jobStatsService.broadcast();
            }
        }
    }

}
