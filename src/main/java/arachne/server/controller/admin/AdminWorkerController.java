package arachne.server.controller.admin;

import arachne.server.controller.admin.dto.WorkerSummary;
import arachne.server.controller.admin.form.WorkerForm;
import arachne.server.domain.Worker;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/workers")
public class AdminWorkerController {

    @Autowired
    private WorkerService workerService;

    @GetMapping("/")
    public Page<Worker> listWorkers(final Pageable pageable) {
        return this.workerService.getWorkers(pageable);
    }

    @GetMapping("/names/")
    public List<WorkerSummary> getWorkerNames() {
        return this.workerService.getWorkers().stream().map(w -> new WorkerSummary(w.getId(), w.getName(), w.getTags())).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Worker getWorker(@PathVariable("id") final String id) {
        return this.workerService.getById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @GetMapping("/tags")
    public List<String> getUniqueTags() {
        return this.workerService.getUniqueTags();
    }

    @PostMapping("/")
    public Worker createWorker(@RequestBody @Valid final WorkerForm form) {
        return this.workerService.addWorker(form);
    }

    @PutMapping("/{id}")
    public Worker updateWorker(@PathVariable("id") final String id, @RequestBody @Valid final WorkerForm form) {
        return this.workerService.updateWorker(id, form);
    }

    @DeleteMapping("/{id}")
    public void removeWorker(@PathVariable("id") final String id) {
        this.workerService.removeWorker(id);
    }

}
