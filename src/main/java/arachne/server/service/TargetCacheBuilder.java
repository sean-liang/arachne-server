package arachne.server.service;

import arachne.server.domain.Target;
import arachne.server.repository.TargetRepository;
import arachne.server.repository.TargetTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Build a memory map of all targets from database
 */
@Slf4j
@Service
public class TargetCacheBuilder {

    @Autowired
    private TargetRepository targetRepo;

    @Autowired
    private TargetTaskRepository taskRepo;

    @Autowired
    private TargetTaskService taskService;

    public Map<String, Target> build() {
        log.info("Build target cache.");
        return this.targetRepo
                .findAll()
                .stream()
                .map(target -> {
                    target.setCurrentTask(this.taskRepo.findFirstByTargetIdOrderByStartTimeDesc(target.getId()));
                    this.attachListeners(target);
                    this.initializePipes(target);
                    log.info("Load Target: {}({})", target.getName(), target.getId());
                    return target;
                })
                .collect(Collectors.toMap(target -> target.getId(), target -> target));
    }

    public void attachListeners(final Target target) {
        target.getListeners().add(this.taskService);
    }

    public void initializePipes(final Target target) {
        target.getPipes().forEach(pipe -> pipe.setTarget(target));
    }

}
