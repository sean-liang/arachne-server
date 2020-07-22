package arachne.server.service;

import arachne.server.domain.Target;
import arachne.server.repository.TargetRepository;
import arachne.server.repository.TargetTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
        final Map<String, Target> cache = new HashMap<>();
        this.targetRepo.findAll().forEach(target -> {
            target.setCurrentTask(this.taskRepo.findFirstByTargetIdOrderByStartTimeDesc(target.getId()));
            cache.put(target.getId(), target);
            this.attachListeners(target);
            log.info("Load Target: {}({})", target.getName(), target.getId());
        });
        return cache;
    }

    public void attachListeners(final Target target) {
        target.getListeners().add(this.taskService);
    }

}
