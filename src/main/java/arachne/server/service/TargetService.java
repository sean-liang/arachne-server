package arachne.server.service;

import arachne.server.controller.admin.form.TargetForm;
import arachne.server.domain.Target;
import arachne.server.domain.TargetStatus;
import arachne.server.domain.target.pipe.TargetPipe;
import arachne.server.exceptions.BadRequestException;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.mongo.MongoTransactionAwareExecutor;
import arachne.server.repository.TargetRepository;
import arachne.server.util.CollectionDiffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TargetService {

    @Autowired
    private MongoTransactionAwareExecutor transaction;

    @Autowired
    private TargetRepository targetRepo;

    @Autowired
    private TargetTaskService taskService;

    @Autowired
    private JobStatsService jobStatsService;

    @Autowired
    private TargetCacheBuilder cacheBuilder;

    @Autowired
    private TargetRoutine routine;

    private ConcurrentHashMap<String, Target> targets = new ConcurrentHashMap<>();

    @PostConstruct
    private void startup() {
        this.targets.putAll(this.cacheBuilder.build());
        this.routine.run(this.targets.values()::stream);
    }

    public Optional<Target> getById(final String id) {
        return Optional.ofNullable(this.targets.get(id));
    }

    public Page<Target> getTargets(final Pageable pageable) {
        final List<Target> list = this.targets
                .values()
                .stream()
                .skip(pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        return new PageImpl<Target>(list, pageable, this.targets.size());
    }

    public Stream<Target> getTargetStream() {
        return this.targets.values().stream();
    }

    public synchronized Target addTarget(final TargetForm form) {
        final Target target = new Target();
        form.applyTo(target);
        target.setStatus(TargetStatus.DISABLED);
        final Target saved = this.addToCache(this.targetRepo.insert(target));
        try {
            try {
                saved.initialize();
            } catch (Throwable t) {
                log.error("Error create target: {}-{}", saved.getId(), saved.getName(), t);
                this.removeTarget(saved.getId());
                throw t;
            }
            this.jobStatsService.broadcast();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return saved;
    }

    public synchronized Target updateTarget(final String id, final TargetForm form) {
        final Target target = this.getById(id).orElseThrow(ResourceNotFoundException::new);
        target.checkStatus(true, TargetStatus.DONE, TargetStatus.DISABLED, TargetStatus.FAIL);

        try {
            if (!Objects.equals(target.getProvider(), form.getProvider())) {
                if (null != target.getProvider()) {
                    target.getProvider().destroy();
                }
                if (null != form.getProvider()) {
                    form.getProvider().setTarget(target);
                    form.getProvider().initialize();
                }
            }

            if (!Objects.equals(target.getStore(), form.getStore())) {
                if (null != target.getStore()) {
                    target.getStore().destroy();
                }
                if (null != form.getStore()) {
                    form.getStore().setTarget(target);
                    form.getStore().initialize();
                }
            }
            if (null != form.getPipes()) {
                form.getPipes().forEach(pipe -> pipe.setTarget(target));
            }
            CollectionDiffer.diff(
                    target.getPipes(),
                    form.getPipes(),
                    TargetPipe::destroy,
                    added -> {
                        added.setTarget(target);
                        added.initialize();
                    });
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        form.applyTo(target);
        final Target updated = this.addToCache(this.targetRepo.save(target));
        this.jobStatsService.broadcast();
        return updated;
    }

    public Target updateStatus(final String id, final String status) {
        final Target target = this.getById(id).orElseThrow(ResourceNotFoundException::new);
        switch (status) {
            case "START":
                target.start();
                break;
            case "STOP":
                target.stop();
                break;
            case "DISABLE":
                target.disable();
                break;
            case "PAUSE":
                target.pause();
                break;
            case "RESUME":
                target.resume();
                break;
            default:
                throw new BadRequestException("Illegal Status: " + status);
        }
        this.jobStatsService.broadcast();
        return target;
    }

    public synchronized void removeTarget(final String id) {
        final Target target = this.getById(id).orElseThrow(ResourceNotFoundException::new);
        target.checkStatus(true, TargetStatus.SCHEDULED, TargetStatus.DONE, TargetStatus.DISABLED, TargetStatus.FAIL);
        this.targetRepo.deleteById(target.getId());
        final Target removed = this.targets.remove(id);
        removed.getPipes().clear();
        try {
            try {
                removed.destroy();
            } catch (Throwable t) {
                log.error("Error destroy target: {}-{}", removed.getId(), removed.getName(), t);
                throw t;
            } finally {
                removed.getListeners().clear();
                this.jobStatsService.broadcast();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private Target addToCache(final Target target) {
        final Target removed = this.targets.put(target.getId(), target);
        if (null != removed) {
            target.setCurrentTask(removed.getCurrentTask());
        } else {
            this.cacheBuilder.attachListeners(target);
        }
        return target;
    }

}
