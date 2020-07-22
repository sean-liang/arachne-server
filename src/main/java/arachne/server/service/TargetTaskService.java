package arachne.server.service;

import arachne.server.domain.*;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.repository.TargetTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TargetTaskService implements TargetListener {

    @Autowired
    private TargetTaskRepository repo;

    @Override
    public void onCreated(final Target target) {
        final List<TargetTaskLog> logs = new ArrayList<>();
        logs.add(TargetTaskLog.now("CREATE"));
        final TargetTask task = TargetTask
                .builder()
                .targetId(target.getId())
                .logs(logs)
                .build();
        target.setCurrentTask(this.repo.insert(task));
    }

    @Override
    public void onDestroyed(final Target target) {
        final long count = this.repo.deleteByTargetId(target.getId());
        log.info("{} target tasks destroyed.", count);
    }

    @Override
    public void onStatusChanged(final Target target, final TargetStatus previousStatus, final String message) {
        final TargetTask task = Optional.ofNullable(target.getCurrentTask()).orElseThrow(ResourceNotFoundException::new);
        if (TargetStatus.RUNNING == target.getStatus()) {
            task.setStartTime(System.currentTimeMillis());
        } else if (Arrays.asList(TargetStatus.DONE, TargetStatus.DISABLED, TargetStatus.FAIL).contains(target.getStatus()) && task.getEndTime() == 0) {
            task.setEndTime(System.currentTimeMillis());
        }
        task.getLogs().add(TargetTaskLog.now(target.getStatus().name(), message));
        target.setCurrentTask(this.repo.save(task));
    }
}
