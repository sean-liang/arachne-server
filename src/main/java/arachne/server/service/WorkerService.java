package arachne.server.service;

import arachne.server.controller.admin.form.WorkerForm;
import arachne.server.domain.Worker;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.mongo.MongoTransactionAwareExecutor;
import arachne.server.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WorkerService {

    private final ConcurrentHashMap<String, Worker> workers = new ConcurrentHashMap<>();

    @Autowired
    private MongoTransactionAwareExecutor transaction;

    @Autowired
    private WorkerRepository repo;

    @PostConstruct
    private void loadWorkers() {
        // cache workers in memory
        this.repo.findAll().forEach(worker -> {
            this.workers.put(worker.getId(), worker);
            log.info("Load Worker: {}({})", worker.getName(), worker.getId());
        });
    }

    public Optional<Worker> getById(final String id) {
        return Optional.ofNullable(this.workers.get(id));
    }

    public Page<Worker> getWorkers(final Pageable pageable) {
        final List<Worker> list = this.workers
                .values()
                .stream()
                .skip(pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        return new PageImpl<Worker>(list, pageable, this.workers.size());
    }

    public List<Worker> getWorkers() {
        return new ArrayList<>(this.workers.values());
    }

    public Stream<Worker> getWorkerStream() {
        return this.workers.values().stream();
    }

    public List<String> getUniqueTags() {
        return this.workers
                .values()
                .stream()
                .flatMap(w -> w.getTags() != null ? w.getTags().stream() : Stream.empty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public synchronized Worker addWorker(final WorkerForm form) {
        final Worker worker = Worker.builder().token(UUID.randomUUID().toString()).build();
        form.applyTo(worker);
        final Worker saved = this.repo.insert(worker);
        this.workers.put(saved.getId(), saved);
        return saved;
    }

    public synchronized Worker updateWorker(final String id, final WorkerForm form) {
        return this.transaction.execute(() -> {
            final Worker worker = this.getById(id).orElseThrow(ResourceNotFoundException::new);
            form.applyTo(worker);
            return this.repo.save(worker);
        });
    }

    public synchronized void removeWorker(final String id) {
        this.transaction.execute(() -> {
            final Worker worker = this.getById(id).orElseThrow(ResourceNotFoundException::new);
            this.repo.deleteById(worker.getId());
            this.workers.remove(id);
        });
    }

}
